package com.mmall.service;


import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

public interface IUserService {
    /**
     * 用户登录逻辑（一个事务内）
     * @param username
     * @param password
     * @return
     */
    public ServerResponse<User> login(String username, String password);

    /**
     * 注册逻辑
     * @param user
     * @return
     */
    ServerResponse<String> regist(User user);

    /**
     * 检测用户名或者邮箱是否已存在
     * @param str   具体value值
     * @param type   传入的注册页面key 值
     * @return
     */
    ServerResponse<String> checkValid(String str, String type);

    /**
     * 忘记密码 根据用户名获取问题
     * @param username
     * @return
     */
    ServerResponse<String> forgetPassGetQuestion(String username);

    /**
     * 忘记密码 检查问题的回答正确与否
     * @param username
     * @param question
     * @param answer
     * @return
     */
    ServerResponse<String> checkAnswer(String username, String question, String answer);

    /**
     * 忘记密码 重设密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    /**
     * 登录状态 重置密码
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    ServerResponse<String> loginResetPassword(String passwordOld, String passwordNew, User user);

    /**
     * 登录状态 修改用户信息逻辑
     * @param alterUser
     * @return
     */
    ServerResponse<User> updateUserInfo(User alterUser);

    /**
     * 获取用户详细信息（数据库）
     * @param id
     * @return
     */
    ServerResponse<User> getUserDetail(Integer id);

    /**
     * 检查用户是否是管理员
     * @param user
     * @return
     */
    ServerResponse checkAdmin(User user);
}
