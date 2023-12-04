package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.dto.OverseasWarehouseInboundAddressDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundAddressEntity;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.wms.mapper.OverseasWarehouseInboundAddressMapper;
import com.erp.server.wms.service.OverseasWarehouseInboundAddressService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-12-04
 */
@Service
public class OverseasWarehouseInboundAddressServiceImpl extends SuperServiceImpl<OverseasWarehouseInboundAddressMapper, OverseasWarehouseInboundAddressEntity> implements OverseasWarehouseInboundAddressService {

    @Resource
    private SysDictFeign sysDictFeign;

    @Override
    public List<OverseasWarehouseInboundAddressDTO.ListDTO> addressList() {
        List<OverseasWarehouseInboundAddressEntity> list = this.list();
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        // 查询关联名称
        List<String> dictIds = list.stream()
                .map(OverseasWarehouseInboundAddressEntity::getAllDictCityId)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        List<DictCityEntity> dictCityEntities = sysDictFeign.listCityByIdList(dictIds);
        Map<String, String> dictCountryEntityMap = dictCityEntities.stream().collect(Collectors.toMap(DictCityEntity::getId, DictCityEntity::getName));

        return list.stream()
                .map(e-> new OverseasWarehouseInboundAddressDTO.ListDTO(e, dictCountryEntityMap))
                .collect(Collectors.toList());
    }

    @Override
    public void add(OverseasWarehouseInboundAddressDTO.AddDTO dto) {
        List<DictCityEntity> dictCityEntities = sysDictFeign.listCityByIdList(dto.getAllDictCityId());
        if (CollectionUtils.isEmpty(dictCityEntities)){
            throw new ServiceException("未找到对应地址");
        }
        Map<String, DictCityEntity> dictCountryEntityMap = dictCityEntities.stream().collect(Collectors.toMap(DictCityEntity::getId, Function.identity()));
        DictCityEntity provinceEntity = dictCountryEntityMap.get(dto.getDictProvinceId());
        DictCityEntity cityEntity = dictCountryEntityMap.get(dto.getDictCityId());
        DictCityEntity districtEntity = dictCountryEntityMap.get(dto.getDictDistrictId());
        if (null == provinceEntity){
            throw new ServiceException("省ID信息不存在");
        }
        if (null == cityEntity){
            throw new ServiceException("城市ID信息不存在");
        }
        if (null == districtEntity){
            throw new ServiceException("地区ID信息不存在");
        }
        if (!districtEntity.getParentId().equalsIgnoreCase(cityEntity.getId())){
            throw new ServiceException("地区对应城市不匹配");
        }
        if (!cityEntity.getParentId().equalsIgnoreCase(provinceEntity.getId())){
            throw new ServiceException("城市对应省不匹配");
        }
        OverseasWarehouseInboundAddressEntity entity = new OverseasWarehouseInboundAddressEntity();
        BeanUtils.copyProperties(dto, entity);
        if (!this.save(entity)){
            throw new ServiceException("保存失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(String id) {
        OverseasWarehouseInboundAddressEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException("记录已被删除");
        }
        if (!this.removeById(entity.getId())){
            throw new ServiceException("[OverseasWarehouseInboundAddressEntity]删除失败");
        }
        return true;
    }
}
