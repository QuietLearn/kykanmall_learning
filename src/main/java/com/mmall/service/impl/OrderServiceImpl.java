package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FtpUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static AlipayTradeService tradeService;
    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse<OrderVo> generateOrderVo(Integer userId,Integer shippingId){
        if (shippingId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        //从购物车中获取已选择的商品列表
        List<Cart> cartList = cartMapper.selectCheckByUserId(userId,Const.Cart.CHECKED);


        long orderNo = this.generateOrderNo();

        ServerResponse response = assemOrderItemList(cartList, orderNo);
        if (!response.isSuccess()){
            return response;
        }

        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        BigDecimal orderTotalPrice =  this.getOrderTotalPrice(orderItemList);



        Order order = assemOrder(orderNo, userId, shippingId, orderTotalPrice);
        if (order==null){
            return ServerResponse.createByErrorMessage("order插入失败");
        }

        //mybatis 批量插入
        int insertCount = orderItemMapper.batchInsert(orderItemList);
        if (insertCount<=0) {
            return ServerResponse.createByErrorMessage("批量插入失败");
        }

        //生成订单成功,我们要减少我们产品的库存
        this.reduceProductStock(orderItemList);
        //生成订单后清空购物车选中的物品
        ServerResponse response1 = emptyCart(userId, cartList);
        //如果清空失败返回消息提示给前端
        if (!response1.isSuccess()){
            return response1;
        }
        //装配ordervo
        OrderVo orderVo = assemOrderVo(order, orderItemList);
        if (orderVo==null){
            return ServerResponse.createByErrorMessage("orderVo装配失败");
        }
        return ServerResponse.createBySuccess(orderVo);
    }

    private OrderVo assemOrderVo(Order order,List<OrderItem> orderItemList){
        if (order==null||CollectionUtils.isEmpty(orderItemList)){
            //logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(), response.getSubMsg()));
            //order{}，旧文件名{}，新文件名{}
            logger.info("（开始）装配orderVo失败（可能order里面没有商品）,传入order{},orderItemList是否为empty：{}",order,CollectionUtils.isEmpty(orderItemList));
        }
        OrderVo orderVo = new OrderVo();

        BeanUtils.copyProperties(order,orderVo);

        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.getOrderStatusEnum(order.getStatus()).getStatus());
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));

        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping!=null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assemShippingVo(shipping));
        }

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = assemOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);

        return orderVo;
    }

    private ServerResponse emptyCart(Integer userId,List<Cart> cartList){
        if (cartList==null){
            return ServerResponse.createByErrorMessage("cartList购物车为空");
        }
        int deleteCount = cartMapper.deleteByCartList(userId,cartList);
        if (deleteCount > 0){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByErrorMessage("删除购物车中已生成订单产品失败");
    }

    private void reduceProductStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock()-orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product); // 最好使用in +foreach是sql查询只用1条语句
        }
    }


    private Order assemOrder(long orderNo,Integer userId,Integer shippingId,BigDecimal orderTotalPrice){
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(orderTotalPrice);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPostage(0);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        int insertCount = orderMapper.insert(order);
        //发货时间等等
        //付款时间等等
        if (insertCount>0){
            return order;
        }
        return null;
    }

    private OrderItemVo assemOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        BeanUtils.copyProperties(orderItem,orderItemVo);
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }


    private ShippingVo assemShippingVo(Shipping shipping){
        ShippingVo shippingVo = null;
        if (shipping!=null){
            shippingVo = new ShippingVo();
            BeanUtils.copyProperties(shipping,shippingVo);
        }
        return shippingVo;
    }

    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal orderTotalPrice = new BigDecimal("0"); //用string构造防止丢失数据精度
        for (OrderItem orderItem:orderItemList) {
            BigDecimal orderItemTotalPrice = orderItem.getTotalPrice();
            orderTotalPrice = BigDecimalUtil.add(orderTotalPrice.doubleValue(),orderItemTotalPrice.doubleValue());
        }
        return orderTotalPrice;
    }





    private ServerResponse assemOrderItemList(List<Cart> cartList,Long orderNo){
        if (CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("您的购物车中没有选中的商品");
        }

        List<OrderItem> orderItemList = Lists.newArrayList();//不用set是因为按原来放入顺序取出，再者重复元素有过处理，add会改成加数量
        for (Cart cart: cartList) {
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            //校验库存
            if (product==null){
                return ServerResponse.createByErrorMessage("购物车中"+product.getName()+"不存在");
            }
            if(cart.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"库存不足");
            }
            if (product.getStatus()!=Const.productStatusCode.ONSALE.getCode()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"不是在线售卖状态");
            }
            OrderItem orderItem = new OrderItem();
            BeanUtils.copyProperties(product,orderItem);
            BeanUtils.copyProperties(cart,orderItem);
            orderItem.setOrderNo(orderNo);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setTotalPrice(BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),orderItem.getQuantity()));
            orderItem.setId(null);
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    /**
     * 生成orderNo的策略
     * @return
     */
    private long generateOrderNo(){
        long currentTimeMillis = System.currentTimeMillis();
        long orderNo = currentTimeMillis + new Random().nextInt(100);
        return orderNo;
    }

    /**
     * 取消订单
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse<String> cancelOrder(Integer userId, Long orderNo) {
        if (orderNo==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo); //严谨逻辑
        if (order==null){
            return ServerResponse.createByErrorMessage("您的这份订单不存在");
        }
        if (order.getStatus()!=Const.OrderStatusEnum.NO_PAY.getCode()){
            return ServerResponse.createByErrorMessage("已付款,无法取消订单");
        }


        //更新订单交易状态
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int updateRow = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (updateRow>0){
            //如果订单取消成功修改商品库存
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoUserId(orderNo, userId);
            ServerResponse stockResponse = this.addProductStock(orderItemList);
            if (!stockResponse.isSuccess()){
                return stockResponse;
            }
           return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    //若取消订单更新商品库存
    private ServerResponse addProductStock(List<OrderItem> orderItemList){
        //遍历
        for (OrderItem orderItem:orderItemList) {
            int updateRow2 = productMapper.updateStockByProductId(orderItem.getQuantity(),orderItem.getProductId()); //根据商品id更新库存单个字段
            if (updateRow2 <= 0){
                return ServerResponse.createByErrorMessage("取消订单更新库存失败");
            }
        }

        return ServerResponse.createBySuccess();
    }

    public ServerResponse getCartCheckProduct(Integer userId){
        OrderProductVo orderProductVo = new OrderProductVo();
        List<Cart> cartList = cartMapper.selectCheckByUserId(userId, Const.Cart.CHECKED);

        ServerResponse response = assemOrderItemList(cartList, null);
        if (!response.isSuccess()){
            return response;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem:orderItemList) {
            OrderItemVo orderItemVo = assemOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        BigDecimal totalPrice = getOrderTotalPrice(orderItemList);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setPayment(totalPrice);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return ServerResponse.createBySuccess(orderProductVo);
    }

    /**
     * 门户订单列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> userListOrder(Integer userId,Integer pageNum,Integer pageSize){

        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        if (CollectionUtils.isEmpty(orderList)){
            return ServerResponse.createByErrorMessage("您好，您目前还没有下过订单");
        }
        PageInfo pageInfo = new PageInfo(orderList);

        pageInfo.setList(assembleOrderVoList(orderList,userId));

        return ServerResponse.createBySuccess(pageInfo);
    }


    public ServerResponse<PageInfo> adminListOrder(Integer pageNum,Integer pageSize){
       /* if(StringUtils.isBlank(receiverName)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        receiverName = new StringBuilder().append("%").append(receiverName).append("%").toString();*/

        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAll();
        if (CollectionUtils.isEmpty(orderList)){
            return ServerResponse.createByErrorMessage("数据库1笔订单都不存在");
        }
        PageInfo pageInfo = new PageInfo(orderList);

        pageInfo.setList(assembleOrderVoList(orderList,null));

        return ServerResponse.createBySuccess(pageInfo);
    }

    private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId){
        if (CollectionUtils.isEmpty(orderList)){
            return null;
        }
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order:orderList) {
            List<OrderItem> orderItemList ;
            if (userId==null){
                //后台管理页面list 订单
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            } else {
                //前台用户list 订单
                orderItemList = orderItemMapper.selectByOrderNoUserId(order.getOrderNo(),userId);
            }
            OrderVo orderVo = assemOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }

        return orderVoList;
    }


    public ServerResponse getOrderDetail(Integer userId,Long orderNo){
        if (orderNo==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order==null){
            return ServerResponse.createByErrorMessage("这份订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoUserId(orderNo, userId);
        if (CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByErrorMessage("这份订单一份商品都没购买，空交易");
        }
        OrderVo orderVo = assemOrderVo(order, orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    public ServerResponse<OrderVo> manageDetail(Long orderNo){
        if (orderNo==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order==null){
            return ServerResponse.createByErrorMessage("这份订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);

        OrderVo orderVo = assemOrderVo(order, orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 后台条件查询，（多条件组合查询）
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse manageCriteriaQuery(Long orderNo,int pageNum,int pageSize){

        if (orderNo==null){
            return ServerResponse.createByErrorMessage("请传入查询条件");
        }
        PageHelper.startPage(pageNum,pageSize);

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order==null){
            return ServerResponse.createByErrorMessage("这份订单不存在");
        }

        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = assemOrderVo(order, orderItemList);
        List<OrderVo> orderVoList = Lists.newArrayList(orderVo);

        PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
        pageInfo.setList(orderVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse manageSendGoods(Long orderNo){
        if (orderNo==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order==null){
            return ServerResponse.createByErrorMessage("这份订单不存在");
        }

        if (Const.OrderStatusEnum.PAID.getCode()==order.getStatus()){
            Order updateShippedOrder = new Order();
            updateShippedOrder.setId(order.getId());
            updateShippedOrder.setSendTime(new Date());
            updateShippedOrder.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            orderMapper.updateByPrimaryKeySelective(updateShippedOrder);
            return ServerResponse.createBySuccess("发货成功");
        }

        return ServerResponse.createByErrorMessage("该订单交易状态不满足发货要求");
    }


    public ServerResponse<Map<String,String>> pay(Integer userId,Long orderNo,String path){
        Map<String ,String> resultMap = Maps.newHashMap();
        if (orderNo ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }

        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        resultMap.put("orderNo",order.getOrderNo().toString());
        if (order !=null){
            // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
            // 需保证商户系统端不能重复，建议通过数据库sequence生成，
            String outTradeNo = order.getOrderNo().toString();

            // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
            String subject = new StringBuilder().append("happymmall扫码支付,订单号:").append(outTradeNo).toString();

            // (必填) 订单总金额，单位为元，不能超过1亿元
            // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
            String totalAmount = order.getPayment().toString();

            // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
            // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
            String undiscountableAmount = "0";

            // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
            // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
            String sellerId = "";

            // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
            String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

            // 商户操作员编号，添加此参数可以为商户操作员做销售统计
            String operatorId = "test_operator_id";

            // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
            String storeId = "test_store_id";

            // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
            ExtendParams extendParams = new ExtendParams();
            extendParams.setSysServiceProviderId("2088100200300400500"); //pid 2088102175911193

            // 支付超时，定义为120分钟
            String timeoutExpress = "120m";

            // 商品明细列表，需填写购买商品详细信息，
            List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
            List<OrderItem> orderItemList = Lists.newArrayList();
            orderItemList = orderItemMapper.selectByOrderNoUserId(orderNo,userId);

            for (OrderItem orderItem: orderItemList) {
                // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
                GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName().toString(), orderItem.getCurrentUnitPrice().longValue(),
                        orderItem.getQuantity());
                // 创建好一个商品后添加至商品明细列表
                goodsDetailList.add(goods);
            }


            // 创建扫码支付请求builder，设置请求参数
            AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                    .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                    .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                    .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                    .setTimeoutExpress(timeoutExpress)
                    .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                    .setGoodsDetailList(goodsDetailList);



            AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
            switch (result.getTradeStatus()) {
                case SUCCESS:
                    File uplodaFile = new File(path);
                    if (!uplodaFile.exists()){
                        uplodaFile.setWritable(true);
                        uplodaFile.mkdirs();
                    }
                    logger.info("支付宝预下单成功: )");

                    AlipayTradePrecreateResponse response = result.getResponse();
                    dumpResponse(response);

                    // 需要修改为运行机器上的路径
                    String qrPath = String.format(path+"/qr-%s.png",
                            response.getOutTradeNo());  // path需要是String类型的
                    String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                    ZxingUtils.getQRCodeImge(response.getQrCode(),256,qrPath); // 需要防止在目标string路径下生成二维码图片时路径不存在的问题
                    //logger.info("qrPath:" + qrPath);
                    //                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                    File targetFile =new File(path,qrFileName);
                    try {
                        FtpUtil.uploadFile(Lists.<File>newArrayList(targetFile));
                    } catch (IOException e) {
                        logger.error("上传二维码异常",e);
                    }
                    logger.info("qrPath:" + qrPath);
                    resultMap.put("qrPath",PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName());
                    return ServerResponse.createBySuccess(resultMap);

                case FAILED:
                    logger.error("支付宝预下单失败!!!");
                    return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

                case UNKNOWN:
                    logger.error("系统异常，预下单状态未知!!!");
                    return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

                default:
                    logger.error("不支持的交易状态，交易返回异常!!!");
                    return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
            }


//            return ServerResponse.createBySuccess(order);
        }
        return ServerResponse.createByErrorMessage("查不到该订单");
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    public ServerResponse alipayCallback(Map<String,String> params){

        String out_trade_no = params.get("out_trade_no"); //获取外部订单号
        String trade_no = params.get("trade_no");//支付宝交易号
        String trade_status = params.get("trade_status"); //获取支付宝端的交易状态
        Order orderItem = orderMapper.selectByUserIdAndOrderNo(null, Long.valueOf(out_trade_no));
        if(orderItem==null){
            logger.error("商户找不到该订单");
            return ServerResponse.createByErrorMessage("非快乐慕商城的订单,回调忽略");
        }
        if (orderItem.getStatus()>= Const.OrderStatusEnum.PAID.getCode()){
            logger.error("支付宝重复回调");
            return ServerResponse.createByErrorMessage("订单已修改状态，过滤重复通知");
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(orderItem.getUserId());
        payInfo.setOrderNo(orderItem.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformStatus(trade_status);
        payInfo.setPlatformNumber(trade_no);
        payInfoMapper.insert(payInfo);

        if (trade_status.equals(Const.AlipayCallbackStatus.TRADE_STATUS_TRADE_SUCCESS)){
            Order updateOrder = new Order();
            updateOrder.setId(orderItem.getId());
            updateOrder.setStatus(Const.OrderStatusEnum.PAID.getCode());
            updateOrder.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            orderMapper.updateByPrimaryKeySelective(updateOrder);
            return ServerResponse.createBySuccess();
        }

        return ServerResponse.createByError();
    }

    public ServerResponse queryOrderPayStatus(Integer userId,Long orderNo){
        if (orderNo==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order==null){
            logger.error("数据库中找不到该订单");
            return ServerResponse.createByErrorMessage("找不到该订单");
        }
        if (order.getStatus()>= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }

        return  ServerResponse.createByError();
    }
}
