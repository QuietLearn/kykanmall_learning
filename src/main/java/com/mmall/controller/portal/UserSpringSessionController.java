package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.ShardedRedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * created by hyj
 */
@Controller
@RequestMapping("/user/springSession")
public class UserSpringSessionController {

    @Autowired
    private IUserService iUserService;

    /**
     * 用户登录
     * @param username springmvc数据绑定
     * @param password
     * @param session  servlet自带session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest){


        ServerResponse<User> response = iUserService.login(username,password);

        if (response.isSuccess()){
            //如果是成功登录的status，那么response就会有data，将成功登录的用户信息存入session
            /*  CookieUtil.writeLoginToken(httpServletResponse,session.getId());
            ShardedRedisPoolUtil.setEx(session.getId(), JsonUtil.obj2Json(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);*/
            session.setAttribute(Const.CURRENT_USER,response.getData());

        }
        return response ;
    }

    /**
     * 用户登出
     * @param session
     * @return
     */
    @RequestMapping(value = "logout.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> logOut(HttpSession session){
        /* String loginToken = CookieUtil.readLoginLoken(httpServletRequest);
        if (StringUtils.isBlank(loginToken))
            return ServerResponse.createBySuccessMessage("您并未登录，无需登出");
        CookieUtil.delLoginToken(httpServletRequest,httpServletResponse);

        ShardedRedisPoolUtil.del(loginToken);*/
        session.removeAttribute(Const.CURRENT_USER);

        return ServerResponse.createBySuccess();
    }

    /**
     * 获取用户信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        /*  String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户尚未登录，无法获取用户相关信息");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);*/

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user!=null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户尚未登录，无法获取用户相关信息");
    }


    @RequestMapping(value = "get_user_detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserDetail(HttpSession session){
        User existUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (existUser==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"必须登录才能获取用户信息");
        }
        ServerResponse<User> userDetailResponse = iUserService.getUserDetail(existUser.getId());
        return userDetailResponse;
    }
}
