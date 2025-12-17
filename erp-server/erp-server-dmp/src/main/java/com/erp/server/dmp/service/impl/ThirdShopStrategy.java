package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO.ThirdAddDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.mapper.ThirdMappingMapper;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.ThirdMappingStrategy;
import com.erp.server.dmp.service.ThirdMappingService;
import com.erp.server.dmp.service.ThirdShopService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;

@Component
@Slf4j
public class ThirdShopStrategy implements ThirdMappingStrategy {

    @Resource
    private ThirdMappingService thirdMappingService;

    @Resource
    private ThirdShopService thirdShopService;

    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private ThirdMappingMapper thirdMappingMapper;
//    @Resource
//    private ThirdMappingStrategy thirdMappingStrategy;

    @Override
    public boolean supports(String type) {
        return ThirdSysTypeEnum.SHOP.getCode().equals(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO addDTO) {
        //校验系统店铺是否存在
        ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(addDTO.getSysId());
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, ThirdSysTypeEnum.getNameByCode(addDTO.getType()));
        }
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
                    makeThirdMappingDto(addDTO, item, null);
                }
            } else {
                List<ThirdMappingDTO.ThirdAddDTO> resultUpdatedList = new ArrayList<>();
                //判断当前平台是否绑定第三方数据,已绑定的更新，未绑定的删除
                checkSysBinding(addDTO, existMappingList, thirdList, null,resultUpdatedList);
                //保存新增数据
//                saveAddDto(addDTO, thirdList, resultUpdatedList);
                thirdList.forEach(thirdAddDTO -> {
                    ThirdMappingDTO.ThirdAddDTO updatedDto = resultUpdatedList.stream().filter(item -> Objects.equals(item.getSysType(), thirdAddDTO.getSysType())).findFirst().orElse(null);
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
        //获取系统店铺
        ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(sysId);
        if (Objects.isNull(shopInfo)) {
            return mappingViewDTO;
        }
        makeShopViewDto(viewParamDTO, sysId, mappingViewDTO, shopInfo);
        return mappingViewDTO;
    }

    @Override
    public void saveOrDeleteFeignBind(ThirdMappingEntity existMapping, String warehouseId, String warehouseCode, String warehouseName, boolean disabled) {
        return;
    }

    private void makeShopViewDto(ThirdMappingDTO.ViewParamDTO viewParamDTO, String sysId, ThirdMappingDTO.MappingViewDTO mappingViewDTO, ShopInfoEntity shopInfo) {
        //获取第三方数据信息
        List<ThirdMappingDTO.ViewDTO> viewDTOList = new ArrayList<>();
        List<ThirdMappingEntity> thirdMappingEntityList = thirdMappingService.getList(viewParamDTO.getType(), sysId);

        getViewVo(mappingViewDTO, shopInfo, thirdShopService, viewDTOList, thirdMappingEntityList);
    }

    static void getViewVo(ThirdMappingDTO.MappingViewDTO mappingViewDTO, ShopInfoEntity shopInfo, ThirdShopService thirdShopService, List<ThirdMappingDTO.ViewDTO> viewDTOList, List<ThirdMappingEntity> thirdMappingEntityList) {
        for (ThirdMappingEntity thirdMappingEntity : thirdMappingEntityList) {
            ThirdMappingDTO.ViewDTO viewDTO = new ThirdMappingDTO.ViewDTO();
            ThirdShopEntity thirdShopEntity = thirdShopService.getByShopId(thirdMappingEntity.getThirdId());
            if (Objects.isNull(thirdShopEntity)) {
                viewDTOList.add(viewDTO);
                continue;
            }
            viewDTO.setName(thirdShopEntity.getName());
            viewDTO.setThirdId(thirdMappingEntity.getThirdId());
            viewDTO.setThirdName(thirdMappingEntity.getThirdName());
            viewDTO.setCode(thirdShopEntity.getCode());
            viewDTO.setId(thirdMappingEntity.getId());
            viewDTO.setSysType(thirdMappingEntity.getThirdSysType());
            viewDTO.setSysTypeName(EnumMessage.getNameByCode(PlatformDictEnum.class, thirdMappingEntity.getThirdSysType()));
            viewDTOList.add(viewDTO);
        }
        mappingViewDTO.setSalesOrgId(shopInfo.getSalesOrgId());
        mappingViewDTO.setSalesOrgName(shopInfo.getSalesOrgName());
        mappingViewDTO.setSysName(shopInfo.getName());
        mappingViewDTO.setSysId(shopInfo.getId());
        mappingViewDTO.setThirdList(viewDTOList);
    }

//    /**
//     * 保存新增数据
//     *
//     * @param addDTO
//     * @param thirdList
//     * @param resultUpdatedList
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
//    public void saveAddDto(ThirdMappingDTO.AddDTO addDTO, List<ThirdMappingDTO.ThirdAddDTO> thirdList, List<ThirdMappingDTO.ThirdAddDTO> resultUpdatedList) {
//        thirdList.forEach(thirdAddDTO -> {
//            if (CollectionUtils.isNotEmpty(resultUpdatedList)) {
//                ThirdMappingDTO.ThirdAddDTO updatedDto = resultUpdatedList.stream().filter(item -> Objects.equals(item.getSysType(), thirdAddDTO.getSysType())).findFirst().orElse(null);
//                if (Objects.isNull(updatedDto)) {
//                    makeThirdMappingDto(addDTO, thirdAddDTO);
//                }
//            } else {
//                makeThirdMappingDto(addDTO, thirdAddDTO);
//            }
//        });
//    }

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
    public void checkSysBinding(ThirdMappingDTO.AddDTO addDTO, List<ThirdMappingEntity> existMappingList, List<ThirdMappingDTO.ThirdAddDTO> thirdList,
                                WarehouseDTO.ListDTO warehouse,
                                 List<ThirdMappingDTO.ThirdAddDTO> resultUpdatedList) {
        existMappingList.forEach(existMapping -> {
            ThirdMappingDTO.ThirdAddDTO thirdAddDTO = thirdList.stream().filter(item ->
                    Objects.equals(item.getSysType(), existMapping.getThirdSysType())).findFirst().orElse(null);
            //如果新增的第三方类型数据在原始数据中不存在，删除原始数据
            if (Objects.isNull(thirdAddDTO)) {
                // 操作日志
                String msg = StrUtil.format("编辑了【{}】的店铺由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
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
            String msg = StrUtil.format("编辑了【{}】的店铺由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
                    existMapping.getThirdName(), "");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
        });
        thirdMappingMapper.deleteBatchIds(existMappingList.stream().map(ThirdMappingEntity::getId).collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void makeThirdMappingDto(ThirdMappingDTO.AddDTO addDTO, ThirdMappingDTO.ThirdAddDTO thirdAddDTO, WarehouseDTO.ListDTO warehouse) {
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
        String msg = StrUtil.format("编辑了【{}】的店铺由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, thirdMappingEntity.getThirdSysType()),
                Objects.isNull(existMapping) ? "" : existMapping.getThirdName(), thirdMappingEntity.getThirdName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), thirdMappingEntity.getSysId(), "编辑操作");
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(ThirdMappingEntity thirdMappingEntity) {
        //校验系统店铺是否存在
        ShopInfoEntity shopInfoEntity = Optional.ofNullable(shopInfoFeign.getShopInfoById(thirdMappingEntity.getSysId()))
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_92058));
        //校验第三方店铺是否存在
        ThirdShopEntity thirdShopEntity = thirdShopService.getByIdOpt(thirdMappingEntity.getThirdId())
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_THIRD_SHOP_NOTFOUND));
        String thirdName = thirdShopEntity.getName();
        String sysName = shopInfoEntity.getName();
        thirdMappingEntity.setThirdInfoId(thirdShopEntity.getShopId());
        thirdMappingEntity.setThirdCode(thirdShopEntity.getCode());
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


}
