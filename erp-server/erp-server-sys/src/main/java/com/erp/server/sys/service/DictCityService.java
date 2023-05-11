package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.model.sys.entity.DictCityEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
public interface DictCityService extends SuperService<DictCityEntity> {

    Boolean add(DictCityDTO.AddDTO dto);

    
    /**
     * 获取省城市
     * @author yl
     * @date 2023-05-11 16:30
     * @param countryCode
     * @return java.util.List<com.erp.model.sys.dto.DictCityDTO.ListDTO>
     */
    List<DictCityDTO.ListDTO> listCity(String countryCode);
}
