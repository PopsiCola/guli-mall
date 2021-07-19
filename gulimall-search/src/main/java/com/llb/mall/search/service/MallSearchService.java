package com.llb.mall.search.service;

import com.llb.mall.search.vo.SearchParam;
import com.llb.mall.search.vo.SearchResult;

/**
 * @Author liulebin
 * @Date 2021/7/14 21:17
 */
public interface MallSearchService {

    SearchResult search(SearchParam param);
}
