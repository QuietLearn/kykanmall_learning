package com.mmall.vo;

import com.google.common.collect.Lists;
import com.mmall.pojo.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public class OrderProductVo {

    private List<OrderItemVo> orderItemVoList = Lists.newArrayList();

    private String imageHost;

    private BigDecimal payment;


    public List<OrderItemVo> getOrderItemVoList() {
        return orderItemVoList;
    }

    public void setOrderItemVoList(List<OrderItemVo> orderItemVoList) {
        this.orderItemVoList = orderItemVoList;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    public BigDecimal getPayment() {
        return payment;
    }

    public void setPayment(BigDecimal payment) {
        this.payment = payment;
    }
}
