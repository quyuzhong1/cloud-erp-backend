package com.erp.server.scm.service;

import cn.hutool.json.JSONArray;
import com.erp.model.scm.dto.AssetPurchaseOrderDTO;
import com.erp.model.scm.entity.AssetPurchaseOrderDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.AssetPurchaseOrderDetailDTO;
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

    Boolean endReceive(List<String> idList, String remark);

    void add(AssetPurchaseOrderDTO.AddDTO addDTO, String assetPurchaseOrderId);

    void update(AssetPurchaseOrderDTO.UpdateDTO updateDTO, String assetPurchaseOrderId);

    /**
     * 更新明细金蝶id
     * @param list
     */
    void updateKingdeeDetailId(JSONArray list);
}
