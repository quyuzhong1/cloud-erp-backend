package com.erp.server.wms.wdt.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.WdtSearchHandelDetailDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.service.WdtStockSpecInventoryService;
import com.common.business.service.WdtVirtualInventoryService;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.InventorySyncModeEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.model.wms.dto.VirtualWarehousePushHandleDetailDTO;
import com.erp.model.wms.dto.WdtCompareInventoryDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.server.wms.service.*;
import com.erp.server.wms.wdt.SyncWdtVirtualWarehousePushOrderService;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwPushHandelDetailPushDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 将erp虚拟仓分货单同步至旺店通
 *
 * @author tanmujin
 * @date 2024-05-16
 */
@Slf4j
@Service
public class SyncWdtVirtualWarehousePushOrderServiceImpl implements SyncWdtVirtualWarehousePushOrderService {

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private VirtualWarehousePushHandleRelationService virtualWarehousePushHandleRelationService;
    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;
    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;
    @Resource
    private VirtualWarehousePushHandleDetailService virtualWarehousePushHandleDetailService;
    @Resource
    private VirtualWarehousePushHandleService virtualWarehousePushHandleService;
    @Resource
    private WmsPushMsgService wmsPushMsgService;
    @Resource
    private WdtVirtualInventoryService wdtVirtualInventoryService;
    @Resource
    private WdtStockSpecInventoryService wdtStockSpecInventoryService;
    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    /**
     * 校验分货单调出明细是否存在未同步成功的数据
     * 校验规则：旺店通虚拟仓可用库存 >= 待同步完成数 + 本次取消/调出数量
     *
     * @param checkDataList 待校验的明细数据
     */
    private void checkHandleDetailListRepeat(List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO> checkDataList) {
        if (CollUtil.isEmpty(checkDataList)) {
            return;
        }
        //来源仓库
        List<String> fromWarehouseIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getWarehouseId).distinct().collect(Collectors.toList());
        //来源虚拟仓
        List<String> fromVirtualWarehouseIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        //sku
        List<String> skuIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getSkuId).distinct().collect(Collectors.toList());

        //查询已存在未同步成功的调出明细
        List<VirtualWarehouseAllocationDetailDTO.RepeatHandleDetailDTO> oldDetailList = virtualWarehouseAllocationDetailService.listRepeatHandleDetail(fromWarehouseIdList, fromVirtualWarehouseIdList, skuIdList);
        if (CollUtil.isEmpty(oldDetailList)) {
            return;
        }

        Map<String, List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO>> map = checkDataList.stream().collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}",obj.getThirdWarehouseNo(),obj.getThirdVirtualWarehouseNo())));
        for (Map.Entry<String, List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO>> entry : map.entrySet()) {
            List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO> value = entry.getValue();
            VirtualWarehousePushHandleDetailDTO.CheckDataDTO fristCheckDataDTO = value.get(0);
            String skuNoList = value.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getSkuNo).collect(Collectors.joining(","));

            WdtSearchHandelDetailDTO.SearchVirtualInventoryParamDTO detailDTO = new WdtSearchHandelDetailDTO.SearchVirtualInventoryParamDTO();
            detailDTO.setSpec_nos(skuNoList);
            detailDTO.setVirtual_warehouse_no(fristCheckDataDTO.getThirdVirtualWarehouseNo());
            detailDTO.setWarehouse_no(fristCheckDataDTO.getThirdWarehouseNo());
            List<WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO> searchVirtualInventoryDTOS = wdtVirtualInventoryService.searchVirtualInventory(detailDTO);
            if (CollUtil.isEmpty(searchVirtualInventoryDTOS)) {
                throw new ServiceException("调用旺店通虚拟仓库存查询接口无可用库存，仓库编码：{}.虚拟仓库编码：{}，SKU列表：{}" , fristCheckDataDTO.getThirdWarehouseNo(), fristCheckDataDTO.getThirdVirtualWarehouseNo(), skuNoList);
            }
            for (VirtualWarehousePushHandleDetailDTO.CheckDataDTO checkDataDTO : value) {
                //待同步完成数
                Integer totalPushQty = oldDetailList.stream().filter(obj ->
                                   !CharSequenceUtil.equals(obj.getDetailId(), checkDataDTO.getDetailId())
                                && CharSequenceUtil.equals(obj.getWarehouseId(), checkDataDTO.getWarehouseId())
                                && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), checkDataDTO.getVirtualWarehouseId())
                                && CharSequenceUtil.equals(obj.getSkuId(), checkDataDTO.getSkuId()))
                        .map(VirtualWarehouseAllocationDetailDTO.RepeatHandleDetailDTO::getQty)
                        .reduce(MathUtil.ZERO, Integer::sum);

                searchVirtualInventoryDTOS.stream().filter(obj -> CharSequenceUtil.equals(checkDataDTO.getSkuNo(),obj.getSkuNo()) && CharSequenceUtil.equals(fristCheckDataDTO.getThirdWarehouseNo(),obj.getWarehouseCode()) && CharSequenceUtil.equals(fristCheckDataDTO.getThirdVirtualWarehouseNo(),obj.getVirtualWarehouseCode()))
                        .findFirst().ifPresent(obj -> {
                            if (MathUtil.compareTo(obj.getQty(),totalPushQty + checkDataDTO.getQty()) < 0) {
                                throw new ServiceException("旺店通虚拟仓【{}】可用库存不足，SKU：【{}】，取消/调出数量：{}，虚拟仓库编码：【{}】，可用库存：{}，待同步完成数：{}，可用库存-取消/调出数量-待同步完成数＜0",obj.getVirtualWarehouseCode(), checkDataDTO.getSkuNo() ,checkDataDTO.getQty() ,fristCheckDataDTO.getThirdVirtualWarehouseNo()
                                        , obj.getQty() , totalPushQty);
                            }
                        });
            }
        }
    }

    @Override
    public void checkAllocationWdtInventory(VirtualWarehouseAllocationEntity allocationEntity,
                                            List<VirtualWarehouseAllocationDetailEntity> detailEntityList,
                                            List<VirtualWarehouseAllocationDTO.TransferWarehouseDTO> transferWarehouseList) {
        if (!VirtualWarehouseAllocationTypeEnum.ALLOCATION.getCode().equals(allocationEntity.getType()) || CollUtil.isEmpty(detailEntityList)) {
            return;
        }
        Map<String, VirtualWarehouseAllocationDTO.TransferWarehouseDTO> transferDetailMap = CollUtil.isEmpty(transferWarehouseList)
                ? Collections.emptyMap()
                : transferWarehouseList.stream()
                .filter(obj -> CharSequenceUtil.isNotBlank(obj.getSourceDetailId()))
                .collect(Collectors.toMap(VirtualWarehouseAllocationDTO.TransferWarehouseDTO::getSourceDetailId, obj -> obj, (left, right) -> left));
        log.warn("开始校验分货单旺店通库存，分货单：{}，明细数：{}，借调明细数：{}",
                allocationEntity.getCode(), detailEntityList.size(), transferDetailMap.size());

        Map<String, Boolean> virtualWarehouseWdtBoundCache = new HashMap<>();
        List<String> entityWarehouseIds = Stream.concat(
                        detailEntityList.stream().map(VirtualWarehouseAllocationDetailEntity::getWarehouseId),
                        transferDetailMap.values().stream().map(VirtualWarehouseAllocationDTO.TransferWarehouseDTO::getFromWarehouseId))
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> entityThirdWarehouseNoMap = buildEntityThirdWarehouseNoMap(entityWarehouseIds);
        Map<String, String> warehouseNameMap = buildWarehouseNameMap(entityWarehouseIds);
        Map<String, Integer> entityAvailableStockCache = new HashMap<>();

        // 场景2：已绑旺店通且未借调；场景3：已绑旺店通且借调且借调仓已绑；场景1/4跳过
        List<VirtualWarehouseAllocationDetailEntity> normalCheckDetailList = new ArrayList<>();
        List<VirtualWarehouseAllocationDetailEntity> borrowCheckDetailList = new ArrayList<>();
        for (VirtualWarehouseAllocationDetailEntity detail : detailEntityList) {
            if (CharSequenceUtil.isBlank(detail.getToVirtualWarehouseId())
                    || !isVirtualWarehouseBoundToWdt(detail.getToVirtualWarehouseId(), virtualWarehouseWdtBoundCache)) {
                log.warn("调入虚拟仓未绑定旺店通，跳过旺店通库存校验，分货单：{}，明细ID：{}，SKU：{}",
                        allocationEntity.getCode(), detail.getId(), detail.getSkuNo());
                continue;
            }
            VirtualWarehouseAllocationDTO.TransferWarehouseDTO transferWarehouse = transferDetailMap.get(detail.getId());
            if (transferWarehouse == null) {
                normalCheckDetailList.add(detail);
                continue;
            }
            String borrowThirdWarehouseNo = entityThirdWarehouseNoMap.get(transferWarehouse.getFromWarehouseId());
            if (CharSequenceUtil.isBlank(borrowThirdWarehouseNo)) {
                log.warn("借调实体仓未绑定旺店通，跳过旺店通库存校验，分货单：{}，明细ID：{}，SKU：{}",
                        allocationEntity.getCode(), detail.getId(), detail.getSkuNo());
                continue;
            }
            borrowCheckDetailList.add(detail);
        }
        if (CollUtil.isEmpty(normalCheckDetailList) && CollUtil.isEmpty(borrowCheckDetailList)) {
            log.warn("分货单旺店通库存校验通过（无可校验明细），分货单：{}", allocationEntity.getCode());
            return;
        }

        validateAllocationEntityWdtInventory(allocationEntity, normalCheckDetailList, transferDetailMap,
                entityThirdWarehouseNoMap, warehouseNameMap, entityAvailableStockCache, false);
        validateAllocationEntityWdtInventory(allocationEntity, borrowCheckDetailList, transferDetailMap,
                entityThirdWarehouseNoMap, warehouseNameMap, entityAvailableStockCache, true);
        log.warn("分货单旺店通库存校验通过，分货单：{}", allocationEntity.getCode());
    }

    /**
     * 按分货实体仓+调入虚拟仓分组校验旺店通实体仓可用库存
     *
     * @param includeBorrowStock true 时叠加借调仓库存（场景3），false 时仅校验分货实体仓（场景2）
     */
    private void validateAllocationEntityWdtInventory(VirtualWarehouseAllocationEntity allocationEntity,
                                                      List<VirtualWarehouseAllocationDetailEntity> checkDetailList,
                                                      Map<String, VirtualWarehouseAllocationDTO.TransferWarehouseDTO> transferDetailMap,
                                                      Map<String, String> entityThirdWarehouseNoMap,
                                                      Map<String, String> warehouseNameMap,
                                                      Map<String, Integer> entityAvailableStockCache,
                                                      boolean includeBorrowStock) {
        if (CollUtil.isEmpty(checkDetailList)) {
            return;
        }
        Map<String, List<VirtualWarehouseAllocationDetailEntity>> groupedDetailMap = checkDetailList.stream()
                .collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}", obj.getWarehouseId(), obj.getToVirtualWarehouseId())));
        for (List<VirtualWarehouseAllocationDetailEntity> groupDetailList : groupedDetailMap.values()) {
            VirtualWarehouseAllocationDetailEntity firstDetail = groupDetailList.get(0);
            String thirdWarehouseNo = entityThirdWarehouseNoMap.get(firstDetail.getWarehouseId());
            if (CharSequenceUtil.isBlank(thirdWarehouseNo)) {
                String warehouseName = warehouseNameMap.getOrDefault(firstDetail.getWarehouseId(),
                        CharSequenceUtil.blankToDefault(firstDetail.getWarehouseName(), "未知"));
                throw new ServiceException("分货实体仓【{}】未配置旺店通仓库映射，仓库ID：{}", warehouseName, firstDetail.getWarehouseId());
            }

            String borrowThirdWarehouseNo = null;
            if (includeBorrowStock) {
                borrowThirdWarehouseNo = groupDetailList.stream()
                        .map(obj -> transferDetailMap.get(obj.getId()))
                        .filter(obj -> obj != null && CharSequenceUtil.isNotBlank(obj.getFromWarehouseId()))
                        .map(obj -> entityThirdWarehouseNoMap.get(obj.getFromWarehouseId()))
                        .filter(CharSequenceUtil::isNotBlank)
                        .findFirst()
                        .orElse(null);
            }

            Map<String, Integer> totalPushQtyMap = groupDetailList.stream()
                    .collect(Collectors.groupingBy(VirtualWarehouseAllocationDetailEntity::getSkuNo,
                            Collectors.summingInt(obj -> MathUtil.valueOfZero(obj.getQty()))));
            for (Map.Entry<String, Integer> skuPushEntry : totalPushQtyMap.entrySet()) {
                String skuNo = skuPushEntry.getKey();
                Integer totalPushQty = skuPushEntry.getValue();
                Integer wdtInventoryQty = queryEntityAvailableStock(entityAvailableStockCache, thirdWarehouseNo, skuNo);
                log.warn("查询实体仓库库存，仓库：{}，SKU：{}，库存：{}，汇总分配数量：{}，叠加借调：{}",
                        thirdWarehouseNo, skuNo, wdtInventoryQty, totalPushQty, includeBorrowStock);

                Integer borrowWdtInventoryQty = 0;
                if (includeBorrowStock && CharSequenceUtil.isNotBlank(borrowThirdWarehouseNo)) {
                    borrowWdtInventoryQty = queryEntityAvailableStock(entityAvailableStockCache, borrowThirdWarehouseNo, skuNo);
                    log.warn("触发借调，叠加借调仓库存，原仓：{}({})，借调仓：{}({})，SKU：{}",
                            thirdWarehouseNo, wdtInventoryQty, borrowThirdWarehouseNo, borrowWdtInventoryQty, skuNo);
                }
                Integer totalWdtInventoryQty = wdtInventoryQty + borrowWdtInventoryQty;
                if (MathUtil.compareTo(totalWdtInventoryQty, totalPushQty) < 0) {
                    String warehouseDesc = CharSequenceUtil.isNotBlank(borrowThirdWarehouseNo)
                            ? CharSequenceUtil.format("{}+{}", thirdWarehouseNo, borrowThirdWarehouseNo)
                            : thirdWarehouseNo;
                    throw new ServiceException(ApiError.VM_WDT_ENTITY_INVENTORY_INSUFFICIENT, warehouseDesc, skuNo, totalWdtInventoryQty, totalPushQty);
                }
            }
        }
    }

    /**
     * 判断虚拟仓是否绑定旺店通（与 /virtualWarehouse/view 的 thirdMappingList.sysType 一致）
     */
    private boolean isVirtualWarehouseBoundToWdt(String virtualWarehouseId, Map<String, Boolean> cache) {
        return cache.computeIfAbsent(virtualWarehouseId, id -> {
            VirtualWarehouseDTO.ViewDTO viewDTO = virtualWarehouseService.view(id);
            if (viewDTO == null || CollUtil.isEmpty(viewDTO.getThirdMappingList())) {
                return false;
            }
            return viewDTO.getThirdMappingList().stream()
                    .anyMatch(mapping -> PlatformDictEnum.WDT.getCode().equals(mapping.getSysType())
                            && CharSequenceUtil.isNotBlank(mapping.getThirdId()));
        });
    }

    private Integer queryEntityAvailableStock(Map<String, Integer> stockCache, String thirdWarehouseNo, String skuNo) {
        String cacheKey = CharSequenceUtil.format("{}-{}", thirdWarehouseNo, skuNo);
        return stockCache.computeIfAbsent(cacheKey, key -> MathUtil.valueOfZero(wdtStockSpecInventoryService.queryAvailableStock(thirdWarehouseNo, skuNo)));
    }

    private Map<String, String> buildWarehouseNameMap(List<String> warehouseIds) {
        if (CollUtil.isEmpty(warehouseIds)) {
            return Collections.emptyMap();
        }
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);
        if (CollUtil.isEmpty(warehouseList)) {
            return Collections.emptyMap();
        }
        return warehouseList.stream()
                .filter(obj -> CharSequenceUtil.isNotBlank(obj.getId()))
                .collect(Collectors.toMap(WarehouseEntity::getId,
                        obj -> CharSequenceUtil.blankToDefault(obj.getName(), "未知"), (left, right) -> left));
    }

    private Map<String, String> buildEntityThirdWarehouseNoMap(List<String> warehouseIds) {
        if (CollUtil.isEmpty(warehouseIds)) {
            return Collections.emptyMap();
        }
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(warehouseIds, DmpBasicSystemCodeEnum.WDT.getCode());
        if (CollUtil.isEmpty(mappingList)) {
            return Collections.emptyMap();
        }
        return mappingList.stream()
                .filter(obj -> CharSequenceUtil.isNotBlank(obj.getSysWarehouseId()) && CharSequenceUtil.isNotBlank(obj.getThirdWarehouseCode()))
                .collect(Collectors.toMap(ThirdMappingDTO.WarehouseMappingDTO::getSysWarehouseId,
                        ThirdMappingDTO.WarehouseMappingDTO::getThirdWarehouseCode, (left, right) -> left));
    }

    @Override
    public void saveTaskList(List<VirtualWarehousePushHandleDetailEntity> handleDetailList, List<String> transferIdList,
                             String vwAllocationCode, String operateCode, String sourceType) {

    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> cfgSettingEntityList = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, sourceType)
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if (CollUtil.isEmpty(cfgSettingEntityList)) {
            throw new ServiceException(ApiError.DMP_PUSH_CFG_NOT_FOUND,"分货单同步旺店通");
        }

    	List<WmsPushMsgEntity> wmsPushMsgEntityList = new ArrayList<>();
        List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO> checkDataList = new ArrayList<>();

        //单据类型:1:锁定分配,2:释放出库,3:虚拟仓间调拨,4:采购入库
        handleDetailList.forEach(handleDetail -> {
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            VwPushHandelDetailPushDTO request = new VwPushHandelDetailPushDTO();

            //获取调出仓 调入仓关联的第三方仓（旺店通）
            if (StringUtils.isNotEmpty(handleDetail.getThirdFromVirtualWarehouseId())
                    && StringUtils.isNotEmpty(handleDetail.getThirdToVirtualWarehouseId())) {
                request.setOrder_type(3);
            } else if (StringUtils.isNotEmpty(handleDetail.getThirdFromVirtualWarehouseId())
                    && StringUtils.isEmpty(handleDetail.getThirdToVirtualWarehouseId())) {
                request.setOrder_type(2);
            } else if (StringUtils.isEmpty(handleDetail.getThirdFromVirtualWarehouseId())
                    && StringUtils.isNotEmpty(handleDetail.getThirdToVirtualWarehouseId())) {
                request.setOrder_type(1);
            }

            request.setPre_time(LocalDateTime.now().plusMinutes(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            request.setVirtual_warehouse_no(StringUtils.isNotEmpty(handleDetail.getThirdFromVirtualWarehouseNo()) ? handleDetail.getThirdFromVirtualWarehouseNo() : handleDetail.getThirdToVirtualWarehouseNo());
            request.setTo_virtual_warehouse_no(handleDetail.getThirdToVirtualWarehouseNo());
            request.setBizType(sourceType);
            request.setSourceId(handleDetail.getId());
            List<VwPushHandelDetailPushDTO.DetailList> detailList = new ArrayList<>();
            //获取明细
            List<VirtualWarehousePushHandleRelationEntity> allocationHandleRelationEntities = virtualWarehousePushHandleRelationService
                    .list(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>()
                            .eq(VirtualWarehousePushHandleRelationEntity::getHandleDetailId, handleDetail.getId()));
            //根据sku和调入虚拟仓进行聚合
            List<String> allocationDetailIds = allocationHandleRelationEntities.stream().map(VirtualWarehousePushHandleRelationEntity::getSourceDetailId).collect(Collectors.toList());
            switch (SourceTypeEnum.getByCode(sourceType)) {
                case VIRTUAL_WAREHOUSE_ALLOCATION:
                    Map<String, List<VirtualWarehouseAllocationDetailEntity>> skuMap = virtualWarehouseAllocationDetailService.listByIds(allocationDetailIds).stream().collect(Collectors.groupingBy(VirtualWarehouseAllocationDetailEntity::getSkuNo));
                    skuMap.forEach((skuNo, list) -> {
                        VwPushHandelDetailPushDTO.DetailList detail = new VwPushHandelDetailPushDTO.DetailList();
                        detail.setNum(BigDecimal.valueOf(list.stream().map(VirtualWarehouseAllocationDetailEntity::getQty).reduce(0, Integer::sum)));
                        detail.setWarehouse_no(handleDetail.getThirdWarehouseId());
                        detail.setSpec_no(skuNo);
                        detailList.add(detail);

                        //添加校验数据
                        builderCheckDataDTO(checkDataList, list, handleDetail, request, skuNo);
                    });
                    break;
                default:
                    break;
            }
            request.setDetailList(detailList);
            request.setRemark("原始单据号：" + vwAllocationCode);

            //添加本地任务
            WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
            wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.WDT.getCode());
            wmsPushMsgEntity.setSourceType(sourceType);
            wmsPushMsgEntity.setSourceId(handleDetail.getId());
            wmsPushMsgEntity.setSourceCode(vwAllocationCode);
            wmsPushMsgEntity.setSyncOperate(operateCode);
            wmsPushMsgEntity.setPushData(JSON.toJSONString(request));
            wmsPushMsgEntity.setParentId(CollUtil.isNotEmpty(transferIdList) ? String.join(",", transferIdList) : "");
            wmsPushMsgEntityList.add(wmsPushMsgEntity);
        });


        //校验之前是否存在未同步成功的分货单调出
        checkHandleDetailListRepeat(checkDataList);

        if(CollUtil.isNotEmpty(wmsPushMsgEntityList)) {
        	wmsPushMsgService.saveBatch(wmsPushMsgEntityList);
        }
    }

    @Override
    public String saveWdtInventoryTask(VirtualWarehouseAllocationEntity allocationEntity, List<VirtualWarehouseAllocationDetailEntity> detailEntityList) {
        //如果明细的调入调出仓库都不在需要比对的仓库列表中，则不触发库存比对任务
        List<String> allDetailWarehouseList = Stream.concat(detailEntityList.stream().map(VirtualWarehouseAllocationDetailEntity::getWarehouseId), detailEntityList.stream().map(VirtualWarehouseAllocationDetailEntity::getToWarehouseId)).distinct().collect(Collectors.toList());

        //查询三方仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(new ArrayList<>(allDetailWarehouseList), "wdt");
        if(mappingList.isEmpty()){
            return null;
        }
        List<ThirdMappingDTO.WarehouseMappingDTO> collect = mappingList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getInventorySyncMode()) && InventorySyncModeEnum.INVENTORY.getCode().equals(e.getInventorySyncMode())).collect(Collectors.toList());
        if (CollUtil.isEmpty(collect)){
            log.warn("分货单库存同步【{}】同步旺店通时，仓库【{}】不存在库存同步配置，跳过同步旺店通",allocationEntity.getCode(), String.join(",", allDetailWarehouseList));
            return null;
        }
        List<String> skuIdList = detailEntityList.stream().map(VirtualWarehouseAllocationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<String> skuNoList = detailEntityList.stream().map(VirtualWarehouseAllocationDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
        WdtCompareInventoryDTO wdtCompareInventoryDTO = WdtCompareInventoryDTO.builder()
                .id(allocationEntity.getId())
                .code(allocationEntity.getCode())
                .erpWarehouseList(allDetailWarehouseList)
                .skuIdList(skuIdList)
                .skuNoList(skuNoList)
                .build();


        String sourceType = SourceTypeEnum.WDT_INVENTORY_COMPARE.getCode();
        //添加本地任务
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.WDT.getCode());
        wmsPushMsgEntity.setSourceType(sourceType);
        wmsPushMsgEntity.setSourceId(allocationEntity.getId());
        wmsPushMsgEntity.setSourceCode(allocationEntity.getCode());
        wmsPushMsgEntity.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
        wmsPushMsgEntity.setPushData(JSON.toJSONString(wdtCompareInventoryDTO));
        wmsPushMsgService.save(wmsPushMsgEntity);
        return allocationEntity.getId();

    }

    /**
     * 构建校验数据
     * @author will
     * @date 2026/1/30 11:27
     * @param checkDataList
     * @param list
     * @param handleDetail
     * @param request
     * @param skuNo
     * @return void
     */
    private void builderCheckDataDTO(List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO> checkDataList, List<VirtualWarehouseAllocationDetailEntity> list,
                                     VirtualWarehousePushHandleDetailEntity handleDetail, VwPushHandelDetailPushDTO request, String skuNo) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (VirtualWarehouseAllocationDetailEntity obj : list) {
            if (CharSequenceUtil.isBlank(handleDetail.getFromVirtualWarehouseId())) {
                continue;
            }
            VirtualWarehousePushHandleDetailDTO.CheckDataDTO checkDataDTO = new VirtualWarehousePushHandleDetailDTO.CheckDataDTO();
            checkDataDTO.setOrderType(request.getOrder_type());
            checkDataDTO.setWarehouseId(handleDetail.getWarehouseId());
            checkDataDTO.setThirdWarehouseNo(handleDetail.getThirdWarehouseId());
            checkDataDTO.setVirtualWarehouseId(handleDetail.getFromVirtualWarehouseId());
            checkDataDTO.setThirdVirtualWarehouseNo(handleDetail.getThirdFromVirtualWarehouseNo());
            checkDataDTO.setSkuId(obj.getSkuId());
            checkDataDTO.setSkuNo(skuNo);
            checkDataDTO.setQty(obj.getQty());
            checkDataDTO.setDetailId(obj.getId());
            checkDataDTO.setHandleDetailId(handleDetail.getId());
            checkDataList.add(checkDataDTO);
        }
    }
}
