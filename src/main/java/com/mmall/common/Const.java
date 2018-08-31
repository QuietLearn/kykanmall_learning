package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

public class Const {
    public static final String CURRENT_USER = "currentUser";

    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public static final int ONSALE_CODE = 1 ;
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
}
