package com.llb.mall.thirdpart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 第三方微服务
 * @Author liulebin
 * @Date 2021/4/22 21:15
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThirdPartyApplication.class, args);
    }
}
