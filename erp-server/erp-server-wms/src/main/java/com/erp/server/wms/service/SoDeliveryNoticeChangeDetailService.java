package com.erp.server.wms.service;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDetailDTO;

/**
 * <p>
 * 发货通知变更单明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
 */
public interface SoDeliveryNoticeChangeDetailService extends SuperService<SoDeliveryNoticeChangeDetailEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-10-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoDeliveryNoticeChangeDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-10-23
    * @param dto
    * @return
    */
    Boolean update(SoDeliveryNoticeChangeDetailDTO.UpdateDTO dto);


}
