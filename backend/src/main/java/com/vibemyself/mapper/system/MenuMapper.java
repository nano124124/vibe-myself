package com.vibemyself.mapper.system;

import com.vibemyself.entity.StMenuBase;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MenuMapper {
    List<StMenuBase> selectAllActive();
}
