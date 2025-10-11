package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetDisposalPhysicalDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetDisposalPhysicalDetailDTO;

/**
 * <p>
 * 资产处置单实物明细表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetDisposalPhysicalDetailService extends SuperService<AssetDisposalPhysicalDetailEntity> {

    /**
    * 新增
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetDisposalPhysicalDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    Boolean update(AssetDisposalPhysicalDetailDTO.UpdateDTO dto);


}
