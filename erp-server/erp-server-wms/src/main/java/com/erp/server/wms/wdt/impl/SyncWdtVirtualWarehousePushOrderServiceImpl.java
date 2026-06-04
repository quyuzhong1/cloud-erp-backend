package com.erp.server.wms.wdt.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.WdtSearchHandelDetailDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
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
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
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
    private DmpInoutTaskFeign dmpInoutTaskFeign;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 兼容校验：
     * 1. 原逻辑：校验旺店通可用库存 >= 待同步完成数 + 本次分配数量
     * 2. 新增逻辑：新增分货/虚拟仓调拨时，校验旺店通可用库存 >= 本次分配数量
     */
    private void checkHandleDetailListRepeat(List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO> checkDataList) {
        if (CollUtil.isEmpty(checkDataList)) {
            return;
        }

        // 原逻辑：查询已存在未同步成功的调出明细
        List<String> fromWarehouseIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> fromVirtualWarehouseIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<String> skuIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getSkuId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseAllocationDetailDTO.RepeatHandleDetailDTO> oldDetailList = virtualWarehouseAllocationDetailService.listRepeatHandleDetail(fromWarehouseIdList, fromVirtualWarehouseIdList, skuIdList);
        boolean needOldPendingCheck = CollUtil.isNotEmpty(oldDetailList);

        Map<String, List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO>> groupedCheckDataMap = checkDataList.stream()
                .collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}", obj.getThirdWarehouseNo(), obj.getThirdVirtualWarehouseNo())));
        for (Map.Entry<String, List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO>> entry : groupedCheckDataMap.entrySet()) {
            List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO> groupCheckDataList = entry.getValue();
            if (CollUtil.isEmpty(groupCheckDataList)) {
                continue;
            }
            boolean hasCurrentPushOrderType = groupCheckDataList.stream().anyMatch(this::isCurrentPushOrderType);
            if (!needOldPendingCheck && !hasCurrentPushOrderType) {
                continue;
            }
            VirtualWarehousePushHandleDetailDTO.CheckDataDTO firstCheckDataDTO = groupCheckDataList.get(0);
            String skuNoList = groupCheckDataList.stream()
                    .map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getSkuNo)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.joining(","));
            if (CharSequenceUtil.isBlank(skuNoList)) {
                continue;
            }
            WdtSearchHandelDetailDTO.SearchVirtualInventoryParamDTO detailDTO = new WdtSearchHandelDetailDTO.SearchVirtualInventoryParamDTO();
            detailDTO.setSpec_nos(skuNoList);
            detailDTO.setVirtual_warehouse_no(firstCheckDataDTO.getThirdVirtualWarehouseNo());
            detailDTO.setWarehouse_no(firstCheckDataDTO.getThirdWarehouseNo());
            List<WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO> searchVirtualInventoryDTOS = wdtVirtualInventoryService.searchVirtualInventory(detailDTO);
            if (searchVirtualInventoryDTOS == null) {
                searchVirtualInventoryDTOS = Collections.emptyList();
            }
            if (CollUtil.isEmpty(searchVirtualInventoryDTOS)) {
                throw new ServiceException("调用旺店通虚拟仓库存查询接口无可用库存，仓库编码：{}.虚拟仓库编码：{}，SKU列表：{}", firstCheckDataDTO.getThirdWarehouseNo(), firstCheckDataDTO.getThirdVirtualWarehouseNo(), skuNoList);
            }

            Map<String, WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO> wdtInventoryMap = searchVirtualInventoryDTOS.stream()
                    .filter(obj -> CharSequenceUtil.equals(firstCheckDataDTO.getThirdWarehouseNo(), obj.getWarehouseCode())
                            && CharSequenceUtil.equals(firstCheckDataDTO.getThirdVirtualWarehouseNo(), obj.getVirtualWarehouseCode()))
                    .collect(Collectors.toMap(WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO::getSkuNo,
                            obj -> obj,
                            (left, right) -> left));

            if (needOldPendingCheck) {
                for (VirtualWarehousePushHandleDetailDTO.CheckDataDTO checkDataDTO : groupCheckDataList) {
                    // 待同步完成数
                    Integer totalPushQty = oldDetailList.stream().filter(obj ->
                                    !CharSequenceUtil.equals(obj.getDetailId(), checkDataDTO.getDetailId())
                                            && CharSequenceUtil.equals(obj.getWarehouseId(), checkDataDTO.getWarehouseId())
                                            && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), checkDataDTO.getVirtualWarehouseId())
                                            && CharSequenceUtil.equals(obj.getSkuId(), checkDataDTO.getSkuId()))
                            .map(VirtualWarehouseAllocationDetailDTO.RepeatHandleDetailDTO::getQty)
                            .reduce(MathUtil.ZERO, Integer::sum);

                    WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO inventoryDTO = wdtInventoryMap.get(checkDataDTO.getSkuNo());
                    if (inventoryDTO == null) {
                        continue;
                    }
                    if (MathUtil.compareTo(inventoryDTO.getQty(), totalPushQty + checkDataDTO.getQty()) < 0) {
                        throw new ServiceException("旺店通虚拟仓【{}】可用库存不足，SKU：【{}】，取消/调出数量：{}，虚拟仓库编码：【{}】，可用库存：{}，待同步完成数：{}，可用库存-取消/调出数量-待同步完成数＜0", inventoryDTO.getVirtualWarehouseCode(), checkDataDTO.getSkuNo(), checkDataDTO.getQty(), firstCheckDataDTO.getThirdVirtualWarehouseNo()
                                , inventoryDTO.getQty(), totalPushQty);
                    }
                }
            }

            if (hasCurrentPushOrderType) {
                Map<String, Integer> currentPushQtyMap = groupCheckDataList.stream()
                        .filter(this::isCurrentPushOrderType)
                        .collect(Collectors.groupingBy(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getSkuNo,
                                Collectors.summingInt(obj -> MathUtil.valueOfZero(obj.getQty()))));
                for (Map.Entry<String, Integer> skuPushEntry : currentPushQtyMap.entrySet()) {
                    String skuNo = skuPushEntry.getKey();
                    Integer currentPushQty = skuPushEntry.getValue();
                    WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO inventoryDTO = wdtInventoryMap.get(skuNo);
                    Integer wdtInventoryQty = inventoryDTO == null ? 0 : MathUtil.valueOf(inventoryDTO.getQty()).intValue();
                    if (MathUtil.compareTo(wdtInventoryQty, currentPushQty) < 0) {
                        throw new ServiceException(ApiError.VM_WDT_ENTITY_INVENTORY_INSUFFICIENT, firstCheckDataDTO.getThirdWarehouseNo(), skuNo, wdtInventoryQty, currentPushQty);
                    }
                }
            }
        }
    }

    private boolean isCurrentPushOrderType(VirtualWarehousePushHandleDetailDTO.CheckDataDTO checkDataDTO) {
        return Integer.valueOf(1).equals(checkDataDTO.getOrderType()) || Integer.valueOf(3).equals(checkDataDTO.getOrderType());
    }

    @Override
    public void saveTaskList(List<VirtualWarehousePushHandleDetailEntity> handleDetailList,List<String> transferIdList,
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
                        builderCheckDataDTO(checkDataList,list,handleDetail,request,skuNo);
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
    private void builderCheckDataDTO(List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO> checkDataList,List<VirtualWarehouseAllocationDetailEntity> list,
                                     VirtualWarehousePushHandleDetailEntity handleDetail,VwPushHandelDetailPushDTO request,String skuNo) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Integer orderType = request.getOrder_type();
        for (VirtualWarehouseAllocationDetailEntity obj : list) {
            String virtualWarehouseId;
            String thirdVirtualWarehouseNo;
            if (Integer.valueOf(1).equals(orderType)) {
                // 新增分货使用调入虚拟仓
                virtualWarehouseId = handleDetail.getToVirtualWarehouseId();
                thirdVirtualWarehouseNo = handleDetail.getThirdToVirtualWarehouseNo();
            } else {
                // 其他类型沿用调出虚拟仓
                virtualWarehouseId = handleDetail.getFromVirtualWarehouseId();
                thirdVirtualWarehouseNo = handleDetail.getThirdFromVirtualWarehouseNo();
            }
            if (CharSequenceUtil.isBlank(virtualWarehouseId)) {
                continue;
            }
            VirtualWarehousePushHandleDetailDTO.CheckDataDTO checkDataDTO = new VirtualWarehousePushHandleDetailDTO.CheckDataDTO();
            checkDataDTO.setOrderType(orderType);
            checkDataDTO.setWarehouseId(handleDetail.getWarehouseId());
            checkDataDTO.setThirdWarehouseNo(handleDetail.getThirdWarehouseId());
            checkDataDTO.setVirtualWarehouseId(virtualWarehouseId);
            checkDataDTO.setThirdVirtualWarehouseNo(thirdVirtualWarehouseNo);
            checkDataDTO.setSkuId(obj.getSkuId());
            checkDataDTO.setSkuNo(skuNo);
            checkDataDTO.setQty(obj.getQty());
            checkDataDTO.setDetailId(obj.getId());
            checkDataDTO.setHandleDetailId(handleDetail.getId());
            checkDataList.add(checkDataDTO);
        }
    }
}
