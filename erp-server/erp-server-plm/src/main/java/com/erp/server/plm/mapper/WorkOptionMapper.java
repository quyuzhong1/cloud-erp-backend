package com.erp.server.plm.mapper;

import com.erp.model.workflow.dto.WorkOptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkOptionMapper {
    Integer getTableNum(@Param("tableName") String string);

    List<WorkOptionDTO.StageViewDTO> stageView(@Param("optionUserId") String optionUserId);
}
