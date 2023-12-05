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
import com.erp.server.wms.service.SysDictService;
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
 * 海外入库单常用揽收地址 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-12-04
 */
@Service
public class OverseasWarehouseInboundAddressServiceImpl extends SuperServiceImpl<OverseasWarehouseInboundAddressMapper, OverseasWarehouseInboundAddressEntity> implements OverseasWarehouseInboundAddressService {

    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private SysDictService sysDictService;

    @Override
    public List<OverseasWarehouseInboundAddressDTO.ListDTO> addressList() {
        List<OverseasWarehouseInboundAddressEntity> list = this.list();
        if (CollectionUtils.isEmpty(list)) {
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
                .map(e -> new OverseasWarehouseInboundAddressDTO.ListDTO(e, dictCountryEntityMap))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(OverseasWarehouseInboundAddressDTO.AddDTO dto) {
        sysDictService.mapAndCheckDictCityIds(
                dto.getDictProvinceId(),
                dto.getDictCityId(),
                dto.getDictDistrictId());
        OverseasWarehouseInboundAddressEntity entity = new OverseasWarehouseInboundAddressEntity();
        BeanUtils.copyProperties(dto, entity);
        if (!this.save(entity)) {
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
        if (!this.removeById(entity.getId())) {
            throw new ServiceException("[OverseasWarehouseInboundAddressEntity]删除失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OverseasWarehouseInboundAddressDTO.UpdateDTO dto) {
        OverseasWarehouseInboundAddressEntity entity = this.getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95146);
        }
        sysDictService.mapAndCheckDictCityIds(
                dto.getDictProvinceId(),
                dto.getDictCityId(),
                dto.getDictDistrictId());

        BeanUtils.copyProperties(dto, entity);
        if (!this.updateById(entity)) {
            throw new ServiceException("更新失败");
        }
    }
}
