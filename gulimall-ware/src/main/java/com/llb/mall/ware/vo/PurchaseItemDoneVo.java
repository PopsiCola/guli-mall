package com.llb.mall.ware.vo;

import lombok.Data;

/**
 * 采购项
 * @Author liulebin
 * @Date 2021/5/9 16:50
 */
@Data
public class PurchaseItemDoneVo {

    // 采购项id
    private Long itemId;
    // 是否完成
    private Integer status;
    /// 原因
    private String reason;
}
