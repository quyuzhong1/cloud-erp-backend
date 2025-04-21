package com.erp.server.wms.service;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.entity.QcNoticeDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.QcNoticeDetailDTO;

import java.util.List;

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
    void add(QcNoticeDTO.AddDTO dto,String mainId);

    /**
    * 修改
    * @author jack
    * @date: 2025-04-21
    * @param dto
    * @return
    */
    void update(QcNoticeDTO.UpdateDTO dto,String mainId);


    List<QcNoticeDetailEntity> listByMainIds(List<String> mainIds);
}
