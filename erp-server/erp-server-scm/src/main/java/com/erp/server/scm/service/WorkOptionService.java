package com.erp.server.scm.service;

import com.erp.model.workflow.dto.WorkOptionDTO;

/**
 * 工作台服务类
 * @Author Luo_WG
 * @Date 2023/4/21 15:52
 **/
public interface WorkOptionService {

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    Integer getTableNum(WorkOptionDTO.TableNumDTO tableNumDTO);
}
