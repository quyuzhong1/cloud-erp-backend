package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetStocktakingDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetStocktakingDetailDTO;

/**
 * <p>
 * 资产盘点明细表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetStocktakingDetailService extends SuperService<AssetStocktakingDetailEntity> {

    /**
    * 新增
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetStocktakingDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    Boolean update(AssetStocktakingDetailDTO.UpdateDTO dto);


}
