package com.llb.mall.product.vo;

import lombok.Data;

/**
 * @Author liulebin
 * @Date 2021/4/29 23:14
 */
@Data
public class AttrResponseVo extends AttrVo{

    /**
     * 所属分类名称
     */
    private String catelogName;

    /**
     * 所属分组名称
     */
    private String groupName;

    /**
     * 分类路径
     */
    private Long[] catelogPath;
}
