package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.model.wms.entity.TransferInEntity;

import java.util.List;

/**
 * <p>
 * 分布式调入单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface TransferInService extends SuperService<TransferInEntity> {

    List<TransferInDTO.TabListDTO> tabList();

    
    /**
     * 下推单据保存
     * @author yl
     * @date 2023-05-26 11:33
     * @param list
     * @return java.lang.Boolean
     */
    Boolean generateTransferIn(ValidList<TransferInDTO.ViewGenerateTransferInDTO> list);
}
