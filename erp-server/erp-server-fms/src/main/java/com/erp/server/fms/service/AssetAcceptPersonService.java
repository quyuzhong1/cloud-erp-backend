package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetAcceptPersonEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetAcceptPersonDTO;

/**
 * <p>
 * 资产验收人员关联表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetAcceptPersonService extends SuperService<AssetAcceptPersonEntity> {

    /**
    * 新增
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetAcceptPersonDTO.AddDTO dto);

    /**
    * 修改
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    Boolean update(AssetAcceptPersonDTO.UpdateDTO dto);


}
