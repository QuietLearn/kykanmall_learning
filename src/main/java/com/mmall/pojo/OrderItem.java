package com.mmall.pojo;

import lombok.*;
import java.math.BigDecimal;
import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * 订单中每个购买商品项的数据
 */
public class OrderItem {
    //id
    private Integer id;
    //用户id
    private Integer userId;
    //订单号
    private Long orderNo;
    //产品id
    private Integer productId;
    //产品名
    private String productName;
    //产品图片
    private String productImage;
    //当前单价
    private BigDecimal currentUnitPrice;
    //数量
    private Integer quantity;
    //总价
    private BigDecimal totalPrice;

    private Date createTime;

    private Date updateTime;

}