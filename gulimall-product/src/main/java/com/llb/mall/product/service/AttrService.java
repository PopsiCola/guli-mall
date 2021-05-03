package com.llb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.llb.common.utils.PageUtils;
import com.llb.mall.product.entity.AttrEntity;
import com.llb.mall.product.vo.AttrResponseVo;
import com.llb.mall.product.vo.AttrVo;

import java.util.Map;

/**
 * 商品属性
 *
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-09 23:02:50
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);
}

