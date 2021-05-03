package com.llb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.llb.common.utils.PageUtils;
import com.llb.mall.product.entity.AttrAttrgroupRelationEntity;
import com.llb.mall.product.vo.AttrGroupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-09 23:02:50
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBatch(List<AttrGroupRelationVo> vos);
}

