package com.vibemyself.mapper.member;

import com.vibemyself.entity.EtMbrBase;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {
    EtMbrBase selectByLoginId(String loginId);
    EtMbrBase selectByMbrNo(String mbrNo);
}
