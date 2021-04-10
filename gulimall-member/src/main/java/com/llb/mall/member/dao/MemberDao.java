package com.llb.mall.member.dao;

import com.llb.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author liulebin
 * @email liulebinn@163.com
 * @date 2021-04-10 22:34:00
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
