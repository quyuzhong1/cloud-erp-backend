package com.erp.server.oms.service;

import com.erp.model.workflow.dto.WorkOptionDTO;

import java.util.List;

public interface WorkOptionService {
    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList);
}
