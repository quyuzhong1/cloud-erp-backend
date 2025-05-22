package com.erp.server.wms.service;

import com.common.business.dto.PlatformDeliveryDTO;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.AliexpressDeliveryDetailDTO;
import com.erp.model.wms.entity.AliexpressDeliveryDetailEntity;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;

import java.util.List;

/**
 * <p>
 * 速卖通发货单详情 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-05-06
 */
public interface AliexpressDeliveryDetailService extends SuperService<AliexpressDeliveryDetailEntity> {

    /**
     * 新增
     *
     * @param dto
     * @param platformCode
     * @param existDetailList
     * @return
     * @author lrp
     * @date: 2024-05-06
     */
    Boolean addOrUpdate(List<AliexpressDeliveryDetailDTO.AddDTO> dto, AliexpressDeliveryEntity platformCode, List<PlatformDeliveryDTO> existDetailList);



}
