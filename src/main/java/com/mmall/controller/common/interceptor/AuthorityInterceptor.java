package com.mmall.controller.common.interceptor;

import com.google.common.collect.Maps;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.ShardedRedisPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//服务器不做服务器转发
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;
    /**
     * 处理(controller)之前，
     * @param request
     * @param response
     * @param handler 实际是handlerMethod方法 ，指定prehandle是哪个controller的，通过clazz
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle");
        //请求中Controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //获取请求的uri
        String requestURI = request.getRequestURI();

        //解析HandlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String clazzName = handlerMethod.getBean().getClass().getSimpleName();

        //https://www.cnblogs.com/ooo0/p/7741651.html
        // indexof 也可
        boolean isAdminRequest = StringUtils.contains(requestURI, "manage");
        //使代码更加灵活
        //两种方式，让工作中选择更加灵活的策略
        if (StringUtils.equals(clazzName,"UserManageController")&&StringUtils.equals(methodName,"login")){
            log.info("权限拦截器拦截到请求,className:{},methodName:{}",clazzName,methodName);
            //如果是拦截到登录请求，不打印参数，因为参数里面有密码，全部会打印到日志中，防止日志泄露
            return true;
        }

        if (StringUtils.equals(clazzName,"UserController")&&StringUtils.equals(methodName,"login")){
            log.info("权限拦截器拦截到请求,className:{},methodName:{}",clazzName,methodName);
            //如果是拦截到登录请求，不打印参数，因为参数里面有密码，全部会打印到日志中，防止日志泄露
            return true;
        }
        if (StringUtils.equals(clazzName,"ProductController")&&StringUtils.equals(methodName,"listProductDetail")){
            log.info("权限拦截器拦截到请求,className:{},methodName:{}",clazzName,methodName);
            //如果是拦截到登录请求，不打印参数，因为参数里面有密码，全部会打印到日志中，防止日志泄露
            return true;
        }

        //解析参数,具体的参数key以及value是什么，我们打印日志
        StringBuffer requestParamBuffer = new StringBuffer();
        Map<String, String[]> parameterMap = request.getParameterMap();
        Iterator iterator = parameterMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry entry = (Map.Entry)iterator.next();
            String key = (String)entry.getKey();
            //这样更加规范，
            // 当我们要查找替换的时候搜索这个关键字会比较准
            // 搜索""就没有EMPTY精确
            String parameterValue = StringUtils.EMPTY;
            //为什么map返回的是泛型，代码返回的是string[]，并且确定
            //因为方法返回的是object,我们还要为了代码的健壮性对这个类型进行判断
            //request这个参数的map，里面的value返回的是一个String[]
            Object value = entry.getValue();
            if (value!=null&&value instanceof String[]){
                String[] strings = (String[]) value;
                parameterValue = Arrays.toString(strings);
                //拼接与url传参相同，拦截器拦截了各种请求，log查问题方便，知道当时的请求参数是什么
                //登录的日志就不打印了，一旦日志泄露，安全性问题很严重
            }
            //建议key是null的不传
            requestParamBuffer.append(key).append("=").append(parameterValue);
        }
        //方便排查问题
        log.info("权限拦截器拦截到请求,className:{},methodName:{},param:{}",clazzName,methodName,requestParamBuffer.toString());

        User user = null;
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isNotBlank(loginToken)) {
            String userStr = ShardedRedisPoolUtil.get(loginToken);
            user = JsonUtil.Json2Obj(userStr, User.class);
        }

        //是admin的后台请求，且没有管理员权限
        if (user==null||(!(userService.checkAdmin(user).isSuccess())&&isAdminRequest)){
            //将response托管到拦截器中，需要对这些属性进行重新设置
            //返回false.即不会调用controller里的方法
            response.reset();//hyjnote 这里要添加reset，否则报异常 getWriter() has already been called for this response.
            response.setCharacterEncoding("UTF-8");//hyjnote 这里要设置编码，否则会乱码
            response.setContentType("application/json;charset=UTF-8");//hyjnote 这里要设置返回值的类型，因为全部是json接口。

            PrintWriter out = response.getWriter();
            if(user==null){
                    //因为simditor返回值有特殊要求，所以拦截器要单独处理
                    //上传由于富文本的控件要求，要特殊处理返回值，这里面区分是否登录以及是否有权限
                    if (StringUtils.equals(clazzName,"ProductManageController" )&& StringUtils.equals(methodName,"richtextImgUpload")){
                        HashMap resultMap = Maps.newHashMap();
                        resultMap.put("success",false);
                        resultMap.put("msg","请登录（以管理员身份）");
                        out.write(JsonUtil.obj2Json(resultMap));
                    }else {

                        out.write(JsonUtil.obj2Json(ServerResponse.createByErrorMessage("拦截器拦截，用户未登录")));
                    }


            }else {
                if (StringUtils.equals(clazzName,"ProductManageController" )&& StringUtils.equals(methodName,"richtextImgUpload")){
                    HashMap map = Maps.newHashMap();
                    map.put("success",false);
                    map.put("msg",",拦截器拦截，用户没有权限");
                    out.write(JsonUtil.obj2Json(map));
                }else {
                    out.write(JsonUtil.obj2Json(ServerResponse.createByErrorMessage("拦截器拦截，用户没有权限")));
                }

            }
            out.flush();
            out.close();//hyjnote 这里要关闭
            return  false;
        }


        return true;
    }

    public PrintWriter getResetResponseOut(HttpServletResponse response){
        response.reset();//hyjnote 这里要添加reset，否则报异常 getWriter() has already been called for this response.
        response.setCharacterEncoding("UTF-8");//hyjnote 这里要设置编码，否则会乱码
        response.setContentType("application/json;charset=UTF-8");//hyjnote 这里要设置返回值的类型，因为全部是json接口。
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * 处理(controller)之后 ，在controller之后，已经完成业务逻辑了，
     * 可能用来再包装数据用的，然后才返回给前端
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle");
    }

    /**
     * 所有处理完成之后，如果不是前后端分离的，返回modelAndView,在页面（视图）展示呈现完成之后调用
     * 一般用来释放一些资源，连接池的连接等
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("afterCompletion");
    }
}
