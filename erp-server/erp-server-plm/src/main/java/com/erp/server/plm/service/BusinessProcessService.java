package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.BusinessProcessDTO;
import com.erp.model.plm.dto.BusinessProcessInfoDTO;
import com.erp.model.plm.entity.BusinessProcessEntity;

import java.util.List;


/**
 *
 */
public interface BusinessProcessService extends IService<BusinessProcessEntity> {

    List<BusinessProcessInfoDTO> getProcessList(String businessType);

    BusinessProcessEntity getProcessByBusinessKey(String businessType);

    Boolean saveProcess(BusinessProcessDTO dto);
}
