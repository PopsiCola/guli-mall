package com.llb.mall.search.controller;

import com.llb.common.constant.BizCodeEnum;
import com.llb.common.to.es.SkuEsModel;
import com.llb.common.utils.R;
import com.llb.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * ElasticSearch服务
 * @Author liulebin
 * @Date 2021/5/16 21:05
 */
@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    private ProductSaveService productSaveService;

    /**
     * 上架商品
     * @param skuEsModels
     * @return
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {

        boolean bool = true;
        try {
            bool = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("ElasticSaveController商品上架错误，{}", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }

        return !bool ? R.ok() : R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
    }
}
