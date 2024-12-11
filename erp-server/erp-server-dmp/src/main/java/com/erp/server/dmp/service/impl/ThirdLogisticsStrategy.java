package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO.ThirdAddDTO;
import com.erp.model.dmp.entity.ThirdLogisticsEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.dmp.mapper.ThirdMappingMapper;
import com.erp.server.dmp.service.*;
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
public class ThirdLogisticsStrategy implements ThirdMappingStrategy {

    @Resource
    private ThirdMappingService thirdMappingService;

    @Resource
    private ThirdLogisticsService thirdLogisticsService;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private ThirdMappingMapper thirdMappingMapper;

    @Override
    public boolean supports(String type) {
        return ThirdSysTypeEnum.LOGISTICS.getCode().equals(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO addDTO) {
        //校验系统物流渠道是否存在
        LogisticsChannelDTO.BaseDTO baseDTO = logisticsFeign.getChannelInfoById(addDTO.getSysId());
        if (Objects.isNull(baseDTO)) {
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
        LogisticsChannelDTO.BaseDTO baseDTO = logisticsFeign.getChannelInfoById(sysId);
        if (Objects.isNull(baseDTO)) {
            return mappingViewDTO;
        }
        makeShopViewDto(viewParamDTO, sysId, mappingViewDTO, baseDTO);
        return mappingViewDTO;
    }

    @Override
    public void saveOrDeleteFeignBind(ThirdMappingEntity existMapping, String warehouseId, String warehouseCode, String warehouseName, boolean disabled) {
        return;
    }

    private void makeShopViewDto(ThirdMappingDTO.ViewParamDTO viewParamDTO, String sysId, ThirdMappingDTO.MappingViewDTO mappingViewDTO, LogisticsChannelDTO.BaseDTO baseDTO) {
        //获取第三方数据信息
        List<ThirdMappingDTO.ViewDTO> viewDTOList = new ArrayList<>();
        List<ThirdMappingEntity> thirdMappingEntityList = thirdMappingService.getList(viewParamDTO.getType(), sysId);

        getViewVo(mappingViewDTO, baseDTO, thirdLogisticsService, viewDTOList, thirdMappingEntityList);
    }

    static void getViewVo(ThirdMappingDTO.MappingViewDTO mappingViewDTO, LogisticsChannelDTO.BaseDTO baseDTO, ThirdLogisticsService thirdLogisticsService, List<ThirdMappingDTO.ViewDTO> viewDTOList, List<ThirdMappingEntity> thirdMappingEntityList) {
        for (ThirdMappingEntity thirdMappingEntity : thirdMappingEntityList) {
            ThirdMappingDTO.ViewDTO viewDTO = new ThirdMappingDTO.ViewDTO();
            ThirdLogisticsEntity thirdLogisticsEntity = thirdLogisticsService.getById(thirdMappingEntity.getThirdId());
            if (Objects.isNull(thirdLogisticsEntity)) {
                viewDTOList.add(viewDTO);
                continue;
            }
            viewDTO.setName(thirdLogisticsEntity.getLogisticsSupplierName());
            viewDTO.setThirdLogisticsId(thirdLogisticsEntity.getChannelId());
            viewDTO.setThirdLogisticsName(thirdLogisticsEntity.getChannelName());
            viewDTO.setThirdId(thirdMappingEntity.getThirdId());
            viewDTO.setId(thirdMappingEntity.getId());
            viewDTO.setSysType(thirdMappingEntity.getThirdSysType());
            viewDTO.setSysTypeName(EnumMessage.getNameByCode(PlatformDictEnum.class, thirdMappingEntity.getThirdSysType()));
            viewDTOList.add(viewDTO);
        }
        mappingViewDTO.setSysName(baseDTO.getName());
        mappingViewDTO.setSysId(baseDTO.getId());
        mappingViewDTO.setLogisticsType(baseDTO.getLogisticsType());
        mappingViewDTO.setLogisticsTypeName(baseDTO.getLogisticsTypeName());
        mappingViewDTO.setLogisticsSupplierId(baseDTO.getLogisticsSupplierId());
        mappingViewDTO.setLogisticsSupplierName(baseDTO.getLogisticsSupplierName());
        mappingViewDTO.setThirdList(viewDTOList);
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public void checkSysBinding(ThirdMappingDTO.AddDTO addDTO, List<ThirdMappingEntity> existMappingList, List<ThirdAddDTO> thirdList,
                                WarehouseDTO.ListDTO warehouse,
                                 List<ThirdAddDTO> resultUpdatedList) {
        existMappingList.forEach(existMapping -> {
            ThirdAddDTO thirdAddDTO = thirdList.stream().filter(item ->
                    Objects.equals(item.getSysType(), existMapping.getThirdSysType())).findFirst().orElse(null);
            //如果新增的第三方类型数据在原始数据中不存在，删除原始数据
            if (Objects.isNull(thirdAddDTO)) {
                // 操作日志
                String msg = StrUtil.format("编辑了【{}】的物流渠道由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public void deleteBinded(List<ThirdMappingEntity> existMappingList) {
        existMappingList.forEach(existMapping -> {
            // 操作日志
            String msg = StrUtil.format("编辑了【{}】的渠道由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, existMapping.getThirdSysType()),
                    existMapping.getThirdName(), "");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), existMapping.getSysId(), "编辑操作");
        });
        thirdMappingMapper.deleteBatchIds(existMappingList.stream().map(ThirdMappingEntity::getId).collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
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
        String msg = StrUtil.format("编辑了【{}】的物流渠道由【{}】到【{}】", EnumMessage.getNameByCode(PlatformDictEnum.class, thirdMappingEntity.getThirdSysType()),
                Objects.isNull(existMapping) ? "" : existMapping.getThirdName(), thirdMappingEntity.getThirdName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_THIRD_MAPPING.getCode(), thirdMappingEntity.getSysId(), "编辑操作");
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(ThirdMappingEntity thirdMappingEntity) {
        //校验系统物流渠道是否存在
        LogisticsChannelEntity logisticsChannelEntity = Optional.ofNullable(logisticsFeign.getChannelById(thirdMappingEntity.getSysId()))
                .orElseThrow(() -> new ServiceException("渠道不存在"));
        //校验第三方物流渠道是否存在
        ThirdLogisticsEntity thirdLogisticsEntity = thirdLogisticsService.getByIdOpt(thirdMappingEntity.getThirdId())
                .orElseThrow(() -> new ServiceException("第三方渠道不存在"));
        String thirdName = thirdLogisticsEntity.getChannelName();
        String sysName = logisticsChannelEntity.getName();
        thirdMappingEntity.setThirdInfoId(thirdLogisticsEntity.getChannelId());
        thirdMappingEntity.setThirdCode(thirdLogisticsEntity.getChannelName());
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
