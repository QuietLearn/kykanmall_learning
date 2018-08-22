package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    /**
     * 根据用户名检测已存在用户名数量
     * @param username
     * @return
     */
    int checkName(String username);
    /**
     * 根据邮箱检测已存在邮箱数量
     * @param email
     * @return
     */
    int checkEmail(String email);
    /**
     * 根据用户名密码查找该用户
     * @param username
     * @param password
     * @return
     */
    User login(@Param("username") String username, @Param("password")String password);


    String selectQuestionByUsername(String username);

    int checkAnswer(@Param("username") String username, @Param("question") String question,@Param("answer") String answer);

    int updatePasswordByUsername(@Param("username") String username,@Param("md5PasswordNew")String md5PasswordNew);


    int checkPassword(@Param("md5PasswordOld") String md5PasswordOld, @Param("userId") Integer userId);

    int checkEmailByUserId(@Param("email")String email, @Param("userId") Integer userId);
}