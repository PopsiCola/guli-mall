package com.llb.mall.ware.service.impl;

import com.llb.common.constant.WareConstant;
import com.llb.mall.ware.entity.PurchaseDetailEntity;
import com.llb.mall.ware.service.PurchaseDetailService;
import com.llb.mall.ware.vo.MergeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.llb.common.utils.PageUtils;
import com.llb.common.utils.Query;

import com.llb.mall.ware.dao.PurchaseDao;
import com.llb.mall.ware.entity.PurchaseEntity;
import com.llb.mall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询采购单
     */
    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("status", 0).or().eq("status", 1);

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 合并整单
     */
    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();

        if (purchaseId == null) {
            // 生成新的采购单进行合并
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            // 状态设置为创建
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }

        // 合并采购单
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(i);
            detailEntity.setPurchaseId(finalPurchaseId);
            // 设置为已分配
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());

            return detailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        // 更新采购单的时间
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        this.updateById(purchaseEntity);
    }

    /**
     * 领取采购单
     * @param ids 采购单id
     */
    @Override
    public void received(List<Long> ids) {
        // 1.确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(item -> {
            return item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                    || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode();
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            return item;
        }).collect(Collectors.toList());
        // 2.改变采购单状态
        this.updateBatchById(collect);

        // 3.改变采购项状态
        collect.forEach(item -> {
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> detailEntities = purchaseDetailEntities.stream().map(entity -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(entity.getId());
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return detailEntity;
            }).collect(Collectors.toList());

            // 将采购项状态设置为 采购中
            purchaseDetailService.updateBatchById(detailEntities);
        });
    }

}