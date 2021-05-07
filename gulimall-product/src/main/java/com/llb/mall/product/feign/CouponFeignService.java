package com.llb.mall.product.feign;

import com.llb.common.to.SkuReductionTo;
import com.llb.common.to.SpuBoundTo;
import com.llb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author liulebin
 * @Date 2021/5/6 21:31
 */
@Component
@FeignClient("mall-coupon")
public interface CouponFeignService {

    /**
     * 保存spu的积分信息
     * @param spuBoundTo
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    /**
     * sky的优惠、满减等信息
     * @param skuReductionTo
     * @return
     */
    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
