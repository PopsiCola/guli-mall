package com.llb.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.llb.common.utils.PageUtils;
import com.llb.mall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-10 22:48:10
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

