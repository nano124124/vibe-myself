package com.vibemyself.mapper.goods;

import com.vibemyself.entity.PrCtgBase;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    List<PrCtgBase> selectAll();
    PrCtgBase selectByCtgNo(Long ctgNo);
    void insertCategory(PrCtgBase category);
    void updateCategory(PrCtgBase category);
}
