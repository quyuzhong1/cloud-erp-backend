package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpPlatformAuthEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpPlatformAuthDTO;

/**
 * <p>
 * 平台token授权表 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-08-28
 */
public interface DmpPlatformAuthService extends SuperService<DmpPlatformAuthEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2025-08-28
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpPlatformAuthDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2025-08-28
    * @param dto
    * @return
    */
    Boolean update(DmpPlatformAuthDTO.UpdateDTO dto);


}
