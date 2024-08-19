package com.erp.server.plm.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.entity.PlmCfgSettingEntity;
import com.erp.model.sys.dto.PlmCfgSettingDTO;

/**
 * <p>
 * 系统配置管理 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-07-25
 */
public interface CfgSettingService extends SuperService<PlmCfgSettingEntity> {


    Boolean addOrUpdate(PlmCfgSettingDTO.CommonDTO dto);

    PlmCfgSettingDTO.CommonDTO view();

}
