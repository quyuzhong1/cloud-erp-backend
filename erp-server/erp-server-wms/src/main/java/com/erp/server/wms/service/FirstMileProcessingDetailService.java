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
    Boolean addFirstMileOrderDetail(List<FirstMileProcessingDetailDTO.AddOrUpdateDTO> allDetailList);
    /**
     * 根据主表id集合查询
     * @author will
     * @date 2025/2/26 09:59
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.entity.FirstMileProcessingDetailEntity>
     */
    List<FirstMileProcessingDetailEntity> listByMainIdList(List<String> mainIdList);
}
