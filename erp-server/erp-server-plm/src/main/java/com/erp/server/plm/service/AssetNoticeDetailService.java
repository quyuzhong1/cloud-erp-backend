package com.erp.server.plm.service;
import com.erp.model.plm.entity.AssetNoticeDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.AssetNoticeDetailDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
public interface AssetNoticeDetailService extends SuperService<AssetNoticeDetailEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetNoticeDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    Boolean update(AssetNoticeDetailDTO.UpdateDTO dto);

    void add(List<AssetNoticeDetailDTO.AddDTO> detailList, String purchaseOrderId);



}
