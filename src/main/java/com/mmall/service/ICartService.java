package com.mmall.service;

import com.mmall.common.ServerResponse;

public interface ICartService {
    ServerResponse addProduct(Integer userId, Integer productId, Integer count);
}
