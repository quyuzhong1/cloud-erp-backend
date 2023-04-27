package com.erp.server.plm.service.impl;

import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.plm.mapper.WorkOptionMapper;
import com.erp.server.plm.service.WorkOptionService;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 工作台服务类
 * @Author Luo_WG
 * @Date 2023/4/21 15:33
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
    @Override
    public Integer getTableNum(String tableName) {
        return 0;
    }

    /**
     * 根据用户获取各任务阶段数量
     * @Author Luo_WG
     * @Date 2023/4/24 9:34
     * @param optionUserId optionUserId
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.StageViewDTO>
     **/
    @Override
    public List<WorkOptionDTO.StageViewDTO> stageView(String optionUserId) {
        return workOptionMapper.stageView(optionUserId);
    }
}
