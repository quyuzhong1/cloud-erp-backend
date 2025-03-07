package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleWarehouseDTO;
import com.erp.model.mrp.dto.CfgRuleWarehouseDetailDTO;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;
import com.erp.model.mrp.entity.CfgRuleWarehouseEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CfgRuleWarehouseTypeEnum;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CfgRuleWarehouseDetailService cfgRuleWarehouseDetailService;

    @Resource
    private CfgPlatformMappingService cfgPlatformMappingService;
    
    @Resource
    private WmsVirtualWarehouseFeign wmsVirtualWarehouseFeign;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleWarehouseDTO.UpdateDTO updateDTO) {
        CfgRuleWarehouseEntity cfgRuleWarehouseEntity =  BeanMapperUtils.map(CfgRuleWarehouseEntity.class, updateDTO);
        //旧数据
        CfgRuleWarehouseEntity old = this.getOne(Wrappers.emptyWrapper());
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
        cfgRuleWarehouseDetailService.update(getWarehouseList(updateDTO.getCfgLocalWarehouseList()), cfgRuleWarehouseEntity.getId(), CfgRuleWarehouseTypeEnum.LOCAL.getCode(),Boolean.FALSE);

        //本地虚拟仓设置
        cfgRuleWarehouseDetailService.update(updateDTO.getCfgLocalVirtualWarehouseList(),cfgRuleWarehouseEntity.getId(), CfgRuleWarehouseTypeEnum.LOCAL.getCode(),Boolean.TRUE);

        //添加海外仓设置
        cfgRuleWarehouseDetailService.update(getWarehouseList(updateDTO.getCfgOverseasWarehouseList()), cfgRuleWarehouseEntity.getId(),CfgRuleWarehouseTypeEnum.OVERSEAS.getCode(),Boolean.FALSE);

        // 记录主单操作日志
        log.info("编辑 开始记录仓库（规则设置）日志数据，id：【{}】", cfgRuleWarehouseEntity.getId());
        operateLogService.addModuleOperateLogByObj(ObjectUtil.isEmpty(old) ? new CfgRuleWarehouseEntity() : old, cfgRuleWarehouseEntity, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), cfgRuleWarehouseEntity.getId(), "");
        return Boolean.TRUE;
    }

    /**
     * 拍平数据
     * @param cfgWarehouseList 参数
     */
    private static List<CfgRuleWarehouseDetailDTO.UpdateDTO> getWarehouseList(List<CfgRuleWarehouseDetailDTO.WarehouseUpdateDTO> cfgWarehouseList) {
        return cfgWarehouseList.stream()
                .map(v -> v.getWarehousePlatform().stream()
                        .map(e -> {
                            CfgRuleWarehouseDetailDTO.UpdateDTO dto = new CfgRuleWarehouseDetailDTO.UpdateDTO();
                            dto.setWarehouseId(v.getWarehouseId());
                            dto.setChannelType(e.getChannelType());
                            if (VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(e.getChannelType())) {
                                dto.setChannelIdList(e.getPlatformList());
                            } else {
                                dto.setChannelIdList(e.getShopIdList());
                                dto.setDictPlatform(e.getPlatformList().stream().findFirst().orElse(null));
                            }
                            return dto;
                        }).collect(Collectors.toList())).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public CfgRuleWarehouseDTO.ViewDTO view() {
        CfgRuleWarehouseDTO.ViewDTO viewDTO = new CfgRuleWarehouseDTO.ViewDTO();
        CfgRuleWarehouseEntity oldEntity = this.getOne(Wrappers.emptyWrapper());
        if (ObjectUtil.isEmpty(oldEntity)) {
            return viewDTO;
        }
        BeanMapperUtils.copy(oldEntity,viewDTO);
        //仓库设置明细
        List<CfgRuleWarehouseDetailDTO.ViewDTO> cfgRuleWarehouseDetailList = cfgRuleWarehouseDetailService.listViewByMainIdList(Collections.singletonList(oldEntity.getId()));
        //本地仓设置(实体仓数据)
        List<CfgRuleWarehouseDetailDTO.ViewDTO> localWarehouseList = cfgRuleWarehouseDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseType(), CfgRuleWarehouseTypeEnum.LOCAL.getCode()) && CharSequenceUtil.isBlank(obj.getVirtualWarehouseId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(localWarehouseList)) {
            viewDTO.setCfgLocalWarehouseList(getWarehouseView(localWarehouseList));
        }
        //本地仓设置(虚拟仓数据)
        List<CfgRuleWarehouseDetailDTO.ViewDTO> localVirtualWarehouseList = cfgRuleWarehouseDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseType(), CfgRuleWarehouseTypeEnum.LOCAL.getCode()) && CharSequenceUtil.isNotBlank(obj.getVirtualWarehouseId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(localVirtualWarehouseList)) {
            viewDTO.setCfgLocalVirtualWarehouseList(localVirtualWarehouseList);
        }

        //海外仓设置
        List<CfgRuleWarehouseDetailDTO.ViewDTO> cfgOverseasWarehouseList = cfgRuleWarehouseDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseType(), CfgRuleWarehouseTypeEnum.OVERSEAS.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(cfgOverseasWarehouseList)) {
            viewDTO.setCfgOverseasWarehouseList(getWarehouseView(cfgOverseasWarehouseList));
        }
        handleCfgRuleWarehouseView(viewDTO);
        return viewDTO;
    }

    /**
     * 组装显示数据
     * @param warehouseList 仓库
     */
    private List<CfgRuleWarehouseDetailDTO.WarehouseViewDTO> getWarehouseView(List<CfgRuleWarehouseDetailDTO.ViewDTO> warehouseList) {
        return warehouseList.stream()
                .collect(Collectors.groupingBy(v -> new CfgRuleWarehouseDetailDTO.WarehouseGroupDTO(v.getWarehouseId(), v.getWarehouseName()))).entrySet().stream()
                .map(entry -> {
                    CfgRuleWarehouseDetailDTO.WarehouseViewDTO warehouseViewDTO = new CfgRuleWarehouseDetailDTO.WarehouseViewDTO();
                    warehouseViewDTO.setWarehouseId(entry.getKey().getWarehouseId());
                    warehouseViewDTO.setWarehouseName(entry.getKey().getWarehouseName());
                    List<CfgRuleWarehouseDetailDTO.WarehousePlatformDTO> platformList = new ArrayList<>();
                    boolean isAllPlatform = entry.getValue().stream().allMatch(v -> VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(v.getChannelType()) && CollectionUtils.isEmpty(v.getChannelIdList()));
                    if (isAllPlatform) {
                        CfgRuleWarehouseDetailDTO.WarehousePlatformDTO platformDTO = new CfgRuleWarehouseDetailDTO.WarehousePlatformDTO();
                        platformDTO.setChannelType(VitualWarehouseChannelTypeEnum.PLATFORM.getCode());
                        platformDTO.setShopIdList(Collections.singletonList(""));
                        platformDTO.setPlatformList(Collections.singletonList(""));
                        platformList.add(platformDTO);
                    } else {
                        for (CfgRuleWarehouseDetailDTO.ViewDTO dto : entry.getValue()) {
                            CfgRuleWarehouseDetailDTO.WarehousePlatformDTO platformDTO = new CfgRuleWarehouseDetailDTO.WarehousePlatformDTO();
                            if (VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(dto.getChannelType())) {
                                platformDTO.setChannelType(VitualWarehouseChannelTypeEnum.PLATFORM.getCode());
                                platformDTO.setPlatformList(dto.getChannelIdList());
                                platformDTO.setShopIdList(Collections.singletonList(""));
                            } else {
                                platformDTO.setShopIdList(dto.getChannelIdList());
                                platformDTO.setPlatformList(Collections.singletonList(dto.getDictPlatform()));
                                platformDTO.setChannelType(VitualWarehouseChannelTypeEnum.SHOP.getCode());
                            }
                            platformList.add(platformDTO);
                        }
                    }
                    warehouseViewDTO.setWarehousePlatform(platformList);
                    return warehouseViewDTO;
                })
                .collect(Collectors.toList());
    }


    @Override
    public void refreshVirtual() {
        CfgRuleWarehouseEntity ruleWarehouseEntity = getOne(Wrappers.emptyWrapper());
        if (ObjectUtil.isEmpty(ruleWarehouseEntity)) {
            throw new ServiceException("暂无仓库配置项，请先保存后配置虚拟仓");
        }
        List<CfgPlatformMappingEntity> platformMappingList = cfgPlatformMappingService.listByEffective();
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
        //FBA默认关闭
        return CfgRulePlatformTypeEnum.AMAZON.getCode().equals(platformType);
    }

    @Override
    public CfgRuleWarehouseDTO.WarehouseShopDTO checkShop(CfgRuleWarehouseDTO.UpdateDTO dto) {
        CfgRuleWarehouseDTO.WarehouseShopDTO resultDTO = new CfgRuleWarehouseDTO.WarehouseShopDTO();
        List<CfgPlatformMappingEntity> cfgPlatformMappingList = cfgPlatformMappingService.listByEffective();
        if (CollectionUtils.isEmpty(cfgPlatformMappingList)) {
            return resultDTO;
        }
        List<String> platformList = cfgPlatformMappingList.stream().map(CfgPlatformMappingEntity::getPlatform).distinct().collect(Collectors.toList());
        //所有店铺
        List<ShopInfoEntity> list = FeignQuery.create(ShopInfoEntity.class).eq(ShopInfoEntity::getDisabled,Boolean.FALSE).in(ShopInfoEntity::getDictPlatform,platformList).list();
        if (CollectionUtils.isEmpty(list)) {
            return new CfgRuleWarehouseDTO.WarehouseShopDTO();
        }
        if (Boolean.FALSE.equals(dto.getIsEnableVirtual())) {
            List<String> shopNameList = handleCheckShop(dto.getCfgLocalWarehouseList(), list);
            if (CollectionUtils.isNotEmpty(shopNameList)) {
                resultDTO.setShopNameList(shopNameList);
            }
        }

        if (Boolean.TRUE.equals(dto.getIsEnableVirtual())) {
            List<String> virtualShopNameList = handleCheckVirtualShop(dto.getCfgLocalVirtualWarehouseList(),list);
            if (CollectionUtils.isNotEmpty(virtualShopNameList)) {
                resultDTO.setVirtualShopNameList(virtualShopNameList);
            }
        }
        //海外仓排除亚马逊
        List<ShopInfoEntity> shopInfoList = list.stream().filter(v -> !PlatformDictEnum.AMAZON.getCode().equals(v.getDictPlatform())).collect(Collectors.toList());
        List<String> overseasShopNameList = handleCheckShop(dto.getCfgOverseasWarehouseList(), shopInfoList);
        if (CollectionUtils.isNotEmpty(overseasShopNameList)) {
            resultDTO.setOverseasShopNameList(overseasShopNameList);
        }
        return resultDTO;
    }

    @Override
    public List<CfgRuleWarehouseDetailDTO.OverseasWarehouseDTO> listOverseasWarehouse() {
        CfgRuleWarehouseEntity oldEntity = getOne(Wrappers.emptyWrapper());
        if (ObjectUtil.isEmpty(oldEntity)) {
            return Collections.emptyList();
        }
        //仓库设置明细
        List<CfgRuleWarehouseDetailDTO.ViewDTO> cfgRuleWarehouseDetailList = cfgRuleWarehouseDetailService.listViewByMainIdList(Collections.singletonList(oldEntity.getId()));
        //海外仓设置
        return cfgRuleWarehouseDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseType(), CfgRuleWarehouseTypeEnum.OVERSEAS.getCode()))
                .map(obj -> new CfgRuleWarehouseDetailDTO.OverseasWarehouseDTO(obj.getWarehouseId(),obj.getWarehouseName())).distinct().collect(Collectors.toList());
    }

    @Override
    public Boolean getIsEnableVirtual() {
        //是否存在海外仓
        CfgRuleWarehouseEntity cfgRuleWarehouseEntity = getOne(Wrappers.emptyWrapper());
        Boolean isEnableOverseas = Boolean.FALSE;
        if (ObjectUtil.isNotEmpty(cfgRuleWarehouseEntity) && Boolean.TRUE.equals(cfgRuleWarehouseEntity.getIsEnableVirtual())) {
            isEnableOverseas = Boolean.TRUE;
        }
        return isEnableOverseas;
    }

    /**
     * 处理验证店铺数据
     * @author will
     * @date 2024/9/13 16:04
     * @param cfgList
     * @param shopList
     * @return List<String>
     */
    private List<String> handleCheckVirtualShop (List<CfgRuleWarehouseDetailDTO.UpdateDTO> cfgList,List<ShopInfoEntity> shopList) {
        if (CollectionUtils.isEmpty(cfgList)) {
            return Collections.emptyList();
        }
        List<String> shopIdList = new ArrayList<>();
        for (CfgRuleWarehouseDetailDTO.UpdateDTO warehouseDTO : cfgList) {
            if (CharSequenceUtil.equals(VitualWarehouseChannelTypeEnum.SHOP.getCode(),warehouseDTO.getChannelType())) {
                //按店铺
                shopIdList.addAll(warehouseDTO.getChannelIdList());
            } else {
                //按平台
                List<String> platformShopIdList = shopList.stream().filter(obj -> CharSequenceUtil.equals(obj.getDictPlatform(), warehouseDTO.getDictPlatform())).map(ShopInfoEntity::getId).distinct().collect(Collectors.toList());
                shopIdList.addAll(platformShopIdList);
            }
        }
        return shopList.stream().filter(obj -> !shopIdList.contains(obj.getId())).map(ShopInfoEntity::getName).distinct().collect(Collectors.toList());
    }


    /**
     * 处理验证店铺数据
     * @author will
     * @return List<String>
     */
    private List<String> handleCheckShop(List<CfgRuleWarehouseDetailDTO.WarehouseUpdateDTO> cfgLocalWarehouseList, List<ShopInfoEntity> shopList) {
        if (CollectionUtils.isEmpty(cfgLocalWarehouseList)) {
            return Collections.emptyList();
        }
        List<String> shopIdList = new ArrayList<>();
        for (CfgRuleWarehouseDetailDTO.WarehouseUpdateDTO updateDTO : cfgLocalWarehouseList) {
            for (CfgRuleWarehouseDetailDTO.WarehousePlatformDTO dto : updateDTO.getWarehousePlatform()) {
                if (CharSequenceUtil.equals(VitualWarehouseChannelTypeEnum.SHOP.getCode(), dto.getChannelType())) {
                    //按店铺
                    shopIdList.addAll(dto.getShopIdList());
                } else {
                    //按平台
                    List<String> platformShopIdList;
                    if (CollectionUtils.isNotEmpty(dto.getPlatformList()) && !"".equals(dto.getPlatformList().get(0))) {
                        platformShopIdList = shopList.stream().filter(obj -> dto.getPlatformList().contains(obj.getDictPlatform())).map(ShopInfoEntity::getId).distinct().collect(Collectors.toList());
                    } else {
                        platformShopIdList = shopList.stream().map(ShopInfoEntity::getId).distinct().collect(Collectors.toList());
                    }
                    shopIdList.addAll(platformShopIdList);
                }
            }
        }
        return shopList.stream().filter(obj -> !shopIdList.contains(obj.getId())).map(ShopInfoEntity::getName).distinct().collect(Collectors.toList());
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
        /**
         * 根据实体仓、虚拟仓、平台合并
         */
        Map<String, List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getWarehouseId().concat(obj.getVirtualWarehouseId()).concat(obj.getDictPlatform()).concat(obj.getType())));
        for (Map.Entry<String, List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO>> entry : map.entrySet()) {
            List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO> value = entry.getValue();
            VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO virtualWarehouseDTO = value.get(0);
            CfgRuleWarehouseDetailDTO.UpdateDTO updateDTO = new CfgRuleWarehouseDetailDTO.UpdateDTO();
            updateDTO.setWarehouseId(virtualWarehouseDTO.getWarehouseId());
            updateDTO.setVirtualWarehouseId(virtualWarehouseDTO.getVirtualWarehouseId());
            updateDTO.setChannelType(virtualWarehouseDTO.getType());
            updateDTO.setDictPlatform(virtualWarehouseDTO.getDictPlatform());
            //店铺数据
            List<String> relationIdList = value.stream().map(VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO::getRelationIdList).filter(CollectionUtils::isNotEmpty).flatMap(List::stream).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            updateDTO.setChannelIdList(relationIdList);
            List<String> partitionIdList = value.stream().map(VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO::getPartitionIdList).filter(CollectionUtils::isNotEmpty).flatMap(List::stream).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            updateDTO.setPartitionIdList(partitionIdList);
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
        // 扩展
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleWarehouseEntity cfgRuleWarehouseEntity) {
    // 扩展
    }
}
