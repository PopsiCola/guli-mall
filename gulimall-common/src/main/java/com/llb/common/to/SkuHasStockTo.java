package com.llb.common.to;

import lombok.Data;

/**
 * 查询sku是否有库存
 * @Author liulebin
 * @Date 2021/5/15 22:33
 */
@Data
public class SkuHasStockTo {

    private Long skuId;
    private Boolean hasStock;
}
