package com.llb.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.llb.common.to.es.SkuEsModel;
import com.llb.mall.search.config.ElasticSearchConfig;
import com.llb.mall.search.constant.EsConstant;
import com.llb.mall.search.service.MallSearchService;
import com.llb.mall.search.vo.SearchParam;
import com.llb.mall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author liulebin
 * @Date 2021/7/14 21:17
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    /**
     * @param param 检索所有参数
     * @return
     */
    @Override
    public SearchResult search(SearchParam param) {

        // 1.动态构建出查询需要的DSL语句
        SearchResult result = null;

        // 1.准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);
        try {
            // 2.执行检索请求
            SearchResponse response = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

            // 3.分析相应数据，封装成我们需要的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 准备检索请求
     * 模糊匹配、过滤（按照属性、分类、品牌、价格区间、库存）、排序、分页、高亮、聚合分析
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        // 构建DSL语句
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /*
         模糊匹配、过滤（按照属性、分类、品牌、价格区间、库存）
         */
        // 1.构建bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 1.1 must模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));

            // 2.3 高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        // 1.2 bool - filter 按照三级分类id查询
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catelogId", param.getCatalog3Id()));
        }
        // 1.2 bool - filter 按照品牌id查询
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 1.2 bool - filter 按照所有指定的属性查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attrStr : param.getAttrs()) {
                // attrs=1_5寸:8寸&attrs=2_16G:8G
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                // attrs=1_5寸:8寸
                String[] s = attrStr.split("_");
                // 检索的属性id
                String attrId = s[0];
                // 这个属性检索用的值
                String[] attrValues = s[1].split("_");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                // 每一个必须都得生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery(
                        "attrs",
                        nestedBoolQuery,
                        ScoreMode.None);

                boolQuery.filter(nestedQuery);
            }
        }
        // 1.2 bool - filter 按照库存是否有进行查询
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1 ? true : false));
        }
        // 1.2 bool - filter 按照价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            // 1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");

            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                // 1_500/_500
                if (param.getSkuPrice().startsWith("_")) {
                    // 区间_500
                    rangeQuery.lte(s[1]);
                } else {
                    // 区间1_500
                    rangeQuery.gte(s[0]).lte(s[1]);
                }
            } else {
                // 区间500_
                rangeQuery.gte(s[0]);
            }

            boolQuery.filter(rangeQuery);
        }

        // 把以前的所有条件都拿来进行封装
        sourceBuilder.query(boolQuery);
        /*
         排序、分页、高亮
         */
        // 2.1 排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            // sort=hotScoure_asc/desc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }

        // 2.2 分页
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        /*
         聚合分析
         */
        // 1.品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        // 品牌聚合的子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        // TODO：聚合品牌信息
        sourceBuilder.aggregation(brandAgg);

        // 2.分类聚合
        TermsAggregationBuilder catelogAgg = AggregationBuilders.terms("catelog_agg").field("catelogId").size(20);
        catelogAgg.subAggregation(AggregationBuilders.terms("catelog_name_agg").field("catelogName").size(1));
        // TODO：聚合分类信息
        sourceBuilder.aggregation(catelogAgg);

        // 3.属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        // 聚合分析出所有的attr_id
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        // 聚合分析出当前attr_id对应的名字
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // 聚合分析出当前attr_id对应的属性值
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(20));
        attrAgg.subAggregation(attrIdAgg);
        // TODO：聚合属性信息
        sourceBuilder.aggregation(attrAgg);

        String s = sourceBuilder.toString();
        System.out.println("构建的DSL：" + s);
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }

    /**
     * 构建结果数据
     *
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult searchResult = new SearchResult();

        SearchHits hits = response.getHits();

        // 1.返回的所有查询到的商品
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);

                // 标题高亮显示
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String highlight = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(highlight);
                }
                esModels.add(esModel);
            }
        }
        searchResult.setProducts(esModels);

        // 2.当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();

        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 1.得到属性id
            long attrId = bucket.getKeyAsNumber().longValue();
            // 2.得到属性名字
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            // 3.得到属性值
            List<String> attrValue = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                return ((Terms.Bucket) item).getKeyAsString();
            }).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValue);

            attrVos.add(attrVo);
        }

        searchResult.setAttrs(attrVos);

        // 3.当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 1.得到品牌id
            long brandId = bucket.getKeyAsNumber().longValue();
            // 2.得到品牌名
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            // 3.得到品牌图片
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();

            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }

        searchResult.setBrands(brandVos);

        // 4.当前所有商品涉及到的所有分类信息
        ParsedLongTerms catelogAgg = response.getAggregations().get("catelog_agg");

        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catelogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 得到分类id
            long catalogId = bucket.getKeyAsNumber().longValue();
            catalogVo.setCatalogId(catalogId);

            // 得到分类名
            ParsedStringTerms catelogNameAgg = bucket.getAggregations().get("catelog_name_agg");
            String catelogName = catelogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catelogName);

            catalogVos.add(catalogVo);
        }
        searchResult.setCatalogs(catalogVos);

        // 5.分页信息-页码
        searchResult.setPageNum(param.getPageNum());

        // 5.分页信息-总记录数
        long total = hits.getTotalHits().value;
        searchResult.setTotal(total);

        // 6.分页信息-总页数
        int totalPages = (int) ((total + EsConstant.PRODUCT_PAGESIZE - 1) / EsConstant.PRODUCT_PAGESIZE);
        searchResult.setTotalPages(totalPages);

        return searchResult;
    }
}
