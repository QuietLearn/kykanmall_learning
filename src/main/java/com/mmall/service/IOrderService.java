package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Order;
import com.mmall.vo.OrderVo;

import java.util.Map;

public interface IOrderService {
    ServerResponse<Map<String,String>> pay(Integer userId, Long orderNo, String path);

    /**
     * 做支付宝回调 商户端的业务处理
     * @param params 支付宝回调传过来的相关订单参数(交易状态)
     * @return
     */
    ServerResponse alipayCallback(Map<String,String> params);

    /**
     * 前端轮询查询订单交易状态判断是否可以跳转页面
     * @param userId
     * @param orderNo
     * @return
     */
    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);


    ServerResponse<OrderVo> generateOrderVo(Integer userId, Integer shippingId);

    ServerResponse cancelOrder(Integer userId, Long orderNo);

    /**
     * 获取购物车选中商品的信息
     * @param userId
     * @return
     */
    ServerResponse getCartCheckProduct(Integer userId);

    ServerResponse<PageInfo> userListOrder(Integer userId, Integer pageNum, Integer pageSize);

    ServerResponse getOrderDetail(Integer userId,Long orderNo);

    //backend
    ServerResponse<PageInfo> adminListOrder(Integer pageNum,Integer pageSize);

    ServerResponse<OrderVo> manageDetail(Long orderNo);

    ServerResponse manageCriteriaQuery(Long orderNo,int pageNum,int pageSize);

    ServerResponse manageSendGoods(Long orderNo);
}
