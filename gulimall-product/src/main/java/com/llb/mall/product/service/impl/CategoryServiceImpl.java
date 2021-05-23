package com.llb.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.llb.common.utils.PageUtils;
import com.llb.common.utils.Query;
import com.llb.mall.product.dao.CategoryDao;
import com.llb.mall.product.entity.CategoryEntity;
import com.llb.mall.product.service.CategoryBrandRelationService;
import com.llb.mall.product.service.CategoryService;
import com.llb.mall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2.组装成父子的树形结构

        // 2.1 找到所有一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid().equals(0L);
        }).map((menu) -> {
            // 找到子菜单
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            // 菜单排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查删除的菜单是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 找到catelogId的完整路径
     * @param catelogId
     * @return [父,子,孙]
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
         List<Long> paths = new ArrayList<>();

        CategoryEntity categoryEntity = this.getById(catelogId);
        while (categoryEntity.getParentCid() != 0) {
            paths.add(categoryEntity.getCatId());
            categoryEntity = this.getById(categoryEntity.getParentCid());
        }
        paths.add(categoryEntity.getCatId());
        Collections.reverse(paths);

        return paths.toArray(new Long[paths.size()]);
    }

    /**
     * 级联更新所有关联数据
     * @param category
     */
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    // 查询所有的一级分类
    @Override
    public List<CategoryEntity> getLevel1Category() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    /**
     * 查出分类
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        // 给缓存中存放json字符串
        String catelogJSON = redisTemplate.opsForValue().get("catelogJSON");
        if (StringUtils.isEmpty(catelogJSON)) {
            // 1.去数据库查询，并进行缓存
            Map<String, List<Catelog2Vo>> catelogJsonFromDb = getCatelogJsonFromDb();
            return catelogJsonFromDb;
        }

        // 缓存中有数据，直接转为指定对象返回
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
        return result;
    }

    /**
     * 查询数据库分类信息
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDb() {
        // 预防缓存击穿，在不存在缓存时，查询数据库进行加锁
        synchronized (this) {
            // 得到锁之后，再去缓存确定一下，如果缓存没有，则查询数据库
            String catelogJSON = redisTemplate.opsForValue().get("catelogJSON");
            if (!StringUtils.isEmpty(catelogJSON)) {
                // 缓存存在，直接返回
                Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
                return result;
            }

            /**
             * 第一种优化：将数据库的多次查询变为一次（避免循环查询数据库）
             * 添加redis缓存，将分类信息进行缓存
             */
            List<CategoryEntity> entities = baseMapper.selectList(null);

            // 1.查出所有分类
            List<CategoryEntity> level1Categorys = getParentCid(entities, 0L);
            // 2.封装数据
            Map<String, List<Catelog2Vo>> collect = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                // 1.每一个的一级分类，查询一级分类的二级分类
                List<CategoryEntity> categoryEntities = getParentCid(entities, v.getCatId());
                List<Catelog2Vo> catelog2Vos = null;
                if (categoryEntities != null) {
                    catelog2Vos = categoryEntities.stream().map(l2 -> {
                        Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                        // 找当前二级分类的三级分类
                        List<CategoryEntity> level3Catelogs = getParentCid(entities, l2.getCatId());
                        if (level3Catelogs != null) {
                            List<Catelog2Vo.Catelog3Vo> leve3Collect = level1Categorys.stream().map(l3 -> {
                                // 封装指定格式数据
                                Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                return catelog3Vo;
                            }).collect(Collectors.toList());
                            catelog2Vo.setCatelog3List(leve3Collect);
                        }
                        return catelog2Vo;
                    }).collect(Collectors.toList());
                }
                return catelog2Vos;
            }));
            // 2.查到的数据放入缓存，将对象转为json放到缓存中
            String catelog = JSON.toJSONString(collect);
            redisTemplate.opsForValue().set("catelogJSON", catelog, 1, TimeUnit.DAYS);
            return collect;
        }
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> entities, Long parentCid) {
        // return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> collect = entities.stream().filter(item -> parentCid.equals(item.getParentCid())).collect(Collectors.toList());
        return collect;
    }

    /**
     * 递归查找所有菜单的子菜单
     *
     * @param root
     * @param all
     * @return
     */
    public List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map((categoryEntity) -> {
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}