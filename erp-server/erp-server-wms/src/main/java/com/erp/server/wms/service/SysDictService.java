package com.erp.server.wms.service;

import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictThirdCity;

import java.util.Map;

/**
 * SYS 业务处理
 *
 * @author Jim
 * @date 2023/12/5
 */
public interface SysDictService {

    /**
     * 查询并校验DictCityId
     * @param dictProvinceId
     * @param dictCityId
     * @param dictDistrictId
     * @return
     */
    Map<String, DictCityEntity> mapAndCheckDictCityIds(String dictProvinceId, String dictCityId, String dictDistrictId);

    /**
     * iml 艾姆勒 查询并校验DictCityId
     *
     * @param dictProvinceId
     * @param dictCityId
     * @param dictDistrictId
     * @param dictPlatform
     * @return
     */
    Map<String, DictThirdCity> mapAndCheckThirdCityIds(String dictProvinceId, String dictCityId, String dictDistrictId, String dictPlatform);
}
