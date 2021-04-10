package com.llb.mall.product;

import com.llb.mall.product.entity.BrandEntity;
import com.llb.mall.product.service.BrandService;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author liulebin
 * @Date 2021/4/10 21:57
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestInsert {

    @Autowired
    private BrandService brandService;
    @Test
    public void test() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("华为");
        brandService.save(brandEntity);
    }
}
