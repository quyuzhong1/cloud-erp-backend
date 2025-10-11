package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetCardDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetCardDetailDTO;

/**
 * <p>
 * 资产卡片明细表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetCardDetailService extends SuperService<AssetCardDetailEntity> {

    /**
    * 新增
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetCardDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    Boolean update(AssetCardDetailDTO.UpdateDTO dto);


}
