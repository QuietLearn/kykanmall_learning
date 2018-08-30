package com.mmall.dao;

import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);


    List<Product> selectByUserId(Integer id);
}