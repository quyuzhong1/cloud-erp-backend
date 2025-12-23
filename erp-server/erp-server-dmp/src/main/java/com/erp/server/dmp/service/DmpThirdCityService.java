package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpThirdCityEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpThirdCityDTO;

import java.util.List;

/**
 * <p>
 * 第三方城市字典表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-12-17
 */
public interface DmpThirdCityService extends SuperService<DmpThirdCityEntity> {

    List<DmpThirdCityDTO.ThirdAddressMappingDTO> getThirdByAddress(DmpThirdCityDTO.SysAddressParamsDTO dto);
}
