package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface IProductService {

    ServerResponse saveProduct(Product product);

    /**
     * 更新或者新增商品逻辑
     * @param product
     * @return
     */
    ServerResponse saveOrUpdateProduct(Product product);

    /**
     * 商品状态修改（上下架）
     * @param status
     * @param productId
     * @return
     */
    ServerResponse productUpOrDown(Integer status,Integer productId);

    /**
     * 获取商品详情
     * @param productId
     * @return
     */
    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    /**
     * 获取商品分页列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize);

    /**
     * 后台根据条件搜索商品
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse<PageInfo> searchProduct(String productName,Integer productId,Integer pageNum,Integer pageSize);

    /**
     * 前台用户获取商品详细信息
     * @param productId
     * @return
     */
    ServerResponse<ProductDetailVo> listProductDetail(Integer productId);

    /**
     * 前台普通用户根据关键字 和品类搜索对应商品列表
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    ServerResponse<PageInfo> listProductByKeywordAndCategoryId(String keyword,Integer categoryId,Integer pageNum,Integer pageSize,String orderBy);
}
