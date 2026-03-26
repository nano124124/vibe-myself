package com.vibemyself.mapper.system;

import com.vibemyself.entity.StAdminBase;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {
    StAdminBase selectByLoginId(String loginId);
}
