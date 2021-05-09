package com.llb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.llb.common.utils.PageUtils;
import com.llb.mall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-09 23:02:50
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveProductAttr(List<ProductAttrValueEntity> collect);

    List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId);

    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities);
}

