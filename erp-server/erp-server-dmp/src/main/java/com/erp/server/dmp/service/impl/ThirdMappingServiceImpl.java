package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO.ThirdAddDTO;
import com.erp.model.dmp.entity.ThirdLogisticsEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.dmp.mapper.ThirdMappingMapper;
import com.erp.server.dmp.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;

/**
 * <p>
 * 第三方系统映射关系表 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
@Slf4j
@Service
public class ThirdMappingServiceImpl extends SuperServiceImpl<ThirdMappingMapper, ThirdMappingEntity> implements ThirdMappingService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private WmsWarehouseFeign wmsWarehouseFeign;
    @Resource
    private ThirdShopService thirdShopService;
    @Resource
    private ThirdWarehouseService thirdWarehouseService;
    @Resource
    private ThirdLogisticsService thirdLogisticsService;
//    @Resource
//    private ThirdMappingService thirdMappingService;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private OverseasProviderFeign overseasProviderFeign;

//    @GlobalTransactional(rollbackFor = Exception.class)
//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO addDTO) {
//        WarehouseDTO.ListDTO warehouse = null;
//        //获取仓库信息
//        if (ThirdSysTypeEnum.WAREHOUSE.getCode().equals(addDTO.getType())) {
//            List<WarehouseDTO.ListDTO> listDTOS = wmsWarehouseFeign.listByIds(Collections.singletonList(addDTO.getSysId()));
//            if (CollectionUtils.isEmpty(listDTOS)) {
//                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, ThirdSysTypeEnum.getNameByCode(addDTO.getType()));
//            }
//            warehouse = listDTOS.get(0);
//        } else {
//            ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(addDTO.getSysId());
//            if (Objects.isNull(shopInfo)) {
//                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, ThirdSysTypeEnum.getNameByCode(addDTO.getType()));
//            }
//        }
//        List<ThirdMappingDTO.ThirdAddDTO> thirdList = addDTO.getThirdList();
//        //如果第三方信息为空，删除绑定关系
//        if (CollectionUtils.isEmpty(thirdList)) {
//            //根据sysId获取所有绑定关系
//            List<ThirdMappingEntity> existMappingList = baseMapper.selectList(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, addDTO.getType())
//                    .eq(ThirdMappingEntity::getSysId, addDTO.getSysId())
//                    .eq(ThirdMappingEntity::getIsDeleted, false));
//            if (CollectionUtils.isEmpty(existMappingList)) {
//                return new BaseResultDTO.AddDTO();
//            }
//            deleteBinded(existMappingList);
//        } else {
//            //同一个第三方平台只能绑定一个仓库
//            checkSysTypeBind(thirdList, addDTO.getType());
//            //查看当前平台sysId绑定的第三方信息
//            List<ThirdMappingEntity> existMappingList = baseMapper.selectList(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, addDTO.getType())
//                    .eq(ThirdMappingEntity::getSysId, addDTO.getSysId())
//                    .eq(ThirdMappingEntity::getIsDeleted, false));
//            //如果当前平台没有绑定第三方数据，直接添加
//            if (CollectionUtils.isEmpty(existMappingList)) {
//                for (ThirdMappingDTO.ThirdAddDTO item : thirdList) {
//                    makeThirdMappingDto(addDTO, item, warehouse);
//                }
//            } else {
//                List<ThirdMappingDTO.ThirdAddDTO> resultUpdatedList = new ArrayList<>();
//                //判断当前平台是否绑定第三方数据
//                checkSysBinding(addDTO, existMappingList, thirdList, warehouse, resultUpdatedList);
//                //保存新增数据
//                saveAddDto(addDTO, thirdList, resultUpdatedList, warehouse);
//            }
//        }
//        return new BaseResultDTO.AddDTO();
//    }


    @Resource
    private List<ThirdMappingStrategy> addStrategies;

//    @GlobalTransactional(rollbackFor = Exception.class)
//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO addDTO) {
//        ThirdMappingStrategy strategy = getStrategy(addDTO.getType());
//        return strategy.add(addDTO);
//    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO addDTO) {
        //同一个第三方平台只能绑定一个仓库
        checkSysTypeBind(addDTO.getThirdList(), addDTO.getType());
        //校验数据
        String kingDeeCode = checkData(addDTO, addDTO.getSaveList(), addDTO.getDeleteList(), addDTO.getUpdateList());
        //删除数据
        List<ThirdMappingEntity> deleteList = addDTO.getDeleteList();
        if (CollectionUtils.isNotEmpty(deleteList)) {
            deleteList.forEach(existMapping -> {
                // 操作日志
                String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
                        existMapping.getThirdName(), "");
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
            });
            baseMapper.deleteBatchIds(deleteList.stream().map(ThirdMappingEntity::getId).collect(Collectors.toList()));
            //如果包含iml谷仓 需要通知海外仓解除绑定
            if (ThirdSysTypeEnum.WAREHOUSE.getCode().equals(addDTO.getType())) {
                deleteList.forEach(thirdMappingEntity -> {
                    if (OmsPlatformEnum.getByCode(thirdMappingEntity.getThirdSysType()) != null) {
                        overseasProviderFeign.feignBind(makeOverseasFeign(thirdMappingEntity, "", "", "", true));
                    }
                });
            }
        }
        List<ThirdMappingEntity> updateList = addDTO.getUpdateList();
        if (CollectionUtils.isNotEmpty(updateList)) {
            List<ThirdMappingEntity> oldList = this.listByIds(updateList.stream().map(ThirdMappingEntity::getId).collect(Collectors.toList()));
            updateList.forEach(existMapping -> {
                ThirdMappingEntity oldEntity = oldList.stream().filter(item -> Objects.equals(item.getId(), existMapping.getId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_WAREHOUSE_NOTFOUND));
                // 操作日志
                String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
                        oldEntity.getThirdName(), existMapping.getThirdName());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
                this.updateById(existMapping);
                if (ThirdSysTypeEnum.WAREHOUSE.getCode().equals(addDTO.getType())) {
                    if (OmsPlatformEnum.getByCode(existMapping.getThirdSysType()) != null) {
                        //保存海外仓设置
                        overseasProviderFeign.feignBind(makeOverseasFeign(existMapping, existMapping.getSysId(), kingDeeCode, existMapping.getSysName(), false));
                    }
                }
            });
        }
        List<ThirdMappingEntity> saveList = addDTO.getSaveList();
        if (CollectionUtils.isNotEmpty(saveList)) {
            saveList.forEach(newEntity -> {
                // 操作日志
                String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, newEntity.getThirdSysType()),
                        "", newEntity.getThirdName());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), newEntity.getSysId(), "新增操作");
                this.save(newEntity);
                if (ThirdSysTypeEnum.WAREHOUSE.getCode().equals(addDTO.getType())) {
                    if (OmsPlatformEnum.getByCode(newEntity.getThirdSysType()) != null) {
                        //保存海外仓设置
                        overseasProviderFeign.feignBind(makeOverseasFeign(newEntity, newEntity.getSysId(), kingDeeCode, newEntity.getSysName(), false));
                    }
                }
            });
        }
        return new BaseResultDTO.AddDTO();
    }

    private ThirdMappingStrategy getStrategy(String type) {
        ThirdMappingStrategy strategy = addStrategies.stream()
                .filter(s -> s.supports(type))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, ThirdSysTypeEnum.getNameByCode(type)));
        return strategy;
    }

    /**
     * 保存新增数据
     *
     * @param addDTO
     * @param thirdList
     * @param resultUpdatedList
     * @param warehouse
     */
    private void saveAddDto(ThirdMappingDTO.AddDTO addDTO, List<ThirdMappingDTO.ThirdAddDTO> thirdList, List<ThirdMappingDTO.ThirdAddDTO> resultUpdatedList, WarehouseDTO.ListDTO warehouse) {
        thirdList.forEach(thirdAddDTO -> {
            if (CollectionUtils.isNotEmpty(resultUpdatedList)) {
                ThirdMappingDTO.ThirdAddDTO updatedDto = resultUpdatedList.stream().filter(item -> Objects.equals(item.getSysType(), thirdAddDTO.getSysType())).findFirst().orElse(null);
                if (Objects.isNull(updatedDto)) {
                    makeThirdMappingDto(addDTO, thirdAddDTO, warehouse);
                }
            } else {
                makeThirdMappingDto(addDTO, thirdAddDTO, warehouse);
            }
        });
    }

//    /**
//     * 判断当前平台是否绑定第三方数据
//     *
//     * @param addDTO
//     * @param existMappingList
//     * @param thirdList
//     * @param warehouse
//     * @param resultUpdatedList
//     */
//    private void checkSysBinding(ThirdMappingDTO.AddDTO addDTO, List<ThirdMappingEntity> existMappingList, List<ThirdMappingDTO.ThirdAddDTO> thirdList, WarehouseDTO.ListDTO warehouse, List<ThirdMappingDTO.ThirdAddDTO> resultUpdatedList) {
//        existMappingList.forEach(existMapping -> {
//            ThirdMappingDTO.ThirdAddDTO thirdAddDTO = thirdList.stream().filter(item ->
//                    Objects.equals(item.getSysType(), existMapping.getThirdSysType())).findFirst().orElse(null);
//            //如果新增的第三方类型数据在原始数据中不存在，删除原始数据
//            if (Objects.isNull(thirdAddDTO)) {
//                // 操作日志
//                String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
//                        existMapping.getThirdName(), "");
//                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
//                baseMapper.deleteById(existMapping.getId());
//                saveOrDeleteFeignBind(existMapping, "", "", "", true);
//            } else {
//                //如果新增的第三方类型数据在原始数据中存在，判断第三方数据是否绑定
//                //绑定则进行更新
//                ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
//                BeanMapperUtils.copy(addDTO, thirdMappingEntity);
//                thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
//                thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
//                thirdMappingEntity.setThirdName(thirdAddDTO.getThirdName());
//                thirdMappingEntity.setSysName(thirdAddDTO.getSysName());
//                addOrUpdate(thirdMappingEntity, existMapping, warehouse);
//                resultUpdatedList.add(thirdAddDTO);
//            }
//        });
//    }

//    /**
//     * 删除绑定数据
//     *
//     * @param existMappingList
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    @GlobalTransactional(rollbackFor = Exception.class)
//    public void deleteBinded(List<ThirdMappingEntity> existMappingList) {
//        existMappingList.forEach(existMapping -> {
//            // 操作日志
//            String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
//                    existMapping.getThirdName(), "");
//            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
//        });
//        baseMapper.deleteBatchIds(existMappingList.stream().map(ThirdMappingEntity::getId).collect(Collectors.toList()));
//        //如果包含iml谷仓 需要通知海外仓解除绑定
//        existMappingList.forEach(existMapping -> {
//            thirdMappingService.saveOrDeleteFeignBind(existMapping, "", "", "", true);
//        });
//    }

    /**
     * 删除海外仓绑定数据
     *
     * @param existMapping
     * @param warehouseId
     * @param warehouseCode
     * @param warehouseName
     * @param disabled
     */
    private void saveOrDeleteFeignBind(ThirdMappingEntity existMapping, String warehouseId, String warehouseCode, String warehouseName, boolean disabled) {
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

    private OverseasProviderDTO.FeignDTO makeOverseasFeign(ThirdMappingEntity existMapping, String warehouseId, String warehouseCode, String warehouseName, boolean disabled) {
        OverseasProviderDTO.FeignDTO feignDTO = new OverseasProviderDTO.FeignDTO();
        feignDTO.setCode(existMapping.getThirdSysType());
        feignDTO.setOverseasProviderWarehouseId(existMapping.getThirdId());
        feignDTO.setPlatformWarehouseCode(existMapping.getThirdCode());
        feignDTO.setPlatformWarehouseName(existMapping.getThirdName());
        feignDTO.setWarehouseId(warehouseId);
        feignDTO.setWarehouseName(warehouseName);
        feignDTO.setWarehouseCode(warehouseCode);
        feignDTO.setDisabled(disabled);
        return feignDTO;
    }

    private void makeThirdMappingDto(ThirdMappingDTO.AddDTO addDTO, ThirdMappingDTO.ThirdAddDTO thirdAddDTO, WarehouseDTO.ListDTO warehouse) {
        ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
        BeanMapperUtils.copy(addDTO, thirdMappingEntity);
        thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
        thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
        thirdMappingEntity.setThirdName(thirdAddDTO.getThirdName());
        thirdMappingEntity.setSysName(thirdAddDTO.getSysName());
        ThirdMappingEntity existMapping = null;
        addOrUpdate(thirdMappingEntity, existMapping, warehouse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(ThirdMappingEntity thirdMappingEntity, ThirdMappingEntity existMapping, WarehouseDTO.ListDTO warehouse) {
        // 数据处理
        handleData(thirdMappingEntity);
        log.info("开始新增第三方系统映射关系单");
        boolean save;
        if (Objects.isNull(thirdMappingEntity.getId())) {
            save = super.save(thirdMappingEntity);
        } else {
            save = super.updateById(thirdMappingEntity);
        }
        if (!save) {
            throw new ServiceException("第三方系统映射关系单保存失败");
        } else {
            if (ThirdSysTypeEnum.WAREHOUSE.getCode().equals(thirdMappingEntity.getType())) {
                //保存海外仓设置
                saveOrDeleteFeignBind(thirdMappingEntity, thirdMappingEntity.getSysId(), warehouse.getKingdeeWarehouseCode(), thirdMappingEntity.getSysName(), false);
            }
        }
        // 操作日志
        String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, thirdMappingEntity.getThirdSysType()),
                Objects.isNull(existMapping) ? "" : existMapping.getThirdName(), thirdMappingEntity.getThirdName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), thirdMappingEntity.getSysId(), "编辑操作");
    }

    /**
     * 查找系统绑定的第三方信息
     *
     * @param thirdMappingEntity
     * @return
     */
    @Override
    public ThirdMappingEntity getByTypeAndSysIdAndSysType(ThirdMappingEntity thirdMappingEntity) {
        return baseMapper.selectOne(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, thirdMappingEntity.getType())
                .eq(ThirdMappingEntity::getSysId, thirdMappingEntity.getSysId()).eq(ThirdMappingEntity::getThirdSysType, thirdMappingEntity.getThirdSysType())
                .eq(ThirdMappingEntity::getIsDeleted, false).eq(ThirdMappingEntity::getDisabled, false));
    }

    /**
     * 查找第三方数据绑定信息
     *
     * @param thirdMappingEntity
     * @return
     */
    @Override
    public ThirdMappingEntity getByTypeAndThirdId(ThirdMappingEntity thirdMappingEntity) {
        return baseMapper.selectOne(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, thirdMappingEntity.getType())
                .eq(ThirdMappingEntity::getThirdId, thirdMappingEntity.getThirdId()).eq(ThirdMappingEntity::getDisabled, false));
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

    //    @Override
//    public ThirdMappingDTO.MappingViewDTO view(ThirdMappingDTO.ViewParamDTO viewParamDTO) {
//        String type = viewParamDTO.getType();
//        return thirdMappingContext.executeStrategy(type, wmsWarehouseFeign, shopInfoFeign, overseasProviderFeign,
//                thirdMappingService, thirdWarehouseService, thirdShopService, viewParamDTO);
//    }
    @Override
    public ThirdMappingDTO.MappingViewDTO view(ThirdMappingDTO.ViewParamDTO viewParamDTO) {
        ThirdMappingStrategy strategy = getStrategy(viewParamDTO.getType());
        return strategy.view(viewParamDTO);
    }

//    @Override
//    public ThirdMappingDTO.MappingViewDTO view(ThirdMappingDTO.ViewParamDTO viewParamDTO) {
//        ThirdMappingDTO.MappingViewDTO mappingViewDTO = new ThirdMappingDTO.MappingViewDTO();
//        String sysId = viewParamDTO.getSysId();
//        if (ThirdSysTypeEnum.SHOP.getCode().equals(viewParamDTO.getType())) {
//            //获取系统店铺
//            ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(sysId);
//            if (Objects.isNull(shopInfo)) {
//                return mappingViewDTO;
//            }
//            makeShopViewDto(viewParamDTO, sysId, mappingViewDTO, shopInfo);
//        } else {
//            //获取系统仓库
//            List<WarehouseDTO.ListDTO> listDTOS =
//                    Optional.ofNullable(wmsWarehouseFeign.listByIds(Collections.singletonList(sysId))).orElse(new ArrayList<>());
//            if (CollectionUtils.isEmpty(listDTOS)) {
//                return mappingViewDTO;
//            }
//            makeWarehouseViewDto(viewParamDTO, sysId, mappingViewDTO, listDTOS);
//        }
//        return mappingViewDTO;
//    }

//    private void makeShopViewDto(ThirdMappingDTO.ViewParamDTO viewParamDTO, String sysId, ThirdMappingDTO.MappingViewDTO mappingViewDTO, ShopInfoEntity shopInfo) {
//        //获取第三方数据信息
//        List<ThirdMappingDTO.ViewDTO> viewDTOList = new ArrayList<>();
//        List<ThirdMappingEntity> thirdMappingEntityList = getList(viewParamDTO.getType(), sysId);
//
//        ThirdShopStrategy.getViewVo(mappingViewDTO, shopInfo, thirdShopService, viewDTOList, thirdMappingEntityList);
//    }

    /**
     * 根据类型和系统id获取数据
     *
     * @param type  类型
     * @param sysId 系统id
     * @return
     */
    @Override
    public List<ThirdMappingEntity> getList(String type, String sysId) {
        return baseMapper.selectList(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getSysId, sysId)
                .eq(ThirdMappingEntity::getType, type));
    }

//    private void makeWarehouseViewDto(ThirdMappingDTO.ViewParamDTO viewParamDTO, String sysId, ThirdMappingDTO.MappingViewDTO mappingViewDTO, List<WarehouseDTO.ListDTO> listDTOS) {
//        //获取第三方数据信息
//        List<ThirdMappingDTO.ViewDTO> viewDTOList = new ArrayList<>();
//        List<ThirdMappingEntity> thirdMappingEntityList = getList(viewParamDTO.getType(), sysId);
//
//        ThirdWarehouseStrategy.getViewDto(mappingViewDTO, listDTOS, thirdWarehouseService, overseasProviderFeign, viewDTOList, thirdMappingEntityList);
//    }

    @Override
    public Boolean getWhetherBind(ThirdMappingDTO.ViewParamDTO viewParamDTO) {
        //查看当前平台sysId绑定的第三方信息
        List<ThirdMappingEntity> existMappingList = baseMapper.selectList(new LambdaQueryWrapper<ThirdMappingEntity>()
                .eq(ThirdMappingEntity::getType, viewParamDTO.getType())
                .eq(ThirdMappingEntity::getSysId, viewParamDTO.getSysId())
                .eq(ThirdMappingEntity::getIsDeleted, false).eq(ThirdMappingEntity::getDisabled, false));
        if (CollectionUtils.isNotEmpty(existMappingList)) {
            return false;
        }
        return true;
    }

    @Override
    public List<ThirdMappingEntity> getByThirdId(ThirdMappingDTO.ViewParamDTO viewParamDTO) {
        //查看当前平台sysId绑定的第三方信息
        List<ThirdMappingEntity> existMappingList = baseMapper.selectList(new LambdaQueryWrapper<ThirdMappingEntity>()
                .eq(ThirdMappingEntity::getType, viewParamDTO.getType())
                .eq(ThirdMappingEntity::getThirdSysType, viewParamDTO.getSysType())
                .eq(ThirdMappingEntity::getThirdId, viewParamDTO.getThirdId())
                .eq(ThirdMappingEntity::getIsDeleted, false).eq(ThirdMappingEntity::getDisabled, false));
        return existMappingList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO batchAdd(ThirdMappingDTO.FeignMappingDTO feignMappingDTO) {
        //查询当前类型下所有的绑定数据
        List<ThirdMappingDTO.ThirdAddDTO> bindedList = baseMapper.getByThirdSysCode(feignMappingDTO.getThirdSysType(), feignMappingDTO.getType());
        //新绑定的数据
        List<ThirdMappingDTO.ThirdAddDTO> addDTOList = feignMappingDTO.getAddDTOList();

        //如果第三方信息为空，删除绑定关系
        if (CollectionUtils.isEmpty(addDTOList)) {
            //根据sysId获取所有绑定关系
            List<ThirdMappingEntity> existMappingList = baseMapper.selectList(new LambdaQueryWrapper<ThirdMappingEntity>()
                    .eq(ThirdMappingEntity::getType, feignMappingDTO.getType())
                    .eq(ThirdMappingEntity::getThirdSysType, feignMappingDTO.getThirdSysType())
                    .eq(ThirdMappingEntity::getIsDeleted, false));
            if (CollectionUtils.isEmpty(existMappingList)) {
                return new BaseResultDTO.AddDTO();
            }
            existMappingList.forEach(existMapping -> {
                // 操作日志
                String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
                        existMapping.getThirdName(), "");
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
            });
            baseMapper.deleteBatchIds(existMappingList.stream().map(ThirdMappingEntity::getId).collect(Collectors.toList()));
        } else {
            //循环所有的新增数据
            addDTOList.forEach(addDto -> {
                //查看当前平台sysId绑定的第三方信息
                ThirdMappingEntity thirdMappingEntity = baseMapper.selectOne(new LambdaQueryWrapper<ThirdMappingEntity>()
                        .eq(ThirdMappingEntity::getSysId, addDto.getSysId())
                        .eq(ThirdMappingEntity::getType, feignMappingDTO.getType())
                        .eq(ThirdMappingEntity::getThirdSysType, feignMappingDTO.getThirdSysType())
                        .eq(ThirdMappingEntity::getIsDeleted, false));
                if (Objects.nonNull(thirdMappingEntity)) {
                    thirdMappingEntity.setSysName(addDto.getSysName());
                    thirdMappingEntity.setThirdId(addDto.getThirdId());
                    thirdMappingEntity.setThirdName(addDto.getThirdName());
                    thirdMappingEntity.setThirdCode(addDto.getThirdCode());
                    //绑定过则修改
                    boolean save = super.updateById(thirdMappingEntity);
                    if (!save) {
                        throw new ServiceException("第三方系统映射关系单保存失败");
                    }
                    // 操作日志
                    String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, thirdMappingEntity.getThirdSysType()),
                            thirdMappingEntity.getThirdName(), addDto.getThirdName());
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), thirdMappingEntity.getSysId(), "编辑操作");
                } else {
                    //未绑定过则新增
                    ThirdMappingEntity entity = new ThirdMappingEntity();
                    BeanMapperUtils.copy(addDto, entity);
                    entity.setThirdSysType(addDto.getSysType());
                    entity.setThirdInfoId(addDto.getThirdId());
                    boolean save = super.save(entity);
                    if (!save) {
                        throw new ServiceException("第三方系统映射关系单保存失败");
                    }
                    // 操作日志
                    String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】",
                            EnumMessage.getNameByCode(PlatformDictEnum.class, entity.getThirdSysType()), "", entity.getThirdName());
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), entity.getSysId(), "编辑操作");
                }
            });
            //删除本次解除绑定的数据
            if (CollectionUtils.isNotEmpty(bindedList)) {
                List<String> deleteList = new ArrayList<>();
                bindedList.forEach(binded -> {
                    ThirdMappingDTO.ThirdAddDTO dto = addDTOList.stream().filter(addDTO -> Objects.equals(addDTO.getSysId(), binded.getSysId())).findFirst().orElse(null);
                    if (Objects.isNull(dto)) {
                        // 操作日志
                        String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】",
                                EnumMessage.getNameByCode(PlatformDictEnum.class, binded.getSysType()), binded.getThirdName(), "");
                        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), binded.getSysId(), "编辑操作");
                        deleteList.add(binded.getId());
                    }
                });
                if (CollectionUtils.isNotEmpty(deleteList)) {
                    baseMapper.deleteBatchIds(deleteList);
                }
            }
        }
        return null;
    }

    /**
     * 根据系统id获取仓库
     *
     * @param sysIds
     * @return
     */
    @Override
    public List<ThirdMappingEntity> getListBySysIds(List<String> sysIds) {
        LambdaQueryWrapper<ThirdMappingEntity> queryWrapper = new LambdaQueryWrapper<ThirdMappingEntity>()
                .in(ThirdMappingEntity::getSysId, sysIds)
                .eq(ThirdMappingEntity::getIsDeleted, false)
                .eq(ThirdMappingEntity::getDisabled, false);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据系统id获取虚拟仓绑定
     *
     * @param sysIds
     * @return
     */
    @Override
    public List<ThirdMappingEntity> getVwListBySysIds(List<String> sysIds) {
        List<ThirdMappingEntity> listBySysIds = getListBySysIds(sysIds);
        if (CollectionUtils.isNotEmpty(listBySysIds)) {
            List<ThirdWarehouseEntity> thirdWarehouseEntities = thirdWarehouseService.listByIds(listBySysIds.stream().map(ThirdMappingEntity::getThirdInfoId).collect(Collectors.toList()));
            listBySysIds.forEach(thirdMappingEntity -> {
                ThirdWarehouseEntity thirdWarehouseEntity = thirdWarehouseEntities.stream().filter(item -> Objects.equals(thirdMappingEntity.getThirdId(), item.getWarehouseId())).findFirst().orElse(null);
                if (Objects.nonNull(thirdWarehouseEntity)) {
                    String warehouseList = thirdWarehouseEntity.getWarehouseList();
                    if (StringUtils.isNotBlank(warehouseList)) {
                         JSONArray jsonArray = JSONObject.parseArray(warehouseList);

                        if (Objects.nonNull(jsonArray)) {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            String warehouseNo = jsonObject.getString("warehouse_no");
                            thirdMappingEntity.setRemark(warehouseNo);
                        }


//                        ThirdMappingDTO.WarehouseListDto warehouseListDto = JSONObject.parseArray(warehouseList, ThirdMappingDTO.WarehouseListDto.class).stream().findFirst().orElse(null);
//                        if (Objects.nonNull(warehouseListDto)) {
//                            thirdWarehouseEntity.setRemark(warehouseListDto.getSys_warehouse_id());
//                        }
                    }
                }
            });
        }
        return listBySysIds;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(ThirdMappingEntity thirdMappingEntity) {
        String thirdName = thirdMappingEntity.getThirdName();
        String sysName = thirdMappingEntity.getSysName();
        if (ThirdSysTypeEnum.SHOP.getCode().equals(thirdMappingEntity.getType())) {
            //校验系统店铺是否存在
            ShopInfoEntity shopInfoEntity = Optional.ofNullable(shopInfoFeign.getShopInfoById(thirdMappingEntity.getSysId()))
                    .orElseThrow(() -> new ServiceException(ApiError.ERROR_92058));
            //校验第三方店铺是否存在
            ThirdShopEntity thirdShopEntity = thirdShopService.getByIdOpt(thirdMappingEntity.getThirdId())
                    .orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_SHOP_NOTFOUND));
            thirdName = thirdShopEntity.getName();
            sysName = shopInfoEntity.getName();
            thirdMappingEntity.setThirdInfoId(thirdShopEntity.getShopId());
            thirdMappingEntity.setThirdCode(thirdShopEntity.getCode());
        } else {
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
        }
        thirdMappingEntity.setThirdName(thirdName);
        thirdMappingEntity.setSysName(sysName);

        //校验平台信息和第三方信息一对一关系
        ThirdMappingEntity existSysMapping = baseMapper.selectOne(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, thirdMappingEntity.getType())
                .eq(ThirdMappingEntity::getSysId, thirdMappingEntity.getSysId()).eq(ThirdMappingEntity::getThirdSysType, thirdMappingEntity.getThirdSysType())
                .eq(ThirdMappingEntity::getIsDeleted, false).eq(ThirdMappingEntity::getDisabled, false));
        if (Objects.nonNull(existSysMapping)) {
            if (!Objects.equals(thirdMappingEntity.getSysId(), existSysMapping.getSysId())) {
                throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getType()), thirdName, sysName);
            } else {
                thirdMappingEntity.setId(existSysMapping.getId());
            }
        }
        ThirdMappingEntity existThirdMapping = baseMapper.selectOne(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, thirdMappingEntity.getType())
                .eq(ThirdMappingEntity::getThirdId, thirdMappingEntity.getThirdId()).eq(ThirdMappingEntity::getDisabled, false));
        if (Objects.nonNull(existThirdMapping) && ((Objects.nonNull(existSysMapping) && !Objects.equals(thirdMappingEntity.getSysId(), existSysMapping.getSysId())) || Objects.isNull(existSysMapping))) {
            throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getType()), sysName, thirdName);
        }
    }

    @Override
    public ThirdWarehouseEntity getBySysId(String sysWarehouseId, String sysType) {
        LambdaQueryWrapper<ThirdMappingEntity> queryWrapper = new LambdaQueryWrapper<ThirdMappingEntity>()
                .eq(ThirdMappingEntity::getSysId, sysWarehouseId)
                .eq(ThirdMappingEntity::getType, "warehouse")
                .eq(ThirdMappingEntity::getThirdSysType, sysType)
                .eq(ThirdMappingEntity::getIsDeleted, false)
                .eq(ThirdMappingEntity::getDisabled, false);
        ThirdMappingEntity mappingEntity = baseMapper.selectOne(queryWrapper);
        if (mappingEntity == null) {
            return null;
        }
        LambdaQueryWrapper<ThirdWarehouseEntity> warehouseWrapper = new LambdaQueryWrapper<ThirdWarehouseEntity>()
                .eq(ThirdWarehouseEntity::getWarehouseId, mappingEntity.getThirdId())
                .eq(ThirdWarehouseEntity::getCategory, mappingEntity.getType())
                .eq(ThirdWarehouseEntity::getIsDeleted, false)
                .eq(ThirdWarehouseEntity::getDisabled, false);
        return thirdWarehouseService.getBaseMapper().selectOne(warehouseWrapper);
    }

    private String checkData(ThirdMappingDTO.AddDTO addDTO, List<ThirdMappingEntity> saveList, List<ThirdMappingEntity> deleteList, List<ThirdMappingEntity> updateList) {
        List<ThirdMappingDTO.ThirdAddDTO> thirdList = addDTO.getThirdList();
        //获取仓库信息
        String sysName = null;
        String kingDeeCode = null;
        switch (ThirdSysTypeEnum.getByCode(addDTO.getType())) {
            case WAREHOUSE:
                WarehouseDTO.ListDTO warehouse = wmsWarehouseFeign.listByIds(Collections.singletonList(addDTO.getSysId())).stream().findFirst().orElse(null);
                if (Objects.isNull(warehouse)) {
                    throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, ThirdSysTypeEnum.getNameByCode(addDTO.getType()));
                }
                sysName = warehouse.getName();
                kingDeeCode = warehouse.getKingdeeWarehouseCode();
                break;
            case SHOP:
                ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(addDTO.getSysId());
                if (Objects.isNull(shopInfo)) {
                    throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, ThirdSysTypeEnum.getNameByCode(addDTO.getType()));
                }
                sysName = shopInfo.getName();
                break;
            case VIRTUAL_WAREHOUSE:
                sysName = addDTO.getSysName();
                break;
            case LOGISTICS:
                LogisticsChannelEntity logisticsChannelEntity = logisticsFeign.getChannelById(addDTO.getSysId());
                if (Objects.isNull(logisticsChannelEntity)) {
                    throw new ServiceException("系统渠道为空");
                }
                sysName = logisticsChannelEntity.getName();
                break;
            default:
                throw new ServiceException(ApiError.ERROR_400);
        }
        addDTO.setSysName(sysName);

        //如果第三方信息为空，删除绑定关系，不用校验数据
        if (CollectionUtils.isEmpty(thirdList)) {
            //根据sysId获取所有绑定关系
            List<ThirdMappingEntity> existMappingList = getList(addDTO.getType(), addDTO.getSysId());
            if (CollectionUtils.isNotEmpty(existMappingList)) {
                deleteList.addAll(existMappingList);
                addDTO.setDeleteList(deleteList);
            }
            return kingDeeCode;
        }
        //校验第三方仓库
        checkThirdAddList(thirdList, sysName, addDTO);
        //查看当前平台sysId绑定的第三方信息
        List<ThirdMappingEntity> existMappingList = getList(addDTO.getType(), addDTO.getSysId());
        //如果当前平台没有绑定第三方数据，直接添加
        if (CollectionUtils.isEmpty(existMappingList)) {
            List<ThirdMappingEntity> saveDtoList = new ArrayList<>();
            for (ThirdMappingDTO.ThirdAddDTO thirdAddDTO : thirdList) {
                ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
                BeanMapperUtils.copy(thirdAddDTO, thirdMappingEntity);
                thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
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
                    BeanMapperUtils.copy(thirdAddDTO, thirdMappingEntity);
                    thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
                    updateList.add(thirdMappingEntity);
                }
            });
            //获取新增数据
            thirdList.forEach(thirdAddDTO -> {
                ThirdMappingEntity thirdMappingEntity = updateList.stream().filter(item -> Objects.equals(item.getThirdId(), thirdAddDTO.getThirdId())).findFirst().orElse(null);
                if (Objects.isNull(thirdMappingEntity)) {
                    ThirdMappingEntity newEntity = new ThirdMappingEntity();
                    BeanUtils.copyProperties(thirdAddDTO, newEntity);
                    newEntity.setThirdSysType(thirdAddDTO.getSysType());
                    saveList.add(newEntity);
                }
            });
        }
        addDTO.setDeleteList(deleteList);
        addDTO.setSaveList(saveList);
        addDTO.setUpdateList(updateList);
        return kingDeeCode;
    }

    private void checkThirdAddList(List<ThirdMappingDTO.ThirdAddDTO> thirdList, String sysName, ThirdMappingDTO.AddDTO addDTO) {
        String type = addDTO.getType();
        String sysId = addDTO.getSysId();
        thirdList.forEach(thirdAddDTO -> {
            String thirdName = null;
            switch (ThirdSysTypeEnum.getByCode(type)) {
                case WAREHOUSE:
                    if (PlatformDictEnum.WDT.getCode().equals(thirdAddDTO.getSysType())) {
                        //校验第三方仓库是否存在
                        ThirdWarehouseEntity thirdWarehouseEntity = Optional.ofNullable(thirdWarehouseService.getByWarehouseId(thirdAddDTO.getThirdId(), ThirdSysTypeEnum.WAREHOUSE.getCode()))
                                .orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_WAREHOUSE_NOTFOUND));
                        thirdName = thirdWarehouseEntity.getName();
                        thirdAddDTO.setThirdInfoId(thirdWarehouseEntity.getId());
                        thirdAddDTO.setThirdCode(thirdWarehouseEntity.getCode());
                    }
                    if (OmsPlatformEnum.getByCode(thirdAddDTO.getSysType()) != null) {
                        OverseasProviderDTO.FeignDTO feignDTO = new OverseasProviderDTO.FeignDTO();
                        feignDTO.setCode(thirdAddDTO.getSysType());
                        feignDTO.setPlatformShortName(thirdAddDTO.getThirdShortName());
                        feignDTO.setOverseasProviderWarehouseId(thirdAddDTO.getThirdId());
                        //校验第三方仓库是否存在
                        OverseasProviderDTO.FeignDTO overseasWarehouse = Optional.ofNullable(overseasProviderFeign.getOverseasWarehouse(feignDTO))
                                .orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_WAREHOUSE_NOTFOUND));
                        thirdName = overseasWarehouse.getPlatformWarehouseName();
                        thirdAddDTO.setThirdInfoId(overseasWarehouse.getOverseasProviderWarehouseId());
                        thirdAddDTO.setThirdCode(overseasWarehouse.getPlatformWarehouseCode());
                    }
                    break;
                case SHOP:
                    //校验第三方店铺是否存在
                    ThirdShopEntity thirdShopEntity = Optional.ofNullable(thirdShopService.getByShopId(thirdAddDTO.getThirdId()))
                            .orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_SHOP_NOTFOUND));
                    thirdName = thirdShopEntity.getName();
                    thirdAddDTO.setThirdInfoId(thirdShopEntity.getId());
                    thirdAddDTO.setThirdCode(thirdShopEntity.getCode());
                    break;
                case VIRTUAL_WAREHOUSE:
                    //校验第三方仓库是否存在
                    ThirdWarehouseEntity thirdWarehouseEntity = Optional.ofNullable(thirdWarehouseService.getByWarehouseId(thirdAddDTO.getThirdId(), ThirdSysTypeEnum.VIRTUAL_WAREHOUSE.getCode()))
                            .orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_WAREHOUSE_NOTFOUND));
                    thirdName = thirdWarehouseEntity.getName();
                    thirdAddDTO.setThirdInfoId(thirdWarehouseEntity.getId());
                    thirdAddDTO.setThirdCode(thirdWarehouseEntity.getCode());
                    break;
                case LOGISTICS:
                    ThirdLogisticsEntity thirdLogisticsEntity = Optional.ofNullable(thirdLogisticsService.getById(thirdAddDTO.getThirdId()))
                            .orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_LOGISTICS_NOTFOUND));
                    thirdName = thirdLogisticsEntity.getChannelName();
                    thirdAddDTO.setThirdInfoId(thirdLogisticsEntity.getId());
                    thirdAddDTO.setThirdCode(thirdLogisticsEntity.getChannelName());
                    break;
                default:
                    throw new ServiceException(ApiError.ERROR_400);
            }

            thirdAddDTO.setThirdName(thirdName);
            thirdAddDTO.setSysName(sysName);
            thirdAddDTO.setSysId(sysId);
            thirdAddDTO.setType(type);

            //校验平台信息和第三方信息一对一关系
            ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
            thirdMappingEntity.setType(thirdAddDTO.getType());
            thirdMappingEntity.setSysId(thirdAddDTO.getSysId());
            thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
            thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
            ThirdMappingEntity existSysMapping = getByTypeAndSysIdAndSysType(thirdMappingEntity);
            if (Objects.nonNull(existSysMapping)) {
                if (!Objects.equals(thirdAddDTO.getSysId(), existSysMapping.getSysId())) {
                    throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getType()), thirdName, existSysMapping.getSysName());
                } else {
                    thirdAddDTO.setId(existSysMapping.getId());
                }
            }
            ThirdMappingEntity existThirdMapping = getByTypeAndThirdId(thirdMappingEntity);
            if (Objects.nonNull(existThirdMapping) && ((Objects.nonNull(existSysMapping) && !Objects.equals(thirdAddDTO.getSysId(), existThirdMapping.getSysId())) || Objects.isNull(existSysMapping))) {
                throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdAddDTO.getType()), thirdName, existThirdMapping.getSysName());
            }
        });
    }

    @Override
    public List<ThirdMappingDTO.WarehouseMappingDTO> listMappingBySysIds(List<String> warehouseIdList, String sysType) {
        if(warehouseIdList.isEmpty()){
           return Collections.emptyList();
        }
        return this.baseMapper.listMappingBySysIds(warehouseIdList, sysType);
    }

    @Override
    public ThirdMappingEntity getByThirdCodeAndType(String warehouseNo, String sysType, String type) {
        return getOne(Wrappers.<ThirdMappingEntity>lambdaQuery()
                .eq(ThirdMappingEntity::getThirdCode, warehouseNo)
                .eq(ThirdMappingEntity::getThirdSysType, sysType)
                .eq(ThirdMappingEntity::getType, type)
                .last(" limit 1")
        );
    }


}
