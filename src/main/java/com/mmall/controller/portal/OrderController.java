package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.OrderItemMapper;
import com.mmall.dao.OrderMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Order;
import com.mmall.pojo.OrderItem;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RequestMapping("/order/")
@Controller
@Slf4j
public class OrderController {


    @Autowired
    private IOrderService iOrderService;


    @RequestMapping("generate_order.do")
    @ResponseBody
    public ServerResponse generateOrder(HttpSession session,Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.generateOrderVo(user.getId(),shippingId);
    }

    @RequestMapping("cancel_order.do")
    @ResponseBody
    public ServerResponse cancelOrder(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancelOrder(user.getId(),orderNo);
    }

    @RequestMapping("get_cart_check_product.do")
    @ResponseBody
    public ServerResponse getCartCheckProduct(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getCartCheckProduct(user.getId());
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse listOrder(HttpSession session, @RequestParam(value ="pageNum",defaultValue = "1") Integer pageNum,@RequestParam(value = "pageSize",defaultValue = "10")  Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.userListOrder(user.getId(),pageNum,pageSize);
    }

    @RequestMapping("get_order_detail.do")
    @ResponseBody
    public ServerResponse getOrderDetail(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }

    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse<Map<String,String>> pay(HttpSession session,Long orderNo,HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(user.getId(),orderNo,path);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        Map<String, String> params = Maps.newHashMap();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = iter.next();
            String[] valueArray = requestParams.get(name);
            String value ="";
            for (int i = 0; i < valueArray.length; i++) {
                value =  i==valueArray.length-1? value + valueArray[i]:value + valueArray[i]+",";
            }
            //乱码解决，这段代码在出现乱码时使用
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");因为tomcat的setting.xml设置过了
            params.put(name,value);
        }
        log.info("支付宝回调，sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());//map自带tostring

        //非常重要,验证回调的正确性,是不是支付宝发的.并且呢还要避免重复通知.

        params.remove("sign_type");
        try {
            //好像因为空格的原因老是验签失败
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if(!alipayRSACheckedV2){
                log.error("验签失败，不是支付宝的回调");
                return ServerResponse.createByErrorMessage("验签不通过，不是支付宝传过来的数据");
            }
            log.info("验签正确，准备根据支付宝回调来的参数做交易状态等的处理");
        } catch (AlipayApiException e) {
            log.error("支付宝验签回调参数异常",e);
        }



        //TODO 执行判断逻辑（验证支付宝返回订单详情与商户订单详情是否一致），然后-库存，更改订单状态

        ServerResponse response = iOrderService.alipayCallback(params);
        if (response.isSuccess()){
            log.info("商户端回调函数处理数据成功，返回success给支付宝服务端结束通知");
            return Const.AlipayCallbackStatus.RESPONSE_SUCCESS;
        }

        return Const.AlipayCallbackStatus.RESPONSE_FAILED;
    }

    /**
     * 前端轮询查询订单交易状态判断是否可以跳转页面
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(),orderNo);
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }
}
