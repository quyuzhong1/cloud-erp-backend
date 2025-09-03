package com.erp.server.oms.service;
import com.erp.model.oms.entity.SoReceiptDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoReceiptDetailDTO;
import com.erp.model.oms.entity.SoReceiptEntity;

import java.util.List;

/**
 * <p>
 * 收款单明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
 */
public interface SoReceiptDetailService extends SuperService<SoReceiptDetailEntity> {

    Boolean addDetail(SoReceiptEntity soReceiptEntity, List<SoReceiptDetailDTO.AddDTO> detailList);

    List<SoReceiptDetailEntity> listByMainIds(List<String> list);

    void updateDetail(SoReceiptEntity soReceiptEntity, List<SoReceiptDetailDTO.UpdateDTO> detailList,boolean isFromSoUpdate);

    void removeByMainId(String id);
}
