package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

public interface IShippingService {

    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse<Integer> update(Shipping shipping,Integer userId);

    ServerResponse<Integer> delete(Integer shippingId,Integer userId);

    ServerResponse list(Integer userId,Integer pageNum,Integer pageSize);

    ServerResponse<Shipping> getDetail(Integer shippingId,Integer userId);
}
