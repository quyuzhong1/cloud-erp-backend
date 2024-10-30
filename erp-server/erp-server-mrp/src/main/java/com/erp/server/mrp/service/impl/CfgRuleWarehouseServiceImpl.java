package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleWarehouseDTO;
import com.erp.model.mrp.dto.CfgRuleWarehouseDetailDTO;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;
import com.erp.model.mrp.entity.CfgRuleWarehouseDetailEntity;
import com.erp.model.mrp.entity.CfgRuleWarehouseEntity;
import com.erp.model.mrp.enums.CfgRuleInventoryAllocateTypeEnum;
import com.erp.model.mrp.enums.CfgRuleWarehouseTypeEnum;
import com.erp.model.mrp.enums.PlatformMappingTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.rpc.wms.feign.WmsVirtualWarehouseFeign;
import com.erp.server.mrp.mapper.CfgRuleWarehouseMapper;
import com.erp.server.mrp.service.CfgPlatformMappingService;
import com.erp.server.mrp.service.CfgRuleWarehouseDetailService;
import com.erp.server.mrp.service.CfgRuleWarehouseService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 仓库（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-24
 */
@Slf4j
@Service
public class CfgRuleWarehouseServiceImpl extends SuperServiceImpl<CfgRuleWarehouseMapper, CfgRuleWarehouseEntity> implements CfgRuleWarehouseService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleWarehouseDetailService cfgRuleWarehouseDetailService;

    @Autowired
    private CfgPlatformMappingService cfgPlatformMappingService;
    
    @Autowired
    private WmsVirtualWarehouseFeign wmsVirtualWarehouseFeign;

    @Autowired
    private CfgRuleWarehouseService cfgRuleWarehouseService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(cacheNames = "cache:mrp:getWarehouse", allEntries = true, beforeInvocation = true)
    public Boolean update(CfgRuleWarehouseDTO.UpdateDTO updateDTO) {
        CfgRuleWarehouseEntity cfgRuleWarehouseEntity =  BeanMapperUtils.map(CfgRuleWarehouseEntity.class, updateDTO);
        //旧数据
        CfgRuleWarehouseEntity old = this.getByPlatformType(updateDTO.getPlatformType());
        if (ObjectUtil.isNotEmpty(old)) {
            cfgRuleWarehouseEntity.setId(old.getId());
        }

        // 数据处理
        handleData(cfgRuleWarehouseEntity);

        boolean save = super.saveOrUpdate(cfgRuleWarehouseEntity);
        if(!save) {
            throw new ServiceException("仓库（规则设置）保存失败");
        }

        //添加本地仓设置
        cfgRuleWarehouseDetailService.update(updateDTO.getCfgLocalWarehouseList(),cfgRuleWarehouseEntity.getId(), CfgRuleWarehouseTypeEnum.LOCAL.getCode(),Boolean.FALSE);

        //本地虚拟仓设置
        cfgRuleWarehouseDetailService.update(updateDTO.getCfgLocalVirtualWarehouseList(),cfgRuleWarehouseEntity.getId(), CfgRuleWarehouseTypeEnum.LOCAL.getCode(),Boolean.TRUE);

        //添加海外仓设置
        cfgRuleWarehouseDetailService.update(updateDTO.getCfgOverseasWarehouseList(),cfgRuleWarehouseEntity.getId(),CfgRuleWarehouseTypeEnum.OVERSEAS.getCode(),Boolean.FALSE);

        // 记录主单操作日志
        log.info("编辑 开始记录仓库（规则设置）日志数据，id：【{}】", cfgRuleWarehouseEntity.getId());
        operateLogService.addModuleOperateLogByObj(old, cfgRuleWarehouseEntity, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), cfgRuleWarehouseEntity.getId(), "");
        return Boolean.TRUE;
    }

    @Override
    public CfgRuleWarehouseDTO.ViewDTO view(String platformType) {
        CfgRuleWarehouseDTO.ViewDTO viewDTO = new CfgRuleWarehouseDTO.ViewDTO();
        CfgRuleWarehouseEntity oldEntity = this.getByPlatformType(platformType);
        if (ObjectUtil.isEmpty(oldEntity)) {
            return viewDTO;
        }
        BeanMapperUtils.copy(oldEntity,viewDTO);
        //仓库设置明细
        List<CfgRuleWarehouseDetailDTO.ViewDTO> cfgRuleWarehouseDetailList = cfgRuleWarehouseDetailService.listViewByMainIdList(Arrays.asList(oldEntity.getId()));
        //本地仓设置(实体仓数据)
        List<CfgRuleWarehouseDetailDTO.ViewDTO> localWarehouseList = cfgRuleWarehouseDetailList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseType(), CfgRuleWarehouseTypeEnum.LOCAL.getCode()) && StrUtil.isBlank(obj.getVirtualWarehouseId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(localWarehouseList)) {
            viewDTO.setCfgLocalWarehouseList(localWarehouseList);
        }
        //本地仓设置(虚拟仓数据)
        List<CfgRuleWarehouseDetailDTO.ViewDTO> localVirtualWarehouseList = cfgRuleWarehouseDetailList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseType(), CfgRuleWarehouseTypeEnum.LOCAL.getCode()) && StrUtil.isNotBlank(obj.getVirtualWarehouseId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(localVirtualWarehouseList)) {
            viewDTO.setCfgLocalVirtualWarehouseList(localVirtualWarehouseList);
        }

        //海外仓设置
        List<CfgRuleWarehouseDetailDTO.ViewDTO> cfgOverseasWarehouseList = cfgRuleWarehouseDetailList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseType(), CfgRuleWarehouseTypeEnum.OVERSEAS.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(cfgOverseasWarehouseList)) {
            viewDTO.setCfgOverseasWarehouseList(cfgOverseasWarehouseList);
        }
        handleCfgRuleWarehouseView(viewDTO);
        return viewDTO;
    }


    /**
     * 根据平台类型查询
     * @author will
     * @date 2024/8/24 15:50
     * @param platformType
     * @return CfgRuleWarehouseEntity
     */
    @Override
    public CfgRuleWarehouseEntity getByPlatformType (String platformType) {
        return lambdaQuery().eq(CfgRuleWarehouseEntity::getPlatformType,platformType)
                .last("limit 1")
                .one();
    }

    @Override
    @CacheEvict(cacheNames = "cache:mrp:getWarehouse", allEntries = true, beforeInvocation = true)
    public void refreshVirtual(String platformType) {
        CfgRuleWarehouseEntity ruleWarehouseEntity = getByPlatformType(platformType);
        if (ObjectUtil.isEmpty(ruleWarehouseEntity)) {
            throw new ServiceException("暂无仓库配置项，请先保存后配置虚拟仓");
        }
        List<CfgPlatformMappingEntity> platformMappingList = cfgPlatformMappingService.listByPlatformType(PlatformMappingTypeEnum.getByPlatformType(platformType));
        if (CollectionUtils.isEmpty(platformMappingList)) {
            throw new ServiceException("平台映射表未配置，不支持配置虚拟仓");
        }
        List<String> platformList = platformMappingList.stream().map(CfgPlatformMappingEntity::getPlatform).distinct().collect(Collectors.toList());
        List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO> list =  wmsVirtualWarehouseFeign.listCfgRuleVirtualWarehouse (platformList);
        List<CfgRuleWarehouseDetailDTO.UpdateDTO> cfgLocalWarehouseList = handleRefreshVirtual(list);
        cfgRuleWarehouseDetailService.update(cfgLocalWarehouseList,ruleWarehouseEntity.getId(),CfgRuleWarehouseTypeEnum.LOCAL.getCode(),Boolean.TRUE);
    }

    @Override
    public Boolean getIsEnableOverseas(String platformType) {
        //是否存在海外仓
        CfgRuleWarehouseEntity cfgRuleWarehouseEntity = this.getByPlatformType(platformType);
        Boolean isEnableOverseas = Boolean.FALSE;
        if (ObjectUtil.isNotEmpty(cfgRuleWarehouseEntity) && cfgRuleWarehouseEntity.getIsEnableOverseas()) {
            isEnableOverseas = Boolean.TRUE;
        }
        return isEnableOverseas;
    }

    @Override
    public CfgRuleWarehouseDTO.WarehouseShopDTO checkShop(CfgRuleWarehouseDTO.UpdateDTO dto) {
        CfgRuleWarehouseDTO.WarehouseShopDTO resultDTO = new CfgRuleWarehouseDTO.WarehouseShopDTO();
        List<CfgPlatformMappingEntity> cfgPlatformMappingList = cfgPlatformMappingService.listByPlatformType(PlatformMappingTypeEnum.getByPlatformType(dto.getPlatformType()));
        if (CollectionUtils.isEmpty(cfgPlatformMappingList)) {
            return resultDTO;
        }
        List<String> platformList = cfgPlatformMappingList.stream().map(CfgPlatformMappingEntity::getPlatform).distinct().collect(Collectors.toList());
        //所有店铺
        List<ShopInfoEntity> list = FeignQuery.create(ShopInfoEntity.class).eq(ShopInfoEntity::getDisabled,Boolean.FALSE).in(ShopInfoEntity::getDictPlatform,platformList).list();
        if (CollectionUtils.isEmpty(list)) {
            return new CfgRuleWarehouseDTO.WarehouseShopDTO();
        }

        if (!dto.getIsEnableVirtual()) {
            List<String> shopNameList = handleCheckShop(dto.getCfgLocalWarehouseList(), list);
            if (CollectionUtils.isNotEmpty(shopNameList)) {
                resultDTO.setShopNameList(shopNameList);
            }
        }
        if (dto.getIsEnableVirtual()) {
            List<String> virtualShopNameList = handleCheckShop(dto.getCfgLocalVirtualWarehouseList(),list);
            if (CollectionUtils.isNotEmpty(virtualShopNameList)) {
                resultDTO.setVirtualShopNameList(virtualShopNameList);
            }
        }
        if (dto.getIsEnableOverseas()) {
            List<String> overseasShopNameList = handleCheckShop(dto.getCfgOverseasWarehouseList(),list);
            if (CollectionUtils.isNotEmpty(overseasShopNameList)) {
                resultDTO.setOverseasShopNameList(overseasShopNameList);
            }
        }
        return resultDTO;
    }

    @Override
    public List<CfgRuleWarehouseDetailDTO.OverseasWarehouseDTO> listOverseasWarehouse(String platformType) {
        CfgRuleWarehouseEntity oldEntity = this.getByPlatformType(platformType);
        if (ObjectUtil.isEmpty(oldEntity)) {
            return Collections.EMPTY_LIST;
        }
        //仓库设置明细
        List<CfgRuleWarehouseDetailDTO.ViewDTO> cfgRuleWarehouseDetailList = cfgRuleWarehouseDetailService.listViewByMainIdList(Arrays.asList(oldEntity.getId()));
        //海外仓设置
        List<CfgRuleWarehouseDetailDTO.OverseasWarehouseDTO> cfgOverseasWarehouseList = cfgRuleWarehouseDetailList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseType(), CfgRuleWarehouseTypeEnum.OVERSEAS.getCode()))
                .map(obj -> new CfgRuleWarehouseDetailDTO.OverseasWarehouseDTO(obj.getWarehouseId(),obj.getWarehouseName())).distinct().collect(Collectors.toList());
        return cfgOverseasWarehouseList;
    }

    /**
     * 处理验证店铺数据
     * @author will
     * @date 2024/9/13 16:04
     * @param cfgList
     * @param shopList
     * @return List<String>
     */
    private List<String> handleCheckShop (List<CfgRuleWarehouseDetailDTO.UpdateDTO> cfgList,List<ShopInfoEntity> shopList) {
        if (CollectionUtils.isEmpty(cfgList)) {
            return Collections.EMPTY_LIST;
        }
        List<String> shopIdList = new ArrayList<>();
        for (CfgRuleWarehouseDetailDTO.UpdateDTO warehouseDTO : cfgList) {
            if (StrUtil.equals(VitualWarehouseChannelTypeEnum.SHOP.getCode(),warehouseDTO.getChannelType())) {
                //按店铺
                shopIdList.addAll(warehouseDTO.getChannelIdList());
            } else {
                //按平台
                List<String> platformShopIdList = shopList.stream().filter(obj -> StrUtil.equals(obj.getDictPlatform(), warehouseDTO.getDictPlatform())).map(ShopInfoEntity::getId).distinct().collect(Collectors.toList());
                shopIdList.addAll(platformShopIdList);
            }
        }
        List<String> shopNameList = shopList.stream().filter(obj -> !shopIdList.contains(obj.getId())).map(ShopInfoEntity::getName).distinct().collect(Collectors.toList());
        return  shopNameList;
    }

    /**
     * 虚拟仓数据刷新处理
     * @author will
     * @date 2024/9/3 19:34
     * @param list
     * @return List<UpdateDTO>
     */
    private List<CfgRuleWarehouseDetailDTO.UpdateDTO> handleRefreshVirtual(List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO> list) {
        List<CfgRuleWarehouseDetailDTO.UpdateDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        List<String> warehouseIdList = list.stream().map(VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> virtualWarehouseIdList = list.stream().map(VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<String> dictPlatformList = list.stream().map(VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO::getDictPlatform).distinct().collect(Collectors.toList());
        //虚拟仓数据
        List<CfgRuleWarehouseDetailEntity> virtualList = cfgRuleWarehouseDetailService.listRefreshVirtual(warehouseIdList, virtualWarehouseIdList, dictPlatformList, CfgRuleWarehouseTypeEnum.LOCAL.getCode());

        /**
         * 根据实体仓、虚拟仓、平台合并
         */
        Map<String, List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getWarehouseId().concat(obj.getVirtualWarehouseId()).concat(obj.getDictPlatform()).concat(obj.getType())));
        for (Map.Entry<String, List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO>> entry : map.entrySet()) {
            List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO> value = entry.getValue();
            VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO virtualWarehouseDTO = value.get(0);
            //库存分配类型
            String inventoryAllocateType = virtualList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), virtualWarehouseDTO.getWarehouseId())
                            && StrUtil.equals(obj.getVirtualWarehouseId(), virtualWarehouseDTO.getVirtualWarehouseId())
                            && StrUtil.equals(obj.getDictPlatform(), virtualWarehouseDTO.getDictPlatform()))
                    .map(CfgRuleWarehouseDetailEntity::getInventoryAllocateType).findFirst().orElse("");

            CfgRuleWarehouseDetailDTO.UpdateDTO updateDTO = new CfgRuleWarehouseDetailDTO.UpdateDTO();
            updateDTO.setWarehouseId(virtualWarehouseDTO.getWarehouseId());
            updateDTO.setVirtualWarehouseId(virtualWarehouseDTO.getVirtualWarehouseId());
            updateDTO.setChannelType(virtualWarehouseDTO.getType());
            updateDTO.setDictPlatform(virtualWarehouseDTO.getDictPlatform());
            updateDTO.setInventoryAllocateType(StrUtil.isBlank(inventoryAllocateType) ? CfgRuleInventoryAllocateTypeEnum.AUTO_ALLOCATION.getCode() : inventoryAllocateType);
            //店铺数据
            List<String> relationIdList = value.stream().filter(obj -> CollectionUtils.isNotEmpty(obj.getRelationIdList())).flatMap(obj -> Stream.of(obj.getRelationIdList().stream().toArray(String[]::new))).distinct().collect(Collectors.toList());
            updateDTO.setChannelIdList(relationIdList);
            resultList.add(updateDTO);
        }
        return resultList;
    }

    /**
     * 处理仓库配置查看数据
     * @author will
     * @date 2024/8/24 16:26
     * @param viewDTO
     */
    private void handleCfgRuleWarehouseView (CfgRuleWarehouseDTO.ViewDTO viewDTO) {

    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleWarehouseEntity cfgRuleWarehouseEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
