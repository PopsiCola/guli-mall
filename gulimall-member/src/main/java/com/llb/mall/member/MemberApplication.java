package com.llb.mall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author liulebin
 * @Date 2021/4/10 22:46
 */
@SpringBootApplication
@MapperScan("com.llb.mall.member.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.llb.mall.member.feign")
public class MemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }
}
