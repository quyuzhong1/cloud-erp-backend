package com.erp.server.wms.mapper;

import com.erp.model.workflow.dto.WorkOptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkOptionMapper {
    Integer getTableNum(@Param("params") WorkOptionDTO.MyWorkOptionDTO tableNumDTO);
}
