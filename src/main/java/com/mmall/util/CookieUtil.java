package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtil {
    //同级别的域名访问(读)不到同级别域名domain下的cookie
    private final static String COOKIE_DOMAIN=".happymmall.com";
    private final static String COOKIE_NAME="mmall_login_token";
    //token就是cookie--jsessionId

    public static void writeLoginToken(HttpServletResponse response,String token){
        Cookie cookie = new Cookie(COOKIE_NAME,token);
        cookie.setDomain(COOKIE_DOMAIN);
        //代表设置在根目录
        cookie.setPath("/");
        //防止脚本攻击带来的信息泄露风险
        //这个属性规定不能用脚本访问cookie，在使用httponly这个cookie之后，web站点就能排除cookie中的敏感信息被发送给黑客的计算机
        //或者使用脚本的web站点的可能性，这么设置之后，无法通过脚本来获取cookie信息，同时浏览器
        //也不会把cookie发送给任何第三方，保证信息的安全，无法全面保障站点的防止脚本攻击的安全，但能提高一定安全性
        //tomcat7默认servlet3.0，httponly属性提供可以设置，tomcat6 servlet 2.x不提供此set方法 ,
        // 只能设置在应答头里，response headers,发给浏览器
        cookie.setHttpOnly(true);
        //单位是秒。
        //如果这个maxage不设置的话，cookie就不会写入硬盘，而是写在内存。只在当前页面有效。
        cookie.setMaxAge(60 * 60 * 24 * 365);//如果是-1，代表永久
        log.info("write cookieName:{},cookieValue:{}",cookie.git etName(),cookie.getValue());
        response.addCookie(cookie);
    }

    public static String readLoginLoken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (cookies!=null){
            for (Cookie cookie:cookies) {
                log.info("read cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
                if (StringUtils.equals(cookie.getName(),COOKIE_NAME)){
                    log.info("return cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
       return null;
    }

    public static void delLoginToken(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        if (cookies!=null){
            for (Cookie cookie:cookies) {
                log.info("read cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
                if (StringUtils.equals(cookie.getName(),COOKIE_NAME)){
                    cookie.setDomain(COOKIE_DOMAIN);
                    cookie.setPath("/");
                    cookie.setMaxAge(0);//0就是没有生存时间
                    log.info("del cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
                    response.addCookie(cookie);
                    return;
                }
            }
        }

    }
}
