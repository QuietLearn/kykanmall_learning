package com.mmall.util;

import org.springframework.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtil {
    //同级别的域名访问(读)不到同级别域名domain下的cookie
    private final static String COOKIE_DOMAIN=".happymmall.com";
    private final static String COOKIE_NAME="mmall_login_token";
    //token就是cookie--jsessionId

    public static void writeCookie(HttpServletRequest request, HttpServletResponse response){

    }
}
