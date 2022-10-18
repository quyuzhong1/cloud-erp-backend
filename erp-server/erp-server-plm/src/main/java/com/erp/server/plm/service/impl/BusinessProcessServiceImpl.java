package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.BusinessProcessEntity;
import com.erp.server.plm.mapper.BusinessProcessMapper;
import com.erp.server.plm.service.BusinessProcessService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class BusinessProcessServiceImpl extends ServiceImpl<BusinessProcessMapper, BusinessProcessEntity>
    implements BusinessProcessService {

    
    /**
     * 获取流程
     * @author yl
     * @date 2022-10-18 10:51
     * @param businessType
     * @return java.util.List<com.erp.model.plm.entity.BusinessProcessEntity>
     */
    @Override
    public List<BusinessProcessEntity> getProcessList(String businessType) {
        LambdaQueryWrapper<BusinessProcessEntity> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(BusinessProcessEntity::getBusinessType,businessType);
        return this.list(queryWrapper);
    }
}




