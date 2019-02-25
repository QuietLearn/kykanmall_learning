package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.RedisPool;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
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
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 用户登录
     * @param username springmvc数据绑定
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest){
        ServerResponse<User> response = iUserService.login(username,password);

        if (response.isSuccess()){
            //如果是成功登录的status，那么response就会有data，将成功登录的用户信息存入session
            //session.setAttribute(Const.CURRENT_USER,response.getData());
            //33E451B8AC8F01F95DED9B55C44075D7
            //B70915CC38AA5AE1D22C48D8869CA054
            CookieUtil.writeLoginToken(httpServletResponse,session.getId());

            RedisPoolUtil.setEx(session.getId(), JsonUtil.obj2Json(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return response ;
    }

    /**
     * 用户登出
     * @param session
     * @return
     */
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logOut(HttpSession session,HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse){
        session.removeAttribute(Const.CURRENT_USER);
        //CookieUtil.readLoginLoken(httpServletRequest);
        CookieUtil.delLoginToken(httpServletRequest,httpServletResponse);
        return ServerResponse.createBySuccess();
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @RequestMapping(value = "regist.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> regist(User user){
        return iUserService.regist(user);
    }

    /**
     * 用户注册页面检测用户名或者邮箱是否已存在
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type){
        return iUserService.checkValid(str,type);
    }

    /**
     * 获取用户信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_user_info_from_session.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfoFromSession(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user!=null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户尚未登录，无法获取用户相关信息");
    }

    /**
     * 忘记密码 返回问题
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_pass_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetPassGetQuestion(String username){
        return iUserService.forgetPassGetQuestion(username);
    }

    /**
     * 忘记密码 检测答案
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return iUserService.checkAnswer(username,question,answer);
    }

    /**
     * 忘记密码 重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        return iUserService.forgetResetPassword(username,passwordNew,forgetToken);
    }

    //要判断登录状态
    //因为只输入用户名，可能登录状态下能把别人的密码改掉了
    @RequestMapping(value = "login_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> loginResetPassword(HttpSession session,String passwordOld,String passwordNew){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return  ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.loginResetPassword(passwordOld,passwordNew,user);
    }

    /**
     * 修改用户信息
     * @param session
     * @param alterUser
     * @return
     */
    @RequestMapping(value = "update_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateUserInfo(HttpSession session,User alterUser){
        User existUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (existUser==null){
            return  ServerResponse.createByErrorMessage("用户未登录");
        }
        alterUser.setId(existUser.getId());//防止越权问题，防止id被变化,从而修改别人的信息
        alterUser.setUsername(existUser.getUsername()); //username不可修改
        ServerResponse<User> infoResponse = iUserService.updateUserInfo(alterUser);
        if (infoResponse.isSuccess()){
            //username不可修改
            infoResponse.getData().setUsername(existUser.getUsername());
            //session更新 更新后的用户信息
            session.setAttribute(Const.CURRENT_USER,infoResponse.getData());
        }
        //将用户信息返回给前端，便于直接展示更新后的用户信息
        return infoResponse;
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
