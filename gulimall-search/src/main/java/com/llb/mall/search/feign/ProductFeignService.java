package com.llb.mall.search.feign;

import com.llb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author liulebin
 * @Date 2021/7/25 21:32
 */
@FeignClient("mall-product")
public interface ProductFeignService {

    @RequestMapping("product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);
}
