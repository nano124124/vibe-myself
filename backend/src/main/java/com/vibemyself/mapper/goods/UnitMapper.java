package com.vibemyself.mapper.goods;

import com.vibemyself.entity.PrUnitBase;
import com.vibemyself.entity.PrUnitOpt;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UnitMapper {

    void insertUnit(PrUnitBase unit);

    void insertUnitOpt(PrUnitOpt unitOpt);
}