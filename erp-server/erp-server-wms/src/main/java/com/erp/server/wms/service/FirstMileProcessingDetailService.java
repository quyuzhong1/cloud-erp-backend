package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.FirstMileProcessingDetailDTO;
import com.erp.model.wms.entity.FirstMileProcessingDetailEntity;

import java.util.List;

/**
 * <p>
 * 头程虚拟仓订单跟踪明细 服务类
 * </p>
 *
 * @author will
 * @since 2025-02-25
 */
public interface FirstMileProcessingDetailService extends SuperService<FirstMileProcessingDetailEntity> {

    /**
     * 新增头程明细
     * @author will
     * @date 2025/2/25 19:14
     * @param allDetailList
     */
    void addFirstMileOrderDetail(List<FirstMileProcessingDetailDTO.AddOrUpdateDTO> allDetailList);
}
