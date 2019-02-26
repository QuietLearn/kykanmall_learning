package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import com.mmall.vo.OrderVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RequestMapping("/manage/order/")
@Controller
public class OrderManageController {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private IUserService iUserService;

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> adminListOrder(HttpServletRequest request, @RequestParam(value ="pageNum",defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize",defaultValue = "10")  Integer pageSize){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iOrderService.adminListOrder(pageNum,pageSize);
        }
        return  ServerResponse.createByErrorMessage("该用户没有权限");

    }


    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> getOrderDetail(HttpServletRequest request, Long orderNo){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iOrderService.manageDetail(orderNo);
        }

        return  ServerResponse.createByErrorMessage("该用户没有权限");
    }

    @RequestMapping("criteria_query.do")
    @ResponseBody
    public ServerResponse<OrderVo> criteriaQuery(HttpServletRequest request,Long orderNo,
                                                 @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                                 @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){

        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iOrderService.manageCriteriaQuery(orderNo,pageNum,pageSize);
        }

        return  ServerResponse.createByErrorMessage("该用户没有权限");
    }

    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<OrderVo> sendGoods(HttpServletRequest request,Long orderNo){

        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.Json2Obj(userStr, User.class);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iOrderService.manageSendGoods(orderNo);
        }

        return  ServerResponse.createByErrorMessage("该用户没有权限");
    }
}
