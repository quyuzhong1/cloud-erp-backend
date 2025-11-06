package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DictBasicDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO.ThirdAddDTO;
import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.server.dmp.mapper.ThirdMappingMapper;
import com.erp.server.dmp.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;

@Component
@Slf4j
public class ThirdPlatformStrategy implements ThirdMappingStrategy {

    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private DmpBasicSystemService dmpBasicSystemService;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private ThirdMappingMapper thirdMappingMapper;
    @Resource
    private DictBasicService dictBasicService;

    @Override
    public boolean supports(String type) {
        return ThirdSysTypeEnum.PLATFORM.getCode().equals(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO addDTO) {
        //校验系统是否存在
        DmpBasicSystemEntity systemEntity = dmpBasicSystemService.getById(addDTO.getSysId());
        if (Objects.isNull(systemEntity)) {
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, ThirdSysTypeEnum.getNameByCode(addDTO.getType()));
        }
        List<ThirdAddDTO> thirdList = addDTO.getThirdList();
        //同一个第三方平台只能绑定一个仓库
        checkSysTypeBind(thirdList, addDTO.getType());
        //如果第三方信息为空，删除绑定关系
        if (CollectionUtils.isEmpty(thirdList)) {
            //根据sysId获取所有绑定关系
            List<ThirdMappingEntity> existMappingList = thirdMappingService.getList(addDTO.getType(), addDTO.getSysId());
            if (CollectionUtils.isEmpty(existMappingList)) {
                return new BaseResultDTO.AddDTO();
            }
            deleteBinded(existMappingList);
        } else {
            //查看当前平台sysId绑定的第三方信息
            List<ThirdMappingEntity> existMappingList = thirdMappingService.getList(addDTO.getType(), addDTO.getSysId());
            //如果当前平台没有绑定第三方数据，直接添加
            if (CollectionUtils.isEmpty(existMappingList)) {
                for (ThirdAddDTO item : thirdList) {
                    makeThirdMappingDto(addDTO, item, null);
                }
            } else {
                List<ThirdAddDTO> resultUpdatedList = new ArrayList<>();
                //判断当前平台是否绑定第三方数据,已绑定的更新，未绑定的删除
                checkSysBinding(addDTO, existMappingList, thirdList, null,resultUpdatedList);
                //保存新增数据
                thirdList.forEach(thirdAddDTO -> {
                    ThirdAddDTO updatedDto = resultUpdatedList.stream().filter(item -> Objects.equals(item.getSysType(), thirdAddDTO.getSysType())).findFirst().orElse(null);
                    if (CollectionUtils.isEmpty(resultUpdatedList) || (CollectionUtils.isNotEmpty(resultUpdatedList) && Objects.isNull(updatedDto))) {
                        makeThirdMappingDto(addDTO, thirdAddDTO, null);
                    }
                });
            }
        }
        return new BaseResultDTO.AddDTO();
    }

    @Override
    public ThirdMappingDTO.MappingViewDTO view(ThirdMappingDTO.ViewParamDTO viewParamDTO) {
        ThirdMappingDTO.MappingViewDTO mappingViewDTO = new ThirdMappingDTO.MappingViewDTO();
        String sysId = viewParamDTO.getSysId();
        //获取系统平台
        DmpBasicSystemEntity systemEntity = dmpBasicSystemService.getById(viewParamDTO.getSysId());
        if (Objects.isNull(systemEntity)) {
            return mappingViewDTO;
        }
        makeViewDto(viewParamDTO, sysId, mappingViewDTO, systemEntity);
        return mappingViewDTO;
    }

    @Override
    public void saveOrDeleteFeignBind(ThirdMappingEntity existMapping, String warehouseId, String warehouseCode, String warehouseName, boolean disabled) {
        return;
    }

    private void makeViewDto(ThirdMappingDTO.ViewParamDTO viewParamDTO, String sysId, ThirdMappingDTO.MappingViewDTO mappingViewDTO, DmpBasicSystemEntity systemEntity) {
        //获取第三方数据信息
        List<ThirdMappingDTO.ViewDTO> viewDTOList = new ArrayList<>();
        List<ThirdMappingEntity> thirdMappingEntityList = thirdMappingService.getList(viewParamDTO.getType(), sysId);

        // 平台系统信息
        Map<String, DictBasicDTO.ViewDTO> dictDasicMap = new HashMap<>();
        Map<String, DictBasicEntity> thirdPlatformSysTypeMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(thirdMappingEntityList)) {
            List<String> sysTypeList = thirdMappingEntityList.stream().map(ThirdMappingEntity::getThirdSysType).distinct().collect(Collectors.toList());
            List<DictBasicEntity> thirdPlatformSysTypeList = dictBasicService.lambdaQuery()
                    .eq(DictBasicEntity::getType, "thirdPlatformSysType")
                    .in(DictBasicEntity::getValue, sysTypeList)
                    .list();
            if (Objects.isNull(thirdPlatformSysTypeList)) {
                throw new ServiceException("系统类型字典信息不存在");
            }
            thirdPlatformSysTypeMap = thirdPlatformSysTypeList.stream().collect(Collectors.toMap(DictBasicEntity::getValue, e->e,  (v1, v2) -> v1));

            Map<String, List<String>> dictBasicGroupMap = thirdPlatformSysTypeList.stream()
                    .collect(Collectors.groupingBy(
                            DictBasicEntity::getRemark,
                            Collectors.mapping(DictBasicEntity::getValue, Collectors.toList())
                    ));
            for (Map.Entry<String, List<String>> entry : dictBasicGroupMap.entrySet()) {
                String database = StringUtils.isBlank(entry.getKey()) ? "dmp" : entry.getKey();
                // 跨库查询
                Map<String, DictBasicDTO.ViewDTO> curDictDasicMap = targetThirdPlatformMap(database, entry.getValue());
                dictDasicMap.putAll(curDictDasicMap);
            }
        }

        // 查询三方系统名称
        getViewVo(mappingViewDTO, systemEntity, viewDTOList, thirdMappingEntityList, dictDasicMap, thirdPlatformSysTypeMap);
    }
    
    static void getViewVo(ThirdMappingDTO.MappingViewDTO mappingViewDTO, DmpBasicSystemEntity systemEntity, List<ThirdMappingDTO.ViewDTO> viewDTOList, List<ThirdMappingEntity> thirdMappingEntityList, Map<String, DictBasicDTO.ViewDTO> dictDasicMap, Map<String, DictBasicEntity> thirdPlatformSysTypeMap) {
        for (ThirdMappingEntity thirdMappingEntity : thirdMappingEntityList) {
            ThirdMappingDTO.ViewDTO viewDTO = new ThirdMappingDTO.ViewDTO();
            DictBasicDTO.ViewDTO thirdEntity = dictDasicMap.get(thirdMappingEntity.getThirdId());
            if (Objects.isNull(thirdEntity)) {
                viewDTOList.add(viewDTO);
                continue;
            }
            viewDTO.setName(thirdEntity.getName());
            viewDTO.setThirdId(thirdMappingEntity.getThirdId());
            viewDTO.setThirdName(thirdMappingEntity.getThirdName());
            viewDTO.setCode(thirdEntity.getValue());
            viewDTO.setId(thirdMappingEntity.getId());
            DictBasicEntity dictBasicEntity = thirdPlatformSysTypeMap.get(thirdMappingEntity.getThirdSysType());
            if (null != dictBasicEntity) {
                viewDTO.setSysType(dictBasicEntity.getValue());
                viewDTO.setSysTypeName(dictBasicEntity.getName());
            }
            viewDTOList.add(viewDTO);
        }
        mappingViewDTO.setSalesOrgId("");
        mappingViewDTO.setSalesOrgName("");
        mappingViewDTO.setSysName(systemEntity.getName());
        mappingViewDTO.setSysId(systemEntity.getId());
        mappingViewDTO.setThirdList(viewDTOList);
    }


    public static Map<String, DictBasicDTO.ViewDTO> targetThirdPlatformMap(String database, List<String> dictBasicTypeList) {
        switch (database) {
            case "dmp":
                List<com.erp.model.dmp.entity.DictBasicEntity> dmpDictBasicEntityList = FeignQuery.create(com.erp.model.dmp.entity.DictBasicEntity.class)
                        .in(com.erp.model.dmp.entity.DictBasicEntity::getType, dictBasicTypeList)
                        .list();
                if (CollectionUtils.isEmpty(dmpDictBasicEntityList)) {
                    return Collections.emptyMap();
                } else {
                    return dmpDictBasicEntityList.stream()
                            .collect(Collectors.toMap(com.erp.model.dmp.entity.DictBasicEntity::getId,
                                    e-> new DictBasicDTO.ViewDTO(e.getId(), e.getRemark(), e.getValue(), e.getType(), e.getName(), e.getSort()),
                                    (v1, v2) -> v1));
                }
            case "oms":
                List<com.erp.model.oms.entity.DictBasicEntity> omsDictBasicEntityList = FeignQuery.create(com.erp.model.oms.entity.DictBasicEntity.class)
                        .in(com.erp.model.oms.entity.DictBasicEntity::getType, dictBasicTypeList)
                        .list();
                if (CollectionUtils.isEmpty(omsDictBasicEntityList)) {
                    return Collections.emptyMap();
                } else {
                    return omsDictBasicEntityList.stream()
                            .collect(Collectors.toMap(com.erp.model.oms.entity.DictBasicEntity::getId,
                                    e-> new DictBasicDTO.ViewDTO(e.getId(), e.getRemark(), e.getValue(), e.getType(), e.getName(), e.getSort()),
                                    (v1, v2) -> v1));
                }
            case "wms":
                List<com.erp.model.wms.entity.DictBasicEntity> wmsDictBasicEntityList = FeignQuery.create(com.erp.model.wms.entity.DictBasicEntity.class)
                        .in(com.erp.model.wms.entity.DictBasicEntity::getType, dictBasicTypeList)
                        .list();
                if (CollectionUtils.isEmpty(wmsDictBasicEntityList)) {
                    return Collections.emptyMap();
                } else {
                    return wmsDictBasicEntityList.stream()
                            .collect(Collectors.toMap(com.erp.model.wms.entity.DictBasicEntity::getId,
                                    e-> new DictBasicDTO.ViewDTO(e.getId(), e.getRemark(), e.getValue(), e.getType(), e.getName(), e.getSort()),
                                    (v1, v2) -> v1));
                }
            case "tms":
                List<com.erp.model.tms.entity.DictBasicEntity> tmsDictBasicEntityList = FeignQuery.create(com.erp.model.tms.entity.DictBasicEntity.class)
                        .in(com.erp.model.tms.entity.DictBasicEntity::getType, dictBasicTypeList)
                        .list();
                if (CollectionUtils.isEmpty(tmsDictBasicEntityList)) {
                    return Collections.emptyMap();
                } else {
                    return tmsDictBasicEntityList.stream()
                            .collect(Collectors.toMap(com.erp.model.tms.entity.DictBasicEntity::getId,
                                    e-> new DictBasicDTO.ViewDTO(e.getId(), e.getRemark(), e.getCode(), e.getType(), e.getName(), e.getIndex()),
                                    (v1, v2) -> v1));
                }
            case "sys":
                List<com.erp.model.sys.entity.DictBasicEntity> sysDictBasicEntityList = FeignQuery.create(com.erp.model.sys.entity.DictBasicEntity.class)
                        .in(com.erp.model.sys.entity.DictBasicEntity::getType, dictBasicTypeList)
                        .list();
                if (CollectionUtils.isEmpty(sysDictBasicEntityList)) {
                    return Collections.emptyMap();
                } else {
                    return sysDictBasicEntityList.stream()
                            .collect(Collectors.toMap(com.erp.model.sys.entity.DictBasicEntity::getId,
                                    e-> new DictBasicDTO.ViewDTO(e.getId(), e.getRemark(), e.getValue(), e.getType(), e.getName(), e.getSort()),
                                    (v1, v2) -> v1));
                }
            default:
                return Collections.emptyMap();
        }
    }
    
    /**
     * 判断当前平台是否绑定第三方数据
     *
     * @param addDTO
     * @param existMappingList
     * @param thirdList
     * @param resultUpdatedList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void checkSysBinding(ThirdMappingDTO.AddDTO addDTO, List<ThirdMappingEntity> existMappingList, List<ThirdAddDTO> thirdList,
                                WarehouseDTO.ListDTO warehouse,
                                 List<ThirdAddDTO> resultUpdatedList) {
        existMappingList.forEach(existMapping -> {
            ThirdAddDTO thirdAddDTO = thirdList.stream().filter(item ->
                    Objects.equals(item.getSysType(), existMapping.getThirdSysType())).findFirst().orElse(null);
            //如果新增的第三方类型数据在原始数据中不存在，删除原始数据
            if (Objects.isNull(thirdAddDTO)) {
                // 操作日志
                String msg = StrUtil.format("编辑了【{}】的平台由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
                        existMapping.getThirdName(), "");
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
                thirdMappingMapper.deleteById(existMapping.getId());
            } else {
                //如果新增的第三方类型数据在原始数据中存在，判断第三方数据是否绑定
                //绑定则进行更新
                ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
                BeanMapperUtils.copy(addDTO, thirdMappingEntity);
                thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
                thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
                thirdMappingEntity.setThirdName(thirdAddDTO.getThirdName());
                thirdMappingEntity.setSysName(thirdAddDTO.getSysName());
                addOrUpdate(thirdMappingEntity, existMapping, null);
                resultUpdatedList.add(thirdAddDTO);
            }
        });
    }

    @Override
    public void checkData(ThirdMappingDTO.AddDTO addDTO, List<ThirdMappingEntity> saveList, List<ThirdMappingEntity> deleteList, List<ThirdMappingEntity> updateList) {

    }

    /**
     * 删除绑定数据
     *
     * @param existMappingList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void deleteBinded(List<ThirdMappingEntity> existMappingList) {
        existMappingList.forEach(existMapping -> {
            // 操作日志
            String msg = StrUtil.format("编辑了【{}】的平台由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
                    existMapping.getThirdName(), "");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
        });
        thirdMappingMapper.deleteBatchIds(existMappingList.stream().map(ThirdMappingEntity::getId).collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void makeThirdMappingDto(ThirdMappingDTO.AddDTO addDTO, ThirdAddDTO thirdAddDTO, WarehouseDTO.ListDTO warehouse) {
        ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
        BeanMapperUtils.copy(addDTO, thirdMappingEntity);
        thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
        thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
        thirdMappingEntity.setThirdName(thirdAddDTO.getThirdName());
        thirdMappingEntity.setSysName(thirdAddDTO.getSysName());
        thirdMappingEntity.setThirdInfoId(thirdAddDTO.getThirdId());
        addOrUpdate(thirdMappingEntity, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(ThirdMappingEntity thirdMappingEntity, ThirdMappingEntity existMapping, WarehouseDTO.ListDTO warehouse) {
        // 数据处理
        handleData(thirdMappingEntity);
        log.info("开始新增第三方系统映射关系单");
        Integer save;
        if (Objects.isNull(thirdMappingEntity.getId())) {
            save = thirdMappingMapper.insert(thirdMappingEntity);
        } else {
            save = thirdMappingMapper.updateById(thirdMappingEntity);
        }
        if (save <= 0) {
            throw new ServiceException("第三方系统映射关系单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("编辑了【{}】的平台由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, thirdMappingEntity.getThirdSysType()),
                Objects.isNull(existMapping) ? "" : existMapping.getThirdName(), thirdMappingEntity.getThirdName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), thirdMappingEntity.getSysId(), "编辑操作");
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(ThirdMappingEntity thirdMappingEntity) {
        //校验系统平台是否存在
        DmpBasicSystemEntity systemEntity = Optional.ofNullable(dmpBasicSystemService.getById(thirdMappingEntity.getSysId()))
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_92058));

        String sysName = thirdMappingEntity.getThirdName();
//        String thirdName = thirdShopEntity.getName();
//        thirdMappingEntity.setThirdInfoId(thirdShopEntity.getShopId());
//        thirdMappingEntity.setThirdCode(thirdShopEntity.getCode());
//        thirdMappingEntity.setThirdName(thirdName);
        thirdMappingEntity.setSysName(sysName);

        //校验平台信息和第三方信息一对一关系
        ThirdMappingEntity existSysMapping = thirdMappingService.getByTypeAndSysIdAndSysType(thirdMappingEntity);
        if (Objects.nonNull(existSysMapping)) {
            if (!Objects.equals(thirdMappingEntity.getSysId(), existSysMapping.getSysId())) {
                throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getType()), thirdMappingEntity.getThirdName(), existSysMapping.getSysName());
            } else {
                thirdMappingEntity.setId(existSysMapping.getId());
            }
        }
        ThirdMappingEntity existThirdMapping = thirdMappingService.getByTypeAndThirdId(thirdMappingEntity);
        if (Objects.nonNull(existThirdMapping) && ((Objects.nonNull(existSysMapping) && !Objects.equals(thirdMappingEntity.getSysId(), existThirdMapping.getSysId())) || Objects.isNull(existSysMapping))) {
            throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getType()), thirdMappingEntity.getThirdName(), existThirdMapping.getSysName());
        }
    }


    /**
     * 同一个第三方平台只能绑定一个仓库
     *
     * @param thirdList
     * @param type
     */
    private static void checkSysTypeBind(List<ThirdAddDTO> thirdList, String type) {
        Map<String, List<ThirdAddDTO>> result = thirdList.stream().collect(groupingBy(ThirdAddDTO::getSysType,
                collectingAndThen(Collectors.toList(), list -> {
                            if (list.size() > 1) {
                                throw new ServiceException(ApiError.ERROR_THIRD_SYS_TYPE_BINDING, ThirdSysTypeEnum.getNameByCode(type));
                            }
                            return list;
                        }
                )
        ));
        log.debug("校验结果：{}" , result);
    }


}
