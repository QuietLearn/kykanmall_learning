package com.mmall.dao;

import com.mmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    List<Shipping> selectListByUserId(Integer userId);

    int updateByIdAndUserId(Shipping shipping);

    int deleteByIdAndUserId(@Param("shippingId") Integer shippingId,@Param("userId")  Integer userId);

    Shipping selectByIdAndUserId(@Param("shippingId") Integer shippingId,@Param("userId")  Integer userId);
}