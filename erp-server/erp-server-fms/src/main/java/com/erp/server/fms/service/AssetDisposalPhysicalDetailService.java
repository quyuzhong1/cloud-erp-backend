package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetDisposalPhysicalDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetDisposalPhysicalDetailDTO;

import java.util.List;

/**
 * <p>
 * 资产处置单实物明细表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-10-29
 */
public interface AssetDisposalPhysicalDetailService extends SuperService<AssetDisposalPhysicalDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-10-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetDisposalPhysicalDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-10-29
    * @param dto
    * @return
    */
    Boolean update(AssetDisposalPhysicalDetailDTO.UpdateDTO dto);


    List<AssetDisposalPhysicalDetailDTO.ViewDTO> listByMainId(String mainId);
}
