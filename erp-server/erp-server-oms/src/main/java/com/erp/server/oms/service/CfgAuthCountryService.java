package com.erp.server.oms.service;
import com.erp.model.oms.entity.CfgAuthCountryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.CfgAuthDTO;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * <p>
 * 授权国家配置 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-04-21
 */
public interface CfgAuthCountryService extends SuperService<CfgAuthCountryEntity> {

    /**
    * 授权区域/国家
    * @author Jim
    * @date: 2025-04-21
    * @param dictPlatform 平台代号
    * @return
    */
    List<CfgAuthDTO.ViewDTO> listByDictPlatform(String dictPlatform);
}
