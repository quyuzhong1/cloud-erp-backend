package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetDisposalDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetDisposalDetailDTO;

import java.util.List;

/**
 * <p>
 * 资产处置单资产明细表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-10-29
 */
public interface AssetDisposalDetailService extends SuperService<AssetDisposalDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-10-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetDisposalDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-10-29
    * @param dto
    * @return
    */
    Boolean update(AssetDisposalDetailDTO.UpdateDTO dto);


    List<AssetDisposalDetailDTO.ViewDTO> listByMainId(String mainId);
}
