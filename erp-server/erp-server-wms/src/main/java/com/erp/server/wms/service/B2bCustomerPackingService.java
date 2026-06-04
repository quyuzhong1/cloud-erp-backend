package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.B2bCustomerPackingDTO;
import com.erp.model.wms.entity.B2bCustomerPackingEntity;

import java.util.List;

/**
 * B2B客户装箱 服务类
 */
public interface B2bCustomerPackingService extends SuperService<B2bCustomerPackingEntity> {

    List<B2bCustomerPackingEntity> listByMainIds(List<String> mainIds);

    List<B2bCustomerPackingEntity> batchSave(String mainId, List<B2bCustomerPackingDTO.AddDTO> packingList);

    void deleteByMainIds(List<String> mainIds);
}
