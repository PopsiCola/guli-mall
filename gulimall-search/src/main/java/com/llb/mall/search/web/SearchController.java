package com.llb.mall.search.web;

import com.llb.mall.search.service.MallSearchService;
import com.llb.mall.search.vo.SearchParam;
import com.llb.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 首页
 *
 * @Author liulebin
 * @Date 2021/5/19 20:22
 */
@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    /**
     * 首页
     *
     * @return
     */
    @GetMapping({"/", "/search"})
    public String index() {
        return "list";
    }

    /**
     * 自动将页面提交过来的所有请求查询参数封装成指定的对象
     *
     * @param param 过滤条件
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request) {

        param.set_queryString(request.getQueryString());

        // 1.根据传递来的页面的查询参数，去es中检索商品
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);

        return "list";
    }
}
