package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    //logback日志
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String cateGoryName,Integer parentId){
        if (parentId == null || StringUtils.isBlank(cateGoryName)){
            return ServerResponse.createByErrorMessage("添加分类参数错误(为空)");
        }
        Category category = new Category();
        category.setName(cateGoryName);
        category.setParentId(parentId);
        category.setStatus(true);//这个分类是可用的

        int insertCount = categoryMapper.insert(category);//选择更新那么两个时间戳就是null了
        if (insertCount>0){
           return  ServerResponse.createBySuccessMessage("添加新分类成功");
        }
        return ServerResponse.createByErrorMessage("添加新分类失败");
    }

    public ServerResponse updateCategoryName(String cateGoryName,Integer cateGoryId){
        if (StringUtils.isBlank(cateGoryName)||cateGoryId == null){
            return ServerResponse.createByErrorMessage("更新分类名参数错误");
        }
        Category category = new Category();
        category.setName(cateGoryName);
        category.setId(cateGoryId);
        int updateCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (updateCount > 0){
            return ServerResponse.createBySuccessMessage("更新分类名成功");
        }
        return ServerResponse.createByErrorMessage("更新分类名失败");
    }

    public ServerResponse<List<Category>> getChildCategory(Integer categoryId){
        if (categoryId == null){
            return ServerResponse.createByErrorMessage("获取子分类参数错误");
        }
        List<Category> categories = categoryMapper.selectChildCategoryByParentId(categoryId);

        if (CollectionUtils.isEmpty(categories)){
            logger.info("此分类下没有子分类");//没有子节点不需要报错，只能说没有，打印一行日志即可
        }
        return ServerResponse.createBySuccess(categories);
    }



   /*   因为耦合度太高，递归查取的只是category对象的id，用一个set add存取就行了，不用serverResponse（服务端相应对象）
    这么复杂的对象， 最后取到set集合，再放到serverResponse返回给前端就可以了
   public ServerResponse<List<Integer>> get_deep_category(Integer categoryId){
        if (categoryId == null){
            return ServerResponse.createByErrorMessage("获取子分类参数错误");
        }
        Set<Integer> list = new HashSet<Integer>();
        ServerResponse response = ServerResponse.createBySuccess(list);
        List<Integer> childCategoryIds = categoryMapper.selectAllChildCategoryId(categoryId);//数据库中查找子节点数据

//        list.addAll(childCategoryIds);
        if (childCategoryIds!=null){
            list.addAll(childCategoryIds); //使用list添加子节点就可以了，因为response的data已经指向了list的头位置（那一块内存区域），所以最后返回response，并不会因为方法结束list销毁，堆内存的list数据也同时销毁
            for (Integer childCategoryId:childCategoryIds) {
                 ServerResponse response1 = get_deep_category(childCategoryId);//递归查看子节点
                 list.addAll((List<Integer>)response1.getData());  //同理 用list添加子节点的子节点
            }
        }
        return response;
    }*/

    /**
     * 递归查询本节点和孩子节点的id
     * @param categoryId 父节点id
     * @return
     */
    @Override
    public ServerResponse<List<Integer>> get_deep_category(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        if (categoryId!=null){
            getCategoryKidBelowSet(categorySet, categoryId);
        }

        List<Integer> categoryIdList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(categorySet)){ //其实不用判断因为[]不会再foreach取值时报空指针异常
            for (Category categoryItem:categorySet) {
                categoryIdList.add(categoryItem.getId());//因为list有序，查找比较方便，hashset只是用来排重
            }
            return ServerResponse.createBySuccess(categoryIdList);
        }
        return ServerResponse.createByErrorMessage("没有查询到该品类和下面的子品类");
    }

    //直接用set递归降低耦合，查什么放什么
    //返回值和参数一致，递归较容易，且是左遍历
    private Set<Category> getCategoryKidBelowSet(Set<Category> categorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category!=null)
            categorySet.add(category); //递归最最要的也是吧自己id先加入
        //查找子节点,递归算法一定要有一个退出的条件
        List<Category> categoryList = categoryMapper.selectChildCategoryByParentId(categoryId);
        for (Category categoryItem: categoryList) {
            getCategoryKidBelowSet(categorySet,categoryItem.getId()); //递归遍历因为永远有指针指向set的堆内存区域，所以set增加是可以一直存在的，所以可以不写返回值
        }

        return categorySet;
    }
}
