package com.llb.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.llb.common.utils.PageUtils;
import com.llb.mall.member.entity.MemberStatisticsInfoEntity;

import java.util.Map;

/**
 * 会员统计信息
 *
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-10 22:34:01
 */
public interface MemberStatisticsInfoService extends IService<MemberStatisticsInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

