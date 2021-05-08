package com.llb.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author liulebin
 * @Date 2021/5/8 22:08
 */
@Data
public class MergeVo {

    // 整单id
    private Long purchaseId;
    // 合并项集合
    private List<Long> items;
}
