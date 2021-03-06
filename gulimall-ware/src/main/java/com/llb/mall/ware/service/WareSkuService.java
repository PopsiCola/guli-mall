package com.llb.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.llb.common.to.SkuHasStockTo;
import com.llb.common.utils.PageUtils;
import com.llb.mall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * εεεΊε­
 *
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-10 22:51:11
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockTo> getSkusHasStock(List<Long> skuIds);
}

