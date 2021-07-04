package com.llb.mall.search.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页
 *
 * @Author liulebin
 * @Date 2021/5/19 20:22
 */
@Controller
public class SearchController {

    /**
     * 首页
     *
     * @return
     */
    @GetMapping({"/", "/search"})
    public String index() {
        return "list";
    }

    @GetMapping("/list.html")
    public String listPage() {
        return "list";
    }
}
