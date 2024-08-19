package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgOutputConvertMappingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgOutputConvertMappingDTO;

/**
 * <p>
 * 推送字段映射表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-15
 */
public interface DmpCfgOutputConvertMappingService extends SuperService<DmpCfgOutputConvertMappingEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-08-15
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgOutputConvertMappingDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-08-15
    * @param dto
    * @return
    */
    Boolean update(DmpCfgOutputConvertMappingDTO.UpdateDTO dto);


}
