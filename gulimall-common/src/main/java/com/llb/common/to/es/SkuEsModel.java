package com.llb.common.to.es;

import jdk.internal.util.xml.impl.Attrs;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * elasticsearch商城索引模型
 * @Author liulebin
 * @Date 2021/5/14 22:28
 */
@Data
public class SkuEsModel {

    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;
    private Boolean hasStock;
    private Long hotScore;
    private Long brandId;
    private Long catelogId;
    private String brandName;
    private String brandImg;
    private String catelogName;
    private List<Attrs> attrs;

    @Data
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}
