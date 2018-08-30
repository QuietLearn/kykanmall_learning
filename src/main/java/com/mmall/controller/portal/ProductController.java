package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/product/")
@Controller
public class ProductController {
    @Autowired
    private IProductService iProductService;

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse listProductDetail(Integer productId){
        return iProductService.listProductDetail(productId);
    }

    //categoryId可以有默认值也可以没有，因为查询条件只有一个也默认查询所有
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> listProduct(@RequestParam(value = "keyword",required = false) String keyword,
                                                @RequestParam(value = "categoryId",defaultValue = "0")Integer categoryId,
                                                @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                                @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                                                @RequestParam(value = "orderBy",defaultValue = "")String orderBy){


        return iProductService.listProductByKeywordAndCategoryId(keyword,categoryId,pageNum,pageSize,orderBy);
    }
}
