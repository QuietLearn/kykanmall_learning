package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.ShardedRedisPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RequestMapping("/manage/user/")
@Controller
public class UserManageController {

    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do" , method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse){

        ServerResponse<User> response = iUserService.login(username,password);
        if (response.isSuccess()){
            User user = response.getData();
            if (Const.Role.ROLE_ADMIN == user.getRole().intValue()){
                //session.setAttribute(Const.CURRENT_USER,user);

                CookieUtil.writeLoginToken(httpServletResponse,session.getId());
                ShardedRedisPoolUtil.setEx(session.getId(), JsonUtil.obj2Json(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
                return response;
            }else {
                return ServerResponse.createByErrorMessage("不是管理员身份，不能登录");
            }
        }
        return response;
    }
}
