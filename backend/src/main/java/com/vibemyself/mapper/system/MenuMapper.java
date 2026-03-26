package com.vibemyself.mapper.system;

import com.vibemyself.model.system.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MenuMapper {
    List<Menu> selectAllActive();
}
