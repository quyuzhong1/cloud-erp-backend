package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.ThirdMappingDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO addDTO) {
        List<ThirdMappingDTO.ThirdAddDTO> thirdList = addDTO.getThirdList();
        //如果第三方信息为空，删除绑定关系
        if (CollectionUtils.isEmpty(thirdList)) {
            //根据sysId获取所有绑定关系
            List<ThirdMappingEntity> existMappingList = baseMapper.selectList(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, addDTO.getType())
                    .eq(ThirdMappingEntity::getSysId, addDTO.getSysId())
                    .eq(ThirdMappingEntity::getIsDeleted, false).eq(ThirdMappingEntity::getIsExpire, false));
            if (CollectionUtils.isEmpty(existMappingList)) {
                return new BaseResultDTO.AddDTO();
            }
            existMappingList.forEach(existMapping -> {
                // 操作日志
                String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", ThirdSysTypeEnum.getNameByCode(existMapping.getThirdSysType()),
                        existMapping.getThirdName(), "");
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
            });
            baseMapper.deleteBatchIds(existMappingList.stream().map(ThirdMappingEntity::getId).collect(Collectors.toList()));
        } else {
            //同一个第三方平台只能绑定一个仓库
            checkSysTypeBind(thirdList);
            //查看当前平台sysId绑定的第三方信息
            List<ThirdMappingEntity> existMappingList = baseMapper.selectList(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, addDTO.getType())
                    .eq(ThirdMappingEntity::getSysId, addDTO.getSysId())
                    .eq(ThirdMappingEntity::getIsDeleted, false).eq(ThirdMappingEntity::getIsExpire, false));
            //如果当前平台没有绑定第三方数据，直接添加
            if (CollectionUtils.isEmpty(existMappingList)) {
                thirdList.forEach(item -> {
                    ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
                    BeanMapperUtils.copy(addDTO, thirdMappingEntity);
                    thirdMappingEntity.setThirdId(item.getThirdId());
                    thirdMappingEntity.setThirdSysType(item.getSysType());
                    ThirdMappingEntity existMapping = null;
                    addOrUpdate(thirdMappingEntity, existMapping);
                });
            } else {
                List<ThirdMappingDTO.ThirdAddDTO> resultUpdatedList = new ArrayList<>();
                //判断当前平台是否绑定第三方数据
                existMappingList.forEach(existMapping -> {
                    ThirdMappingDTO.ThirdAddDTO thirdAddDTO = thirdList.stream().filter(item -> Objects.equals(item.getSysType(), existMapping.getThirdSysType())).findFirst().orElse(null);
                    //如果新增的第三方类型数据在原始数据中不存在，删除原始数据
                    if (Objects.isNull(thirdAddDTO)) {
                        // 操作日志
                        String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", ThirdSysTypeEnum.getNameByCode(existMapping.getThirdSysType()),
                                existMapping.getThirdName(), "");
                        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
                        baseMapper.deleteById(existMapping.getId());
                    } else {
                        //如果新增的第三方类型数据在原始数据中存在，判断第三方数据是否绑定
                        //绑定则进行更新
//                        if (!Objects.equals(thirdAddDTO.getThirdId(), existMapping.getThirdId())) {
                        ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
                        BeanMapperUtils.copy(addDTO, thirdMappingEntity);
                        thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
                        thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
                        addOrUpdate(thirdMappingEntity, existMapping);
                        resultUpdatedList.add(thirdAddDTO);
//                        }
                    }
                });

                //保存新增数据
                thirdList.forEach(thirdAddDTO -> {
                    if (CollectionUtils.isNotEmpty(resultUpdatedList)) {
                        ThirdMappingDTO.ThirdAddDTO updatedDto = resultUpdatedList.stream().filter(item -> Objects.equals(item.getSysType(), thirdAddDTO.getSysType())).findFirst().orElse(null);
                        if (Objects.isNull(updatedDto)) {
                            ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
                            BeanMapperUtils.copy(addDTO, thirdMappingEntity);
                            thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
                            thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
                            ThirdMappingEntity existMapping = null;
                            addOrUpdate(thirdMappingEntity, existMapping);
                        }
                    } else {
                        ThirdMappingEntity thirdMappingEntity = new ThirdMappingEntity();
                        BeanMapperUtils.copy(addDTO, thirdMappingEntity);
                        thirdMappingEntity.setThirdId(thirdAddDTO.getThirdId());
                        thirdMappingEntity.setThirdSysType(thirdAddDTO.getSysType());
                        ThirdMappingEntity existMapping = null;
                        addOrUpdate(thirdMappingEntity, existMapping);
                    }
                });
            }
        }
        return new BaseResultDTO.AddDTO();
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
        String msg = StrUtil.format("编辑了【{}】的仓库由【{}】到【{}】", ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getThirdSysType()),
                Objects.isNull(existMapping) ? "" : existMapping.getThirdName(), thirdMappingEntity.getThirdName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), thirdMappingEntity.getSysId(), "编辑操作");
    }

    private static void checkSysTypeBind(List<ThirdMappingDTO.ThirdAddDTO> thirdList) {
        Map<String, List<ThirdMappingDTO.ThirdAddDTO>> listMap = thirdList.stream()
                .collect(groupingBy(
                        ThirdMappingDTO.ThirdAddDTO::getSysType,
                        collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    if (list.size() > 1) {
                                        throw new ServiceException(ApiError.ERROR_THIRD_SYS_TYPE_BINDING);
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
                viewDTO.setSysTypeName(ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getThirdSysType()));
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

            thirdMappingEntityList.forEach(thirdMappingEntity -> {
                ThirdMappingDTO.ViewDTO viewDTO = new ThirdMappingDTO.ViewDTO();
                ThirdWarehouseEntity thirdWarehouseEntity = thirdWarehouseService.getById(thirdMappingEntity.getThirdId());
                if (Objects.isNull(thirdWarehouseEntity)) {
                    viewDTOList.add(viewDTO);
                }
                viewDTO.setName(thirdWarehouseEntity.getName());
                viewDTO.setThirdId(thirdMappingEntity.getThirdId());
                viewDTO.setCode(thirdWarehouseEntity.getCode());
                viewDTO.setId(thirdMappingEntity.getId());
                viewDTO.setSysType(thirdMappingEntity.getThirdSysType());
                viewDTO.setSysTypeName(ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getThirdSysType()));
                viewDTOList.add(viewDTO);
            });
            mappingViewDTO.setOrgId(listDTOS.get(0).getOrgId());
            mappingViewDTO.setOrgName(listDTOS.get(0).getOrgName());
            mappingViewDTO.setSysName(listDTOS.get(0).getName());
            mappingViewDTO.setSysId(listDTOS.get(0).getId());
            mappingViewDTO.setThirdList(viewDTOList);
        }
        return mappingViewDTO;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(ThirdMappingEntity thirdMappingEntity) {
        String thirdName;
        String sysName;
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
            //校验第三方仓库是否存在
            ThirdWarehouseEntity thirdWarehouseEntity = thirdWarehouseService.getByIdOpt(thirdMappingEntity.getThirdId())
                    .orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_WAREHOUSE_NOTFOUND));
            thirdName = thirdWarehouseEntity.getName();
            sysName = listDTOS.get(0).getName();
            thirdMappingEntity.setThirdInfoId(thirdWarehouseEntity.getWarehouseId());
            thirdMappingEntity.setThirdCode(thirdWarehouseEntity.getCode());
        }
        thirdMappingEntity.setThirdName(thirdName);
        thirdMappingEntity.setSysName(sysName);

        //校验平台信息和第三方信息一对一关系
        ThirdMappingEntity existSysMapping = baseMapper.selectOne(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, thirdMappingEntity.getType())
                .eq(ThirdMappingEntity::getSysId, thirdMappingEntity.getSysId()).eq(ThirdMappingEntity::getThirdSysType, thirdMappingEntity.getThirdSysType())
                .eq(ThirdMappingEntity::getIsDeleted, false));
        if (Objects.nonNull(existSysMapping)) {
            if (!Objects.equals(thirdMappingEntity.getSysId(), existSysMapping.getSysId())) {
                throw new ServiceException(ApiError.ERROR_THIRD_BINDED, ThirdSysTypeEnum.getNameByCode(thirdMappingEntity.getType()), thirdName, sysName);
            } else {
                thirdMappingEntity.setId(existSysMapping.getId());
            }
        }
        ThirdMappingEntity existThirdMapping = baseMapper.selectOne(new LambdaQueryWrapper<ThirdMappingEntity>().eq(ThirdMappingEntity::getType, thirdMappingEntity.getType())
                .eq(ThirdMappingEntity::getThirdId, thirdMappingEntity.getThirdId()));
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
