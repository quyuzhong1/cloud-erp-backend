package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.CfgSettingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgSettingDTO;

import java.util.Map;

/**
 * <p>
 * 系统配置管理 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-20
 */
public interface CfgSettingService extends SuperService<CfgSettingEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-05-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgSettingDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-05-20
    * @param dto
    * @return
    */
    Boolean update(CfgSettingDTO.UpdateDTO dto);


    //根据环境配置返回不同的PC链接
    String getPcLinkByEnv();

    Map<String, Object> getFsActionCallback();
}
