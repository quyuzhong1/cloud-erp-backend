package com.erp.server.plm.service;
import com.erp.model.plm.entity.AssetPurchaseOrderDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.AssetPurchaseOrderDetailDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
public interface AssetPurchaseOrderDetailService extends SuperService<AssetPurchaseOrderDetailEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetPurchaseOrderDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    Boolean update(AssetPurchaseOrderDetailDTO.UpdateDTO dto);

    Boolean endReceive(List<String> idList, String remark,Boolean b);

    void add(List<AssetPurchaseOrderDetailDTO.AddDTO> detailList, String assetPurchaseOrderId);


}
