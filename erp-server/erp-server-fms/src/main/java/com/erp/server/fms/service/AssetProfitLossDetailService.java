package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetProfitLossDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetProfitLossDetailDTO;

/**
 * <p>
 * 盘盈盘亏单明细表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetProfitLossDetailService extends SuperService<AssetProfitLossDetailEntity> {

    /**
    * 新增
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetProfitLossDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    Boolean update(AssetProfitLossDetailDTO.UpdateDTO dto);


}
