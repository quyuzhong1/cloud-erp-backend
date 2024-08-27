package com.erp.server.wms.service.impl;

import com.common.core.exception.ServiceException;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictThirdCity;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.wms.service.SysDictService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SYS 业务处理
 *
 * @author Jim
 * @date 2023/12/5
 */
@Service
public class SysDictServiceImpl implements SysDictService {
    @Resource
    private SysDictFeign sysDictFeign;


    @Override
    public Map<String, DictCityEntity> mapAndCheckDictCityIds(String dictProvinceId, String dictCityId, String dictDistrictId) {
        List<String> ids = Arrays.asList(dictProvinceId, dictCityId, dictDistrictId);
        List<DictCityEntity> dictCityEntities = sysDictFeign.listCityByIdList(ids);
        if (CollectionUtils.isEmpty(dictCityEntities)) {
            throw new ServiceException("未找到对应地址");
        }
        Map<String, DictCityEntity> dictCountryEntityMap = dictCityEntities.stream().collect(Collectors.toMap(DictCityEntity::getId, Function.identity()));
        DictCityEntity provinceEntity = dictCountryEntityMap.get(dictProvinceId);
        DictCityEntity cityEntity = dictCountryEntityMap.get(dictCityId);
        DictCityEntity districtEntity = dictCountryEntityMap.get(dictDistrictId);
        if (null == provinceEntity) {
            throw new ServiceException("省ID信息不存在");
        }
        if (null == cityEntity) {
            throw new ServiceException("城市ID信息不存在");
        }
        if (null == districtEntity) {
            throw new ServiceException("地区ID信息不存在");
        }
        if (!districtEntity.getParentId().equalsIgnoreCase(cityEntity.getId())) {
            throw new ServiceException("地区对应城市不匹配");
        }
        if (!cityEntity.getParentId().equalsIgnoreCase(provinceEntity.getId())) {
            throw new ServiceException("城市对应省不匹配");
        }
        return dictCountryEntityMap;
    }

    @Override
    public Map<String, DictThirdCity> mapAndCheckThirdCityIds(String dictProvinceId, String dictCityId, String dictDistrictId, String dictPlatform) {
        List<String> ids = Arrays.asList(dictProvinceId, dictCityId, dictDistrictId);
        List<DictThirdCity> dictCityEntities = sysDictFeign.listThirdCityByDictIdList(ids,dictPlatform);
        if (CollectionUtils.isEmpty(dictCityEntities)) {
            throw new ServiceException("未找到对应第三方地址");
        }
        Map<String, DictThirdCity> dictCountryEntityMap = dictCityEntities.stream().collect(Collectors.toMap(DictThirdCity::getDictCityId, Function.identity()));
        DictThirdCity provinceEntity = dictCountryEntityMap.get(dictProvinceId);
        DictThirdCity cityEntity = dictCountryEntityMap.get(dictCityId);
        DictThirdCity districtEntity = dictCountryEntityMap.get(dictDistrictId);
        if (null == provinceEntity) {
            throw new ServiceException("第三方仓省ID信息不存在");
        }
        if (null == cityEntity) {
            throw new ServiceException("第三方城市ID信息不存在");
        }
        if (null == districtEntity) {
            throw new ServiceException("第三方仓地区ID信息不存在");
        }
        if (!districtEntity.getParentRegionId().equalsIgnoreCase(cityEntity.getRegionId())) {
            throw new ServiceException("第三方仓地区对应城市不匹配");
        }
        if (!cityEntity.getParentRegionId().equalsIgnoreCase(provinceEntity.getRegionId())) {
            throw new ServiceException("第三方城市对应省不匹配");
        }
        return dictCountryEntityMap;
    }
}
