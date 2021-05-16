package com.llb.mall.product.feign;

import com.llb.common.to.es.SkuEsModel;
import com.llb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * ElasticSearch远程调用
 * @Author liulebin
 * @Date 2021/5/16 21:23
 */
@FeignClient("mall-search")
public interface SearchFeignService {

    /**
     * 商品上架
     * @param skuEsModels 构建的商品上架索引信息
     * @return
     */
    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
