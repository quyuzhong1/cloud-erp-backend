package com.erp.server.wms.mapper;

import com.erp.model.workflow.dto.WorkOptionDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkOptionMapper {
    Integer getTableNum(WorkOptionDTO.TableNumDTO tableNumDTO);
}
