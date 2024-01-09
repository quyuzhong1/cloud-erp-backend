package com.erp.server.wms.service;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgSettingDTO;

/**
 * <p>
 * 系统配置管理 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
public interface CfgSettingService extends SuperService<CfgSettingEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-01-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgSettingDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-01-08
    * @param dto
    * @return
    */
    Boolean update(CfgSettingDTO.UpdateDTO dto);


}
