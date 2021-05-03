package com.llb.mall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.llb.mall.product.vo.AttrResponseVo;
import com.llb.mall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.llb.mall.product.entity.AttrEntity;
import com.llb.mall.product.service.AttrService;
import com.llb.common.utils.PageUtils;
import com.llb.common.utils.R;


/**
 * 商品属性
 *
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-09 23:28:10
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String attrType) {

        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, attrType);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
        // AttrEntity attr = attrService.getById(attrId);

        AttrResponseVo attrResponseVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrResponseVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr) {
        attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
