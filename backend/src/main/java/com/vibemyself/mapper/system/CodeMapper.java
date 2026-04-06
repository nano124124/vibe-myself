package com.vibemyself.mapper.system;

import com.vibemyself.dto.system.CodeResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CodeMapper {

    List<CodeResponse> selectCodesByGrp(String codeGrpCd);
}
