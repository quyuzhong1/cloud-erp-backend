package com.erp.server.wms.service.impl;

import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.wms.mapper.WorkOptionMapper;
import com.erp.server.wms.service.WorkOptionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 工作台服务类
 * @Author Luo_WG
 * @Date 2023/4/21 15:52
 **/
@Service
public class WorkOptionServiceImpl implements WorkOptionService {

    @Resource
    private WorkOptionMapper workOptionMapper;

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    public Integer getTableNum(WorkOptionDTO.TableNumDTO tableNumDTO) {
        Integer tableNum = workOptionMapper.getTableNum(tableNumDTO);
        return tableNum;
    }
}
