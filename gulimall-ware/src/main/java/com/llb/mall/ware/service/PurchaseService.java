package com.llb.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.llb.common.utils.PageUtils;
import com.llb.mall.ware.entity.PurchaseEntity;
import com.llb.mall.ware.vo.MergeVo;

import java.util.Map;

/**
 * 采购信息
 *
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-10 22:51:11
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);
}

