package com.llb.mall.coupon.dao;

import com.llb.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-10 22:19:26
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
