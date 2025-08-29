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


    /**
    * 修改
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return
    */
    Boolean update(SoReceiptDetailDTO.UpdateDTO dto);


    Boolean addDetail(SoReceiptEntity soReceiptEntity, List<SoReceiptDetailDTO.AddDTO> detailList);
}
