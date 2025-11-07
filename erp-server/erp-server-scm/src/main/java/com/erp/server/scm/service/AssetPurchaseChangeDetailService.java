package com.erp.server.scm.service;
import com.erp.model.scm.dto.AssetPurchaseChangeDTO;
import com.erp.model.scm.entity.AssetPurchaseChangeDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.AssetPurchaseChangeDetailDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
public interface AssetPurchaseChangeDetailService extends SuperService<AssetPurchaseChangeDetailEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetPurchaseChangeDetailDTO.AddDTO dto);

    /**
     * 修改
     * @author wtr
     * @date: 2025-10-16
     * @param
     * @return
     */
    Boolean update(AssetPurchaseChangeDetailDTO.UpdateDTO addOrUpdateDTO);

    /**
     * 修改
     * @author wtr
     * @date: 2025-10-16
     * @param
     * @return
     */
    Boolean update(List<AssetPurchaseChangeDetailDTO.UpdateDTO> assetPurchaseChangeDetailDTOList);


}
