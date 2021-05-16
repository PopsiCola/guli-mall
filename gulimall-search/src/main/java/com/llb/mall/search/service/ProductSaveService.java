package com.llb.mall.search.service;

import com.llb.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @Author liulebin
 * @Date 2021/5/16 21:07
 */
public interface ProductSaveService {

    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
