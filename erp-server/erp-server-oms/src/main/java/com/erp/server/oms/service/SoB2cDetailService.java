package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;

import java.util.List;


/**
 * <p>
 * B2C销售订单明细表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cDetailService extends SuperService<SoB2cDetailEntity> {

    /**
     * @description: 新增
     * @author Will
     * @date: 2023/8/21 17:13
     * @param detailList
     * @param mainId
     * @return Boolean
     */
    Boolean add(List<SoB2cDetailDTO.AddDTO> detailList, String mainId);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/8/21 17:13
     * @param detailList
     * @param mainId
     * @return Boolean
     */
    Boolean update(List<SoB2cDetailDTO.UpdateDTO> detailList, String mainId);
}
