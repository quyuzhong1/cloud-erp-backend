package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingExtendDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.entity.SkuMappingExtendEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.WarehouseDeliveryTypeEnum;
import com.erp.model.wms.enums.WarehouseManageTypeEnum;
import com.erp.server.oms.mapper.SkuMappingExtendMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SkuMappingExtendService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.oms.service.DictBasicService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

/**
 * <p>
 * sku仓库发货配置 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-02-27
 */
@Slf4j
@Service
public class SkuMappingExtendServiceImpl extends SuperServiceImpl<SkuMappingExtendMapper, SkuMappingExtendEntity> implements SkuMappingExtendService {

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    public List<SkuMappingExtendEntity> findByMainIds(List<String> mainIds) {
        return this.lambdaQuery()
                .in(SkuMappingExtendEntity::getMainId, mainIds)
                .list();
    }

    @Override
    public Map<String, List<SkuMappingExtendDTO.ListDTO>> mapByMainIds(List<String> mainIds, boolean defaultNullThrow) {
        List<DictBasicDTO.ViewDTO> defaultConfigList = dictBasicService.getByKey(DictBasicEnum.SKU_MAPPING_DEFAULT_MANAGE_DELIVERY_TYPE.getKey());
        if (CollectionUtils.isEmpty(defaultConfigList) && defaultNullThrow){
            throw new ServiceException("默认SKU仓库发货配置缺失");
        }
        Map<String, List<SkuMappingExtendEntity>> entityMap = this.findByMainIds(mainIds)
                .stream()
                .collect(Collectors.groupingBy(SkuMappingExtendEntity::getMainId));

        Map<String, List<SkuMappingExtendDTO.ListDTO>> resultMap = new HashMap<>();

        for (String mainId : mainIds) {
            List<SkuMappingExtendEntity> cfgConfig = entityMap.get(mainId);
            List<SkuMappingExtendDTO.ListDTO> currentValue;
            if (CollectionUtils.isEmpty(cfgConfig)){
                 currentValue = defaultConfigList.stream().map(e -> new SkuMappingExtendDTO.ListDTO(null, e)).collect(Collectors.toList());
            } else{
                Map<String, SkuMappingExtendEntity> currentExistMap = cfgConfig.stream().collect(Collectors.toMap(SkuMappingExtendEntity::getWarehouseManageType, Function.identity()));
                 currentValue = defaultConfigList.stream().map(e -> {
                    SkuMappingExtendEntity entity = currentExistMap.get(e.getValue());
                    return new SkuMappingExtendDTO.ListDTO(entity, e);
                }).collect(Collectors.toList());
            }
            resultMap.put(mainId, currentValue);
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndSave(SkuMappingEntity entity, List<SkuMappingDTO.SkuMappingExtendListDTO> extendList) {
        if (CollectionUtils.isEmpty(extendList)){
            return;
        }
        // 查询历史配置
        List<SkuMappingExtendEntity> list = this.lambdaQuery()
                .eq(SkuMappingExtendEntity::getMainId, entity.getId())
                .list();

        // 查询默认配置
//        List<DictBasicDTO.ViewDTO> defaultConfigList = dictBasicService.getByKey(DictBasicEnum.SKU_MAPPING_DEFAULT_MANAGE_DELIVERY_TYPE.getKey());
//        if (CollectionUtils.isEmpty(defaultConfigList)){
//            throw new ServiceException("默认SKU仓库发货配置缺失");
//        }
        Map<String, SkuMappingExtendEntity> entityMap = list.stream().collect(Collectors.toMap(SkuMappingExtendEntity::getWarehouseManageType, Function.identity()));
        // 保存的列表
        List<SkuMappingExtendEntity> saveList = new ArrayList<>();
        // 更新的列表
        List<SkuMappingExtendEntity> updateList = new ArrayList<>();

        for (SkuMappingDTO.SkuMappingExtendListDTO dto : extendList) {
            // 是否已存在
            SkuMappingExtendEntity existEntity = entityMap.get(dto.getWarehouseManageType());
            String msg;
            String warehouseTypeName = WarehouseManageTypeEnum.getName(dto.getWarehouseManageType());
            String warehouseDeliveryTypeName = WarehouseDeliveryTypeEnum.getNameByCode(dto.getWarehouseDeliveryType());
            if (null != existEntity){
                existEntity.setDeliveryType(dto.getWarehouseDeliveryType());
                msg = StrUtil.format("用户【{}】新增仓库类型为【{}】发货配置为【{}】", UserContext.getDefaultLoginUser().getUserName(),warehouseTypeName,warehouseDeliveryTypeName);
                updateList.add(existEntity);
            } else {
                SkuMappingExtendEntity saveEntity = new SkuMappingExtendEntity(entity.getId(), dto.getWarehouseManageType(), dto.getWarehouseDeliveryType());
                msg = StrUtil.format("用户【{}】修改仓库类型为【{}】发货配置为【{}】", UserContext.getDefaultLoginUser().getUserName(),warehouseTypeName,warehouseDeliveryTypeName);
                saveList.add(saveEntity);
            }
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LISTING_INFO.getCode(), entity.getListingId(), "修改发货配置");
        }

        if (!CollectionUtils.isEmpty(saveList)){
            this.saveBatch(saveList);
        }
        if (!CollectionUtils.isEmpty(updateList)){
            this.updateBatchById(updateList);
        }
    }
}
