package com.llb.mall.product.web;

import com.llb.mall.product.entity.CategoryEntity;
import com.llb.mall.product.service.CategoryService;
import com.llb.mall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 首页
 * @Author liulebin
 * @Date 2021/5/19 20:22
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 首页
     * @param model
     * @return
     */
    @GetMapping({"/", "/index"})
    public String index(Model model) {
        // 查出所有一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Category();

        model.addAttribute("categorys", categoryEntities);
        // 视图解析器进行拼串
        // classpath:/template/ + 返回值 + .html
        return "index";
    }

    // index/json/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }
}
