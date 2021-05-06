package com.llb.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author liulebin
 * @Date 2021/5/6 21:41
 */
@Data
public class SkuReductionTo {

    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
