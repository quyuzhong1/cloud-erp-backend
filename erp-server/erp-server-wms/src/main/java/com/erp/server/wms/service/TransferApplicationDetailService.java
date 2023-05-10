package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.TransferApplicationDetailDTO;
import com.erp.model.wms.entity.TransferApplicationDetailEntity;

import java.util.List;

/**
 * <p>
 * 调拨申请单明细表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface TransferApplicationDetailService extends SuperService<TransferApplicationDetailEntity> {
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/10 19:49
     * @param details
     * @param mainId
     */
    void add(List<TransferApplicationDetailDTO.AddDTO> details, String mainId);
}
