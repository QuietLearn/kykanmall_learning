package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

public interface ICategoryService {
    /**
     * 添加商品新分类
     * @param cateGoryName 分类名
     * @param parentId  父节点（父分类id）
     * @return
     */
    ServerResponse addCategory(String cateGoryName, Integer parentId);

    ServerResponse updateCategoryName(String cateGoryName,Integer cateGoryId);

    ServerResponse<List<Category>> getChildCategory(Integer cateGoryId);

    /**
     * 递归查询本节点和孩子节点的id
     * @param categoryId 父节点id
     * @return
     */
   ServerResponse<List<Integer>> get_deep_category(Integer categoryId);
}
