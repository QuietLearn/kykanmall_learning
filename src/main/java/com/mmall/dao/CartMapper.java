package com.mmall.dao;

import com.alipay.api.domain.Car;
import com.google.common.collect.Lists;
import com.mmall.pojo.Cart;
import com.mmall.pojo.OrderItem;
import com.mmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);


    List<Cart> selectByUserId(Integer id);

    Cart selectByUserIdProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    int selectUnCheckByUserId(Integer userId);

    List<Cart> selectCheckByUserId(@Param("userId") Integer userId,@Param("checked") Integer checked);

    int deleteByProductIds(@Param("userId") Integer userId, @Param("productIdList") List<String> productIdList);

    int deleteByCartList(@Param("userId") Integer userId,@Param("cartList") List<Cart> cartList);

    int checkedOrUncheckedProduct(@Param("userId")Integer userId,@Param("productId")Integer productId,@Param("checked")int checked);

    int selectCartProductTotalCount(Integer userId);
}