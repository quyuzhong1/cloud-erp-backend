package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO.ThirdAddDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.dmp.mapper.ThirdMappingMapper;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.ThirdMappingService;
import com.erp.server.dmp.service.ThirdWarehouseService;
import com.erp.server.dmp.service.ThirdMappingStrategy;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Component
public class ThirdWarehouseStrategy implements ThirdMappingStrategy {
    @Resource
    private WmsWarehouseFeign wmsWarehouseFeign;

    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private ThirdMappingMapper thirdMappingMapper;

    @Resource
    private ThirdWarehouseService thirdWarehouseService;

    @Resource
    private OverseasProviderFeign overseasProviderFeign;

    @Resource
    private OperateLogService operateLogService;
//    @Resource
//    private ThirdMappingStrategy thirdMappingStrategy;

    @Override
    public boolean supports(String type) {
        return ThirdSysTypeEnum.WAREHOUSE.getCode().equals(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO addDTO) {
        WarehouseDTO.ListDTO warehouse = null;
        //获取仓库信息
        List<WarehouseDTO.ListDTO> listDTOS = wmsWarehouseFeign.listByIds(Collections.singletonList(addDTO.getSysId()));
        if (CollectionUtils.isEmpty(listDTOS)) {
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, ThirdSysTypeEnum.getNameByCode(addDTO.getType()));
        }
        warehouse = listDTOS.get(0);
        List<ThirdMappingDTO.ThirdAddDTO> thirdList = addDTO.getThirdList();
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
                for (ThirdMappingDTO.ThirdAddDTO item : thirdList) {
                    makeThirdMappingDto(addDTO, item, warehouse);
                }
            } else {
                List<ThirdMappingDTO.ThirdAddDTO> resultUpdatedList = new ArrayList<>();
                //判断当前平台是否绑定第三方数据
                checkSysBinding(addDTO, existMappingList, thirdList, warehouse, resultUpdatedList);
                //保存新增数据
                for (ThirdMappingDTO.ThirdAddDTO thirdAddDTO : thirdList) {
                    ThirdMappingDTO.ThirdAddDTO updatedDto = resultUpdatedList.stream().filter(item -> Objects.equals(item.getSysType(), thirdAddDTO.getSysType())).findFirst().orElse(null);
                    if (CollectionUtils.isEmpty(resultUpdatedList) || (CollectionUtils.isNotEmpty(resultUpdatedList) && Objects.isNull(updatedDto))) {
                        makeThirdMappingDto(addDTO, thirdAddDTO, warehouse);
                    }
                }
            }
        }
        return new BaseResultDTO.AddDTO();
    }

    @Override
    public ThirdMappingDTO.MappingViewDTO view(ThirdMappingDTO.ViewParamDTO viewParamDTO) {
        ThirdMappingDTO.MappingViewDTO mappingViewDTO = new ThirdMappingDTO.MappingViewDTO();
        String sysId = viewParamDTO.getSysId();
        //获取系统仓库
        List<WarehouseDTO.ListDTO> listDTOS =
                Optional.ofNullable(wmsWarehouseFeign.listByIds(Collections.singletonList(sysId))).orElse(new ArrayList<>());
        if (CollectionUtils.isEmpty(listDTOS)) {
            return mappingViewDTO;
        }
        makeWarehouseViewDto(viewParamDTO, sysId, mappingViewDTO, listDTOS);
        return mappingViewDTO;
    }

//    /**
//     * 保存新增数据
//     *
//     * @param addDTO
//     * @param thirdList
//     * @param resultUpdatedList
//     * @param warehouse
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    @GlobalTransactional(rollbackFor = Exception.class)
//    public void saveAddDto(ThirdMappingDTO.AddDTO addDTO, List<ThirdMappingDTO.ThirdAddDTO> thirdList, List<ThirdMappingDTO.ThirdAddDTO> resultUpdatedList, WarehouseDTO.ListDTO warehouse) {
//        thirdList.forEach(thirdAddDTO -> {
//            if (CollectionUtils.isNotEmpty(resultUpdatedList)) {
//                ThirdMappingDTO.ThirdAddDTO updatedDto = resultUpdatedList.stream().filter(item -> Objects.equals(item.getSysType(), thirdAddDTO.getSysType())).findFirst().orElse(null);
//                if (Objects.isNull(updatedDto)) {
//                    makeThirdMappingDto(addDTO, thirdAddDTO, warehouse);
//                }
//            } else {
//                makeThirdMappingDto(addDTO, thirdAddDTO, warehouse);
//            }
//        });
//    }

    /**
     * 判断当前平台是否绑定第三方数据
     *
     * @param addDTO
     * @param existMappingList
     * @param thirdList
     * @param warehouse
     * @param resultUpdatedList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void checkSysBinding(ThirdMappingDTO.AddDTO addDTO, List<ThirdMappingEntity> existMappingList, List<ThirdMappingDTO.ThirdAddDTO> thirdList, WarehouseDTO.ListDTO warehouse, List<ThirdMappingDTO.ThirdAddDTO> resultUpdatedList) {
        existMappingList.forEach(existMapping -> {
            ThirdMappingDTO.ThirdAddDTO thirdAddDTO = thirdList.stream().filter(item ->
                    Objects.equals(item.getSysType(), existMapping.getThirdSysType())).findFirst().orElse(null);
            //如果新增的第三方类型数据在原始数据中不存在，删除原始数据
            if (Objects.isNull(thirdAddDTO)) {
                // 操作日志
                String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
                        existMapping.getThirdName(), "");
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
                thirdMappingMapper.deleteById(existMapping.getId());
                saveOrDeleteFeignBind(existMapping, "", "", "", true);
            } else {
                //如果新增的第三方类型数据在原始数据中存在，判断第三方数据是否绑定
                //绑定则进行更新
                ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
                BeanMapperUtils.copy(addDTO, thirdMappingEntity);
                thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
                thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
                thirdMappingEntity.setThirdName(thirdAddDTO.getThirdName());
                thirdMappingEntity.setSysName(thirdAddDTO.getSysName());
                thirdMappingEntity.setThirdInfoId(thirdAddDTO.getThirdId());
                addOrUpdate(thirdMappingEntity, existMapping, warehouse);
                resultUpdatedList.add(thirdAddDTO);
            }
        });
    }

    @Override
    public void checkData(ThirdMappingDTO.AddDTO addDTO, List<ThirdMappingEntity> saveList, List<ThirdMappingEntity> deleteList, List<ThirdMappingEntity> updateList) {
        List<ThirdMappingDTO.ThirdAddDTO> thirdList = addDTO.getThirdList();
        //获取仓库信息
        WarehouseDTO.ListDTO warehouse = wmsWarehouseFeign.listByIds(Collections.singletonList(addDTO.getSysId())).stream().findFirst().orElse(null);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, ThirdSysTypeEnum.getNameByCode(addDTO.getType()));
        }
        String sysName = warehouse.getName();
        addDTO.setSysName(sysName);

        //如果第三方信息为空，删除绑定关系，不用校验数据
        if (CollectionUtils.isEmpty(thirdList)) {
            //根据sysId获取所有绑定关系
            List<ThirdMappingEntity> existMappingList = thirdMappingService.getList(addDTO.getType(), addDTO.getSysId());
            if (CollectionUtils.isNotEmpty(existMappingList)) {
                deleteList.addAll(existMappingList);
            }
            return;
        }
        //校验第三方仓库
        checkThirdAddList(thirdList, sysName);
        //查看当前平台sysId绑定的第三方信息
        List<ThirdMappingEntity> existMappingList = thirdMappingService.getList(addDTO.getType(), addDTO.getSysId());
        //如果当前平台没有绑定第三方数据，直接添加
        if (CollectionUtils.isEmpty(existMappingList)) {
            List<ThirdMappingEntity> saveDtoList = new ArrayList<>();
            for (ThirdMappingDTO.ThirdAddDTO thirdAddDTO : thirdList) {
//                makeThirdMappingDto(addDTO, item, warehouse);
                ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
                BeanMapperUtils.copy(addDTO, thirdMappingEntity);
                thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
                thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
                thirdMappingEntity.setThirdName(thirdAddDTO.getThirdName());
                thirdMappingEntity.setSysName(thirdAddDTO.getSysName());
//                addOrUpdate(thirdMappingEntity, null, warehouse);
                saveDtoList.add(thirdMappingEntity);
            }
            if (CollectionUtils.isNotEmpty(saveDtoList)) {
                saveList.addAll(saveDtoList);
            }
        } else {
            //判断当前平台是否绑定第三方数据
            existMappingList.forEach(existMapping -> {
                ThirdMappingDTO.ThirdAddDTO thirdAddDTO = thirdList.stream().filter(item ->
                        Objects.equals(item.getSysType(), existMapping.getThirdSysType())).findFirst().orElse(null);
                //如果新增的第三方类型数据在原始数据中不存在，删除原始数据
                if (Objects.isNull(thirdAddDTO)) {
                    deleteList.add(existMapping);
                } else {
                    //如果新增的第三方类型数据在原始数据中存在，判断第三方数据是否绑定
                    //绑定则进行更新
                    ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
                    BeanMapperUtils.copy(addDTO, thirdMappingEntity);
                    thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
                    thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
                    thirdMappingEntity.setThirdName(thirdAddDTO.getThirdName());
                    thirdMappingEntity.setSysName(thirdAddDTO.getSysName());
                    thirdMappingEntity.setThirdInfoId(thirdAddDTO.getId());
//                    addOrUpdate(thirdMappingEntity, existMapping, warehouse);
                    updateList.add(thirdMappingEntity);
                }
            });
            //获取新增数据
            thirdList.forEach(thirdAddDTO -> {
                ThirdMappingEntity thirdMappingEntity = updateList.stream().filter(item -> !Objects.equals(item.getThirdId(), thirdAddDTO.getThirdId())).findFirst().orElse(null);
                if (Objects.isNull(thirdMappingEntity)) {
                    ThirdMappingEntity newEntity = new ThirdMappingEntity();
                    BeanUtils.copyProperties(thirdAddDTO, newEntity);
                    saveList.add(newEntity);
                }
            });
        }
    }

    private void checkThirdAddList(List<ThirdMappingDTO.ThirdAddDTO> thirdList, String sysName) {
        thirdList.forEach(thirdAddDTO -> {
            String thirdName = null;
            if (PlatformDictEnum.WDT.getCode().equals(thirdAddDTO.getSysType())) {
                //校验第三方仓库是否存在
                ThirdWarehouseEntity thirdWarehouseEntity = Optional.ofNullable(thirdWarehouseService.getByWarehouseId(thirdAddDTO.getThirdId(), ThirdSysTypeEnum.WAREHOUSE.getCode()))
                        .orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_WAREHOUSE_NOTFOUND));
                thirdName = thirdWarehouseEntity.getName();
                thirdAddDTO.setThirdInfoId(thirdWarehouseEntity.getId());
                thirdAddDTO.setThirdCode(thirdWarehouseEntity.getCode());
            }
            if (OmsPlatformEnum.isThirdWarehouse(thirdAddDTO.getSysType())) {
                OverseasProviderDTO.FeignDTO feignDTO = new OverseasProviderDTO.FeignDTO();
                feignDTO.setCode(thirdAddDTO.getSysType());
                feignDTO.setOverseasProviderWarehouseId(thirdAddDTO.getThirdId());
                //校验第三方仓库是否存在
                OverseasProviderDTO.FeignDTO overseasWarehouse = Optional.ofNullable(overseasProviderFeign.getOverseasWarehouse(feignDTO))
                        .orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_WAREHOUSE_NOTFOUND));
                thirdName = overseasWarehouse.getPlatformWarehouseName();
                thirdAddDTO.setThirdInfoId(overseasWarehouse.getOverseasProviderWarehouseId());
                thirdAddDTO.setThirdCode(overseasWarehouse.getPlatformWarehouseCode());
            }
            thirdAddDTO.setThirdName(thirdName);
            thirdAddDTO.setSysName(sysName);

            //校验平台信息和第三方信息一对一关系
            ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
            thirdMappingEntity.setType(thirdAddDTO.getType());
            thirdMappingEntity.setSysId(thirdAddDTO.getSysId());
            thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
            thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
            ThirdMappingEntity existSysMapping = thirdMappingService.getByTypeAndSysIdAndSysType(thirdMappingEntity);
            if (Objects.nonNull(existSysMapping)) {
                if (!Objects.equals(thirdAddDTO.getSysId(), existSysMapping.getSysId())) {
                    throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getType()), thirdName, existSysMapping.getSysName());
                } else {
                    thirdAddDTO.setId(existSysMapping.getId());
                }
            }
            ThirdMappingEntity existThirdMapping = thirdMappingService.getByTypeAndThirdId(thirdMappingEntity);
            if (Objects.nonNull(existThirdMapping) && ((Objects.nonNull(existSysMapping) && !Objects.equals(thirdAddDTO.getSysId(), existThirdMapping.getSysId())) || Objects.isNull(existSysMapping))) {
                throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdAddDTO.getType()), thirdName, existThirdMapping.getSysName());
            }
        });
    }

    /**
     * 删除绑定数据
     *
     * @param existMappingList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void deleteBinded(List<ThirdMappingEntity> existMappingList) {
        existMappingList.forEach(existMapping -> {
            // 操作日志
            String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
                    existMapping.getThirdName(), "");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
        });
        thirdMappingMapper.deleteBatchIds(existMappingList.stream().map(ThirdMappingEntity::getId).collect(Collectors.toList()));
        //如果包含iml谷仓 需要通知海外仓解除绑定
        existMappingList.forEach(thirdMappingEntity -> {
            saveOrDeleteFeignBind(thirdMappingEntity, "", "", "", true);
        });
    }

    /**
     * 删除海外仓绑定数据
     *
     * @param existMapping
     * @param warehouseId
     * @param warehouseCode
     * @param warehouseName
     * @param disabled
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void saveOrDeleteFeignBind(ThirdMappingEntity existMapping, String warehouseId, String warehouseCode, String warehouseName, boolean disabled) {
        if (OmsPlatformEnum.isThirdWarehouse(existMapping.getThirdSysType())) {
            OverseasProviderDTO.FeignDTO feignDTO = new OverseasProviderDTO.FeignDTO();
            feignDTO.setCode(existMapping.getThirdSysType());
            feignDTO.setOverseasProviderWarehouseId(existMapping.getThirdId());
            feignDTO.setPlatformWarehouseCode(existMapping.getThirdCode());
            feignDTO.setPlatformWarehouseName(existMapping.getThirdName());
            feignDTO.setWarehouseId(warehouseId);
            feignDTO.setWarehouseName(warehouseName);
            feignDTO.setWarehouseCode(warehouseCode);
            feignDTO.setDisabled(disabled);
            overseasProviderFeign.feignBind(feignDTO);
        }
    }

    /**
     * 保存数据
     *
     * @param addDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void makeThirdMappingDto(ThirdMappingDTO.AddDTO addDTO, ThirdMappingDTO.ThirdAddDTO thirdAddDTO, WarehouseDTO.ListDTO warehouse) {
        ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
        BeanMapperUtils.copy(addDTO, thirdMappingEntity);
        thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
        thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
        thirdMappingEntity.setThirdName(thirdAddDTO.getThirdName());
        thirdMappingEntity.setSysName(thirdAddDTO.getSysName());
        addOrUpdate(thirdMappingEntity, null, warehouse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
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
        } else {
            //保存海外仓设置
            saveOrDeleteFeignBind(thirdMappingEntity, thirdMappingEntity.getSysId(), warehouse.getKingdeeWarehouseCode(), thirdMappingEntity.getSysName(), false);
        }
        // 操作日志
        String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, thirdMappingEntity.getThirdSysType()),
                Objects.isNull(existMapping) ? "" : existMapping.getThirdName(), thirdMappingEntity.getThirdName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), thirdMappingEntity.getSysId(), "编辑操作");
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(ThirdMappingEntity thirdMappingEntity) {
        String thirdName = thirdMappingEntity.getThirdName();
        String sysName = thirdMappingEntity.getSysName();
        //校验系统仓库是否存在
        List<WarehouseDTO.ListDTO> listDTOS = Optional.ofNullable(wmsWarehouseFeign.listByIds(Collections.singletonList(thirdMappingEntity.getSysId())))
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_92058));
        if (PlatformDictEnum.WDT.getCode().equals(thirdMappingEntity.getThirdSysType())) {

            //校验第三方仓库是否存在
            ThirdWarehouseEntity thirdWarehouseEntity = thirdWarehouseService.getByIdOpt(thirdMappingEntity.getThirdId())
                    .orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_WAREHOUSE_NOTFOUND));
            thirdName = thirdWarehouseEntity.getName();
            sysName = listDTOS.get(0).getName();
            thirdMappingEntity.setThirdInfoId(thirdWarehouseEntity.getWarehouseId());
            thirdMappingEntity.setThirdCode(thirdWarehouseEntity.getCode());
        }
        if (OmsPlatformEnum.isThirdWarehouse(thirdMappingEntity.getThirdSysType())) {
            OverseasProviderDTO.FeignDTO feignDTO = new OverseasProviderDTO.FeignDTO();
            feignDTO.setCode(thirdMappingEntity.getThirdSysType());
            feignDTO.setOverseasProviderWarehouseId(thirdMappingEntity.getThirdId());
            //校验第三方仓库是否存在
            OverseasProviderDTO.FeignDTO overseasWarehouse = Optional.ofNullable(overseasProviderFeign.getOverseasWarehouse(feignDTO))
                    .orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_WAREHOUSE_NOTFOUND));
            thirdName = overseasWarehouse.getPlatformWarehouseName();
            sysName = overseasWarehouse.getWarehouseName();
            thirdMappingEntity.setThirdInfoId(overseasWarehouse.getOverseasProviderWarehouseId());
            thirdMappingEntity.setThirdCode(overseasWarehouse.getPlatformWarehouseCode());
        }
        thirdMappingEntity.setThirdName(thirdName);
        thirdMappingEntity.setSysName(sysName);

        //校验平台信息和第三方信息一对一关系
        ThirdMappingEntity existSysMapping = thirdMappingService.getByTypeAndSysIdAndSysType(thirdMappingEntity);
        if (Objects.nonNull(existSysMapping)) {
            if (!Objects.equals(thirdMappingEntity.getSysId(), existSysMapping.getSysId())) {
                throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getType()), thirdName, existSysMapping.getSysName());
            } else {
                thirdMappingEntity.setId(existSysMapping.getId());
            }
        }
        ThirdMappingEntity existThirdMapping = thirdMappingService.getByTypeAndThirdId(thirdMappingEntity);
        if (Objects.nonNull(existThirdMapping) && ((Objects.nonNull(existSysMapping) && !Objects.equals(thirdMappingEntity.getSysId(), existThirdMapping.getSysId())) || Objects.isNull(existSysMapping))) {
            throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getType()), thirdName, existThirdMapping.getSysName());
        }
    }


    /**
     * 同一个第三方平台只能绑定一个仓库
     *
     * @param thirdList
     * @param type
     */
    private static void checkSysTypeBind(List<ThirdMappingDTO.ThirdAddDTO> thirdList, String type) {
        Map<String, List<ThirdAddDTO>> result = thirdList.stream().collect(groupingBy(ThirdMappingDTO.ThirdAddDTO::getSysType,
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

    private void makeWarehouseViewDto(ThirdMappingDTO.ViewParamDTO viewParamDTO, String sysId, ThirdMappingDTO.MappingViewDTO mappingViewDTO, List<WarehouseDTO.ListDTO> listDTOS) {
        //获取第三方数据信息
        List<ThirdMappingDTO.ViewDTO> viewDTOList = new ArrayList<>();
        List<ThirdMappingEntity> thirdMappingEntityList = thirdMappingService.getList(viewParamDTO.getType(), sysId);

        getViewDto(mappingViewDTO, listDTOS, thirdWarehouseService, overseasProviderFeign, viewDTOList, thirdMappingEntityList);
    }

    static void getViewDto(ThirdMappingDTO.MappingViewDTO mappingViewDTO, List<WarehouseDTO.ListDTO> listDTOS, ThirdWarehouseService thirdWarehouseService, OverseasProviderFeign overseasProviderFeign, List<ThirdMappingDTO.ViewDTO> viewDTOList, List<ThirdMappingEntity> thirdMappingEntityList) {
        for (ThirdMappingEntity thirdMappingEntity : thirdMappingEntityList) {
            ThirdMappingDTO.ViewDTO viewDTO = new ThirdMappingDTO.ViewDTO();

            if (PlatformDictEnum.WDT.getCode().equals(thirdMappingEntity.getThirdSysType())) {
                ThirdWarehouseEntity thirdWarehouseEntity = thirdWarehouseService.getByWarehouseId(thirdMappingEntity.getThirdId(), ThirdSysTypeEnum.WAREHOUSE.getCode());
                if (Objects.isNull(thirdWarehouseEntity)) {
                    viewDTOList.add(viewDTO);
                    continue;
                }
                viewDTO.setName(thirdWarehouseEntity.getName());
                viewDTO.setCode(thirdWarehouseEntity.getCode());
            }
            if (OmsPlatformEnum.isThirdWarehouse(thirdMappingEntity.getThirdSysType())) {
                //校验第三方仓库是否存在
                OverseasProviderDTO.FeignDTO feignDTO = new OverseasProviderDTO.FeignDTO();
                feignDTO.setCode(thirdMappingEntity.getThirdSysType());
                feignDTO.setOverseasProviderWarehouseId(thirdMappingEntity.getThirdId());
                OverseasProviderDTO.FeignDTO overseasWarehouse = overseasProviderFeign.getOverseasWarehouse(feignDTO);
                if (Objects.isNull(overseasWarehouse)) {
                    viewDTOList.add(viewDTO);
                    continue;
                }
                viewDTO.setThirdShortName(overseasWarehouse.getPlatformShortName());
                viewDTO.setName(overseasWarehouse.getPlatformWarehouseName());
                viewDTO.setCode(overseasWarehouse.getPlatformWarehouseCode());
            }
            viewDTO.setThirdId(thirdMappingEntity.getThirdId());
            viewDTO.setId(thirdMappingEntity.getId());
            viewDTO.setSysType(thirdMappingEntity.getThirdSysType());
            viewDTO.setSysTypeName(EnumMessage.getNameByCode(PlatformDictEnum.class, thirdMappingEntity.getThirdSysType()));
            viewDTOList.add(viewDTO);
        }
        mappingViewDTO.setOrgId(listDTOS.get(0).getOrgId());
        mappingViewDTO.setOrgName(listDTOS.get(0).getOrgName());
        mappingViewDTO.setSysName(listDTOS.get(0).getName());
        mappingViewDTO.setSysId(listDTOS.get(0).getId());
        mappingViewDTO.setThirdList(viewDTOList);
    }
}
