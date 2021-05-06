package com.llb.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author liulebin
 * @Date 2021/5/6 21:36
 */
@Data
public class SpuBoundTo {

    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
