package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetDisposalDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetDisposalDetailDTO;

/**
 * <p>
 * 资产处置单资产明细表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetDisposalDetailService extends SuperService<AssetDisposalDetailEntity> {

    /**
    * 新增
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetDisposalDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    Boolean update(AssetDisposalDetailDTO.UpdateDTO dto);


}
