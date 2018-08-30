package com.mmall.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;


@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    private Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    public ServerResponse saveProduct(Product product) {
        if (product==null){
           return ServerResponse.createByErrorMessage("增加商品参数错误");
        }
        int insertCount = productMapper.insert(product);
        if (insertCount > 0 ){
            return ServerResponse.createBySuccess("商品新增成功");
        }
        return ServerResponse.createByErrorMessage("商品新增失败");
    }

    /**
     * 更新或者新增商品逻辑
     * @param product
     * @return
     */
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product!=null){
            //防止.split报空指针
            if (product.getSubImages()!=null) {
                String[] subImages = product.getSubImages().split(",");
                if (subImages.length>0){
                    product.setMainImage(subImages[0]);
                }
            }

            if (product.getId()!=null){
                int updateCount = productMapper.updateByPrimaryKey(product);//更新商品信息一般前端就全部传过来更新了（null也更新）
                if (updateCount > 0 ){
                    return ServerResponse.createBySuccess("商品信息修改成功");
                } else{
                    return ServerResponse.createByErrorMessage("商品信息修改失败");
                }
            } else {
                int insertCount = productMapper.insert(product);
                if (insertCount > 0 ){
                    return ServerResponse.createBySuccess("商品新增成功");
                }
                return ServerResponse.createByErrorMessage("商品新增失败");
            }
        }
        return ServerResponse.createByErrorMessage("新增或更新商品参数错误");
    }

    /**
     * 修改商品状态（上下架）
     * @param status
     * @param productId
     * @return
     */
    public ServerResponse<String> productUpOrDown(Integer status,Integer productId){
        if (status==null||productId==null){
//          status!=1||status!=2||status!=3||
//          return ServerResponse.createByErrorMessage("商品状态修改参数错误");
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int updateCount = productMapper.updateByPrimaryKeySelective(product);
        if (updateCount > 0){
            return ServerResponse.createBySuccessMessage("修改产品销售状态成功");
        }
        return ServerResponse.createByErrorMessage("修改产品销售状态失败");
    }

    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){
        if (productId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return ServerResponse.createByErrorMessage("该id对应产品已下架或删除");
        }

        ProductDetailVo productDetailVo = assembleProductDetailVo(product);

        return ServerResponse.createBySuccess(productDetailVo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();

        BeanUtils.copyProperties(product,productDetailVo);//对象copy工具类

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category==null){
            productDetailVo.setParentCategoryId(0);
            logger.info("product转换为detailVo，product的categoryid获取不到category，是错误值，所以默认父品类id为0");
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }


        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return  productDetailVo;
    }

    /**
     * 获取商品分页列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> getProductList(Integer pageNum,Integer pageSize){
        //startPage--start
        //填充自己的sql查询逻辑
        //pageHelper-收尾
         PageHelper.startPage(pageNum,pageSize);//pageHelper.startPage紧跟着的第一个方法会被分页,分页时，实际返回的结果list类型是Page<E>,而且需要传分页信息参数给pagehelper从而才能用aop进行具体的分页

        List<Product> productList = productMapper.selectList();//先让pagehelper用aop切入limit、offset完成并拿到分页信息
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem : productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);//是要对dao的原始数据来进行分页；

        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 将pojo 转为需要给用户展示的vo业务对象
     * @param product
     * @return
     */
    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();

        BeanUtils.copyProperties(product,productListVo);//对象copy工具类

        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));


        return  productListVo;
    }


    public ServerResponse<PageInfo> searchProduct(String productName,Integer productId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);

        if (StringUtils.isNotBlank(productName)){
            StringBuilder builder = new StringBuilder();
            productName = builder.append("%").append(productName).append("%").toString();
        }

        List<Product> productList = productMapper.selectListByProductNameOrId(productName,productId);//先让pagehelper用aop切入limit、offset完成并拿到分页信息
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem : productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);//是要对dao的原始数据来进行分页；

        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    public ServerResponse<ProductDetailVo> listProductDetail(Integer productId){
        if (productId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return ServerResponse.createByErrorMessage("数据库中查找不到该商品");
        }
        if (product.getStatus()!= Const.productStatusCode.ONSALE.getCode()){
            return ServerResponse.createByErrorMessage("该id对应产品已下架或删除");
        }

        ProductDetailVo productDetailVo = assembleProductDetailVo(product);

        return ServerResponse.createBySuccess(productDetailVo);
    }

    public ServerResponse<PageInfo> listProductByKeywordAndCategoryId(String keyword,Integer categoryId,Integer pageNum,Integer pageSize,String orderBy){
        if (StringUtils.isBlank(keyword)&&categoryId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        if (StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        List<Integer> caragoryList = Lists.newArrayList();// 变量声明在外面以引用
        if (categoryId !=null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category==null){
                return ServerResponse.createByErrorMessage("该品类不存在");
            }
            /*if(category == null && StringUtils.isBlank(keyword)){
                //没有该分类,并且还没有关键字,这个时候返回一个空的结果集,不报错
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }*/
           caragoryList = iCategoryService.get_deep_category(category.getId()).getData();
        }

        PageHelper.startPage(pageNum,pageSize);
        //如果是符合约定的排序字段，则对pagehelper插件设置排序的字段
        if (Const.PriceOrderBy.PRICE_ORDER_RULE.contains(orderBy)){
            String[] orderBys = orderBy.split("_");//对前端约定是传过来要有 _的
            PageHelper.orderBy(orderBys[0]+" "+orderBys[1]);//pagehelper插件默认的排序string
        }

        List<Product> productList = productMapper.selectListByProductNameAndCategory(StringUtils.isBlank(keyword)?null: keyword,caragoryList.size()==0?null:caragoryList);

        List<ProductListVo> productListVoList = Lists.newArrayList();
        PageInfo pageInfo = new PageInfo(productList);
        for (Product product:productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        pageInfo.setList(productListVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

}
