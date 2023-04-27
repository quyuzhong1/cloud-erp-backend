package com.erp.server.scm.mapper;

import com.erp.model.workflow.dto.WorkOptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkOptionMapper {
    Integer getTableNum(WorkOptionDTO.TableNumDTO tableNumDTO);
}
