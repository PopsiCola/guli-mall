package com.llb.mall.search.vo;

import com.llb.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * 返回数据格式
 *
 * @Author liulebin
 * @Date 2021/7/14 21:45
 */
@Data
public class SearchResult {

    // 查询到的所有商品信息
    private List<SkuEsModel> products;

    // 当前页码
    private Integer pageNum;
    // 总记录数
    private Long total;
    // 总页码
    private Integer totalPages;

    // 当前查询到的结果，所有涉及到的品牌
    private List<BrandVo> brands;
    // 当前查询到的结果，所涉及到的所有属性
    private List<AttrVo> attrs;
    // 当前查询到的结果，所设计到的所有属性
    private List<CatalogVo> catalogs;

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

}
