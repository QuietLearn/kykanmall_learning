package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;


    public ServerResponse<Integer> add(Integer userId, Shipping shipping){//shipping不为空，因为对象参数绑定，springmvc会先new一个对象
        if (shipping.getId()!=null){
            Shipping shippingItem = shippingMapper.selectByPrimaryKey(shipping.getId());
            if (shippingItem!=null){
                this.update(shipping,userId);
            }
            //add
        }
        shipping.setUserId(userId);
        int insertCount = shippingMapper.insert(shipping);
        if (insertCount>0){
            return ServerResponse.createBySuccess("新增地址成功",shipping.getId());
        }

        return ServerResponse.createByErrorMessage("新增地址失败");
    }

    public ServerResponse<Integer> update(Shipping shipping,Integer userId){//只传id，可能会横向越权把其他人的地址改掉

        if (shipping.getId()!=null) {
            Shipping shippingItem = shippingMapper.selectByPrimaryKey(shipping.getId());
            if (shippingItem!=null){
                shipping.setUserId(userId);
                int updateCount = shippingMapper.updateByIdAndUserId(shipping);
                if (updateCount>0){
                    return ServerResponse.createBySuccessMessage("更新地址成功");
                }
                return ServerResponse.createByErrorMessage("更新地址失败");
            }
        }

        return ServerResponse.createByErrorMessage("shipping Id 为null");
    }


    public ServerResponse<Integer> delete(Integer shippingId,Integer userId){
        if (shippingId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }

        int rowCount = shippingMapper.deleteByIdAndUserId(shippingId,userId);
        if (rowCount >0){
            return ServerResponse.createBySuccessMessage("地址删除成功");
        }
        return ServerResponse.createByErrorMessage("地址删除失败");
    }

    public ServerResponse list(Integer userId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList =  shippingMapper.selectListByUserId(userId); //mybatis 查找集合的sql，会先创建一个size=0的list，然后将查到的结果集封装成对象放进去
        if (CollectionUtils.isNotEmpty(shippingList)){
            PageInfo pageInfo = new PageInfo(shippingList);
            return ServerResponse.createBySuccess(pageInfo);
        }

        return ServerResponse.createByErrorMessage("该用户还没有添加地址");
    }

    public ServerResponse<Shipping> getDetail(Integer shippingId,Integer userId){
        if (shippingId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        Shipping shippingItem = shippingMapper.selectByIdAndUserId(shippingId,userId);
        if (shippingItem!=null){
            return ServerResponse.createBySuccess(shippingItem);
        }

        return ServerResponse.createByErrorMessage("找不到该地址");
    }
}
