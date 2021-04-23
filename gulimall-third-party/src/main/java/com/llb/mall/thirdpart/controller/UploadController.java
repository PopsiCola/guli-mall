package com.llb.mall.thirdpart.controller;

import com.llb.mall.thirdpart.util.MinioUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 上传接口
 * @Author liulebin
 * @Date 2021/4/22 22:30
 */
@RestController
public class UploadController {

    @Autowired
    private MinioUtils minioUtils;
    @Value("${minio.url}")
    private String minioUrl;
    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 上传单个文件
     * @return
     */
    @RequestMapping("/thirdParty/singleUpload")
    public String singleUpload(@RequestParam("file") MultipartFile file) {
        String uploadFile = "";
        try {
            uploadFile = minioUtils.uploadPath(false, file.getOriginalFilename());
            minioUtils.putObject(bucketName, uploadFile, file.getInputStream(), file.getContentType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return minioUrl + "/" + bucketName + "/" +uploadFile;
    }
}
