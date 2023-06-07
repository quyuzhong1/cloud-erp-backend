package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.TaskPagingShowDTO;
import com.erp.model.plm.dto.TaskSearchParamDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkOptionMapper {
    Integer getTableNum(WorkOptionDTO.TableNumDTO tableNumDTO);

    Integer getProductDetailNum(WorkOptionDTO.TableNumDTO tableNumDTO, @Param("status") Integer status);

    Integer getProductChangeNum(WorkOptionDTO.TableNumDTO tableNumDTO, @Param("status") Integer status);

    Integer getProductBomInfoNum(WorkOptionDTO.TableNumDTO tableNumDTO, @Param("status") Integer status);

    List<WorkOptionDTO.StageViewDTO> stageView(@Param("optionUserId") String optionUserId);

    List<TaskPagingShowDTO> listProductTaskBySearchCategory(@Param("notStateList") List<Integer> notStateList, @Param("params") TaskSearchParamDTO params);

}
