package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FilterUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.wms.dto.PdaWarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationStatusEnum;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.WarehouseLocationMapper;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import com.google.common.collect.Lists;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 仓库仓位表 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-22
 */
@Service
public class WarehouseLocationServiceImpl extends SuperServiceImpl<WarehouseLocationMapper, WarehouseLocationEntity> implements WarehouseLocationService {

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private WarehouseService warehouseService;

    @Override
    public List<WarehouseLocationDTO.LocationListDTO> select(String warehouseId) {
        // 根据仓库查询仓位
        List<WarehouseLocationEntity> warehouseLocationList =  lambdaQuery().eq(WarehouseLocationEntity::getWarehouseId, warehouseId)
                .eq(WarehouseLocationEntity::getType, WarehouseLocationTypeEnum.LOCATION.getCode()).list();
        if(CollUtil.isEmpty(warehouseLocationList)) {
            return Lists.newArrayList();
        }
        List<WarehouseLocationDTO.LocationListDTO> dataList = Lists.newArrayListWithExpectedSize(warehouseLocationList.size());
        // 让空仓位排前面
        warehouseLocationList = warehouseLocationList.stream().sorted(Comparator.comparing(WarehouseLocationEntity::getCode)).collect(Collectors.toList());

        warehouseLocationList.stream().forEach(warehouseLocation->{
            WarehouseLocationDTO.LocationListDTO data = new WarehouseLocationDTO.LocationListDTO();
            data.setId(warehouseLocation.getId());
            data.setCode(warehouseLocation.getCode());
            data.setName(warehouseLocation.getName());
            data.setStatus(warehouseLocation.getStatus());
            WarehouseLocationStatusEnum warehouseLocationStatus = WarehouseLocationStatusEnum.getByCode(data.getStatus());
            data.setStatusName(WarehouseLocationStatusEnum.getName(data.getStatus()));
            data.setCanCheck(Boolean.TRUE);
            if(Objects.equals(warehouseLocation.getDisabled(), Boolean.TRUE) || Objects.equals(warehouseLocationStatus, WarehouseLocationStatusEnum.STOP)) {
                data.setCanCheck(Boolean.FALSE);
            }
            dataList.add(data);
        });
        return dataList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void quoteLocation(List<String> ids) {
        if (CollUtil.isNotEmpty(ids)) {
            this.lambdaUpdate().in(WarehouseLocationEntity::getId, ids).
                    set(WarehouseLocationEntity::getOccupyStatus, Boolean.TRUE).update();
        }
    }

    @Override
    public WarehouseLocationDTO.LocationDetailDTO findById(String id) {
        WarehouseLocationEntity warehouseLocation = super.getById(id);
        Optional.ofNullable(warehouseLocation).orElseThrow(()->new ServiceException("仓位信息不存在"));

        WarehouseLocationTypeEnum warehouseLocationType = WarehouseLocationTypeEnum.getByCode(warehouseLocation.getType());

        WarehouseLocationDTO.LocationDetailDTO detailDTO = new WarehouseLocationDTO.LocationDetailDTO();
        detailDTO.setType(warehouseLocation.getType());
        detailDTO.setTypeName(WarehouseLocationTypeEnum.getName(warehouseLocation.getType()));
        detailDTO.setCode(warehouseLocation.getCode());
        detailDTO.setName(warehouseLocation.getName());
        detailDTO.setWarehouseId(warehouseLocation.getWarehouseId());
        detailDTO.setDisabled(warehouseLocation.getDisabled());
        detailDTO.setRemark(warehouseLocation.getRemark());
        detailDTO.setStatus(warehouseLocation.getStatus());
        if(Objects.equals(warehouseLocationType, WarehouseLocationTypeEnum.LOCATION)) {
            detailDTO.setStatusName(WarehouseLocationStatusEnum.getName(warehouseLocation.getStatus()));

            // 查询分区信息
            WarehouseLocationEntity warehouseArea = super.getById(warehouseLocation.getParentId());
            if(Objects.nonNull(warehouseArea)) {
                detailDTO.setAreaId(warehouseArea.getId());
                detailDTO.setAreaType(warehouseArea.getCode());
                detailDTO.setAreaName(warehouseArea.getName());
            }
        }
        return detailDTO;
    }

    @Override
    public WarehouseLocationEntity findByWarehouseIdAndCode(String warehouseId, String code) {
        LambdaQueryWrapper<WarehouseLocationEntity> lambdaQuery = new LambdaQueryWrapper<WarehouseLocationEntity>().eq(WarehouseLocationEntity::getWarehouseId, warehouseId).eq(WarehouseLocationEntity::getCode, code)
                .eq(WarehouseLocationEntity::getType, WarehouseLocationTypeEnum.LOCATION.getCode()).last("limit 1");
        return baseMapper.selectOne(lambdaQuery);
    }

    @Override
    public List<WarehouseLocationEntity> listByWarehouseIdAndCode(List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> listParam) {
        if (CollectionUtils.isEmpty(listParam)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listByWarehouseIdAndCode(listParam);
    }

    @Override
    public List<WarehouseLocationDTO.LocationSelectDTO> all( ) {
        List<WarehouseLocationEntity> warehouseLocationList =  lambdaQuery()
                .eq(WarehouseLocationEntity::getType, WarehouseLocationTypeEnum.LOCATION.getCode()).list();
        if(CollUtil.isEmpty(warehouseLocationList)) {
            return Lists.newArrayList();
        }
        List<WarehouseLocationDTO.LocationSelectDTO> dataList = Lists.newArrayListWithExpectedSize(warehouseLocationList.size());
        // 让空仓位排前面
        warehouseLocationList = warehouseLocationList.stream().sorted(Comparator.comparing(WarehouseLocationEntity::getCode)).collect(Collectors.toList());
        // 根据编码+名称去重
        warehouseLocationList = warehouseLocationList.stream().collect(
                Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(
                        o -> StrUtils.null2EmptyWithTrim(o.getCode()) + "-" + StrUtils.null2EmptyWithTrim(o.getName())))), ArrayList::new));

        warehouseLocationList.stream().forEach(warehouseLocation->{
            WarehouseLocationDTO.LocationSelectDTO data = new WarehouseLocationDTO.LocationSelectDTO();
            data.setCode(warehouseLocation.getCode());
            data.setName(warehouseLocation.getName());
            dataList.add(data);
        });
        return dataList;
    }

    @Override
    public List<WarehouseLocationEntity> list(List<String> warehouseIds) {
        if(CollUtil.isEmpty(warehouseIds)) {
            return Lists.newArrayList();
        }
        List<WarehouseLocationEntity> list = this.baseMapper.list(warehouseIds);
        return CollUtil.isNotEmpty(list) ? list : Lists.newArrayList();
    }

    @Override
    public List<WarehouseLocationEntity> listByWarehouseIds(List<String> warehouseIds) {
        if(CollUtil.isEmpty(warehouseIds)) {
            return Lists.newArrayList();
        }
        List<WarehouseLocationEntity> list = lambdaQuery().in(WarehouseLocationEntity::getWarehouseId, warehouseIds).list();
        return CollUtil.isNotEmpty(list) ? list : Lists.newArrayList();
    }

    @Override
    public List<BaseDropDownDTO.CommonDTO> getWarehouseArea(String warehouseId,  WarehouseLocationTypeEnum returnType, String areaId) {
        // 根据仓库查询区域
        List<WarehouseLocationEntity> warehouseLocationList =  lambdaQuery()
                .eq(StrUtil.isNotBlank(warehouseId), WarehouseLocationEntity::getWarehouseId, warehouseId)
                .eq(ObjectUtil.isNotEmpty(returnType), WarehouseLocationEntity::getType, returnType.getCode())
                .eq(StrUtil.isNotBlank(areaId) && ObjectUtil.equals(WarehouseLocationTypeEnum.LOCATION, returnType), WarehouseLocationEntity::getParentId, areaId)
                .eq(WarehouseLocationEntity::getDisabled, Boolean.FALSE)
                .list();
        if (CollUtil.isEmpty(warehouseLocationList)) {
            return Lists.newArrayList();
        }
        warehouseLocationList=warehouseLocationList.stream().distinct().
                filter(FilterUtil.distinctByKey(WarehouseLocationEntity::getCode)).
                collect(Collectors.toList());

        return warehouseLocationList.stream().map(warehouseLocation->{
            BaseDropDownDTO.CommonDTO data = new BaseDropDownDTO.CommonDTO();
            data.setCode(warehouseLocation.getId());
            data.setValue(warehouseLocation.getCode());
            return data;
        }).collect(Collectors.toList());
    }

    @Override
    public PagingVO<WarehouseLocationDTO.PagingViewDTO> paging(PagingDTO<WarehouseLocationDTO.PagingParamDTO> dto) {
        Page<WarehouseLocationDTO.PagingViewDTO> pageDto = new Page<>(dto.getCurrPage(), dto.getPageSize());
        Page<WarehouseLocationDTO.PagingViewDTO> pagResult = baseMapper.pagingByParams(pageDto, dto.getParams());
        List<WarehouseLocationDTO.PagingViewDTO> records = pagResult.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new PagingVO<>(pagResult);
        }
        List<String> orgIdList = records.stream().map(WarehouseLocationDTO.PagingViewDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);
        if(CollUtil.isNotEmpty(records)) {
            records.stream().forEach(record->{
                String orgId = record.getOrgId();
                String orgName = orgList.stream().filter(o -> orgId.equals(o.getId())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                record.setOrgName(orgName);
            });
        }
        return new PagingVO<>(pagResult);
    }

    @Override
    public Map<String, String> locationAreaMap() {
        List<WarehouseLocationEntity> list = lambdaQuery()
                .eq(WarehouseLocationEntity::getDisabled, Boolean.FALSE)
                .list();
        Map<String, List<WarehouseLocationEntity>> locationMap = list.stream()
                .collect(Collectors.groupingBy(WarehouseLocationEntity::getType));
        List<WarehouseLocationEntity> areaList = locationMap.get(WarehouseLocationTypeEnum.AREA.getCode());
        Map<String, String> areaMap = areaList.stream()
                .collect(Collectors.toMap(WarehouseLocationEntity::getId, WarehouseLocationEntity::getCode));

        List<WarehouseLocationEntity> locationEntityList = locationMap.get(WarehouseLocationTypeEnum.LOCATION.getCode());
        Map<String, String> locationAreaMap = locationEntityList
                .stream()
                .collect(Collectors.toMap(WarehouseLocationEntity::getCode, item -> areaMap.get(item.getParentId()), (e1, e2) -> e2));
        return locationAreaMap;
    }

    @Override
    public List<PdaWarehouseLocationDTO.WarehouseAreaDTO> listWarehouseArea() {
        List<WarehouseEntity> list = warehouseService.list();
        List<WarehouseLocationEntity> warehouseAreaList = lambdaQuery().eq(WarehouseLocationEntity::getType, WarehouseLocationTypeEnum.AREA.getCode()).list();
        List<PdaWarehouseLocationDTO.WarehouseAreaDTO> warehouseAreaDTOList = new ArrayList<>();
        for (WarehouseEntity warehouseEntity : list) {
            PdaWarehouseLocationDTO.WarehouseAreaDTO warehouseAreaDTO = new PdaWarehouseLocationDTO.WarehouseAreaDTO();
            warehouseAreaDTO.setWarehouseId(warehouseEntity.getId());
            warehouseAreaDTO.setWarehouseName(warehouseEntity.getName());
            List<WarehouseLocationEntity> locationEntities = warehouseAreaList.stream().filter(req -> req.getWarehouseId().equals(warehouseEntity.getId())).collect(Collectors.toList());
            List<PdaWarehouseLocationDTO.AreaDTO> areaList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(locationEntities)) {
                for (WarehouseLocationEntity locationEntity : locationEntities) {
                    PdaWarehouseLocationDTO.AreaDTO areaDTO = new PdaWarehouseLocationDTO.AreaDTO();
                    areaDTO.setAreaId(locationEntity.getId());
                    areaDTO.setAreaName(locationEntity.getName());
                    areaList.add(areaDTO);
                }
            }
            warehouseAreaDTO.setAreaList(areaList);
            warehouseAreaDTOList.add(warehouseAreaDTO);
        }
        return warehouseAreaDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addWarehouseLocation(PdaWarehouseLocationDTO.WarehouseLocationAddDTO dto) {
        List<String> areaIdList = dto.getAreaIdList();
        List<WarehouseLocationEntity> locationEntities = this.listByIds(areaIdList);
        for (WarehouseLocationEntity locationEntity : locationEntities) {
            // 新增仓位
            WarehouseLocationEntity warehouseLocationEntity = new WarehouseLocationEntity();
            warehouseLocationEntity.setWarehouseId(locationEntity.getWarehouseId());
            warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
            warehouseLocationEntity.setCode(dto.getWarehouseLocation());
            warehouseLocationEntity.setName(dto.getWarehouseLocation());
            warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
            warehouseLocationEntity.setParentId(locationEntity.getId());
            this.save(warehouseLocationEntity);
        }
        return Boolean.TRUE;
    }
}
