package com.llb.mall.search;

import com.alibaba.fastjson.JSON;
import com.llb.mall.search.config.ElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @Author liulebin
 * @Date 2021/5/13 21:06
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {

    @Autowired
    private RestHighLevelClient client;

    /**
     * 测试存储数据到es，
     * 更新也可以
     */
    @Test
    public void testIndex() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");

        User user = new User();
        user.setUserName("张三");
        user.setGender("男");
        user.setAge(23);

        String json = JSON.toJSONString(user);
        indexRequest.source(json, XContentType.JSON);

        // 执行操作
        IndexResponse index = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);

        System.out.println(index);
    }

    @Data
    class User {
        private String userName;
        private String gender;
        private Integer age;
    }

    @Test
    public void test() {
        System.out.println(client);
    }
}
