package com.mmall.controller.backend;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.ShardedRedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 命名空间都在manage下边
 */
@RequestMapping("/manage/category")
@Controller
public class CategoryManageController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;

    //不把category封装成对象是因为防止传入id造成不必要的判断（插入已有id的对象会报错（id唯一））
    @RequestMapping("/addCategory.do")
    @ResponseBody
    public ServerResponse addCategory(HttpServletRequest request, String categoryName, @RequestParam(value = "parentId",defaultValue = "0") Integer parentId){
       /* String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User existUser = JsonUtil.Json2Obj(userStr, User.class);

        if (existUser==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //检查用户是否是管理员
        if (iUserService.checkAdmin(existUser).isSuccess()){
            //操作
            //执行添加分类逻辑
            return iCategoryService.addCategory(categoryName,parentId);
        }
        return ServerResponse.createByErrorMessage("该用户没有权限");*/

        //全部通过拦截器验证是否登录以及权限
        //执行添加分类逻辑
        return iCategoryService.addCategory(categoryName,parentId);
    }


    @RequestMapping("/updateCategoryName.do")
    @ResponseBody
    public ServerResponse updateCategoryName(HttpServletRequest request,String categoryName,Integer categoryId){
        /*String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User existUser = JsonUtil.Json2Obj(userStr, User.class);

        if (existUser==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //检查用户是否是管理员
        if (iUserService.checkAdmin(existUser).isSuccess()){
            //操作
            //执行更新分类逻辑
            return iCategoryService.updateCategoryName(categoryName,categoryId);
        }
        return ServerResponse.createByErrorMessage("该用户没有权限");*/

        //全部通过拦截器验证是否登录以及权限
        //执行更新分类逻辑
        return iCategoryService.updateCategoryName(categoryName,categoryId);
    }

    @RequestMapping("/getChildCategory.do")
    @ResponseBody
    public ServerResponse getChildCategory(HttpServletRequest request,@RequestParam(value = "categoryId",defaultValue = "0")Integer categoryId){
        /*String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User existUser = JsonUtil.Json2Obj(userStr, User.class);

        if (existUser==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //检查用户是否是管理员
        if (iUserService.checkAdmin(existUser).isSuccess()){
            //查询该节点下的子节点 category信息，且不递归，保持平级
            return iCategoryService.getChildCategory(categoryId);
        }
        return ServerResponse.createByErrorMessage("该用户没有权限");*/

        //全部通过拦截器验证是否登录以及权限
        //查询该节点下的子节点 category信息，且不递归，保持平级
        return iCategoryService.getChildCategory(categoryId);
    }

    @RequestMapping("/get_deep_category.do")
    @ResponseBody
    public ServerResponse<List<Integer>> getCategoryAndDeepChildrenCategory(HttpServletRequest request,@RequestParam(value = "categoryId",defaultValue = "0")Integer categoryId){
        /*String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User existUser = JsonUtil.Json2Obj(userStr, User.class);

        if (existUser==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //检查用户是否是管理员
        if (iUserService.checkAdmin(existUser).isSuccess()){
            //查询当前节点id和递归查询所有子节点id
            return iCategoryService.get_deep_category(categoryId);
        }
        return ServerResponse.createByErrorMessage("该用户没有权限");*/

        //全部通过拦截器验证是否登录以及权限
        //查询当前节点id和递归查询所有子节点id
        return iCategoryService.get_deep_category(categoryId);
    }


}
