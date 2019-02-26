package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisPoolUtil;
import com.mmall.vo.ProductDetailVo;
import com.sun.xml.internal.ws.resources.HttpserverMessages;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/manage/product/")
@Controller
public class ProductManageController {
    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    @RequestMapping("addProduct.do")
    @ResponseBody
    public ServerResponse addProduct(HttpServletRequest request, Product product){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = RedisPoolUtil.get(loginToken);
        User existUser = JsonUtil.Json2Obj(userStr, User.class);
        if (existUser==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //检查用户是否是管理员
        if (iUserService.checkAdmin(existUser).isSuccess()){
            //执行添加商品逻辑
            return iProductService.saveOrUpdateProduct(product);
        }
        return ServerResponse.createByErrorMessage("该用户没有权限");
    }

    @RequestMapping("productUpOrDown.do")
    @ResponseBody
    public ServerResponse productUpOrDown(HttpServletRequest request, Integer status,Integer productId){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = RedisPoolUtil.get(loginToken);
        User existUser = JsonUtil.Json2Obj(userStr, User.class);

        if (existUser==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //检查用户是否是管理员
        if (iUserService.checkAdmin(existUser).isSuccess()){
            //执行商品上下架逻辑
            return iProductService.productUpOrDown(status,productId);
        }
        return ServerResponse.createByErrorMessage("该用户没有权限");
    }

    @RequestMapping("get_product_detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProductDetail(HttpServletRequest request, Integer productId){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = RedisPoolUtil.get(loginToken);
        User existUser = JsonUtil.Json2Obj(userStr, User.class);
        if (existUser==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //检查用户是否是管理员
        if (iUserService.checkAdmin(existUser).isSuccess()){
            //获取商品详情逻辑
            return iProductService.getProductDetail(productId);
        }
        return ServerResponse.createByErrorMessage("该用户没有权限");
    }

    @RequestMapping("get_product_list.do")
    @ResponseBody
    public ServerResponse<PageInfo> getProductList(HttpServletRequest request, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = RedisPoolUtil.get(loginToken);
        User existUser = JsonUtil.Json2Obj(userStr, User.class);
        if (existUser==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //检查用户是否是管理员
        if (iUserService.checkAdmin(existUser).isSuccess()){
            //获取商品列表
            return iProductService.getProductList(pageNum,pageSize);
        }
        return ServerResponse.createByErrorMessage("该用户没有权限");
    }

    @RequestMapping("search_product.do")
    @ResponseBody
    public ServerResponse searchProduct(HttpServletRequest request,String productName,Integer productId, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,  @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = RedisPoolUtil.get(loginToken);
        User existUser = JsonUtil.Json2Obj(userStr, User.class);

        if (existUser==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //检查用户是否是管理员
        if (iUserService.checkAdmin(existUser).isSuccess()){
            //根据商品id或商品名搜索商品
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }
        return ServerResponse.createByErrorMessage("该用户没有权限");
    }

    @RequestMapping("upload_photo.do")
    @ResponseBody
    public ServerResponse uploadPhoto(@RequestParam(value = "uploadFile",required = false) MultipartFile file, HttpServletRequest request){
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userStr = RedisPoolUtil.get(loginToken);
        User existUser = JsonUtil.Json2Obj(userStr, User.class);

        if (existUser==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //检查用户是否是管理员
        if (iUserService.checkAdmin(existUser).isSuccess()){
            String path = request.getSession().getServletContext().getRealPath("upload");
            //返回上传图片的结果
            Map map = Maps.newHashMap();
            String targetFileName = iFileService.uploadPhoto(file,path);
            if(StringUtils.isBlank(targetFileName)){
                map.put("success",false);
                map.put("msg","上传失败");
                return ServerResponse.createByErrorMessage((String) map.get("msg"));
            }
            map.put("uri",targetFileName);
            map.put("url", PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName);
            return ServerResponse.createBySuccess(map);
//            return iFileService.uploadPhoto(file,request);
        }
        return ServerResponse.createByErrorMessage("该用户没有权限");
    }

    @RequestMapping("richtext_upload.do")
    @ResponseBody
    public Map richtextUpload(HttpSession session, @RequestParam(value = "uploadFile",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map map = Maps.newHashMap();
        String loginToken = CookieUtil.readLoginLoken(request);
        if (StringUtils.isBlank(loginToken)){
            map.put("success",false);
            map.put("msg","请登录（以管理员身份）");
            return map;
        }
        String userStr = RedisPoolUtil.get(loginToken);
        User existUser = JsonUtil.Json2Obj(userStr, User.class);

        if (existUser==null){
            map.put("success",false);
            map.put("msg","请登录（以管理员身份）");
            return map;
        }
        //检查用户是否是管理员
        if (iUserService.checkAdmin(existUser).isSuccess()){
            //返回上传图片的结果
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.uploadPhoto(file, path);
            if(StringUtils.isBlank(targetFileName)){
                map.put("success",false);
                map.put("msg","上传失败");
                return map;
            }
            map.put("success",true);
            map.put("msg","上传成功");
            map.put("file_path",PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName);

            response.addHeader("Access-Control-Allow-Headers","X-File-Name");//在服务器响应客户端的时候，带上Access-Control-Allow-Origin头信息，是解决跨域的一种方法
//            return iFileService.uploadPhoto(file,request);
            return map;
        }
        map.put("success",false);
        map.put("msg","此用户没有权限");
        return map;
    }




}
