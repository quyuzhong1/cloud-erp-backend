package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleWarehouseDetailDTO;
import com.erp.model.mrp.entity.CfgRuleWarehouseDetailEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleWarehouseDetailMapper;
import com.erp.server.mrp.service.CfgRuleWarehouseDetailService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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


    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<CfgRuleWarehouseDetailDTO.UpdateDTO> cfgLocalWarehouseList,String mainId,String type) {
        if (CollectionUtils.isEmpty(cfgLocalWarehouseList)) {
            cfgLocalWarehouseList = Collections.EMPTY_LIST;
        }
        List<CfgRuleWarehouseDetailEntity> list = BeanMapperUtils.copyList(CfgRuleWarehouseDetailEntity.class, cfgLocalWarehouseList);
        //原仓库配置明细
        List<CfgRuleWarehouseDetailEntity> oldList = listByMainIdList(Collections.singletonList(mainId));

        //删除明细
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        // 数据处理
        handleData(list,mainId,type);
        log.info("编辑 开始修改仓库（规则设置）明细数据，id：【{}】", mainId);
        boolean save = super.updateBatchById(list);
        if(!save) {
            throw new ServiceException("仓库（规则设置）明细保存失败");
        }
        return Boolean.TRUE;
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
            detailEntity.setChannelIdList(channelIdList);
            //关联类型
            String channelType = detailEntity.getChannelType();
            viewDTO.setChannelTypeName(VitualWarehouseChannelTypeEnum.getName(channelType));

            if (VitualWarehouseChannelTypeEnum.SHOP.getCode().equals(channelType)) {
                String shopNames = shopInfoList.stream().filter(obj -> channelIdList.contains(obj.getId())).map(ShopInfoEntity::getName).collect(Collectors.joining(","));
                viewDTO.setChannelIdJsonName(shopNames);
            } else {
                String platformNames = dictBasicList.stream().filter(obj -> channelIdList.contains(obj.getValue())).map(DictBasicEntity::getName).collect(Collectors.joining(","));
                viewDTO.setChannelIdJsonName(platformNames);
            }
            resultList.add(viewDTO);
        }
        return resultList;
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
    private void handleData(List<CfgRuleWarehouseDetailEntity> list,String mainId,String type) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (CfgRuleWarehouseDetailEntity warehouseDetailEntity : list) {
            //主表id
            warehouseDetailEntity.setMainId(mainId);
            //仓库类型
            warehouseDetailEntity.setWarehouseType(type);
            //渠道（店铺）id
            JSONArray channelIdJson = JSONUtil.parseArray(warehouseDetailEntity.getChannelIdList());
            warehouseDetailEntity.setChannelIdJson(channelIdJson);
        }

    }
}
