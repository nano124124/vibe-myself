package com.vibemyself.service.system;

import com.vibemyself.dto.system.CodeResponse;
import com.vibemyself.mapper.system.CodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeQueryService {

    private final CodeMapper codeMapper;

    @Transactional(readOnly = true)
    public List<CodeResponse> getCodes(String codeGrpCd) {
        return codeMapper.selectCodesByGrp(codeGrpCd);
    }
}
