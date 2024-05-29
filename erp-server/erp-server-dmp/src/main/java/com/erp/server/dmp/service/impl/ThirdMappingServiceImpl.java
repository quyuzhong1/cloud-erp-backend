package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.constant.EnumMessage;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.dmp.mapper.ThirdMappingMapper;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.ThirdMappingService;
import com.common.business.service.impl.SuperServiceImpl;
//import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.erp.server.dmp.service.ThirdShopService;
import com.erp.server.dmp.service.ThirdWarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.ThirdMappingDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
    private OverseasProviderFeign overseasProviderFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO addDTO) {
        List<ThirdMappingDTO.ThirdAddDTO> thirdList = addDTO.getThirdList();
        //如果第三方信息为空，删除绑定关系
        if (CollectionUtils.isEmpty(thirdList)) {
            //根据sysId获取所有绑定关系
            List<ThirdMappingEntity> existMappingList = baseMapper.selectList(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, addDTO.getType())
                    .eq(ThirdMappingEntity::getSysId, addDTO.getSysId())
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
            //同一个第三方平台只能绑定一个仓库
            checkSysTypeBind(thirdList, addDTO.getType());
            //查看当前平台sysId绑定的第三方信息
            List<ThirdMappingEntity> existMappingList = baseMapper.selectList(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, addDTO.getType())
                    .eq(ThirdMappingEntity::getSysId, addDTO.getSysId())
                    .eq(ThirdMappingEntity::getIsDeleted, false));
            //如果当前平台没有绑定第三方数据，直接添加
            if (CollectionUtils.isEmpty(existMappingList)) {
                thirdList.forEach(item -> {
                    makeThirdMappingDto(addDTO, item);
                });
            } else {
                List<ThirdMappingDTO.ThirdAddDTO> resultUpdatedList = new ArrayList<>();
                //判断当前平台是否绑定第三方数据
                existMappingList.forEach(existMapping -> {
                    ThirdMappingDTO.ThirdAddDTO thirdAddDTO = thirdList.stream().filter(item ->
                            Objects.equals(item.getSysType(), existMapping.getThirdSysType())).findFirst().orElse(null);
                    //如果新增的第三方类型数据在原始数据中不存在，删除原始数据
                    if (Objects.isNull(thirdAddDTO)) {
                        // 操作日志
                        String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
                                existMapping.getThirdName(), "");
                        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
                        baseMapper.deleteById(existMapping.getId());
                    } else {
                        //如果新增的第三方类型数据在原始数据中存在，判断第三方数据是否绑定
                        //绑定则进行更新
                        ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
                        BeanMapperUtils.copy(addDTO, thirdMappingEntity);
                        thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
                        thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
                        thirdMappingEntity.setThirdName(thirdAddDTO.getThirdName());
                        thirdMappingEntity.setSysName(thirdAddDTO.getSysName());
                        addOrUpdate(thirdMappingEntity, existMapping);
                        resultUpdatedList.add(thirdAddDTO);
                    }
                });

                //保存新增数据
                thirdList.forEach(thirdAddDTO -> {
                    if (CollectionUtils.isNotEmpty(resultUpdatedList)) {
                        ThirdMappingDTO.ThirdAddDTO updatedDto = resultUpdatedList.stream().filter(item -> Objects.equals(item.getSysType(), thirdAddDTO.getSysType())).findFirst().orElse(null);
                        if (Objects.isNull(updatedDto)) {
                            makeThirdMappingDto(addDTO, thirdAddDTO);
                        }
                    } else {
                        makeThirdMappingDto(addDTO, thirdAddDTO);
                    }
                });
            }
        }
        return new BaseResultDTO.AddDTO();
    }

    private void makeThirdMappingDto(ThirdMappingDTO.AddDTO addDTO, ThirdMappingDTO.ThirdAddDTO thirdAddDTO) {
        ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
        BeanMapperUtils.copy(addDTO, thirdMappingEntity);
        thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
        thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
        thirdMappingEntity.setThirdName(thirdAddDTO.getThirdName());
        thirdMappingEntity.setSysName(thirdAddDTO.getSysName());
        ThirdMappingEntity existMapping = null;
        addOrUpdate(thirdMappingEntity, existMapping);
    }

    private void addOrUpdate(ThirdMappingEntity thirdMappingEntity, ThirdMappingEntity existMapping) {
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
        }
        // 操作日志
        String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, thirdMappingEntity.getThirdSysType()),
                Objects.isNull(existMapping) ? "" : existMapping.getThirdName(), thirdMappingEntity.getThirdName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), thirdMappingEntity.getSysId(), "编辑操作");
    }

    private static void checkSysTypeBind(List<ThirdMappingDTO.ThirdAddDTO> thirdList, String type) {
        Map<String, List<ThirdMappingDTO.ThirdAddDTO>> listMap = thirdList.stream()
                .collect(groupingBy(
                        ThirdMappingDTO.ThirdAddDTO::getSysType,
                        collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    if (list.size() > 1) {
                                        throw new ServiceException(ApiError.ERROR_THIRD_SYS_TYPE_BINDING, EnumMessage.getNameByCode(PlatformDictEnum.class, type));
                                    }
                                    return list;
                                }
                        )
                ));
    }


    @Override
    public ThirdMappingDTO.MappingViewDTO view(ThirdMappingDTO.ViewParamDTO viewParamDTO) {
        ThirdMappingDTO.MappingViewDTO mappingViewDTO = new ThirdMappingDTO.MappingViewDTO();
        String sysId = viewParamDTO.getSysId();
        if (ThirdSysTypeEnum.SHOP.getCode().equals(viewParamDTO.getType())) {
            //获取系统店铺
            ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(sysId);
            if (Objects.isNull(shopInfo)) {
                return mappingViewDTO;
            }
            //获取第三方数据信息
            List<ThirdMappingDTO.ViewDTO> viewDTOList = new ArrayList<>();
            List<ThirdMappingEntity> thirdMappingEntityList = baseMapper.selectList(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getSysId, sysId)
                    .eq(ThirdMappingEntity::getType, viewParamDTO.getType()));

            thirdMappingEntityList.forEach(thirdMappingEntity -> {
                ThirdMappingDTO.ViewDTO viewDTO = new ThirdMappingDTO.ViewDTO();
                ThirdShopEntity thirdShopEntity = thirdShopService.getById(thirdMappingEntity.getThirdId());
                if (Objects.isNull(thirdShopEntity)) {
                    viewDTOList.add(viewDTO);
                }
                viewDTO.setName(thirdShopEntity.getName());
                viewDTO.setThirdId(thirdMappingEntity.getThirdId());
                viewDTO.setCode(thirdShopEntity.getCode());
                viewDTO.setId(thirdMappingEntity.getId());
                viewDTO.setSysType(thirdMappingEntity.getThirdSysType());
                viewDTO.setSysTypeName(EnumMessage.getNameByCode(PlatformDictEnum.class, thirdMappingEntity.getThirdSysType()));
                viewDTOList.add(viewDTO);
            });
            mappingViewDTO.setSalesOrgId(shopInfo.getSalesOrgId());
            mappingViewDTO.setSalesOrgName(shopInfo.getSalesOrgName());
            mappingViewDTO.setSysName(shopInfo.getName());
            mappingViewDTO.setSysId(shopInfo.getId());
            mappingViewDTO.setThirdList(viewDTOList);
        } else {
            //获取系统仓库
            List<WarehouseDTO.ListDTO> listDTOS =
                    Optional.ofNullable(wmsWarehouseFeign.listByIds(Collections.singletonList(sysId))).orElse(new ArrayList<>());
            if (CollectionUtils.isEmpty(listDTOS)) {
                return mappingViewDTO;
            }
            //获取第三方数据信息
            List<ThirdMappingDTO.ViewDTO> viewDTOList = new ArrayList<>();
            List<ThirdMappingEntity> thirdMappingEntityList = baseMapper.selectList(new LambdaQueryWrapper<ThirdMappingEntity>()
                    .eq(ThirdMappingEntity::getSysId, sysId)
                    .eq(ThirdMappingEntity::getType, viewParamDTO.getType()));

            for (ThirdMappingEntity thirdMappingEntity : thirdMappingEntityList) {
                ThirdMappingDTO.ViewDTO viewDTO = new ThirdMappingDTO.ViewDTO();

                if (PlatformDictEnum.WDT.getCode().equals(thirdMappingEntity.getThirdSysType())) {
                    ThirdWarehouseEntity thirdWarehouseEntity = thirdWarehouseService.getById(thirdMappingEntity.getThirdId());
                    if (Objects.isNull(thirdWarehouseEntity)) {
                        continue;
                    }
                    viewDTO.setName(thirdWarehouseEntity.getName());
                    viewDTO.setCode(thirdWarehouseEntity.getCode());
                }
                if (PlatformDictEnum.IML.getCode().equals(thirdMappingEntity.getThirdSysType()) || PlatformDictEnum.GOOD_CANG.getCode().equals(thirdMappingEntity.getThirdSysType())) {
                    //校验第三方仓库是否存在
                    OverseasProviderDTO.FeignDTO feignDTO = new OverseasProviderDTO.FeignDTO();
                    feignDTO.setCode(thirdMappingEntity.getThirdSysType());
                    feignDTO.setOverseasProviderWarehouseId(thirdMappingEntity.getThirdId());
                    OverseasProviderDTO.FeignDTO overseasWarehouse = overseasProviderFeign.getOverseasWarehouse(feignDTO);
                    if (Objects.isNull(overseasWarehouse)) {
                        continue;
                    }
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
        return mappingViewDTO;
    }

    @Override
    public Boolean getByThirdId(ThirdMappingDTO.ViewParamDTO viewParamDTO) {
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
            if (PlatformDictEnum.IML.getCode().equals(thirdMappingEntity.getThirdSysType()) || PlatformDictEnum.GOOD_CANG.getCode().equals(thirdMappingEntity.getThirdSysType())) {
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
//    @Resource
//    private CheckStrategy checkStrategy;
//    private void handleData1(ThirdMappingEntity thirdMappingEntity) {
//        String thirdName;
//        String sysName;
//        ThirdMappingEntity check = checkStrategy.getType(thirdMappingEntity.getType()).check(thirdMappingEntity);
//
//        if (ThirdSysTypeEnum.SHOP.getCode().equals(thirdMappingEntity.getType())) {
//            //校验系统店铺是否存在
//            ShopInfoEntity shopInfoEntity = Optional.ofNullable(shopInfoFeign.getShopInfoById(thirdMappingEntity.getSysId())).orElseThrow(() -> new ServiceException(ApiError.ERROR_92058));
//            //校验第三方店铺是否存在
//            ThirdShopEntity thirdShopEntity = thirdShopService.getByIdOpt(thirdMappingEntity.getThirdId()).orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_SHOP_NOTFOUND));
//            thirdName = thirdShopEntity.getName();
//            sysName = shopInfoEntity.getName();
//        } else {
//            //校验系统仓库是否存在
//            List<WarehouseDTO.ListDTO> listDTOS = Optional.ofNullable(wmsWarehouseFeign.listByIds(Collections.singletonList(thirdMappingEntity.getSysId()))).orElseThrow(() -> new ServiceException(ApiError.ERROR_92058));
//            //校验第三方仓库是否存在
//            ThirdWarehouseEntity thirdWarehouseEntity = thirdWarehouseService.getByIdOpt(thirdMappingEntity.getThirdId()).orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_WAREHOUSE_NOTFOUND));
//            thirdName = thirdWarehouseEntity.getName();
//            sysName = listDTOS.get(0).getName();
//        }
//        thirdMappingEntity.setThirdName(thirdName);
//        thirdMappingEntity.setSysName(sysName);
//
//        //校验仓库和第三方信息一对一关系
//        ThirdMappingEntity existSysMapping = baseMapper.selectOne(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, thirdMappingEntity.getType())
//                .eq(ThirdMappingEntity::getSysId, thirdMappingEntity.getSysId()).eq(ThirdMappingEntity::getIsDeleted, false));
//        if (Objects.nonNull(existSysMapping) && !Objects.equals(thirdMappingEntity.getSysId(), existSysMapping.getSysId())) {
//            throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getType()), thirdName, sysName);
//        }
//        ThirdMappingEntity existThirdMapping = baseMapper.selectOne(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, thirdMappingEntity.getType())
//                .eq(ThirdMappingEntity::getThirdId, thirdMappingEntity.getThirdId()));
//        if (Objects.nonNull(existThirdMapping)) {
//            if (!Objects.equals(thirdMappingEntity.getThirdId(), existSysMapping.getThirdId())) {
//                throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getType()), sysName, thirdName);
//            } else {
//                thirdMappingEntity.setId(existThirdMapping.getId());
//            }
//        }
//    }

}
