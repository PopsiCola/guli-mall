package com.llb.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author liulebin
 * @Date 2021/4/10 21:52
 */
@SpringBootApplication
@MapperScan("com.llb.mall.product.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.llb.mall.product.feign")
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
