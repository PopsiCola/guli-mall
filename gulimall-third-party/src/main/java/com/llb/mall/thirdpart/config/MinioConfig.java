package com.llb.mall.thirdpart.config;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio文件服务器配置类
 * @Author liulebin
 * @Date 2020/11/4 13:46
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    // Minio文件服务器地址
    private String url;
    // 用户名
    private String accessKey;
    // 密码
    private String secretKey;

    @Bean
    public MinioClient getMinioClient() throws InvalidPortException, InvalidEndpointException {
        MinioClient minioClient = new MinioClient(url, accessKey, secretKey);
        return minioClient;
    }

}
