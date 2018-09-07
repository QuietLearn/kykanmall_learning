package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Order;

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


    ServerResponse generateOrderVo(Integer userId,Integer shippingId);
}
