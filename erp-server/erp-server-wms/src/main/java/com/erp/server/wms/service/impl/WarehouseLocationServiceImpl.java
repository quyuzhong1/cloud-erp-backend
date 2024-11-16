package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.PdaWarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.excel.WarehouseLocationExcelDto;
import com.erp.model.wms.dto.pickingstrategy.WarehouseAreaDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationStatusEnum;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.model.wms.vo.WarehouseLocationExportVo;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.listener.WarehouseLocationExcelListener;
import com.erp.server.wms.mapper.InventoryMapper;
import com.erp.server.wms.mapper.WarehouseLocationMapper;
import com.erp.server.wms.mapper.WarehouseMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_WAREHOUSE_LOCATION;

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
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private InventoryMapper inventoryMapper;
    @Resource
    private WarehouseLocationMapper warehouseLocationMapper;
    @Resource
    private WarehouseMapper warehouseMapper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public List<WarehouseLocationDTO.LocationListDTO> select(String warehouseId) {
        // 根据仓库查询仓位
        Page query = new Page(0, 10000);
        WarehouseLocationDTO.SelectDTO params = new WarehouseLocationDTO.SelectDTO();
        params.setWarehouseId(warehouseId);
        IPage<WarehouseLocationDTO.LocationListDTO> pagResult = baseMapper.pagingSelect(query, params);
        List<WarehouseLocationDTO.LocationListDTO> warehouseLocationList = pagResult.getRecords();
//        List<WarehouseLocationEntity> warehouseLocationList =  lambdaQuery().eq(WarehouseLocationEntity::getWarehouseId, warehouseId)
//                .eq(WarehouseLocationEntity::getType, WarehouseLocationTypeEnum.LOCATION.getCode())
//                .orderByAsc(WarehouseLocationEntity::getCode).list();
        if(CollUtil.isEmpty(warehouseLocationList)) {
            return Lists.newArrayList();
        }
        List<WarehouseLocationDTO.LocationListDTO> dataList = Lists.newArrayListWithExpectedSize(warehouseLocationList.size());
        // 让空仓位排前面
//        warehouseLocationList = warehouseLocationList.stream().sorted(Comparator.comparing(WarehouseLocationEntity::getCode)).collect(Collectors.toList());

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

    @Override
    public List<WarehouseLocationDTO.WarehouseLocationListDTO> selectByWarehouseIds(List<String> warehouseIds) {
        if (CollUtil.isEmpty(warehouseIds)) {
            return Lists.newArrayList();
        }
        List<WarehouseLocationEntity> list = lambdaQuery().in(WarehouseLocationEntity::getWarehouseId, warehouseIds).list();
        if (CollUtil.isEmpty(list)) {
            return Lists.newArrayList();
        }
        Map<String, List<WarehouseLocationEntity>> warehouseLocationMap = list.stream().collect(Collectors.groupingBy(WarehouseLocationEntity::getWarehouseId));
        List<WarehouseLocationDTO.WarehouseLocationListDTO> resultList = Lists.newArrayListWithExpectedSize(warehouseLocationMap.size());
        List<String> warehouseList = warehouseIds.stream().distinct().collect(Collectors.toList());
        //填充仓库及仓位
        for (String warehouseId : warehouseList) {
            WarehouseLocationDTO.WarehouseLocationListDTO warehouseLocationListDTO = new WarehouseLocationDTO.WarehouseLocationListDTO();
            warehouseLocationListDTO.setWarehouseId(warehouseId);
            List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationMap.get(warehouseId);
            List<WarehouseLocationDTO.LocationListDTO> dataList;

            if (!CollectionUtils.isEmpty(warehouseLocationList)) {
                dataList = Lists.newArrayListWithExpectedSize(warehouseLocationList.size());
                // 让空仓位排前面
                warehouseLocationList = warehouseLocationList.stream().sorted(Comparator.comparing(WarehouseLocationEntity::getCode)).collect(Collectors.toList());
                warehouseLocationList.stream().forEach(warehouseLocation -> {
                    WarehouseLocationDTO.LocationListDTO data = new WarehouseLocationDTO.LocationListDTO();
                    data.setId(warehouseLocation.getId());
                    data.setCode(warehouseLocation.getCode());
                    data.setName(warehouseLocation.getName());
                    data.setStatus(warehouseLocation.getStatus());
                    WarehouseLocationStatusEnum warehouseLocationStatus = WarehouseLocationStatusEnum.getByCode(data.getStatus());
                    data.setStatusName(WarehouseLocationStatusEnum.getName(data.getStatus()));
                    data.setCanCheck(Boolean.TRUE);
                    if (Objects.equals(warehouseLocation.getDisabled(), Boolean.TRUE) || Objects.equals(warehouseLocationStatus, WarehouseLocationStatusEnum.STOP)) {
                        data.setCanCheck(Boolean.FALSE);
                    }
                    dataList.add(data);
                });
            } else {
                dataList = new ArrayList<>();
            }
            warehouseLocationListDTO.setLocationList(dataList);
            resultList.add(warehouseLocationListDTO);
        }
        return resultList;
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
    public WarehouseLocationEntity findLocationById(String id) {
        LambdaQueryWrapper<WarehouseLocationEntity> lambdaQuery = new LambdaQueryWrapper<WarehouseLocationEntity>()
                .eq(WarehouseLocationEntity::getId, id)
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
    @Cacheable(cacheNames = "cache:wms:listByWarehouseIds",keyGenerator = "myKeyGenerator")
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
            if (!CollectionUtils.isEmpty(locationEntities)) {
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
    @CacheEvict(cacheNames = "cache:wms:listByWarehouseIds", allEntries = true)
    public Boolean addWarehouseLocation(PdaWarehouseLocationDTO.WarehouseLocationAddDTO dto) {
        List<String> areaIdList = dto.getAreaIdList();
        List<WarehouseLocationEntity> areaEntities = this.listByIds(areaIdList);
        List<WarehouseLocationEntity> warehouseLocationList = lambdaQuery().in(WarehouseLocationEntity::getParentId, areaIdList).list();
        for (WarehouseLocationEntity area : areaEntities) {
            // 新增仓位
            WarehouseLocationEntity warehouseLocationEntity = new WarehouseLocationEntity();
            warehouseLocationEntity.setWarehouseId(area.getWarehouseId());
            warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
            warehouseLocationEntity.setCode(dto.getWarehouseLocation());
            warehouseLocationEntity.setName(dto.getWarehouseLocation());
            warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
            warehouseLocationEntity.setParentId(area.getId());
            List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationList.stream().filter(req -> req.getParentId().equals(area.getId()) && req.getCode().equals(dto.getWarehouseLocation())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(warehouseLocationEntities)) {
                this.save(warehouseLocationEntity);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public WarehouseLocationEntity findArea(String warehouseId, String warehouseAreaCode) {
        return lambdaQuery().eq(WarehouseLocationEntity::getWarehouseId, warehouseId)
                .eq(WarehouseLocationEntity::getCode, warehouseAreaCode)
                .eq(WarehouseLocationEntity::getType, WarehouseLocationTypeEnum.LOCATION.getCode())
                .last("limit 1")
                .one();
    }

    @Override
    public PagingVO<WarehouseLocationDTO.LocationListDTO> pagingSelect(PagingDTO<WarehouseLocationDTO.SelectDTO> searchDTO) {
        Page query = new Page(searchDTO.getCurrPage(), searchDTO.getPageSize());
        
        WarehouseLocationDTO.SelectDTO params = JSON.parseObject(JSON.toJSONString(searchDTO.getParams()), WarehouseLocationDTO.SelectDTO.class);
        IPage<WarehouseLocationDTO.LocationListDTO> pagResult;
        if (CharSequenceUtil.isNotBlank(params.getSkuNo())){
            pagResult = baseMapper.pagingSelectBySku(query, params);
        }else {
            pagResult = baseMapper.pagingSelect(query, params);
        }
        List<WarehouseLocationDTO.LocationListDTO> records = pagResult.getRecords();
        handleSelect(records);
        return new PagingVO<>(pagResult);
    }

    @Override
    public PagingVO<WarehouseAreaDTO.PagingView> areaPaging(PagingDTO<WarehouseAreaDTO.PagingParam> dto) {
        IPage<WarehouseAreaDTO.PagingView> paging = baseMapper.areaPaging(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(paging);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "cache:wms:listByWarehouseIds", allEntries = true)
    public void addArea(WarehouseAreaDTO.Add dto) {
        existCode(dto.getCode(), null, WarehouseLocationTypeEnum.AREA.getCode(), dto.getWarehouseId());
        existName(dto.getName(), null, WarehouseLocationTypeEnum.AREA.getCode(), dto.getWarehouseId());
        WarehouseLocationEntity entity = dto.getWarehouseAreaInfo();
        save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "cache:wms:listByWarehouseIds", allEntries = true)
    public void updateArea(WarehouseAreaDTO.Update dto) {
        existCode(dto.getCode(), dto.getId(), WarehouseLocationTypeEnum.AREA.getCode(), dto.getWarehouseId());
        existName(dto.getName(), dto.getId(), WarehouseLocationTypeEnum.AREA.getCode(), dto.getWarehouseId());
        WarehouseLocationEntity entity;
        entity = dto.getWarehouseAreaInfo();
        entity.setId(dto.getId());
        updateById(entity);
    }

    @Override
    public WarehouseAreaDTO.View viewArea(String id) {
        WarehouseLocationEntity entity = getById(id);
        return BeanMapperUtils.map(WarehouseAreaDTO.View.class, entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "cache:wms:listByWarehouseIds", allEntries = true)
    public void deleteArea(List<String> ids) {
        List<WarehouseLocationEntity> occupyStatusAreas = list(Wrappers.<WarehouseLocationEntity>lambdaQuery()
                .eq(WarehouseLocationEntity::getOccupyStatus, true)
                .in(WarehouseLocationEntity::getId, ids));
        if (!CollectionUtils.isEmpty(occupyStatusAreas)) {
            String codes = occupyStatusAreas.stream().map(WarehouseLocationEntity::getCode).collect(Collectors.joining(","));
            throw new ServiceException(ApiError.POSITION_BINDING_EXIST, codes);
        }
        this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "cache:wms:listByWarehouseIds", allEntries = true)
    public void updateStatusArea(UpdateStateDTO.BatchUpdateDTO dto) {
        if (Boolean.TRUE.equals(dto.getDisabled())) {
            List<WarehouseLocationEntity> occupyStatusAreas = list(Wrappers.<WarehouseLocationEntity>lambdaQuery()
                    .eq(WarehouseLocationEntity::getOccupyStatus, true)
                    .in(WarehouseLocationEntity::getId, dto.getIds())
            );
            if (!CollectionUtils.isEmpty(occupyStatusAreas)) {
                String occupyStatusArea = occupyStatusAreas.stream().map(WarehouseLocationEntity::getCode).collect(Collectors.joining(","));
                throw new ServiceException(ApiError.POSITION_BINDING_EXIST, occupyStatusArea);
            }
        }
        update(Wrappers.<WarehouseLocationEntity>lambdaUpdate()
                .set(WarehouseLocationEntity::getDisabled, dto.getDisabled())
                .in(WarehouseLocationEntity::getId, dto.getIds()));
    }

    @Override
    public WarehouseLocationEntity findByWarehouseCode(String warehouseLocation) {
        if (StringUtils.isEmpty(warehouseLocation)){
            return null;
        }
        List<WarehouseLocationEntity> list = lambdaQuery().eq(WarehouseLocationEntity::getCode, warehouseLocation).list();
        if (CollectionUtils.isNotEmpty(list)){
            return list.get(0);
        }else {
            return null;
        }
    }

    @Override
    public List<WarehouseLocationEntity> listByWarehouseIdsAndCodeList(List<String> warehouseIds, List<String> warehouseLocationList) {
        if (CollectionUtils.isEmpty(warehouseIds) && CollectionUtils.isEmpty(warehouseLocationList)){
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(CollectionUtils.isNotEmpty(warehouseIds), WarehouseLocationEntity::getWarehouseId,warehouseIds)
                .in(CollectionUtils.isNotEmpty(warehouseLocationList), WarehouseLocationEntity::getCode, warehouseLocationList)
                .list();
    }

    @Override
    public PagingVO<WarehouseLocationExportVo> exportWarehouseLocation(PagingDTO<WarehouseLocationDTO.exportParamDto> dto) {
        Page<WarehouseLocationExportVo> page = baseMapper.listAllByParam(new Page<>(dto.getCurrPage(), dto.getPageSize()) ,dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    public List<WarehouseLocationDTO.CoreDTO> listArea(String warehouseId, String areaTypeCode) {
        return Collections.emptyList();
    }

    @Override
    @Cacheable(cacheNames = "cache:wms:getWarehouseLocation",keyGenerator = "myKeyGenerator")
    public WarehouseLocationEntity getWarehouseLocation(String warehouseId, String warehouseLocation,WarehouseLocationTypeEnum type) {
        WarehouseLocationEntity warehouseLocationEntity = lambdaQuery()
                .eq(WarehouseLocationEntity::getWarehouseId, warehouseId)
                .eq(WarehouseLocationEntity::getCode, warehouseLocation)
                .eq(WarehouseLocationEntity::getType, type.getCode())
                .last("limit 1")
                .one();
        return warehouseLocationEntity;
    }


    /**
     * @description: 下拉数据处理
     * @author Will
     * @date: 2024/5/16 15:49
     * @param records
     */
    private void handleSelect (List<WarehouseLocationDTO.LocationListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        for (WarehouseLocationDTO.LocationListDTO locationListDTO: records) {
            locationListDTO.setStatusName(WarehouseLocationStatusEnum.getName(locationListDTO.getStatus()));
            locationListDTO.setCanCheck(Boolean.TRUE);
            if(Objects.equals(locationListDTO.getDisabled(), Boolean.TRUE) || Objects.equals(locationListDTO.getStatus(), WarehouseLocationStatusEnum.STOP.getCode())) {
                locationListDTO.setCanCheck(Boolean.FALSE);
            }
        }
    }

    @Override
    public PagingVO<WarehouseLocationDTO.ViewDto> pagingByParam(PagingDTO<WarehouseLocationDTO.SearchParamDTO> dto) {
        IPage<WarehouseLocationDTO.ViewDto> result = baseMapper.pagingByArgs(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(result);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(cacheNames = "cache:wms:listByWarehouseIds", allEntries = true)
    public List<String> deleteBatch(WarehouseLocationDTO.IdsDto idsDto) {
        List<String> errorList = new ArrayList<>();
        LoginUser user = UserContext.getNonLoginUser();

        List<WarehouseLocationEntity> list = baseMapper.selectBatchIds(idsDto.getIds());
        LambdaQueryWrapper<InventoryEntity> queryWrapper;
        for (WarehouseLocationEntity entity : list) {
            queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(InventoryEntity::getWarehouseLocation, entity.getCode()).eq(InventoryEntity::getIsDeleted, false);
            List<InventoryEntity> inventoryList = inventoryMapper.selectList(queryWrapper);
            if (! CollectionUtils.isEmpty(inventoryList)) {
                //仓位有商品，不能删除
                errorList.add(String.format("仓位：%s 存在商品，不能删除", entity.getCode()));
                continue;
            }
            baseMapper.deleteById(entity.getId());
            List<WarehouseLocationEntity> brotherList = baseMapper.selectList(new QueryWrapper<WarehouseLocationEntity>().eq("warehouse_id", entity.getWarehouseId()).eq("is_deleted", false).eq("parent_id", entity.getParentId()));
            if(brotherList.isEmpty()){
                WarehouseLocationEntity updateArea = new WarehouseLocationEntity();
                updateArea.setId(entity.getParentId());
                updateArea.setOccupyStatus(Boolean.FALSE);
                baseMapper.updateById(updateArea);
            }
            operateLogService.addModuleOperateLog(String.format("删除仓位【%s】", entity.getCode()), ModuleTypeEnum.WAREHOUSE_LOCATION.getCode(), entity.getId(), "删除", user.getUid(), user.getUserName());
        }
        return errorList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(cacheNames = "cache:wms:listByWarehouseIds", allEntries = true)
    public void importExcel(MultipartFile file, HttpServletResponse response) {
        LoginUser user = UserContext.getNonLoginUser();
        //读取Excel
        WarehouseLocationExcelListener listener = new WarehouseLocationExcelListener();
        try {
            EasyExcel.read(file.getInputStream(), listener).sheet(0).doRead();
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95124);
        }

        List<WarehouseLocationExcelDto> errorList = listener.getErrorList();
        //通过必填校验的行
        List<WarehouseLocationExcelDto> verifyList = listener.getSuccessList();
        List<String> excelWarehouseNameList = verifyList.stream().map(WarehouseLocationExcelDto::getWarehouseName).collect(Collectors.toList());
        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.getByNames(excelWarehouseNameList);
        Map<String, String> warehouseName2IdMap = warehouseList.stream().collect(Collectors.toMap(WarehouseDTO.ListDTO::getName, WarehouseDTO.ListDTO::getId));
        for (WarehouseLocationExcelDto row : verifyList) {
            String warehouseId = warehouseName2IdMap.get(row.getWarehouseName());
            if(warehouseId == null){
                row.setErrorMsg("仓库不存在");
                errorList.add(row);
                continue;
            }
            WarehouseEntity warehouseEntity = warehouseMapper.selectById(warehouseId);
            if(warehouseEntity == null || warehouseEntity.getIsDeleted()) {
                row.setErrorMsg("仓库不存在");
                errorList.add(row);
                continue;
            }
            if(warehouseEntity.getDisabled()){
                row.setErrorMsg("仓库被禁用");
                errorList.add(row);
                continue;
            }
            if(warehouseEntity.getApproveStatus() != ApproveStatusEnum.APPROVE){
                row.setErrorMsg("仓库未审核");
                errorList.add(row);
                continue;
            }

            //当前仓库下必须存在对应的库区
            WarehouseLocationEntity areaEntity = baseMapper.selectOne(new QueryWrapper<WarehouseLocationEntity>().eq("warehouse_id", warehouseId).eq("type", "area").eq("name", row.getWarehouseAreaName()).eq("is_deleted", false));
            if (areaEntity == null || areaEntity.getIsDeleted()) {
                row.setErrorMsg("库区不存在");
                errorList.add(row);
                continue;
            }
            if(areaEntity.getDisabled()){
                row.setErrorMsg("库区被禁用");
                errorList.add(row);
                continue;
            }

            //仓库下没有该仓位，直接新增
            WarehouseLocationEntity locationEntity = baseMapper.selectOne(new QueryWrapper<WarehouseLocationEntity>().in("warehouse_id", warehouseId).eq("type", "location").eq("code", row.getWarehouseLocationCode()).eq("is_deleted", false));
            if(locationEntity == null || locationEntity.getIsDeleted()){
                WarehouseLocationEntity addEntity = buildAddEntity(row, areaEntity, warehouseId);
                baseMapper.insert(addEntity);
                WarehouseLocationEntity one = baseMapper.selectOne(new QueryWrapper<WarehouseLocationEntity>().eq("warehouse_id", warehouseId).eq("code", row.getWarehouseLocationCode())
                        .eq("type", "location").eq("is_deleted", false).eq("disabled", false));

                areaEntity.setOccupyStatus(Boolean.TRUE);
                baseMapper.updateById(areaEntity);
                operateLogService.addModuleOperateLog(String.format("新增仓位【%s】", row.getWarehouseLocationCode()), ModuleTypeEnum.WAREHOUSE_LOCATION.getCode(), one.getId(), "新增", user.getUid(), user.getUserName());
                continue;
            }

            //比较仓位名称是否一致，不一致则更新仓位名称
            //比较所属库区是否一致，不一致则更新库区
            boolean equalLocation = locationEntity.getName().equals(row.getWarehouseLocationName().trim());
            boolean equalArea = areaEntity.getId().equals(locationEntity.getParentId());
            if(!equalLocation || !equalArea){
                WarehouseLocationEntity updateEntity = new WarehouseLocationEntity();
                updateEntity.setId(locationEntity.getId());
                updateEntity.setName(row.getWarehouseLocationName());
                updateEntity.setParentId(areaEntity.getId());
                baseMapper.updateById(updateEntity);
                WarehouseLocationEntity oldParentArea = warehouseLocationMapper.selectOne(new LambdaQueryWrapper<WarehouseLocationEntity>().eq(WarehouseLocationEntity::getId, locationEntity.getParentId()));
                operateLogService.addModuleOperateLog(String.format("更新仓位名称【%s】，【%s】->【%s】", locationEntity.getCode(), locationEntity.getName(), updateEntity.getName()), ModuleTypeEnum.WAREHOUSE_LOCATION.getCode(), locationEntity.getId(), "编辑操作", user.getUid(), user.getUserName());
                operateLogService.addModuleOperateLog(String.format("更新仓位所属库区【%s】，【%s】->【%s】", locationEntity.getCode(), oldParentArea.getName(), areaEntity.getName()), ModuleTypeEnum.WAREHOUSE_LOCATION.getCode(), locationEntity.getId(), "编辑操作", user.getUid(), user.getUserName());
                continue;
            }

            //仓位重复
            row.setErrorMsg("仓位已存在");
            errorList.add(row);
        }

        if(! errorList.isEmpty()){
            ExcelUtil.export("错误数据", "sheet1", errorList, WarehouseLocationExcelDto.class, response);
        }
    }

    private static WarehouseLocationEntity buildAddEntity(WarehouseLocationExcelDto row, WarehouseLocationEntity areaEntity, String warehouseId) {
        WarehouseLocationEntity addEntity = new WarehouseLocationEntity();
        addEntity.setCode(row.getWarehouseLocationCode());
        addEntity.setName(row.getWarehouseLocationName());
        addEntity.setType("location");
        addEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        addEntity.setDisabled(false);
        addEntity.setIsDeleted(false);
        addEntity.setParentId(areaEntity.getId());
        addEntity.setWarehouseId(warehouseId);
        return addEntity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(cacheNames = "cache:wms:listByWarehouseIds", allEntries = true)
    public List<BatchResultDTO> recycle(WarehouseLocationDTO.IdsDto idsDto) {
        LoginUser user = UserContext.getNonLoginUser();
        List<BatchResultDTO> errorList = new ArrayList<>();
        List<WarehouseLocationEntity> locationList = baseMapper.selectBatchIds(idsDto.getIds());
        for (WarehouseLocationEntity item : locationList) {
            LambdaQueryWrapper<InventoryEntity> queryWrapper = Wrappers.lambdaQuery();
            List<InventoryEntity> inventoryList = inventoryMapper.selectList(queryWrapper.eq(InventoryEntity::getWarehouseLocation, item.getCode()).eq(InventoryEntity::getIsDeleted, false));
            if(CollectionUtils.isEmpty(inventoryList)){
//                errorList.add(String.format("仓位【%s】没有分配商品，不需要回收", item.getCode()));
                errorList.add(BatchResultDTO.fail(item.getId(), item.getCode(), String.format("仓位【%s】没有分配商品，不需要回收", item.getCode())));
                continue;
            }
            //核对商品库存
            int sum = inventoryList.stream().mapToInt(InventoryEntity::getQty).sum();
            if(sum > 0){
//                errorList.add(String.format("仓位【%s】已分配商品，且库存不为0，不允许回收", item.getCode()));
                errorList.add(BatchResultDTO.fail(item.getId(), item.getCode(), String.format("仓位【%s】已分配商品，且库存不为0，不允许回收", item.getCode())));
                continue;
            }

            WarehouseLocationEntity recycleEntity = new WarehouseLocationEntity();
            recycleEntity.setId(item.getId());
            recycleEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
            baseMapper.updateById(recycleEntity);
            operateLogService.addModuleOperateLog(String.format("回收仓位【%s】", recycleEntity.getCode()), ModuleTypeEnum.WAREHOUSE_LOCATION.getCode(), recycleEntity.getId(), "编辑操作", user.getUid(), user.getUserName());
        }
        return errorList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(cacheNames = "cache:wms:listByWarehouseIds", allEntries = true)
    public void updateDisabled(WarehouseLocationDTO.UpdateStatusDto dto) {
        LoginUser user = UserContext.getNonLoginUser();
        WarehouseLocationEntity entity = new WarehouseLocationEntity();
        entity.setId(dto.getId());
        entity.setDisabled(Boolean.valueOf(dto.getDisabled()));
        int i = baseMapper.updateById(entity);
        operateLogService.addModuleOperateLog(String.format("更新仓位状态：%s", entity.getDisabled() ? "禁用" : "启用"), ModuleTypeEnum.WAREHOUSE_LOCATION.getCode(), entity.getId(), "状态变更", user.getUid(), user.getUserName());
    }

    @Override
    public void exportExcel(WarehouseLocationDTO.exportParamDto dto) {
        downloadTaskFeign.saveDownloadTask("仓位数据导出", EXPORT_WMS_WAREHOUSE_LOCATION.getCode(), dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(cacheNames = "cache:wms:listByWarehouseIds", allEntries = true)
    public void add(WarehouseLocationDTO.AddDTO dto) {
        LoginUser user = UserContext.getNonLoginUser();
        WarehouseLocationEntity codeEntity = baseMapper.selectOne(new QueryWrapper<WarehouseLocationEntity>().eq("warehouse_id", dto.getWarehouseId())
                .eq("code", dto.getCode()).eq("type", "location").eq("is_deleted", false));
        if(codeEntity != null){
            throw new ServiceException("仓位编码重复");
        }

        WarehouseLocationEntity nameEntity = baseMapper.selectOne(new QueryWrapper<WarehouseLocationEntity>().eq("warehouse_id", dto.getWarehouseId())
                .eq("name", dto.getName()).eq("type", "location").eq("is_deleted", false));
        if(nameEntity != null){
            throw new ServiceException("仓位名称重复");
        }

        if(dto.getCode().length() > 32){
            throw new ServiceException("仓位编码过长");
        }
        if(dto.getName().length() > 200){
            throw new ServiceException("仓位名称过长");
        }
        if(dto.getRemark() != null && dto.getRemark().length() > 200){
            throw new ServiceException("备注过长");
        }

        WarehouseLocationEntity insertEntity = new WarehouseLocationEntity();
        insertEntity.setType("location");
        insertEntity.setCode(dto.getCode());
        insertEntity.setName(dto.getName());
        insertEntity.setWarehouseId(dto.getWarehouseId());
        insertEntity.setParentId(dto.getWarehouseAreaId());
        insertEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        insertEntity.setRemark(dto.getRemark());
        baseMapper.insert(insertEntity);
        WarehouseLocationEntity one = baseMapper.selectOne(new QueryWrapper<WarehouseLocationEntity>().eq("warehouse_id", dto.getWarehouseId())
                .eq("code", dto.getCode()).eq("type", "location"));

        WarehouseLocationEntity updateEntity = new WarehouseLocationEntity();
        updateEntity.setId(dto.getWarehouseAreaId());
        updateEntity.setOccupyStatus(true);
        baseMapper.updateById(updateEntity);

        operateLogService.addModuleOperateLog(String.format("新增仓位：%s", insertEntity), ModuleTypeEnum.WAREHOUSE_LOCATION.getCode(), one.getId(), "新增", user.getUid(), user.getUserName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(cacheNames = "cache:wms:listByWarehouseIds", allEntries = true)
    public void update(WarehouseLocationDTO.UpdateDto dto) {
        LoginUser user = UserContext.getNonLoginUser();
        if(dto.getCode().length() > 32){
            throw new ServiceException("仓位编码过长");
        }
        WarehouseLocationEntity codeEntity = baseMapper.selectOne(new QueryWrapper<WarehouseLocationEntity>().eq("warehouse_id", dto.getWarehouseId())
                .eq("code", dto.getCode()).eq("type", "location").eq("is_deleted", false));
        if(codeEntity != null && !codeEntity.getId().equals(dto.getId())){
            throw new ServiceException("仓位编码重复");
        }

        if(dto.getName().length() > 200){
            throw new ServiceException("仓位名称过长");
        }
        WarehouseLocationEntity nameEntity = baseMapper.selectOne(new QueryWrapper<WarehouseLocationEntity>().eq("warehouse_id", dto.getWarehouseId())
                .eq("name", dto.getName()).eq("type", "location").eq("is_deleted", false));
        if(nameEntity != null && !nameEntity.getId().equals(dto.getId())){
            throw new ServiceException("仓位名称重复");
        }

        if(dto.getRemark() != null && dto.getRemark().length() > 200){
            throw new ServiceException("备注过长");
        }
        WarehouseLocationEntity one = baseMapper.selectById(dto.getId());

        WarehouseLocationEntity newEntity = new WarehouseLocationEntity();
        newEntity.setId(dto.getId());
        newEntity.setCode(dto.getCode());
        newEntity.setName(dto.getName());
        newEntity.setWarehouseId(dto.getWarehouseId());
        newEntity.setParentId(dto.getWarehouseAreaId());
        newEntity.setRemark(dto.getRemark());
        baseMapper.updateById(newEntity);

        WarehouseLocationEntity areaEntity = new WarehouseLocationEntity();
        areaEntity.setId(dto.getWarehouseAreaId());
        areaEntity.setOccupyStatus(true);
        baseMapper.updateById(areaEntity);

        generateLog(user, one, newEntity);
    }

    private void existCode(String code, String id, String type, String warehouseId) {
        int count = count(Wrappers.<WarehouseLocationEntity>lambdaQuery()
                .eq(WarehouseLocationEntity::getCode, code)
                .eq(WarehouseLocationEntity::getType, type)
                .eq(WarehouseLocationEntity::getWarehouseId,warehouseId)
                .ne(CharSequenceUtil.isNotBlank(id), WarehouseLocationEntity::getId, id));
        if (count > 0) {
            throw new ServiceException(ApiError.WAREHOUSE_AREA_EXIST, "编码", code);
        }
    }

    private void existName(String name, String id , String type, String warehouseId) {
        int count = count(Wrappers.<WarehouseLocationEntity>lambdaQuery()
                .eq(WarehouseLocationEntity::getName, name)
                .eq(WarehouseLocationEntity::getType, type)
                .eq(WarehouseLocationEntity::getWarehouseId, warehouseId)
                .ne(CharSequenceUtil.isNotBlank(id), WarehouseLocationEntity::getId, id));
        if (count > 0) {
            throw new ServiceException(ApiError.WAREHOUSE_AREA_EXIST, "名称", name);
        }
    }

    @Override
    public List<WarehouseLocationDTO.ViewDto> listAreaByWarehouseId(String warehouseId) {
        return baseMapper.listAreaByWarehouseId(warehouseId);
    }

    @Override
    public List<WarehouseLocationDTO.TabDto> tabList() {
        List<WarehouseLocationDTO.TabDto> list = new ArrayList<>(4);
        Integer occupiedCount = warehouseLocationMapper.countByStatus(WarehouseLocationStatusEnum.OCCUPIED.getCode());
        WarehouseLocationDTO.TabDto occupied = new WarehouseLocationDTO.TabDto(WarehouseLocationStatusEnum.OCCUPIED.getCode(), occupiedCount);
        list.add(occupied);

        Integer recyclableCount = warehouseLocationMapper.countByStatus(WarehouseLocationStatusEnum.RECYCLABLE.getCode());
        WarehouseLocationDTO.TabDto recyclable = new WarehouseLocationDTO.TabDto(WarehouseLocationStatusEnum.RECYCLABLE.getCode(), recyclableCount);
        list.add(recyclable);

        Integer idleCount = warehouseLocationMapper.countByStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        WarehouseLocationDTO.TabDto idle = new WarehouseLocationDTO.TabDto(WarehouseLocationStatusEnum.IDLE.getCode(), idleCount);
        list.add(idle);

        Integer allCount = warehouseLocationMapper.countByStatus(null);
        WarehouseLocationDTO.TabDto all = new WarehouseLocationDTO.TabDto("all", allCount);
        list.add(all);

        return list;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/warehouseLocation.xlsx";
        String excelName = "template.xlsx";

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @CacheEvict(cacheNames = "cache:wms:listByWarehouseIds", allEntries = true)
    public void updateLocationStatus(String warehouseId, String warehouseLocation, String status) {
        baseMapper.updateLocationStatus(warehouseId, warehouseLocation, status);
    }

    private void generateLog(LoginUser user, WarehouseLocationEntity old, WarehouseLocationEntity young) {
        if(StringUtils.compare(old.getWarehouseId(), young.getWarehouseId()) != 0) {
            WarehouseEntity oldWarehouse = warehouseMapper.selectById(old.getWarehouseId());
            WarehouseEntity youngWarehouse = warehouseMapper.selectById(young.getWarehouseId());
            operateLogService.addModuleOperateLog(String.format("编辑仓库，由【%s】变更为【%s】", oldWarehouse.getName(), youngWarehouse.getName()), ModuleTypeEnum.WAREHOUSE_LOCATION.getCode(), old.getId(), "编辑操作", user.getUid(), user.getUserName());
        }
        if(StringUtils.compare(old.getParentId(), young.getParentId()) != 0) {
            WarehouseLocationEntity oldLocation = warehouseLocationMapper.selectById(old.getParentId());
            WarehouseLocationEntity youngLocation = warehouseLocationMapper.selectById(young.getParentId());
            operateLogService.addModuleOperateLog(String.format("编辑库区，由【%s】变更为【%s】", oldLocation.getName(), youngLocation.getName()), ModuleTypeEnum.WAREHOUSE_LOCATION.getCode(), old.getId(), "编辑操作", user.getUid(), user.getUserName());
        }
        if(StringUtils.compare(old.getName(), young.getName()) != 0) {
            operateLogService.addModuleOperateLog(String.format("编辑仓位名称，由【%s】变更为【%s】", old.getName(), young.getName()), ModuleTypeEnum.WAREHOUSE_LOCATION.getCode(), old.getId(), "编辑操作", user.getUid(), user.getUserName());
        }
        if(StringUtils.compare(old.getRemark(), young.getRemark()) != 0) {
            operateLogService.addModuleOperateLog(String.format("编辑备注，由【%s】变更为【%s】", old.getRemark(), young.getRemark()), ModuleTypeEnum.WAREHOUSE_LOCATION.getCode(), old.getId(), "编辑操作", user.getUid(), user.getUserName());
        }
    }

    @Override
    public List<WarehouseLocationEntity> listByLocationName(String warehouseLocationName) {
        return this.baseMapper.selectList(new QueryWrapper<WarehouseLocationEntity>()
                .eq("type", "location")
                .eq("name", warehouseLocationName)
                .eq("is_deleted", false));
    }

    @Override
    public List<WarehouseLocationDTO.CoreDTO> listAllArea() {
        List<WarehouseLocationEntity> entityList = this.baseMapper.selectList(new QueryWrapper<WarehouseLocationEntity>()
                .eq("type", "area"));
        HashMap<String, String> hashMap = new HashMap<>();
        for (WarehouseLocationEntity locationEntity : entityList) {
            hashMap.put(locationEntity.getCode(), locationEntity.getName());
        }
        List<WarehouseLocationDTO.CoreDTO> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            WarehouseLocationDTO.CoreDTO dto = new WarehouseLocationDTO.CoreDTO();
            dto.setCode(entry.getKey());
            dto.setName(entry.getValue());
            list.add(dto);
        }
        return list;
    }

    @Override
    public List<WarehouseLocationDTO.ReplenishAreaDTO> listArea(BaseIdsDTO.IdsDTO idsDTO) {
        List<String> ids = idsDTO.getIds();
        if(ids.isEmpty()){
            return Collections.emptyList();
        }
        List<WarehouseLocationEntity> entityList = warehouseLocationMapper.selectList(new QueryWrapper<WarehouseLocationEntity>()
                .in("warehouse_id", ids)
                .eq("disabled", false)
        );
        List<WarehouseLocationDTO.ReplenishAreaDTO> replenishAreaDTOList = new ArrayList<>(ids.size());
        for (String warehouseId : ids) {
            List<WarehouseLocationEntity> areaList = entityList.stream().filter(item -> item.getWarehouseId().equals(warehouseId) && item.getType().equals("area")).collect(Collectors.toList());
            List<WarehouseLocationDTO.ReplenishAreaDTO.WarehouseAreaDTO> stockingAreaDTOList = new ArrayList<>();
            List<WarehouseLocationDTO.ReplenishAreaDTO.WarehouseAreaDTO> pickingAreaDTOList = new ArrayList<>();
            for (WarehouseLocationEntity areaEntity : areaList) {
                if(! CharSequenceUtil.isNotBlank(areaEntity.getAreaType())){
                    continue;
                }
                if(areaEntity.getAreaType().equals("stockingArea")){
                    WarehouseLocationDTO.ReplenishAreaDTO.WarehouseAreaDTO areaDTO = new WarehouseLocationDTO.ReplenishAreaDTO.WarehouseAreaDTO(areaEntity.getCode(), areaEntity.getName());
                    stockingAreaDTOList.add(areaDTO);
                }
                if(areaEntity.getAreaType().equals("pickingArea")){
                    WarehouseLocationDTO.ReplenishAreaDTO.WarehouseAreaDTO areaDTO = new WarehouseLocationDTO.ReplenishAreaDTO.WarehouseAreaDTO(areaEntity.getCode(), areaEntity.getName());
                    pickingAreaDTOList.add(areaDTO);
                }
            }
            WarehouseLocationDTO.ReplenishAreaDTO replenishAreaDTO = new WarehouseLocationDTO.ReplenishAreaDTO();
            replenishAreaDTO.setWarehouseId(warehouseId);
            replenishAreaDTO.setStockingAreaList(stockingAreaDTOList);
            replenishAreaDTO.setPickingAreaList(pickingAreaDTOList);
            replenishAreaDTOList.add(replenishAreaDTO);
        }
        return replenishAreaDTOList;
    }

    @Override
    public List<WarehouseLocationEntity> listLocation(String warehouseId, String warehouseArea) {
        return warehouseLocationMapper.listLocation(warehouseId, warehouseArea);
    }

    @Override
    public WarehouseLocationEntity findWarehouseArea(String warehouseId, String warehouseLocation) {
        return warehouseLocationMapper.findWarehouseArea(warehouseId, warehouseLocation);
    }

    @Override
    public List<WarehouseLocationDTO.MappingDTO> listArea2LocationMapping(String warehouseId) {
        return warehouseLocationMapper.listArea2LocationMapping(warehouseId);
    }


    public WarehouseLocationDTO.WareInventoryQtyDTO getOneWareInventoryQty(String warehouseId, String skuNo){
        if(CharSequenceUtil.isBlank(warehouseId)||CharSequenceUtil.isBlank(skuNo)){
            return null;
        }
        return warehouseLocationMapper.getOneWareInventoryQty(warehouseId,skuNo);
    }
}
