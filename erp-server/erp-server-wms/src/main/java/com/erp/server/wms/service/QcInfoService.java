package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.entity.QcInfoEntity;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
public interface QcInfoService extends SuperService<QcInfoEntity> {


    /**
     * 质检信息 暂存
     * @author yl
     * @date 2023-04-19 10:11
     * @param billId
     * @param qcInfo
     * @return void
     */
    void draft(String billId, QcInfoDTO.AddDTO qcInfo);

    
    /**
     * 获取到质检信息
     * @author yl
     * @date 2023-04-19 12:24
     * @param id
     * @return com.erp.model.wms.dto.QcInfoDTO.ViewDTO
     */
    QcInfoDTO.ViewDTO getByMainId(String id);
}
