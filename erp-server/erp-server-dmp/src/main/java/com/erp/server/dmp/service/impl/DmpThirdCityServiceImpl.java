package com.erp.server.dmp.service.impl;


import com.erp.model.dmp.dto.DmpThirdCityDTO;
import com.erp.model.dmp.entity.DmpThirdCityEntity;
import com.erp.server.dmp.mapper.DmpThirdCityMapper;
import com.erp.server.dmp.service.DmpThirdCityService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 第三方城市字典表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-12-17
 */
@Slf4j
@Service
public class DmpThirdCityServiceImpl extends SuperServiceImpl<DmpThirdCityMapper, DmpThirdCityEntity> implements DmpThirdCityService {
    @Override
    public List<DmpThirdCityDTO.ThirdAddressMappingDTO> getThirdByAddress(DmpThirdCityDTO.SysAddressParamsDTO dto) {
        return this.baseMapper.getThirdByAddress(dto);
    }
}
