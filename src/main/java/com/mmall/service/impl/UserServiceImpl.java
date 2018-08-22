package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {

        int userNum = userMapper.checkName(username);

        if (userNum==0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //密码转为md5格式验证
        String md5password = MD5Util.MD5EncodeUtf8(password);


        User user = userMapper.login(username,md5password);

        if (user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createBySuccess("登录成功",user);
    }

    @Override
    public ServerResponse<String> regist(User user) {
        ServerResponse response = this.checkValid(user.getUsername(),Const.USERNAME);//调用checkValid方法以重用代码检测用户名是否已存在,返回的response用status标注结果
        //判断status来判断是否注册成功（无重复值）
        if (!response.isSuccess()){
            return response;
        }
        response = this.checkValid(user.getEmail(),Const.EMAIL);
        if (!response.isSuccess()){
            return response;
        }
        //设置用户为普通用户
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //md5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);
        if (resultCount == 0){//检测是否数据库有插入成功
            return ServerResponse.createByErrorMessage("注册失败");
        }

        return ServerResponse.createByErrorMessage("注册成功");
    }

    /**
     *
     * @param str   具体value值
     * @param type   传入的注册页面key 值
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)){  //检测type是否为空值包括"  ",空值检测没有意义
            if (Const.USERNAME.equals(type)){
               int resultCount = userMapper.checkName(str);
               if (resultCount>0)
                   return ServerResponse.createByErrorMessage("用户名已存在");
            }
            if (Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if (resultCount>0)
                    return ServerResponse.createByErrorMessage("邮箱已存在");
            }
        }

        return ServerResponse.createBySuccessMessage("检测成功");
    }

    @Override
    public ServerResponse<String> forgetPassGetQuestion(String username) {

        ServerResponse invalidResponse = this.checkValid(username,Const.USERNAME);
        if (invalidResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (!StringUtils.isNotBlank(question)){
            return ServerResponse.createByErrorMessage("问题为空，尚未设置");
        }
        return ServerResponse.createBySuccess(question);
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if (resultCount>0){
           String token =  UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,token);
            return ServerResponse.createBySuccess(token);
        }
        return ServerResponse.createByErrorMessage("问题的答案不正确");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        ServerResponse invalidResponse = this.checkValid(username,Const.USERNAME);
        if (invalidResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        if (StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("token为空，请重新回答问题获取");
        }

        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.equals(forgetToken,token)){
            String md5PasswordNew = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5PasswordNew);
            if (rowCount>0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新回答问题获取");
        }

        return ServerResponse.createByErrorMessage("修改密码错误");
    }

    public ServerResponse<String> loginResetPassword(String passwordOld, String passwordNew, User user){
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;
        //因为可能有时候登录未登出，有其他人使用这台电脑并通过此 修改了密码，那么是很不安全的
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));

        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount>0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }

        return ServerResponse.createBySuccessMessage("密码更新失败");
    }

    @Override
    public ServerResponse<User> updateUserInfo(User alterUser) {
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.否则就是逻辑错误了
        int resultCount = userMapper.checkEmailByUserId(alterUser.getEmail(),alterUser.getId());
        if (resultCount>0){
            return  ServerResponse.createByErrorMessage("此邮箱已被其他用户使用");
        }
        User updateUser = new User();
        updateUser.setId(alterUser.getId());
        updateUser.setEmail(alterUser.getEmail());
        updateUser.setPhone(alterUser.getPhone());
        updateUser.setQuestion(alterUser.getQuestion());
        updateUser.setAnswer(alterUser.getAnswer());

        //便于日后扩展
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount>0){
            //user.setPassword(StringUtils.EMPTY);//密码设为空，不暴露出去
            return  ServerResponse.createBySuccess("用户信息更新成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("用户信息更新失败");
    }

}
