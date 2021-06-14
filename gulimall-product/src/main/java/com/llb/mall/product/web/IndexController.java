package com.llb.mall.product.web;

import com.llb.mall.product.entity.CategoryEntity;
import com.llb.mall.product.service.CategoryService;
import com.llb.mall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 首页
 * @Author liulebin
 * @Date 2021/5/19 20:22
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedissonClient redisson;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 首页
     * @param model
     * @return
     */
    @GetMapping({"/", "/index"})
    public String index(Model model) {
        // 查出所有一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Category();

        model.addAttribute("categorys", categoryEntities);
        // 视图解析器进行拼串
        // classpath:/template/ + 返回值 + .html
        return "index";
    }

    // index/json/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }

    @GetMapping("hello")
    public String hello() {
        // 1.获取一把锁，只要锁的名字一样，就是同一把锁
        // 1）锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s。不用担心业务时间长，锁自动过期被删掉。
        // 2）加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除
        RLock lock = redisson.getLock("my-lock");

        // 2.加锁
        lock.lock();

        try {
            System.out.println("加锁成功，执行业务..." + Thread.currentThread().getId());

            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 3.解锁
            lock.unlock();
        }

        return "hello";
    }

    /**
     * 写锁，保证一定能读到最新数据，修改时间，写锁是一个排它锁。读锁是一个共享锁
     * 写锁没释放就必须一直等待
     * 写锁没释放读就必须等待
     * @return
     */
    @GetMapping("/write")
    @ResponseBody
    public String writeValue() {

        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock wlock = lock.writeLock();
        String s = "";

        wlock.lock();
        System.out.println("写锁加锁成功..." + Thread.currentThread().getName());
        try {
            s = UUID.randomUUID().toString();
            Thread.sleep(10000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            wlock.unlock();
            System.out.println("写锁解锁成功..." + Thread.currentThread().getName());
        }

        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue() {

        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rlock = lock.writeLock();
        String s = "";

        rlock.lock();
        System.out.println("读锁加锁成功..." + Thread.currentThread().getName());
        try {
            s = (String) redisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rlock.unlock();
            System.out.println("读锁解锁成功..." + Thread.currentThread().getName());
        }

        return s;
    }
}
