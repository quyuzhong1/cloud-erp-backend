package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.QcProductDTO;
import com.erp.model.wms.entity.QcProductEntity;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
public interface QcProductService extends SuperService<QcProductEntity> {

    
    /**
     * 质检产品信息 暂存
     * @author yl
     * @date 2023-04-19 9:45
     * @param billId
     * @param qcProduct
     * @return void
     */
    void draft(String billId, QcProductDTO.AddDTO qcProduct);
}
