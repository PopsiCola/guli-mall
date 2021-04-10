package com.llb.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.llb.common.utils.PageUtils;
import com.llb.mall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-10 22:34:00
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

