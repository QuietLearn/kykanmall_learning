package com.mmall.controller.portal;


import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Cart;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@RequestMapping("/cart/")
@Controller
public class CartController {

    @Autowired
    private ICartService icartService;

    @RequestMapping("add.do")
    public ServerResponse addProduct(HttpSession session,Integer productId, @RequestParam(value = "count",defaultValue = "1") Integer count){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.addProduct(user.getId(),productId,count);
    }

    @RequestMapping("update_cart.do")
    public ServerResponse<CartVo> updateCart(HttpSession session, Integer productId, @RequestParam(value = "count",defaultValue = "1") Integer count){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.updateCart(user.getId(),productId,count);
    }

    @RequestMapping("delete.do")
    public ServerResponse<CartVo> removeProduct(HttpSession session,String productIds){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.removeProduct(user.getId(),productIds);
    }

    @RequestMapping("select_all.do")
    public ServerResponse<CartVo> selectAll(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.selectProductOrNot(user.getId(),null,Const.Cart.CHECKED);
    }

    @RequestMapping("un_select_all.do")
    public ServerResponse<CartVo> unSelectAll(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.selectProductOrNot(user.getId(),null,Const.Cart.NO_CHECKED);
    }

    @RequestMapping("select_product.do")
    public ServerResponse<CartVo> selectProduct(HttpSession session,Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.selectProductOrNot(user.getId(),productId,Const.Cart.CHECKED);
    }

    @RequestMapping("un_select_product.do")
    public ServerResponse<CartVo> unSelectProduct(HttpSession session,Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return icartService.selectProductOrNot(user.getId(),productId,Const.Cart.NO_CHECKED);
    }
}
