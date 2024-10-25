package com.erp.server.wms.service;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDetailDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeEntity;

/**
 * <p>
 * 发货通知变更单明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
 */
public interface SoDeliveryNoticeChangeDetailService extends SuperService<SoDeliveryNoticeChangeDetailEntity> {




    void add(SoDeliveryNoticeChangeDTO.ViewDTO addDTO, SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity);
}
