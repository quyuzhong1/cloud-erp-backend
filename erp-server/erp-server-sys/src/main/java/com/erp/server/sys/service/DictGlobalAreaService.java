package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.model.sys.entity.DictGlobalAreaEntity;

import java.util.List;

/**
 * <p>
 * 区域表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
public interface DictGlobalAreaService extends SuperService<DictGlobalAreaEntity> {

    
    /**
     * 添加地区
     * @author yl
     * @date 2023-05-11 14:57
     * @param list
     * @return java.lang.Boolean
     */
    Boolean addOrUpdate(List<DictGlobalAreaDTO.AddOrUpdateDTO> list);

    
    /**
     * 根据国家id获取地区信息
     * @author yl
     * @date 2023-05-15 11:39
     * @param countryIds
     * @return java.util.List<com.erp.model.sys.dto.DictGlobalAreaDTO.InfoDTO>
     */
    List<DictGlobalAreaDTO.InfoDTO> listGlobalAreaByCountryIds(List<String> countryIds);
}
