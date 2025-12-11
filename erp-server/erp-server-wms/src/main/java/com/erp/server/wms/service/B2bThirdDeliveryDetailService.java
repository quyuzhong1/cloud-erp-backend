package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;

import java.util.List;

/**
 * <p>
 * B2B三方发货单明细 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
 */
public interface B2bThirdDeliveryDetailService extends SuperService<B2bThirdDeliveryDetailEntity> {



    List<B2bThirdDeliveryDetailEntity> listByMainIds(List<String> ids);

    List<B2bThirdDeliveryDetailEntity> batchAdd(String id, List<B2bThirdDeliveryDetailDTO.AddDTO> detailList);

    List<B2bThirdDeliveryDetailEntity> listBySoDetailIds(List<String> soDetailIds);

    /**
     *
     * @param mainIds
     */
    void deleteByMainIds(List<String> mainIds);
}
