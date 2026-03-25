package com.vibemyself.mapper.member;

import com.vibemyself.model.member.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {
    Member selectByLoginId(String loginId);
    Member selectByMbrNo(String mbrNo);
}
