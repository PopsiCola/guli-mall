package com.llb.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 检索条件参数。封装页面所有可能传递过来的查询条件
 *
 * @Author liulebin
 * @Date 2021/7/14 21:16
 */
@Data
public class SearchParam {

    // 页面传递过来的全文匹配关键字
    private String keyword;
    // 三级分类id
    private Long catalog3Id;
    /**
     * 排序条件
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hostScore_asc/desc
     */
    private String sort;
    /**
     * 显示是否有货
     * hashStock=0/1（0：无库存，1：有库存）
     */
    private Integer hasStock;
    /**
     * 价格区间查询
     * skuPrice=1_500/_500/500_
     */
    private String skuPrice;
    // 按照品牌进行查询，可以多选
    private List<Long> brandId;
    // 按照属性进行筛选
    private List<String> attrs;
    // 页码
    private Integer pageNum = 1;

}
