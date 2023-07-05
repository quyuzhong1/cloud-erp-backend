package com.erp.server.oms.mapper;

import com.erp.model.workflow.dto.WorkOptionDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkOptionMapper {
    Integer getTableNum(WorkOptionDTO.MyWorkOptionDTO tableNumDTO);
}
