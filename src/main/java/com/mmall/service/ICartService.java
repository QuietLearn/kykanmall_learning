package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

public interface ICartService {

    ServerResponse<CartVo> list(Integer userId);

    ServerResponse<CartVo> addProduct(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> updateCart(Integer userId,Integer productId,Integer count);

    ServerResponse<CartVo> removeProduct(Integer userId,String productIds);

    ServerResponse<CartVo> selectProductOrNot(Integer userId,Integer productId,Integer checked);

    ServerResponse<Integer> getCartProductTotalCount(Integer userId);
}
