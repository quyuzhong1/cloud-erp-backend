package com.erp.server.oms.service;

import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.oms.dto.SoB2cCoreDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.dto.SoOutstockDTO;

import java.util.List;

/**
 * sob2c额外类
 * @author will
 * @date 2025/4/24 20:07
 */
public interface SoB2cCoreService {
    /**
     * 查询重新出库数据
     * @author will
     * @date 2025/4/24 20:11
     * @param dto
     * @return List<ListRetryOutstockDTO>
     */
    List<SoB2cCoreDTO.ListRetryOutstockDTO> listRetryOutstock(BaseIdsDTO.IdsDTO dto);
    /**
     * 重新出库
     * @author will
     * @date 2025/4/24 20:28
     * @param list
     * @return Boolean
     */
    Boolean retryOutstock(List<SoB2cCoreDTO.RetryOutstockDTO> list);

    /**
     * 拆分b2c销售订单数据
     * @author will
     * @date 2025/4/28 10:22
     * @param mainEntity
     * @param generateB2cDTO
     * @return List<GenerateB2cDTO>
     */
    List<SoOutstockDTO.GenerateB2cDTO> splitB2cSoOutstock(SoB2cEntity mainEntity, SoOutstockDTO.GenerateB2cDTO generateB2cDTO);
}
