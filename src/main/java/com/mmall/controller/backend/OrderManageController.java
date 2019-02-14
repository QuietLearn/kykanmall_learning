package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public ServerResponse<PageInfo> adminListOrder(HttpSession session, @RequestParam(value ="pageNum",defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize",defaultValue = "10")  Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

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
    public ServerResponse<OrderVo> getOrderDetail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);

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
    public ServerResponse<OrderVo> criteriaQuery(HttpSession session,Long orderNo,
                                                 @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                                 @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

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
    public ServerResponse<OrderVo> sendGoods(HttpSession session,Long orderNo){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iOrderService.manageSendGoods(orderNo);
        }

        return  ServerResponse.createByErrorMessage("该用户没有权限");
    }
}
