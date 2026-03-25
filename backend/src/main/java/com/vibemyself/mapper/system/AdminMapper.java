package com.vibemyself.mapper.system;

import com.vibemyself.model.system.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {
    Admin selectByLoginId(String loginId);
}
