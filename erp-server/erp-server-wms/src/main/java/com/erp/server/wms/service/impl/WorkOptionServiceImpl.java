package com.erp.server.wms.service.impl;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.wms.mapper.WorkOptionMapper;
import com.erp.server.wms.service.WorkOptionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
    @Override
    public List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList) {
        for (WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO : myWorkOptionDTOList) {
            if (!myWorkOptionDTO.getIsApprovalWorkflow()) {
                myWorkOptionDTO.setModuleCode(SourceTypeEnum.getByCode(myWorkOptionDTO.getModuleCode()).getTableName());
                myWorkOptionDTO.setTableNumber(workOptionMapper.getTableNum(myWorkOptionDTO));
            } else {

            }

        }
        return myWorkOptionDTOList;
    }
}
