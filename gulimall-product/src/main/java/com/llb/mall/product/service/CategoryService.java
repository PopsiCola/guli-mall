package com.llb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.llb.common.utils.PageUtils;
import com.llb.mall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-09 23:02:49
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);
}

