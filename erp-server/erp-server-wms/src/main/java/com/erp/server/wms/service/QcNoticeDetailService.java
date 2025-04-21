package com.erp.server.wms.service;
import com.erp.model.wms.entity.QcNoticeDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.QcNoticeDetailDTO;

/**
 * <p>
 * 质检通知单明细 服务类
 * </p>
 *
 * @author jack
 * @since 2025-04-21
 */
public interface QcNoticeDetailService extends SuperService<QcNoticeDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-04-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(QcNoticeDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-04-21
    * @param dto
    * @return
    */
    Boolean update(QcNoticeDetailDTO.UpdateDTO dto);


}
