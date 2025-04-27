package com.erp.server.oms.service;

import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.oms.dto.SoB2cCoreDTO;

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
}
