package com.erp.server.scm.service.impl;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.scm.mapper.WorkOptionMapper;
import com.erp.server.scm.service.WorkOptionService;
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

    @Override
    public List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList) {
        for (WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO : myWorkOptionDTOList) {

            myWorkOptionDTO.setModuleCode(SourceTypeEnum.getByCode(myWorkOptionDTO.getModuleCode()).getTableName());
            myWorkOptionDTO.setTableNumber(workOptionMapper.getTableNum(myWorkOptionDTO));
        }
        return myWorkOptionDTOList;
    }
}
