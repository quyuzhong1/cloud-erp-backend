package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.StocktakingProfitLossDetailMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.StocktakingProfitLossDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘盈盘亏单详情 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Slf4j
@Service
public class StocktakingProfitLossDetailServiceImpl extends SuperServiceImpl<StocktakingProfitLossDetailMapper, StocktakingProfitLossDetailEntity> implements StocktakingProfitLossDetailService {

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WarehouseLocationService warehouseLocationService;
    /**
     * 根据主表id 获取到对应详情信息
     *
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.dto.StocktakingProfitLossDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-08-11 10:11
     */
    @Override
    public List<StocktakingProfitLossDetailDTO.ViewDTO> listByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        List<StocktakingProfitLossDetailDTO.ViewDTO> viewList = baseMapper.listByMainIds(mainIdList);
        List<String> skuIdList = viewList.stream().map(StocktakingProfitLossDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listProductDetailByIds(skuIdList);
        //库位信息查询
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = viewList.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.listByWarehouseIdAndCode(paramList);
        for (StocktakingProfitLossDetailDTO.ViewDTO item : viewList) {
            String skuId = item.getSkuId();
            ProductDetailEntity sku = skuList.stream().filter(s -> s.getId().equals(skuId)).findFirst().orElse(null);
            if (Objects.nonNull(sku)) {
                item.setProductName(sku.getName());
                item.setUnit(sku.getUnitName());
            } else {
                item.setProductName("");
                item.setUnit("");
            }
            //仓位信息
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntityList.stream().filter(e -> Objects.nonNull(e) && e.getWarehouseId().equals(item.getWarehouseId())
                    && e.getCode().equals(item.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            item.setWarehouseLocationName(warehouseLocationEntity.getName());
        }
        return viewList;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateInfo(String mainId, List<StocktakingProfitLossDetailDTO.UpdateDTO> detailList) {
        List<StocktakingProfitLossDetailEntity> dbList = this.listBaseByMainId(mainId);
        List<Pair<String, String>> pairList = detailList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            List<StocktakingProfitLossDetailEntity> removeList = dbList.stream().filter(d -> deleteIdList.contains(d.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairLogList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), pairLogList, "编辑操作");
            this.removeByIds(deleteIdList);
        }
        List<StocktakingProfitLossDetailEntity> updateList = BeanMapperUtils.copyList(StocktakingProfitLossDetailEntity.class, detailList);
        List<WarehouseEntity> warehouseList = warehouseService.list();
        for (StocktakingProfitLossDetailEntity item : updateList) {
            item.setMainId(mainId);
            String id = item.getId();
            if (CharSequenceUtil.isNotBlank(id)) {
                StocktakingProfitLossDetailEntity old = dbList.stream().
                        filter(d -> d.getId().equals(id)).findFirst().orElse(null);
                if (Objects.isNull(old)) {
                    throw new ServiceException("未找到盘盈盘亏单明细");
                }
                String oldWarehouseName = warehouseList.stream().filter(w -> w.getId().equals(old.getWarehouseId())).
                        findFirst().map(WarehouseEntity::getName).orElse("");
                old.setWarehouseName(oldWarehouseName);
                String warehouseName = warehouseList.stream().filter(w -> w.getId().equals(item.getWarehouseId())).
                        findFirst().map(WarehouseEntity::getName).orElse("");
                item.setWarehouseName(warehouseName);
                operateLogService.addModuleOperateLogByObj(old, item, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), mainId, "", String.format("【%s】", old.getSkuNo()));
            }
        }
        List<StocktakingProfitLossDetailEntity> addList = updateList.stream().filter(u -> CharSequenceUtil.isBlank(u.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), addPairList, "编辑操作");
        }

        this.saveOrUpdateBatch(updateList);
        //标记SKU
        List<String> skuIds = updateList.stream().map(StocktakingProfitLossDetailEntity::getSkuId).collect(Collectors.toList());
        plmTaskFeign.updateOccupyStatus(skuIds);
    }

    @Override
    public void removeByMainId(String mainId) {
        lambdaUpdate().eq(StocktakingProfitLossDetailEntity::getMainId, mainId).remove();
    }

    /**
     * 获取删除ids
     *
     * @param pairList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-10-20 14:05
     */
    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<StocktakingProfitLossDetailEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(StocktakingProfitLossDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<StocktakingProfitLossDetailEntity> listBaseByMainId(String mainId) {
        if (CharSequenceUtil.isNotBlank(mainId)) {
            return this.lambdaQuery().eq(StocktakingProfitLossDetailEntity::getMainId, mainId).orderByAsc(StocktakingProfitLossDetailEntity::getId).list();
        }
        return Collections.emptyList();
    }
}
