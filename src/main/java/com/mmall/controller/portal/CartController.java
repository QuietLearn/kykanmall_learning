package com.mmall.controller.portal;


import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.ShardedRedisPoolUtil;
import com.mmall.vo.CartVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/cart/")
@Controller
public class CartController {
    int x[]={1,2,3};

    @Autowired
    private ICartService icartService;

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<CartVo> listCartProduct(HttpServletRequest request, Integer productId, @RequestParam(value = "count",defaultValue = "1") Integer count){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.list(user.getId());
    }


    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<CartVo> addCartProduct(HttpServletRequest request,Integer productId, @RequestParam(value = "count",defaultValue = "1") Integer count){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.addProduct(user.getId(),productId,count);
    }

    @RequestMapping("update_cart.do")
    @ResponseBody
    public ServerResponse<CartVo> updateCart(HttpServletRequest request, Integer productId, @RequestParam(value = "count",defaultValue = "1") Integer count){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.updateCart(user.getId(),productId,count);
    }

    @RequestMapping("delete.do")
    @ResponseBody
    public ServerResponse<CartVo> removeProduct(HttpServletRequest request,String productIds){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.removeProduct(user.getId(),productIds);
    }

    @RequestMapping("select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.selectProductOrNot(user.getId(),null,Const.Cart.CHECKED);
    }

    @RequestMapping("un_select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelectAll(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.selectProductOrNot(user.getId(),null,Const.Cart.NO_CHECKED);
    }

    @RequestMapping("select_product.do")
    @ResponseBody
    public ServerResponse<CartVo> selectProduct(HttpServletRequest request,Integer productId){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.selectProductOrNot(user.getId(),productId,Const.Cart.CHECKED);
    }

    @RequestMapping("un_select_product.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelectProduct(HttpServletRequest request,Integer productId){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.selectProductOrNot(user.getId(),productId,Const.Cart.NO_CHECKED);
    }

    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    public ServerResponse<Integer> getCartProductTotalCount(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = ShardedRedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return ServerResponse.createBySuccess(0);
        }

        return icartService.getCartProductTotalCount(user.getId());
    }
}
