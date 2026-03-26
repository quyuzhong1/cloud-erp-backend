package com.erp.server.plm.mapper;

import com.erp.model.plm.dto.TaskPagingShowDTO;
import com.erp.model.plm.dto.TaskSearchParamDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkOptionMapper {
    Integer getTableNum(WorkOptionDTO.MyWorkOptionDTO tableNumDTO);

    Integer getProductDetailNum(@Param("params") WorkOptionDTO.MyWorkOptionDTO tableNumDTO, @Param("status") Integer status);

    Integer getBomChangeNum(@Param("params") WorkOptionDTO.MyWorkOptionDTO tableNumDTO, @Param("status") Integer status);

    Integer getProductBomInfoNum(@Param("params") WorkOptionDTO.MyWorkOptionDTO tableNumDTO, @Param("status") Integer status);

    List<WorkOptionDTO.StageViewDTO> stageView(@Param("optionUserId") String optionUserId);

    List<TaskPagingShowDTO> listProductTaskBySearchCategory(@Param("notStateList") List<Integer> notStateList, @Param("params") TaskSearchParamDTO params);

}
