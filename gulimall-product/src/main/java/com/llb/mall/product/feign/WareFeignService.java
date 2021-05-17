package com.llb.mall.product.feign;

import com.llb.common.to.SkuHasStockTo;
import com.llb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author liulebin
 * @Date 2021/5/15 22:40
 */
@FeignClient("mall-ware")
public interface WareFeignService {

    /**
     * 查询sku是否有库存
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasStock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds);
}
