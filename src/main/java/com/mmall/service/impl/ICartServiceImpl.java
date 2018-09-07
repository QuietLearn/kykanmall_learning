package com.mmall.service.impl;

import com.github.pagehelper.StringUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service("iCartService")
public class ICartServiceImpl implements ICartService {

    private Logger logger = LoggerFactory.getLogger(ICartServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartMapper cartMapper;

    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = getCartVoLimit(userId);//列举session缓存用户对应的购物车的所有商品/价格（根据实际业务展示需要的）

        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> addProduct(Integer userId,Integer productId,Integer count){
        if (productId ==null||count==null){
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

        Cart cart = cartMapper.selectByUserIdProductId(userId,productId);
        Cart cartItem = new Cart();
        if (cart !=null){
            //这个产品已经在购物车里了.
            //如果产品已存在,数量相加
            cartItem.setId(cart.getId());
            cartItem.setQuantity(cart.getQuantity()+count);
            cartMapper.updateByPrimaryKeySelective(cartItem);
        } else {
            if (product.getStatus()==Const.productStatusCode.ONSALE.getCode()){
                //这个产品不在这个购物车里,需要新增一个这个产品的记录
                cartItem.setProductId(productId);
                cartItem.setUserId(userId);
                cartItem.setChecked(Const.Cart.CHECKED);
                cartItem.setQuantity(count);
                int insertCount = cartMapper.insert(cartItem);
                if (insertCount <= 0 ){
                    return ServerResponse.createByErrorMessage("购物车新添商品失败");
                }
            }

            return ServerResponse.createByErrorMessage("购物车新增的此商品已下架");
        }

        return this.list(userId);
    }



    public ServerResponse<CartVo> updateCart(Integer userId,Integer productId,Integer count){
        if (productId ==null||count==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product ==null){
            return ServerResponse.createByErrorMessage("数据库中没有此商品");
        }

        Cart cart = cartMapper.selectByUserIdProductId(userId,productId);
        if (cart ==null){
            return ServerResponse.createByErrorMessage("找不到对应的购物车");
        }else {
            Cart cartItem = new Cart();
            cartItem.setId(cart.getId());
            cartItem.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cartItem);
        }

       return this.list(userId);
    }


    public ServerResponse<CartVo> removeProduct(Integer userId,String productIds){
        List<String> productIdList = null;
        try{
            productIdList = Splitter.on(",").splitToList(productIds); //会检查null不检查 " "，如果null报空指针异常
        } catch (Exception e){
            logger.error("productIds为空",e);
        }
        if (CollectionUtils.isEmpty(productIdList)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }

        int rowCount = cartMapper.deleteByProductIds(userId,productIdList);
        if (rowCount <= 0){
            return ServerResponse.createByErrorMessage("购物车中移除商品失败");
        }

        return this.list(userId);
    }


    public ServerResponse<CartVo> selectProductOrNot(Integer userId,Integer productId,Integer checked){
        int updateCount = cartMapper.checkedOrUncheckedProduct(userId,productId,checked);
        if (updateCount > 0){
           return this.list(userId);
        }

        return ServerResponse.createByErrorMessage("选择勾选购物车中商品异常");
    }

    public ServerResponse<Integer> getCartProductTotalCount(Integer userId){
        if (userId ==null){
            return ServerResponse.createBySuccess(0);
        }
        int cartProductTotalCount = cartMapper.selectCartProductTotalCount(userId);

        return ServerResponse.createBySuccess(cartProductTotalCount);
    }


    private CartVo getCartVoLimit(Integer userId){
        List<Cart> cartList = cartMapper.selectByUserId(userId);

        CartVo cartVo = new CartVo();
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");
        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cart : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                BeanUtils.copyProperties(cart, cartProductVo);
                Product productItem = productMapper.selectByPrimaryKey(cart.getProductId());
                if (productItem != null) {
                    BeanUtils.copyProperties(productItem, cartProductVo);
                    cartProductVo.setId(cart.getId());

                    cartProductVo.setTotalPrice(BigDecimalUtil.mul(cartProductVo.getQuantity(), cartProductVo.getPrice().doubleValue()));

                    //判断库存
                    /*int buyLimitCount = 0;*/
                    if (cart.getQuantity() <= productItem.getStock()) {
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新有效库存
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cart.getId());
                        cartForQuantity.setQuantity(productItem.getStock());
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                        cartProductVo.setQuantity(productItem.getStock());
                    }

                }
                if (cart.getChecked() == Const.Cart.CHECKED) {
                    //如果已经勾选,增加到整个的购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        cartVo.setAll_checked(getAllCheckedStatus(userId));

        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        return cartMapper.selectUnCheckByUserId(userId) == 0;

    }

}
