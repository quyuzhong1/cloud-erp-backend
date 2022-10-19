package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.BusinessProcessDTO;
import com.erp.model.plm.entity.BusinessProcessEntity;

import java.util.List;


/**
 *
 */
public interface BusinessProcessService extends IService<BusinessProcessEntity> {

    List<BusinessProcessEntity> getProcessList(String businessType);

    BusinessProcessEntity getProcessByBusinessType(String businessType);

    Boolean saveProcess(BusinessProcessDTO dto);
}
