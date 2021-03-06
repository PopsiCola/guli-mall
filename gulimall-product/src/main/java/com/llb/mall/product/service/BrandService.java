package com.llb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.llb.common.utils.PageUtils;
import com.llb.mall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-09 23:02:50
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);
}

