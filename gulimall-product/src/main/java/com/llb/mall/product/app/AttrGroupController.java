package com.llb.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.llb.mall.product.entity.AttrEntity;
import com.llb.mall.product.service.AttrAttrgroupRelationService;
import com.llb.mall.product.service.AttrService;
import com.llb.mall.product.service.CategoryService;
import com.llb.mall.product.vo.AttrGroupRelationVo;
import com.llb.mall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.llb.mall.product.entity.AttrGroupEntity;
import com.llb.mall.product.service.AttrGroupService;
import com.llb.common.utils.PageUtils;
import com.llb.common.utils.R;


/**
 * 属性分组
 *
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-09 23:28:10
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * 列表
     */
    @RequestMapping("/list/{categoryId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("categoryId") Long catelogId) {
        // PageUtils page = attrGroupService.queryPage(params);

        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }

    /**
     * 根据分类id查出所有的分组以及这些组里面的属性
     * @param catelogId
     * @return
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId) {
        // 1.查出当前分类下的所有属性分组
        // 2.查出每个属性分组的所有属性
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data", vos);
    }

    /**
     * 保存关联
     * @param vos
     * @return
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        relationService.saveBatch(vos);
        return R.ok();
    }

    /**
     * 根据分组id查找关联的所有基本属性
     * @param attrgroupid
     * @return
     */
    @GetMapping("/{attrgroupid}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupid") Long attrgroupid) {
        List<AttrEntity> attrEntities = attrService.getRelationAttr(attrgroupid);
        return R.ok().put("data", attrEntities);
    }

    /**
     * 获取当前分组没有关联的基本属性
     * @param attrgroupid
     * @return
     */
    @GetMapping("/{attrgroupid}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupid") Long attrgroupid,
                            @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getNoRelationAttr(params, attrgroupid);
        return R.ok().put("page", page);
    }

    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
