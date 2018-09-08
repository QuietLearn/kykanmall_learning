package com.mmall.dao;

import com.mmall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> selectByOrderNoUserId(@Param("orderNo") Long orderNo,@Param("userId") Integer userId);

    List<OrderItem> selectByOrderNo(@Param("orderNo") Long orderNo); //为了以后服务化把前台和后台管理拆开


    int batchInsert(@Param("orderItemList") List<OrderItem> orderItemList);
}