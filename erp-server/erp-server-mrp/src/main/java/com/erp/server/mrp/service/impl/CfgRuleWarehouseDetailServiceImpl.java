package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleWarehouseDetailDTO;
import com.erp.model.mrp.entity.CfgRuleWarehouseDetailEntity;
import com.erp.model.mrp.entity.CfgRuleWarehouseEntity;
import com.erp.model.mrp.enums.CfgRuleInventoryAllocateTypeEnum;
import com.erp.model.mrp.enums.CfgRuleWarehouseTypeEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleWarehouseDetailMapper;
import com.erp.server.mrp.service.CfgRuleWarehouseDetailService;
import com.erp.server.mrp.service.CfgRuleWarehouseService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 仓库（规则设置）明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-24
 */
@Slf4j
@Service
public class CfgRuleWarehouseDetailServiceImpl extends SuperServiceImpl<CfgRuleWarehouseDetailMapper, CfgRuleWarehouseDetailEntity> implements CfgRuleWarehouseDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleWarehouseService cfgRuleWarehouseService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<CfgRuleWarehouseDetailDTO.UpdateDTO> cfgLocalWarehouseList,String mainId,String type,Boolean isVirtual) {
        if (CollectionUtils.isEmpty(cfgLocalWarehouseList)) {
            cfgLocalWarehouseList = Collections.EMPTY_LIST;
        }
        List<CfgRuleWarehouseDetailEntity> list = BeanMapperUtils.copyList(CfgRuleWarehouseDetailEntity.class, cfgLocalWarehouseList);
        //原仓库配置明细
        List<String> virtualWarehouseIdList = cfgLocalWarehouseList.stream().filter(obj -> StrUtil.isNotBlank(obj.getVirtualWarehouseId())).map(CfgRuleWarehouseDetailDTO.UpdateDTO::getVirtualWarehouseId).collect(Collectors.toList());
        List<CfgRuleWarehouseDetailEntity> oldList = listByWarehouseType(isVirtual,Collections.singletonList(mainId),type);

        //删除明细
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        if (CollectionUtils.isEmpty(list)) {
            return  Boolean.TRUE;
        }
        CfgRuleWarehouseEntity ruleWarehouseEntity = cfgRuleWarehouseService.getById(mainId);
        if (ObjectUtil.isEmpty(ruleWarehouseEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"仓库规则");
        }
        //仓库
        List<String> warehouseIdList = list.stream().map(CfgRuleWarehouseDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntityList = FeignQuery.getByIds(WarehouseEntity.class,warehouseIdList);

        // 数据处理
        handleData(list,mainId,type,warehouseEntityList);
        log.info("编辑 开始修改仓库（规则设置）明细数据，id：【{}】", mainId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("仓库（规则设置）明细保存失败");
        }
        //日志
        addOperateLog(list,warehouseEntityList,mainId,virtualWarehouseIdList);

        return Boolean.TRUE;
    }
    /**
     * 添加日志
     * @author will
     * @date 2024/9/12 12:01
     * @param list
     * @param warehouseEntityList
     * @param mainId
     * @param virtualWarehouseIdList
     */
    private void addOperateLog (List<CfgRuleWarehouseDetailEntity> list,List<WarehouseEntity> warehouseEntityList, String mainId,List<String> virtualWarehouseIdList) {
        //虚拟仓
        List<VirtualWarehouseEntity> virtualWarehouseList = FeignQuery.getByIds(VirtualWarehouseEntity.class, virtualWarehouseIdList);

        //店铺信息
        List<String> shopIdList = list.stream().filter(obj -> StrUtil.equals(obj.getChannelType(), VitualWarehouseChannelTypeEnum.SHOP.getCode()))
                .flatMap(obj -> Stream.of(obj.getChannelIdJson().stream().map(Object::toString).toArray(String[]::new))).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = CollectionUtils.isEmpty(shopIdList) ? Collections.EMPTY_LIST : FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);

       //销售平台
        List<DictBasicEntity> dictBasicList =  FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();

        Map<String, List<CfgRuleWarehouseDetailEntity>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getWarehouseType()));
        StringBuffer msg = new StringBuffer();
        //日志
        for (Map.Entry<String, List<CfgRuleWarehouseDetailEntity>> entry : map.entrySet()) {
            List<CfgRuleWarehouseDetailEntity> value = entry.getValue();
            if (CfgRuleWarehouseTypeEnum.LOCAL.getCode().equals(entry.getKey())) {
                if (StrUtil.isBlank(value.get(0).getVirtualWarehouseId())) {
                    msg.append("本地仓:<br>");
                } else {
                    msg.append("虚拟仓:<br>");
                }
            } else {
                msg.append("海外仓:<br>");
            }
            for (CfgRuleWarehouseDetailEntity detailEntity : value) {
                //实体仓
                String warehouseName = warehouseEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
                //虚拟仓
                String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
                //店铺
                String shopNames = StrUtil.equals(VitualWarehouseChannelTypeEnum.PLATFORM.getCode(),detailEntity.getChannelType()) ? "全部店铺":
                        shopInfoList.stream().filter(obj -> detailEntity.getChannelIdList().contains(obj.getId())).map(ShopInfoEntity::getName).distinct().collect(Collectors.joining(","));
                //平台
                String dictPlatformName = dictBasicList.stream().filter(obj -> StrUtil.equals(obj.getValue(), detailEntity.getDictPlatform())).map(DictBasicEntity::getName).findFirst().orElse("");

                if (CfgRuleWarehouseTypeEnum.LOCAL.getCode().equals(detailEntity.getWarehouseType())) {
                    if (StrUtil.isBlank(detailEntity.getVirtualWarehouseId())) {
                        msg.append(StrUtil.format("•实体仓【{}】、平台【{}】、店铺【{}】、库存分配【{}】<br>",  warehouseName,dictPlatformName,shopNames, CfgRuleInventoryAllocateTypeEnum.getName(detailEntity.getInventoryAllocateType())));
                    } else {
                        msg.append(StrUtil.format("•虚拟仓【{}】、关联实体仓【{}】、关联平台【{}】、关联店铺【{}】、库存分配【{}】<br>", virtualWarehouseName,warehouseName,dictPlatformName,shopNames, CfgRuleInventoryAllocateTypeEnum.getName(detailEntity.getInventoryAllocateType())));
                    }
                } else {
                    msg.append(StrUtil.format("•海外备货仓【{}】、平台【{}】、店铺【{}】、库存分配【{}】<br>", warehouseName,dictPlatformName,shopNames, CfgRuleInventoryAllocateTypeEnum.getName(detailEntity.getInventoryAllocateType())));
                }
            }
        }
        operateLogService.addModuleOperateLog(msg.toString(), ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), mainId, "仓库");
    }

    /**
     * 根据主表id集合查询
     * @author will
     * @date 2024/8/24 15:04
     * @param mainIdList
     * @return List<CfgRuleWarehouseDetailEntity>
     */
    @Override
    public List<CfgRuleWarehouseDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<CfgRuleWarehouseDetailEntity> list = lambdaQuery().in(CfgRuleWarehouseDetailEntity::getMainId, mainIdList).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        return list;
    }

    /**
     * 根据主表id集合查询实体仓数据
     * @author will
     * @date 2024/8/24 15:04
     * @param mainIdList
     * @return List<CfgRuleWarehouseDetailEntity>
     */
    @Override
    public List<CfgRuleWarehouseDetailEntity> listByWarehouseType(Boolean isVirtual,List<String> mainIdList,String type) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<CfgRuleWarehouseDetailEntity> list = lambdaQuery()
                .in(CfgRuleWarehouseDetailEntity::getMainId, mainIdList)
                .eq(CfgRuleWarehouseDetailEntity::getWarehouseType,type)
                .eq(!isVirtual,CfgRuleWarehouseDetailEntity::getVirtualWarehouseId,"")
                .ne(isVirtual,CfgRuleWarehouseDetailEntity::getVirtualWarehouseId,"")
                .list();
        return list;
    }

    @Override
    public List<CfgRuleWarehouseDetailDTO.ViewDTO> listViewByMainIdList(List<String> mainIdList) {
        List<CfgRuleWarehouseDetailDTO.ViewDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<CfgRuleWarehouseDetailEntity> list = listByMainIdList(mainIdList);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        //仓库信息
        List<String> warehouseIdList = list.stream().map(CfgRuleWarehouseDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = CollectionUtils.isEmpty(warehouseIdList) ? Collections.EMPTY_LIST : FeignQuery.getByIds(WarehouseEntity.class, warehouseIdList);

        //虚拟仓信息
        List<String> virtualWarehouseIdList = list.stream().map(CfgRuleWarehouseDetailEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseList = CollectionUtils.isEmpty(virtualWarehouseIdList) ? Collections.EMPTY_LIST : FeignQuery.getByIds(VirtualWarehouseEntity.class, virtualWarehouseIdList);

        //店铺信息
        List<String> shopIdList = list.stream().filter(obj -> StrUtil.equals(obj.getChannelType(), VitualWarehouseChannelTypeEnum.SHOP.getCode()))
                .flatMap(obj -> Stream.of(obj.getChannelIdJson().stream().map(Object::toString).toArray(String[]::new))).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = CollectionUtils.isEmpty(shopIdList) ? Collections.EMPTY_LIST : FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);

        //平台信息
        List<String> platformList = list.stream().filter(obj -> StrUtil.equals(obj.getChannelType(), VitualWarehouseChannelTypeEnum.PLATFORM.getCode()))
                .flatMap(obj -> Stream.of(obj.getChannelIdJson().stream().map(Object::toString).toArray(String[]::new))).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = CollectionUtils.isEmpty(platformList) ? Collections.EMPTY_LIST : FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();


        for (CfgRuleWarehouseDetailEntity detailEntity : list) {
            CfgRuleWarehouseDetailDTO.ViewDTO viewDTO = new CfgRuleWarehouseDetailDTO.ViewDTO();
            BeanMapperUtils.copy(detailEntity,viewDTO);
            //仓库信息
            String warehouseName = warehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getWarehouseId()))
                    .map(WarehouseEntity::getName).findFirst().orElse("");
            viewDTO.setWarehouseName(warehouseName);
            //虚拟仓信息
            String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(),detailEntity.getVirtualWarehouseId()))
                    .map(VirtualWarehouseEntity::getName).findFirst().orElse("");
            viewDTO.setVirtualWarehouseName(virtualWarehouseName);

            //关联店铺json
            List<String> channelIdList = detailEntity.getChannelIdJson().stream().map(Object::toString).collect(Collectors.toList());
            viewDTO.setChannelIdList(channelIdList);

            //关联类型
            String channelType = detailEntity.getChannelType();
            viewDTO.setChannelTypeName(VitualWarehouseChannelTypeEnum.getName(channelType));

            //平台
            String platformNames = dictBasicList.stream().filter(obj -> StrUtil.equals(obj.getValue(),viewDTO.getDictPlatform())).map(DictBasicEntity::getName).distinct().collect(Collectors.joining(","));
            viewDTO.setDictPlatformName(platformNames);

            if (VitualWarehouseChannelTypeEnum.SHOP.getCode().equals(channelType)) {
                List<ShopInfoEntity> shopList = shopInfoList.stream().filter(obj -> channelIdList.contains(obj.getId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(shopList)) {
                    //渠道名称
                    String shopNames = shopList.stream().map(ShopInfoEntity::getName).distinct().collect(Collectors.joining(","));
                    viewDTO.setChannelIdJsonName(shopNames);
                }
            } else {
                //店铺
                viewDTO.setChannelIdList(channelIdList);
                viewDTO.setChannelIdJsonName("全部店铺");
            }
            resultList.add(viewDTO);
        }
        return resultList;
    }

    @Override
    public List<CfgRuleWarehouseDetailEntity> listRefreshVirtual(List<String> warehouseIdList, List<String> virtualWarehouseIdList, List<String> dictPlatformList, String type) {
        return  lambdaQuery().in(CollectionUtils.isNotEmpty(warehouseIdList),CfgRuleWarehouseDetailEntity::getWarehouseId,warehouseIdList)
                .in(CollectionUtils.isNotEmpty(virtualWarehouseIdList),CfgRuleWarehouseDetailEntity::getVirtualWarehouseId,virtualWarehouseIdList)
                .in(CollectionUtils.isNotEmpty(dictPlatformList),CfgRuleWarehouseDetailEntity::getDictPlatform,dictPlatformList)
                .eq(CfgRuleWarehouseDetailEntity::getWarehouseType,type)
                .list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgRuleWarehouseDetailEntity> newList, List<CfgRuleWarehouseDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgRuleWarehouseDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgRuleWarehouseDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<CfgRuleWarehouseDetailEntity> list,String mainId,String type, List<WarehouseEntity> warehouseEntityList) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (CfgRuleWarehouseDetailEntity warehouseDetailEntity : list) {
            //数据验证
            checkData(list,warehouseDetailEntity,warehouseEntityList);
            //按平台
            if (VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(warehouseDetailEntity.getChannelType())) {
                warehouseDetailEntity.setChannelIdList(Arrays.asList(warehouseDetailEntity.getDictPlatform()));
            }
            //主表id
            warehouseDetailEntity.setMainId(mainId);
            //仓库类型
            warehouseDetailEntity.setWarehouseType(type);
            //渠道（店铺）id
            JSONArray channelIdJson = JSONUtil.parseArray(warehouseDetailEntity.getChannelIdList());
            warehouseDetailEntity.setChannelIdJson(channelIdJson);
        }
    }

    /**
     * 验证数据
     */
    private void checkData (List<CfgRuleWarehouseDetailEntity> list,CfgRuleWarehouseDetailEntity entity,List<WarehouseEntity> warehouseEntityList) {
        List<CfgRuleWarehouseDetailEntity> detailList = list.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), entity.getWarehouseId())
                && StrUtil.equals(obj.getVirtualWarehouseId(), entity.getVirtualWarehouseId())
                && StrUtil.equals(obj.getWarehouseType(), entity.getWarehouseType())
                && StrUtil.equals(obj.getDictPlatform(), entity.getDictPlatform())
        ).collect(Collectors.toList());
        //仓库名称
        String warehouseName = warehouseEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), entity.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
        //既有按平台也有按店铺
        long count = detailList.stream().map(CfgRuleWarehouseDetailEntity::getChannelType).distinct().count();
        if (count > 1) {
            throw new ServiceException(StrUtil.format("实体仓【{}】不能既按平台又按店铺分配",warehouseName));
        }
        long platformCount = detailList.stream().filter(obj -> StrUtil.equals(obj.getChannelType(), VitualWarehouseChannelTypeEnum.PLATFORM.getCode())).map(CfgRuleWarehouseDetailEntity::getDictPlatform).count();
        if (platformCount > 1) {
            throw new ServiceException(StrUtil.format("实体仓【{}】不能按多个平台分配",warehouseName));
        }
        //店铺集合
        List<String> channelIdList = entity.getChannelIdList();
        if (CollectionUtils.isNotEmpty(channelIdList)) {
            for (String channelId : channelIdList) {
                long channelIdCount = detailList.stream().filter(obj -> StrUtil.equals(obj.getChannelType(), VitualWarehouseChannelTypeEnum.SHOP.getCode()) && obj.getChannelIdList().contains(channelId)).map(CfgRuleWarehouseDetailEntity::getDictPlatform).distinct().count();
                if (channelIdCount > 1) {
                    throw new ServiceException(StrUtil.format("实体仓【{}】存在重复店铺分配",warehouseName));
                }
            }
        }
    }

}
