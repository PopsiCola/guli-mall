package com.llb.mall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.llb.common.constant.ProductConstant;
import com.llb.common.to.SkuHasStockTo;
import com.llb.common.to.SkuReductionTo;
import com.llb.common.to.SpuBoundTo;
import com.llb.common.to.es.SkuEsModel;
import com.llb.common.utils.R;
import com.llb.mall.product.entity.*;
import com.llb.mall.product.feign.CouponFeignService;
import com.llb.mall.product.feign.SearchFeignService;
import com.llb.mall.product.feign.WareFeignService;
import com.llb.mall.product.service.*;
import com.llb.mall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.llb.common.utils.PageUtils;
import com.llb.common.utils.Query;

import com.llb.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductAttrValueService attrValueService;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * ??????????????????
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1.??????spu???????????? pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());

        this.saveBatchSpuInfo(spuInfoEntity);
        // 2.??????spu??????????????? pms_spu_info_desc
        List<String> decripts = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decripts));

        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
        // 3.??????spu???????????? pms_spu_images
        List<String> images = vo.getImages();

        spuImagesService.saveImages(spuInfoEntity.getId(), images);
        // 4.??????spu??????????????? pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());

            return productAttrValueEntity;
        }).collect(Collectors.toList());

        productAttrValueService.saveProductAttr(collect);
        // 5.??????spu??????????????? sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("????????????spu??????????????????");
        }

        // 6.????????????spu???????????????sku??????
        List<Skus> skus = vo.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {
            skus.forEach(sku -> {
                // ????????????
                String defaultImg = "";
                List<Images> skuImages = sku.getImages();
                for (Images skuImage : skuImages) {
                    if (skuImage.getDefaultImg() == 1) {
                        defaultImg = skuImage.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);

                skuInfoService.saveSkuInfo(skuInfoEntity);

                // 6.1 sku??????????????? pms_sku_info
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> skuImagesEntities = sku.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    // ??????true?????????????????????false????????????
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());

                skuImagesService.saveBatch(skuImagesEntities);

                // 6.3 sku????????????????????? pms_sku_sale_attr_value
                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);

                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());

                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 6.4 sku??????????????????????????????????????? sms_sku_ladder sms_sku_full_reduction
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("????????????sku??????????????????");
                    }
                }
            });
        }
    }

    /**
     * ??????spu????????????
     * @param spuInfoEntity
     */
    @Override
    public void saveBatchSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    /**
     * ??????????????????
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equals(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equals(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * ????????????
     * @param spuId
     */
    @Override
    public void up(Long spuId) {

        // 1.????????????spuId???????????????sku??????????????????
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);

        // ???????????????skuid
        List<Long> skuIdList = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        // 5.????????????sku?????????????????????????????????????????? attrs
        List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        // ???????????????????????????????????????????????????
        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        // 1.?????????????????????????????????????????????????????? hasStock
        Map<Long, Boolean> stockMap = null;
        try {
            R skusHasStock = wareFeignService.getSkusHasStock(skuIdList);
            TypeReference<List<SkuHasStockTo>> typeReference = new TypeReference<List<SkuHasStockTo>>() {
            };
            stockMap = skusHasStock.getData(typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockTo::getSkuId, item -> item.getHasStock()));
        } catch (Exception e) {
            log.error("?????????????????????????????????{}", e);
        }
        Map<Long, Boolean> finalStockMap = stockMap;

        // 2.??????es???????????????????????????
        List<SkuEsModel> skuEsModels = skuInfoEntities.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            skuEsModel.setCatelogId(sku.getCatalogId());

            // ??????????????????
            skuEsModel.setHasStock(finalStockMap.getOrDefault(sku.getSkuId(), true));

            // TODO 2.???????????? hotScore
            skuEsModel.setHotScore(0L);

            // 3.????????????????????????
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            // 4.??????????????????
            CategoryEntity category = categoryService.getById(skuEsModel.getCatelogId());
            skuEsModel.setCatelogName(category.getName());

            // 5.??????????????????
            skuEsModel.setAttrs(attrsList);

            return skuEsModel;
        }).collect(Collectors.toList());

        // 6.??????????????????es????????????
        R r = searchFeignService.productStatusUp(skuEsModels);
        if (r.getCode() == 0) {
            /**
             * ??????feign?????????
             *  1.????????????????????????????????????json
             *  RequestTemplate template = this.buildTemplateFromArgs.create(argv);
             *  2. ???????????????????????????(?????????????????????????????????)
             *  return this.executeAndDecode(template, options);
             *  3.?????????????????????????????????????????????5???
             *  Retryer retryer = this.retryer.clone();
             *  while(true) {
             *             try {
             *                 executeAndDecode(template, options);
             *             } catch() {
             *                 retryer.continueOrPropagate(e);
             *             }
             *             }
             */
            // ????????????????????????spu??????????????????
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // ??????????????????
        }
    }
}