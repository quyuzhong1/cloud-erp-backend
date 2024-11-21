package com.erp.server.wms.service;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeEntity;

import java.util.List;

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

    void update(SoDeliveryNoticeChangeDTO.ViewDTO updateDTO,SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity);

    List<SoDeliveryNoticeChangeDetailEntity> listByMainIds(List<String> mainIds);

    void removeByMainId(String id);
    void checkData( List<SoDeliveryNoticeChangeDTO.ViewDetail> viewDetailList);
    /**
     * 根据主表id查询
     * @author will
     * @date 2024/10/30 9:38
     * @param id
     * @return List<SoDeliveryNoticeChangeDetailEntity>
     */
    List<SoDeliveryNoticeChangeDetailEntity> listByMainId(String id);
}
