package com.llb.mall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 完成采购
 * @Author liulebin
 * @Date 2021/5/9 16:49
 */
@Data
public class PurchaseDoneVo {

    // 采购单id
    @NotNull
    private Long id;
    // 采购项数据
    private List<PurchaseItemDoneVo> items;
}
