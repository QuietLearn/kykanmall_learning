/**
 * 业务上的common
 */
package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

public class Const {
    public static final String CURRENT_USER = "currentUser";

    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public static final int ONSALE_CODE = 1 ;

    public interface RedisCacheExtime{
        int REDIS_SESSION_EXTIME = 60 * 30;// 30 min
    }
    public interface Role{
        int ROLE_CUSTOMER = 0; //普通用户
        int ROLE_ADMIN = 1;//管理员
    }

    public interface Cart{
        int CHECKED = 1;
        int NO_CHECKED =0;
        String LIMIT_NUM_SUCCESS ="LIMIT_NUM_SUCCESS";
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL ";
    }

    public interface PriceOrderBy{
        Set<String> PRICE_ORDER_RULE = Sets.newHashSet("price_desc","price_asc");
    }


    public enum productStatusCode{
        ONSALE(1,"在售");
        private int code;
        private String status;

        private productStatusCode(int code,String status){
            this.code = code;
            this.status = status;
        }

        public int getCode() {
            return code;
        }

        public String getStatus() {
            return status;
        }
    }

    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭");

        private int code;
        private String status;

        private OrderStatusEnum(int code,String status){
            this.code = code;
            this.status = status;
        }

        public int getCode() {
            return code;
        }

        public String getStatus() {
            return status;
        }

        public static OrderStatusEnum getOrderStatusEnum(int code){
            for(OrderStatusEnum orderStatusEnum:values()){
                if (orderStatusEnum.getCode()==code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("找不到该code对应枚举");
        }
    }

    public interface AlipayCallbackStatus{
        String  TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝");

        private int code;
        private String status;

        private PayPlatformEnum(int code,String status){
            this.code = code;
            this.status = status;
        }

        public int getCode() {
            return code;
        }

        public String getStatus() {
            return status;
        }

        public static PayPlatformEnum codeOf(int code){
            for(PayPlatformEnum payPlatformEnum:values()){
                if (payPlatformEnum.getCode()==code){
                    return payPlatformEnum;
                }
            }
            throw new RuntimeException("找不到该code对应支付类型枚举");
        }
    }

    public enum PaymentTypeEnum{
        ONLINE_PAY(1,"在线支付");

        private int code;
        private String status;

        private PaymentTypeEnum(int code,String status){
            this.code = code;
            this.status = status;
        }

        public int getCode() {
            return code;
        }

        public String getStatus() {
            return status;
        }

        public static PaymentTypeEnum codeOf(int code){
            for(PaymentTypeEnum paymentTypeEnum:values()){
                if (paymentTypeEnum.getCode()==code){
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("找不到该code对应支付类型枚举");
        }
    }
}
