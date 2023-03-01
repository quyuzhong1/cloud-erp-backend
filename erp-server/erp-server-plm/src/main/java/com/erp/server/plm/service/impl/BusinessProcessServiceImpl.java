package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.BusinessProcessDTO;
import com.erp.model.plm.dto.BusinessProcessInfoDTO;
import com.erp.model.plm.entity.BusinessProcessEntity;
import com.erp.model.plm.enums.BusinessProcessEnum;
import com.erp.server.plm.mapper.BusinessProcessMapper;
import com.erp.server.plm.service.BusinessProcessService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
public class BusinessProcessServiceImpl extends ServiceImpl<BusinessProcessMapper, BusinessProcessEntity>
        implements BusinessProcessService {


    /**
     * 获取流程
     *
     * @param businessType
     * @return java.util.List<com.erp.model.plm.entity.BusinessProcessEntity>
     * @author yl
     * @date 2022-10-18 10:51
     */
    @Override
    public List<BusinessProcessInfoDTO> getProcessList(String businessType) {
        LambdaQueryWrapper<BusinessProcessEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(BusinessProcessEntity::getBusinessType, businessType);
        queryWrapper.orderByAsc(BusinessProcessEntity::getId);
        List<BusinessProcessEntity> list = this.list(queryWrapper);
        List<BusinessProcessInfoDTO> resultList = new ArrayList<>();
        for (BusinessProcessEntity item : list) {
            BusinessProcessInfoDTO info = new BusinessProcessInfoDTO();
            info.setId(item.getId());
            info.setBusinessName(item.getBusinessName());
            String param = item.getParam();
            String[] params = param.split(",");
            info.setAuditorTotal(params.length);
            info.setParam(param);
            info.setBusinessKey(item.getBusinessKey());
            String businessKey = item.getBusinessKey();
            String generalTask = BusinessProcessEnum.GENERAL_TASK.getBusinessKey();
            if (generalTask.equals(businessKey)) {
                info.setIsMultiple(true);
            } else {
                info.setIsMultiple(false);
            }
            resultList.add(info);
        }
        return resultList;
    }


    /**
     * 根据businessType  获取对应的数据
     *
     * @param businessType
     * @return com.erp.model.plm.entity.BusinessProcessEntity
     * @author yl
     * @date 2022-10-18 17:10
     */
    @Override
    public BusinessProcessEntity getProcessByBusinessKey(String businessType) {
        LambdaQueryWrapper<BusinessProcessEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(BusinessProcessEntity::getBusinessKey, businessType);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }


    /**
     * 保存流程
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean saveProcess(BusinessProcessDTO dto) {
        BusinessProcessEntity processEntity = new BusinessProcessEntity();
        BeanMapper.copy(dto, processEntity);
        return this.save(processEntity);
    }
}




