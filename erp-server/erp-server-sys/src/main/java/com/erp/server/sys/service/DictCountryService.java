package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCountryEntity;

import java.util.List;

/**
 * <p>
 * 国家字典表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
public interface DictCountryService extends SuperService<DictCountryEntity> {

    
    /**
     * 获取国家列表
     * @author yl
     * @date 2023-05-11 16:18
     * @param
     * @return java.util.List<com.erp.model.sys.dto.DictCountryDTO.ListDTO>
     */
    List<DictCountryDTO.ListDTO> listCountry();
}
