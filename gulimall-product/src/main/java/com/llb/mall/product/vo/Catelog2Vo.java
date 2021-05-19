package com.llb.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author liulebin
 * @Date 2021/5/19 21:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catelog2Vo {

    private String catelogId;   // 一级分类id
    private List<Catelog3Vo> catelog3List;  // 三级分类
    private String id;  // 二级分类id
    private String name; // 二级分类名称

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Catelog3Vo {
        private String catelog2Id; // 2级分类id
        private String id;
        private String name;
    }
}
