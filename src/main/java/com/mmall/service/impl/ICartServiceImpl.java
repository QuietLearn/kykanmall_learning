package com.mmall.service.impl;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartProductVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ICartServiceImpl implements ICartService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartMapper cartMapper;
    public ServerResponse addProduct(Integer userId,Integer productId,Integer count){
        if (productId ==null||userId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        if (product ==null){
            return ServerResponse.createByErrorMessage("数据库中没有此商品");
        }
        User user = userMapper.selectByPrimaryKey(userId);
        if (user ==null){
            return ServerResponse.createByErrorMessage("数据库中没有此用户");
        }

        Cart cart = new Cart();
        cart.setProductId(product.getId());
        cart.setUserId(user.getId());
        cart.setChecked(1);
        cart.setQuantity(count);
        int insertCount = cartMapper.insert(cart);
        if (insertCount > 0 ){
            List<Product> CartProductVoList = cartMapper.selectByUserId(user.getId());
            return ServerResponse.createBySuccessMessage("购物车新增商品成功");
        }

        return ServerResponse.createBySuccessMessage("购物车新增商品失败");
    }

    public CartProductVo assemCartProductVo(Product product,Cart cart){
        CartProductVo cartProductVo = new CartProductVo();
        BeanUtils.copyProperties(product,cartProductVo);
        BeanUtils.copyProperties(cart,cartProductVo);
        return cartProductVo;
    }
}
