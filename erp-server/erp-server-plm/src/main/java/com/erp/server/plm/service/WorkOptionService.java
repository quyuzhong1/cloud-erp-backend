package com.erp.server.plm.service;

import com.erp.model.workflow.dto.WorkOptionDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作台服务类
 * @Author Luo_WG
 * @Date 2023/4/21 15:32
 **/
public interface WorkOptionService {
    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    Integer getTableNum(String tableName);

    /**
     * 根据用户获取各任务阶段数量
     * @Author Luo_WG
     * @Date 2023/4/24 9:34
     * @param optionUserId optionUserId
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.StageViewDTO>
     **/
    List<WorkOptionDTO.StageViewDTO> stageView(String optionUserId);
}
