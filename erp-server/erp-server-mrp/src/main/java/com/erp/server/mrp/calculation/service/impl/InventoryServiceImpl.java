package com.erp.server.mrp.calculation.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.enums.*;
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.model.wms.enums.DeliveryPlanTypeEnum;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.mapper.InventoryMapper;
import com.erp.server.mrp.service.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.SnapshotTableEnum.*;

@Service
public class InventoryServiceImpl implements InventoryService {
    @Resource
    private InventoryMapper inventoryMapper;
    @Resource
    private FbaHistoryInventoryService fbaHistoryInventoryService;
    @Resource
    private OverseasHistoryInventoryService overseasHistoryInventoryService;
    @Resource
    private LocalHistoryInventoryService localHistoryInventoryService;
    @Resource
    private VirtualInventoryHistoryService virtualInventoryHistoryService;
    @Resource
    private CfgRuleCommonService cfgRuleCommonService;
    @Resource
    private CfgRuleOrderStrategyService cfgRuleOrderStrategyService;

    public List<ReplenishmentInventoryDTO.FbaUsableDTO> getFbaUsable(Set<String> codes, String calcDate) {
        return inventoryMapper.getFbaUsable(String.join("+", codes), getTableName(FBA_INVENTORY, calcDate));
    }

    public List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> getPlanDelivery(ReplenishmentResultDTO replenishmentResultDTO,
                                                                                   Set<String> strategyCodes,
                                                                                   CfgRuleExpireTimeDTO.StrategyResultDTO expireTimeResult,
                                                                                   String sourceType,
                                                                                   ReplenishmentInventoryTypeEnum inventoryTypeEnum,
                                                                                   String type) {
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> estimatedDeliveryDetails = inventoryMapper.getPlanDelivery(type, strategyCodes,
                replenishmentResultDTO, getTableName(WMS_DELIVERY_PLAN, calcDate),
                getTableName(WMS_DELIVERY_PLAN_DETAIL, calcDate), sourceType);
        for (ReplenishmentResultDTO.EstimatedDeliveryDetailDTO detail : estimatedDeliveryDetails) {
            detail.setType(inventoryTypeEnum.getCode());
            detail.setPlanDeliveryDate(detail.getPlanDeliveryDate().plusDays(expireTimeResult.getLogisticsResult().getLogisticsCycleDays()));
            detail.setEstimateSalesDate(detail.getPlanDeliveryDate()
                    .plusDays(expireTimeResult.getInstockDays())
                    .plusDays(expireTimeResult.getLogisticsResult().getLogisticsDays()));
            detail.setReceivingChannel(detail.getWarehouseId());
            detail.setSourceType(sourceType);
        }
        return estimatedDeliveryDetails;
    }

    public List<ReplenishmentInventoryDTO.OverseasUsableDTO> getOverseasUsable(Set<String> codes, String calcDate) {
        return inventoryMapper.getOverseasUsable(String.join("+", codes), getTableName(OVERSEAS_INVENTORY, calcDate));
    }

    /**
     * 分摊平台销量或店铺库存
     *
     * @param replenishmentResultDTO 建议
     * @param warehouseList          仓库
     * @param inventoryList          仓库库存
     * @param inventoryDetail        库存详情
     * @param shopDemandQty          店铺需求数量
     */
    public int getAllocateQty(ReplenishmentResultDTO replenishmentResultDTO,
                              List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> warehouseList,
                              List<LocalInventoryDTO> inventoryList,
                              List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> inventoryDetail,
                              Map<String, Integer> shopDemandQty, ReplenishmentInventoryTypeEnum inventoryType,
                              CfgRuleWarehouseTypeEnum warehouseType) {
        inventoryList = inventoryList.stream()
                .filter(dto -> dto.getQty() > 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(inventoryList)) {
            return 0;
        }
        int qty = 0;
        for (LocalInventoryDTO dto : inventoryList) {
            //采购建议，发货建议数据无仓库，不进行分摊
            if (ObjectUtils.isEmpty(dto.getWarehouseId())) {
                inventoryDetail.add(ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO.buildReplenishmentInventoryDetailDTO(
                        inventoryType.getCode(), dto,
                        Collections.singletonList(new ReplenishmentResultDTO.ShopInventoryDetailDTO(replenishmentResultDTO.getReplenishment().getShopId(), new BigDecimal(dto.getQty()), dto.getQty()))));
                qty += dto.getQty();
            } else {
                Map<String, List<String>> shopIdByPlatform = replenishmentResultDTO.getShopIdByPlatform();
                //对仓库配置进行合并同维度（虚拟仓存在相同虚拟仓，相同实体仓，不同平台）
                Map<String, List<CfgRuleWarehouseDTO.StrategyDetailResultDTO>> warehouses = filterWarehouseList(warehouseList, warehouseType, dto.getWarehouseId());
                //先获取和dto中相同仓库id的数据
                List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> resultList = Optional.ofNullable(warehouses.get(dto.getWarehouseId())).orElse(new ArrayList<>());
                //获取该仓库配置下对应平台对应店铺的需求数
                Map<CfgRuleWarehouseDTO.StrategyDetailResultDTO, Map<String, Integer>> warehouseShopMap = getWarehouseShopMap(shopDemandQty, resultList, shopIdByPlatform);
                //获取总需求数
                int totalSum = warehouseShopMap.values().stream()
                        .flatMap(innerMap -> innerMap.values().stream())
                        .mapToInt(Integer::intValue)
                        .sum();
                int k = 0;
                BigDecimal warehouseCount = BigDecimal.ZERO;
                //排序从小到大
                resultList = resultList.stream()
                        .sorted(Comparator.comparing(v -> Optional.ofNullable(warehouseShopMap.get(v)).orElse(new HashMap<>()).values()
                                    .stream()
                                    .reduce(0, Math::addExact)
                        ))
                        .collect(Collectors.toList());
                //分摊同仓库维度数据
                for (CfgRuleWarehouseDTO.StrategyDetailResultDTO resultDTO : resultList) {
                    //获取仓库需求数
                    Map<String, Integer> shopQtyMap = warehouseShopMap.get(resultDTO);
                    if (ObjectUtils.isEmpty(shopQtyMap)) {
                        k++;
                        continue;
                    }
                    int warehouseDemandQty = shopQtyMap
                            .values().stream()
                            .mapToInt(Integer::intValue)
                            .sum();
                    BigDecimal warehouseQty = getWarehouseQty(dto, warehouseCount, warehouseDemandQty, totalSum, k == resultList.size() - 1);
                    List<ReplenishmentResultDTO.ShopInventoryDetailDTO> detailDTOS = new ArrayList<>();
                    //分摊店铺数量
                    qty += calculateInventoryQty(replenishmentResultDTO.getReplenishment().getShopId(), warehouseQty, warehouseDemandQty, shopQtyMap, detailDTOS);
                    inventoryDetail.add(ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO.buildReplenishmentInventoryDetailDTO(
                            inventoryType.getCode(), resultDTO, dto, warehouseQty.intValue(), detailDTOS));
                    k++;
                    warehouseCount = warehouseCount.add(warehouseQty);
                }
            }
        }
        return qty;
    }

    private static BigDecimal getWarehouseQty(LocalInventoryDTO dto, BigDecimal warehouseCount, int warehouseDemandQty, int totalSum, Boolean isLastWarehouse) {
        if (Boolean.TRUE.equals(isLastWarehouse)) {
            return new BigDecimal(dto.getQty()).subtract(warehouseCount);
        } else {
            return new BigDecimal(dto.getQty())
                    .multiply(new BigDecimal(warehouseDemandQty))
                    .divide(new BigDecimal(0 == totalSum ? 1 : totalSum), 0, RoundingMode.FLOOR);
        }
    }

    /**
     * 获取该仓库配置下对应平台对应店铺的需求数
     *
     * @param shopDemandQty    店铺需求数
     * @param resultList       配置结果
     * @param shopIdByPlatform 店铺平台数量
     */
    private Map<CfgRuleWarehouseDTO.StrategyDetailResultDTO, Map<String, Integer>> getWarehouseShopMap(Map<String, Integer> shopDemandQty, List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> resultList, Map<String, List<String>> shopIdByPlatform) {
        return resultList.stream()
                .collect(Collectors.toMap(v -> v, v -> {
                    //根据配置类型获取店铺
                    Set<String> shopSet;
                    if (VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(v.getChannelType())) {
                        List<String> dictPlatformList = v.getChannelIdJson().stream().map(Object::toString).collect(Collectors.toList());
                        if (dictPlatformList.contains("") || ObjectUtil.isEmpty(v.getChannelIdJson())) {
                            shopSet = new HashSet<>();
                            // 全量平台：合并所有店铺ID（去重）
                            shopIdByPlatform.values().stream() // 非必要不用parallelStream
                                    .filter(Objects::nonNull)  // 过滤空List
                                    .flatMap(List::stream)
                                    .forEach(shopSet::add);    // 避免中间collect
                        } else {
                            // 指定平台：累加对应店铺ID
                            shopSet = dictPlatformList.stream()
                                    .map(shopIdByPlatform::get)
                                    .filter(Objects::nonNull)
                                    .flatMap(List::stream)
                                    .collect(Collectors.toSet());
                        }
                    } else {
                        shopSet = v.getChannelIdJson().stream().map(Object::toString).collect(Collectors.toSet());
                    }
                    //过滤需求数的店铺
                    return shopDemandQty.entrySet().stream()
                            .filter(e -> shopSet.contains(e.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                }));
    }

    /**
     * 获取全部库存
     *
     * @param inventoryResult 库存
     * @param calculationDate 计算日
     */
    @Override
    public ReplenishmentInventoryDTO getAllInventoryQty(List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult, LocalDate calculationDate) {
        ReplenishmentInventoryDTO dto = new ReplenishmentInventoryDTO();
        String baseKey = CfgRuleCommonTypeEnum.getBaseInventoryRedisKey();
        String calcDate = calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        getFbaUsable(inventoryResult, baseKey, dto, calcDate);
        getFbaInTransit(inventoryResult, baseKey, dto, calcDate);
        getOverseasUsable(inventoryResult, baseKey, dto, calcDate);
        getLocalUsable(inventoryResult, baseKey, dto, calcDate);
        getVirtualUsable(inventoryResult, baseKey, dto, calcDate);
        getLocalWaitQcQty(inventoryResult, baseKey, dto, calcDate);
        getLocalInTransit(inventoryResult, baseKey, dto, calcDate);
        getEstimatedPurchase(inventoryResult, baseKey, dto, calcDate);
        return dto;
    }

    /**
     * 获取本地在途
     *
     * @param inventoryResult 库存配置
     * @param baseKey         公共key
     * @param dto             库存参数
     * @param calcDate        日期
     */
    private void getLocalInTransit(List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult, String baseKey, ReplenishmentInventoryDTO dto, String calcDate) {
        Set<String> codes = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalInTransit());
        if (CollectionUtils.isEmpty(codes)) {
            dto.setLocalInTransitList(Collections.emptyList());
            return;
        }
        boolean isPurchase = codes.contains(CfgRuleInventoryNodeEnum.LOCAL_IN_TRANSIT_PURCHASE.getCode());
        boolean isTransfer = codes.contains(CfgRuleInventoryNodeEnum.LOCAL_IN_TRANSIT_TRANSFER.getCode());
        List<ReplenishmentInventoryDTO.LocalInTransitDTO> localInTransitDetails = inventoryMapper.getLocalInTransitDetail(isPurchase, isTransfer, getTableName(TRANSACTION_FLOW, calcDate),
                getTableName(INSTOCK_FORCAST, calcDate), getTableName(PO_RECEIVE, calcDate), getTableName(PO_INSTOCK, calcDate), getTableName(PO_RETURN, calcDate),
                getTableName(TRANSFER_OUT, calcDate), getTableName(TRANSFER_IN, calcDate));
        List<String> sourceCodeList = localInTransitDetails.stream()
                .filter(v -> CfgRuleInventoryNodeEnum.LOCAL_IN_TRANSIT_PURCHASE.getCode().equals(v.getSourceType()))
                .map(ReplenishmentInventoryDTO.LocalInTransitDTO::getSourceCode)
                .distinct()
                .collect(Collectors.toList());
        List<PurchaseOrderDTO.ViewSubcontractPoDTO> purchaseOrderList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sourceCodeList)) {
            purchaseOrderList = inventoryMapper.getPurchaseOrder(sourceCodeList, getTableName(PURCHASE_ORDER, calcDate), getTableName(PURCHASE_ORDER_DETAIL, calcDate));
        }
        for (ReplenishmentInventoryDTO.LocalInTransitDTO detailDTO : localInTransitDetails) {
            if (LocalInTransitTypeEnum.PURCHASE_IN_TRANSIT.getCode().equals(detailDTO.getSourceType())) {
                PurchaseOrderDTO.ViewSubcontractPoDTO viewDTO = purchaseOrderList.stream()
                        .filter(v -> detailDTO.getSourceCode().equals(v.getCode()))
                        .filter(v -> detailDTO.getSkuId().equals(v.getSkuId()))
                        .findFirst()
                        .orElse(new PurchaseOrderDTO.ViewSubcontractPoDTO());
                detailDTO.setEstimatedPutAwayDate(viewDTO.getPlanDeliveryDate());
            }
        }
        dto.setLocalInTransitList(localInTransitDetails);
    }

    /**
     * 获取FBA在途
     *
     * @param inventoryResult 库存配置
     * @param baseKey         公共key
     * @param dto             库存参数
     * @param calcDate        日期
     */
    private void getFbaInTransit(List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult, String baseKey, ReplenishmentInventoryDTO dto, String calcDate) {
        Set<String> codes = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getFbaInTransit());
        if (CollectionUtils.isEmpty(codes)) {
            dto.setFbaInTransitList(Collections.emptyList());
            return;
        }
        String code = new ArrayList<>(codes).get(0);
        //1、若已生成头程物流单，货件--发货单--头程物流单：
        //已生成头程物流单，且已下单，则预计到货日期 = 头程物流单的下单时间 + 头程物流单上的预计时效
        //已生成头程物流单，但未下单，则预计到货时间 = 发货单的发货时间 + 本地发FBA时效 + FBA入库时间
        //2、若未生成头程物流单，已生成发货单，则预计到货时间 = 发货单的发货时间 + 本地发FBA时效 + FBA入库时间
        if (CfgRuleInventoryNodeEnum.FBA_DELIVERY.getCode().equals(code)) {
            //发FBA，签收数量取对应货件的签收数量 在途数量 = 发货单上的实发数量 - 签收数量；
            List<ReplenishmentInventoryDTO.FbaInTransitDTO> fbaInTransitList = inventoryMapper.getFbaDelivery(getTableName(FIRST_MILE_DELIVERY, calcDate),
                    getTableName(FIRST_MILE_DELIVERY_DETAIL, calcDate),
                    getTableName(FBA_SHIPMENT, calcDate),
                    getTableName(FBA_SHIPMENT_DETAIL, calcDate)
            );
            if (CollectionUtils.isEmpty(fbaInTransitList)) {
                dto.setFbaInTransitList(Collections.emptyList());
                return;
            }
            List<String> firstMileDeliveryIds = fbaInTransitList.stream().map(ReplenishmentInventoryDTO.FbaInTransitDTO::getSourceId).collect(Collectors.toList());
            //查询头程物流单
            List<LogisticsBillEntity> logisticsBills = inventoryMapper.getLogisticsBillBySourceIds(firstMileDeliveryIds, getTableName(LOGISTICS_BILL, calcDate));
            for (ReplenishmentInventoryDTO.FbaInTransitDTO detail : fbaInTransitList) {
                LogisticsBillEntity logisticsBill = logisticsBills.stream().filter(v -> v.getSourceId().equals(detail.getSourceId()))
                        .filter(v -> !ObjectUtils.isEmpty(v.getOrderTime()))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isEmpty(logisticsBill)) {
                    detail.setEstimateSalesDate(detail.getDeliveryDate());
                } else {
                    detail.setEstimateSalesDate(logisticsBill.getOrderTime().toLocalDate());
                }
            }
            dto.setFbaInTransitList(fbaInTransitList);
        } else if (CfgRuleInventoryNodeEnum.FBA_SHIPMENT.getCode().equals(code)) {
            //FBA在途 =发货数量 - 签收数量
            List<ReplenishmentInventoryDTO.FbaInTransitDTO> fbaInTransitList = inventoryMapper.getFbaShipment(getTableName(FBA_SHIPMENT, calcDate),
                    getTableName(FBA_SHIPMENT_DETAIL, calcDate)
            );
            if (CollectionUtils.isEmpty(fbaInTransitList)) {
                dto.setFbaInTransitList(Collections.emptyList());
                return;
            }
            List<String> sourceCode = fbaInTransitList.stream().map(ReplenishmentInventoryDTO.FbaInTransitDTO::getSourceCode).collect(Collectors.toList());
            List<FirstMileDeliveryDTO.FbaShipmentDTO> firstMileDeliveryList = inventoryMapper.listFirstMileDelivery(sourceCode, getTableName(FIRST_MILE_DELIVERY, calcDate),
                    getTableName(FIRST_MILE_DELIVERY_DETAIL, calcDate));
            List<String> firstMileDeliveryIds = firstMileDeliveryList.stream().map(FirstMileDeliveryDTO.FbaShipmentDTO::getId).collect(Collectors.toList());
            List<LogisticsBillEntity> logisticsBills = inventoryMapper.getLogisticsBillBySourceIds(firstMileDeliveryIds, getTableName(LOGISTICS_BILL, calcDate));
            for (ReplenishmentInventoryDTO.FbaInTransitDTO detail : fbaInTransitList) {
                FirstMileDeliveryDTO.FbaShipmentDTO fbaShipmentDTO = firstMileDeliveryList.stream()
                        .filter(v -> v.getFbaShipmentCode().equals(detail.getSourceCode()))
                        .findFirst()
                        .orElse(new FirstMileDeliveryDTO.FbaShipmentDTO());
                LogisticsBillEntity logisticsBill = logisticsBills.stream().filter(v -> v.getSourceId().equals(fbaShipmentDTO.getId()))
                        .filter(v -> !ObjectUtils.isEmpty(v.getOrderTime()))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isEmpty(logisticsBill)) {
                    detail.setEstimateSalesDate(fbaShipmentDTO.getApproveTime().toLocalDate());
                } else {
                    detail.setEstimateSalesDate(logisticsBill.getOrderTime().toLocalDate());
                }
            }
            dto.setFbaInTransitList(fbaInTransitList);
        }

    }

    /**
     * 获取待质检
     *
     * @param inventoryResult 库存配置
     * @param baseKey         公共key
     * @param dto             库存参数
     * @param calcDate        日期
     */
    private void getLocalWaitQcQty(List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult, String baseKey, ReplenishmentInventoryDTO dto, String calcDate) {
        Set<String> waitQc = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalWaitQc());
        if (CollectionUtils.isEmpty(waitQc)) {
            dto.setLocalWaitQcList(Collections.emptyList());
            return;
        }
        List<ReplenishmentInventoryDTO.LocalWaitQcDTO> localWaitQcList = getLocalWaitQc(waitQc, calcDate);
        dto.setLocalWaitQcList(localWaitQcList);
    }

    private List<ReplenishmentInventoryDTO.LocalWaitQcDTO> getLocalWaitQc(Set<String> waitQc, String calcDate) {
        return inventoryMapper.getLocalWaitQc(waitQc, getTableName(INVENTORY, calcDate));
    }

    /**
     * 获取预计采购数据
     *
     * @param inventoryResult 库存配置
     * @param baseKey         公共key
     * @param dto             库存参数
     * @param calcDate        日期
     */
    private void getEstimatedPurchase(List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult, String baseKey, ReplenishmentInventoryDTO dto, String calcDate) {
        List<ReplenishmentInventoryDTO.EstimatedPurchaseDTO> estimatedPurchaseList = new ArrayList<>();
        //预计采购
        Set<String> localReplenishmentPlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalReplenishmentPlan());
        if (!CollectionUtils.isEmpty(localReplenishmentPlan)) {
            getReplenishmentPlan(dto, calcDate, localReplenishmentPlan);
        }

        //采购计划_预计采购下推
        Set<String> localReplenishmentPurchasePlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalReplenishmentPurchasePlan());
        if (!CollectionUtils.isEmpty(localReplenishmentPurchasePlan)) {
            List<ReplenishmentInventoryDTO.EstimatedPurchaseDTO> applications = getPurchasePlan(calcDate, localReplenishmentPurchasePlan, CfgRuleInventoryNodeEnum.LOCAL_REPLENISHMENT_PURCHASE_PLAN.getCode());
            if (!CollectionUtils.isEmpty(applications)) {
                estimatedPurchaseList.addAll(applications);
            }
        }

        //采购计划手动新增
        Set<String> localPurchasePlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalPurchasePlan());
        if (!CollectionUtils.isEmpty(localPurchasePlan)) {
            List<ReplenishmentInventoryDTO.EstimatedPurchaseDTO> applications = getPurchasePlan(calcDate, localPurchasePlan, CfgRuleInventoryNodeEnum.LOCAL_PURCHASE_PLAN.getCode());
            if (!CollectionUtils.isEmpty(applications)) {
                estimatedPurchaseList.addAll(applications);
            }
        }

        //采购订单
        Set<String> localPurchaseOrder = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalPurchaseOrder());
        if (!CollectionUtils.isEmpty(localPurchaseOrder)) {
            List<ReplenishmentInventoryDTO.EstimatedPurchaseDTO> purchaseDetails = inventoryMapper.listPurchase(localPurchaseOrder, getTableName(PURCHASE_ORDER, calcDate), getTableName(PURCHASE_ORDER_DETAIL, calcDate));
            if (!CollectionUtils.isEmpty(purchaseDetails)) {
                estimatedPurchaseList.addAll(purchaseDetails);
            }
        }
        dto.setEstimatedPurchaseList(estimatedPurchaseList.stream()
                .filter(v -> v.getQty() > 0)
                .collect(Collectors.toList()));
    }

    /**
     * 采购建议
     *
     * @param dto                    参数
     * @param calcDate               计算日
     * @param localReplenishmentPlan 本地计划
     */
    private void getReplenishmentPlan(ReplenishmentInventoryDTO dto, String calcDate, Set<String> localReplenishmentPlan) {
        CfgRuleOrderStrategyDTO.ViewDTO view = cfgRuleOrderStrategyService.view();
        List<ReplenishmentInventoryDTO.ReplenishmentPurchaseDTO> replenishmentPurchases;
        if (Boolean.TRUE.equals(view.getIsSplit())) {
            replenishmentPurchases = inventoryMapper.getReplenishmentPurchaseMergePlan(localReplenishmentPlan, getTableName(PURCHASE_SUGGEST_MERGE, calcDate), true);
        } else {
            replenishmentPurchases = inventoryMapper.getReplenishmentPurchaseMergePlan(localReplenishmentPlan, getTableName(PURCHASE_SUGGEST_MERGE, calcDate), false);
        }
        if (CollectionUtils.isEmpty(replenishmentPurchases)) {
            dto.setReplenishmentPurchaseList(new ArrayList<>());
            return;
        }
        dto.setReplenishmentPurchaseList(replenishmentPurchases.stream()
                .filter(v -> v.getQty() > 0)
                .collect(Collectors.toList()));
    }


    /**
     * 获取采购计划
     *
     * @param calcDate          计算日
     * @param localPurchasePlan 采购计划
     */
    private List<ReplenishmentInventoryDTO.EstimatedPurchaseDTO> getPurchasePlan(String calcDate, Set<String> localPurchasePlan, String sourceType) {
        List<ReplenishmentInventoryDTO.EstimatedPurchaseDTO> applications = inventoryMapper.listPurchasePlan(localPurchasePlan, sourceType, getTableName(PURCHASE_APPLICATION, calcDate), getTableName(PURCHASE_APPLICATION_DETAIL, calcDate));
        //查询关联采购
        List<String> detailIds = applications.stream().map(ReplenishmentInventoryDTO.EstimatedPurchaseDTO::getDetailId).collect(Collectors.toList());
        List<PurchaseApplicationRefPoDTO.ListDTO> refList = null;
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = null;
        if (!CollectionUtils.isEmpty(detailIds)) {
            refList = inventoryMapper.listPurchaseApplicationRefPo(detailIds, getTableName(PURCHASE_ORDER, calcDate), getTableName(PURCHASE_ORDER_DETAIL, calcDate), getTableName(PURCHASE_APPLICATION_REF_PO, calcDate));
            subcontractOrderDetailList = inventoryMapper.listSubcontractOrderDetail(detailIds, getTableName(SUBCONTRACT_ORDER, calcDate), getTableName(SUBCONTRACT_ORDER_DETAIL, calcDate), getTableName(PURCHASE_ORDER_DETAIL, calcDate));
        }
        //已下推委外订单的数量
        //处理已审核 & 部分生成 数据
        for (ReplenishmentInventoryDTO.EstimatedPurchaseDTO application : applications) {
            application.setSourceType(sourceType);
            if (ApproveStatusEnum.APPROVE.getCode().equals(application.getStatus())) {
                application.setStatus(ReplenishmentBillStatusEnum.TO_BE_CREATE.getCode());
            }
            if (ApproveStatusEnum.APPROVE.getCode().equals(application.getStatus()) && CreatePoTypeEnum.PARTIAL_GENERATED.getStatus().equals(application.getCreatePoType())) {
                application.setStatus(ReplenishmentBillStatusEnum.PART_CREATED.getCode());
                //委外数量
                Integer subcontractQty = MathUtil.ZERO;
                if (!CollectionUtils.isEmpty(subcontractOrderDetailList)) {
                    subcontractQty = subcontractOrderDetailList.stream().filter(v -> v.getSourceDetailId().equals(application.getDetailId()) && StringUtils.isBlank(v.getParentId()))
                            .map(SubcontractOrderDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                //采购数量
                Integer purchaseQty = MathUtil.ZERO;
                if (!CollectionUtils.isEmpty(refList)) {
                    purchaseQty = refList.stream().filter(e -> e.getPurchaseApplicationDetailId().equals(application.getDetailId()))
                            .map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                application.setQty(application.getQty() - subcontractQty - purchaseQty);
            }
        }
        return applications;
    }

    /**
     * 获取虚拟仓可用库存
     *
     * @param inventoryResult 库存配置
     * @param baseKey         公共key
     * @param dto             库存参数
     * @param calcDate        日期
     */
    private void getVirtualUsable(List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult, String baseKey, ReplenishmentInventoryDTO dto, String calcDate) {
        Set<String> usable = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalUsable());
        if (CollectionUtils.isEmpty(usable)) {
            dto.setVirtualUsableList(Collections.emptyList());
            return;
        }
        List<ReplenishmentInventoryDTO.VirtualUsableDTO> virtualUsableList = getVirtualUsable(usable, calcDate);
        dto.setVirtualUsableList(virtualUsableList);
    }

    /**
     * 获取本地仓可用库存
     *
     * @param inventoryResult 库存配置
     * @param baseKey         公共key
     * @param dto             库存参数
     * @param calcDate        日期
     */
    private void getLocalUsable(List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult, String baseKey, ReplenishmentInventoryDTO dto, String calcDate) {
        Set<String> usable = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalUsable());
        if (CollectionUtils.isEmpty(usable)) {
            dto.setLocalUsableList(Collections.emptyList());
            return;
        }
        List<ReplenishmentInventoryDTO.LocalUsableDTO> localUsableList = getLocalUsable(usable, calcDate);
        dto.setLocalUsableList(localUsableList);
    }

    /**
     * 获取海外仓可用库存
     *
     * @param inventoryResult 库存配置
     * @param baseKey         公共key
     * @param dto             库存参数
     * @param calcDate        日期
     */
    private void getOverseasUsable(List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult, String baseKey, ReplenishmentInventoryDTO dto, String calcDate) {
        Set<String> usable = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getOverseasUsable());
        if (CollectionUtils.isEmpty(usable)) {
            dto.setOverseasUsableList(Collections.emptyList());
            return;
        }
        List<ReplenishmentInventoryDTO.OverseasUsableDTO> overseasUsableList = getOverseasUsable(usable, calcDate);
        dto.setOverseasUsableList(overseasUsableList);
    }

    /**
     * 获取FBA可用库存
     *
     * @param inventoryResult 库存配置
     * @param baseKey         公共key
     * @param dto             库存参数
     * @param calcDate        日期
     */
    private void getFbaUsable(List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult, String baseKey, ReplenishmentInventoryDTO dto, String calcDate) {
        //获取fba可用
        Set<String> usable = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getFbaUsable());
        if (CollectionUtils.isEmpty(usable)) {
            dto.setFbaUsableList(Collections.emptyList());
            return;
        }
        List<ReplenishmentInventoryDTO.FbaUsableDTO> fbaUsableList = getFbaUsable(usable, calcDate);
        dto.setFbaUsableList(fbaUsableList);
    }

    /**
     * 过滤仓库方法
     *
     * @param warehouseList 仓库列表
     * @param warehouseType 仓库类型
     * @param warehouseId   仓库id
     */
    private Map<String, List<CfgRuleWarehouseDTO.StrategyDetailResultDTO>> filterWarehouseList(List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> warehouseList,
                                                                                               CfgRuleWarehouseTypeEnum warehouseType,
                                                                                               String warehouseId) {

        return warehouseList.stream()
                .filter(v -> CfgRuleWarehouseTypeEnum.VIRTUAL.equals(warehouseType) ?
                        v.getVirtualWarehouseId().equals(warehouseId) :
                        v.getWarehouseId().equals(warehouseId))
                .collect(Collectors.groupingBy(v -> CfgRuleWarehouseTypeEnum.VIRTUAL.equals(warehouseType) ?
                        v.getVirtualWarehouseId() :
                        v.getWarehouseId()));
    }


    /**
     * 分摊库存
     *
     * @param shopId             建议店铺
     * @param warehouseQty       被分摊数量
     * @param warehouseDemandQty 仓库需求量
     * @param shopQtyMap         店铺需求量
     * @param detailDTOS         明细
     */
    private int calculateInventoryQty(String shopId, BigDecimal warehouseQty, int warehouseDemandQty,
                                      Map<String, Integer> shopQtyMap,
                                      List<ReplenishmentResultDTO.ShopInventoryDetailDTO> detailDTOS) {
        int qty = 0;
        BigDecimal otherSales = BigDecimal.ZERO;
        int index = 0;
        Set<Map.Entry<String, Integer>> entries = shopQtyMap.entrySet();
        LinkedHashSet<Map.Entry<String, Integer>> data = entries.stream().sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (Map.Entry<String, Integer> entry : data) {
            BigDecimal shopQty;
            if (index == shopQtyMap.size() - 1) {
                shopQty = warehouseQty.subtract(otherSales);
            } else {
                shopQty = warehouseQty.multiply(new BigDecimal(entry.getValue()))
                        .divide(new BigDecimal(warehouseDemandQty == 0 ? 1 : warehouseDemandQty), 0, RoundingMode.FLOOR);
                otherSales = otherSales.add(shopQty);
            }

            // 将结果加入 detailDTOS
            detailDTOS.add(new ReplenishmentResultDTO.ShopInventoryDetailDTO(entry.getKey(), shopQty, entry.getValue()));
            // 如果当前 shopId 匹配，则累加结果
            if (entry.getKey().equals(shopId)) {
                qty += shopQty.intValue();
            }

            index++;
        }
        return qty;
    }


    public List<ReplenishmentInventoryDTO.LocalUsableDTO> getLocalUsable(Set<String> codes, String calcDate) {
        return inventoryMapper.getLocalUsable(codes, getTableName(INVENTORY, calcDate));
    }

    public List<ReplenishmentInventoryDTO.VirtualUsableDTO> getVirtualUsable(Set<String> codes, String calcDate) {
        return inventoryMapper.getVirtualUsable(codes, getTableName(VIRTUAL_INVENTORY, calcDate));
    }

    @Override
    public int getLocalInTransit(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO, List<ReplenishmentResultDTO> dtoList) {
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localInTransitDetail = new ArrayList<>();
        List<LocalInventoryDTO> invetoryList = getLocalInTransitInventory(replenishmentResultDTO, cfgRuleStrategyDTO.getExpireTimeResult());
        int qty = getAllocateQty(replenishmentResultDTO, cfgRuleStrategyDTO.getWarehouseResult().getLocalWarehouseList(),
                invetoryList, localInTransitDetail, getShopDemandQtyMap(dtoList), ReplenishmentInventoryTypeEnum.LOCAL_IN_TRANSIT, CfgRuleWarehouseTypeEnum.LOCAL);
        Map<String, List<ReplenishmentResultDTO.LocalInTransitDetailDTO>> collect = Optional.ofNullable(replenishmentResultDTO.getLocalInTransitDetails()).orElse(new ArrayList<>())
                .stream()
                .filter(v -> !ObjectUtils.isEmpty(v.getWarehouseId()))
                .collect(Collectors.groupingBy(ReplenishmentResultDTO.LocalInTransitDetailDTO::getWarehouseId));
        for (Map.Entry<String, List<ReplenishmentResultDTO.LocalInTransitDetailDTO>> entry : collect.entrySet()) {
            BigDecimal shopQty = localInTransitDetail.stream()
                    .filter(v -> v.getWarehouseId().equals(entry.getKey()))
                    .map(ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO::getShopInventoryDetails)
                    .flatMap(Collection::stream)
                    .filter(v -> v.getShopId().equals(replenishmentResultDTO.getReplenishment().getShopId()))
                    .map(ReplenishmentResultDTO.ShopInventoryDetailDTO::getQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int totalQty = entry.getValue()
                    .stream()
                    .map(ReplenishmentResultDTO.LocalInTransitDetailDTO::getQty)
                    .reduce(0, Math::addExact);
            int i = 0;
            BigDecimal countQty = BigDecimal.ZERO;
            //排序
            List<ReplenishmentResultDTO.LocalInTransitDetailDTO> dtos = entry.getValue().stream()
                    .sorted(Comparator.comparing(ReplenishmentResultDTO.LocalInTransitDetailDTO::getQty))
                    .collect(Collectors.toList());
            for (ReplenishmentResultDTO.LocalInTransitDetailDTO dto : dtos) {
                BigDecimal currentQty = getCurrentDocQty(i == entry.getValue().size() - 1, totalQty, countQty, dto.getQty(), shopQty);
                dto.setShopPreQty(currentQty.intValue());
                countQty = countQty.add(currentQty);
                i++;
            }

        }
        replenishmentResultDTO.setLocalInTransitDetail(localInTransitDetail);
        return qty;
    }

    /**
     * 获取本地在途库存
     *
     * @param replenishmentResultDTO 建议
     * @param expireTimeResult          时效配置
     */
    private List<LocalInventoryDTO> getLocalInTransitInventory(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleExpireTimeDTO.StrategyResultDTO expireTimeResult) {
        String platformType = replenishmentResultDTO.getReplenishment().getPlatformType();
        List<String> localWarehouseIds = replenishmentResultDTO.getLocalWarehouseId();
        if (CollectionUtils.isEmpty(localWarehouseIds)) {
            return Collections.emptyList();
        }
        List<ReplenishmentResultDTO.LocalInTransitDetailDTO> localInTransitDetails = replenishmentResultDTO.getInventoryDTO().getLocalInTransitList()
                .stream().filter(v -> v.getSkuId().equals(replenishmentResultDTO.getReplenishment().getSkuId()))
                .filter(v -> localWarehouseIds.contains(v.getWarehouseId()))
                .map(ReplenishmentResultDTO.LocalInTransitDetailDTO::buildLocalInTransitDetailDTO)
                .collect(Collectors.toList());
        for (ReplenishmentResultDTO.LocalInTransitDetailDTO detail : localInTransitDetails) {
            if (LocalInTransitTypeEnum.TRANSFER_IN_TRANSIT.getCode().equals(detail.getSourceType())) {
                //预计入库日期 = 采购订单的审核日期 + 生产周期 + 供应商发货时长 + 质检入库时长
                detail.setEstimatedPutAwayDate(detail.getEstimatedPutAwayDate()
                        .plusDays(expireTimeResult.getPurchaseApproveDays())
                        .plusDays(expireTimeResult.getProductionDays())
                        .plusDays(expireTimeResult.getSupplierDeliveryDays()).plusDays(expireTimeResult.getQcDays()));
            }
            if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(platformType) || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(platformType)) {
                //预计到货日期（Amazon） = 预计入库日期 + 本地发FBA时效 + FBA入库时间
                //预计到货日期（海外） = 预计入库日期 + 本地发海外时效 + 海外仓入库时间
                detail.setEstimateSalesDate(detail.getEstimatedPutAwayDate().plusDays(expireTimeResult.getLogisticsResult().getLogisticsDays()).plusDays(expireTimeResult.getInstockDays()));
            } else if (CfgRulePlatformTypeEnum.B2B.getCode().equals(platformType) || CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(platformType)) {
                //预计到货日期（本地）= 预计入库日期
                detail.setEstimateSalesDate(detail.getEstimatedPutAwayDate());
            }
        }
        replenishmentResultDTO.setLocalInTransitDetails(localInTransitDetails);
        return new ArrayList<>(localInTransitDetails.parallelStream()
                .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty(), v.getSourceType()))
                .collect(Collectors.toMap(
                        v -> v.getWarehouseId() + ":" + v.getSourceType(),
                        v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty(), v.getSourceType()),
                        (existing, replacement) -> {
                            // 合并 qty
                            existing.setQty(existing.getQty() + replacement.getQty());
                            return existing;
                        }
                )).values());
    }

    @Override
    public int getLocalPurchase(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO, List<ReplenishmentResultDTO> dtoList) {
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localPurchaseDetail = new ArrayList<>();
        List<LocalInventoryDTO> invetoryList = getEstimatedPurchaseInventory(replenishmentResultDTO, cfgRuleStrategyDTO);
        int qty = getAllocateQty(replenishmentResultDTO, cfgRuleStrategyDTO.getWarehouseResult().getLocalWarehouseList(),
                invetoryList, localPurchaseDetail, getShopDemandQtyMap(dtoList), ReplenishmentInventoryTypeEnum.LOCAL_ESTIMATED_DELIVERY, CfgRuleWarehouseTypeEnum.LOCAL);
        Map<String, List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO>> collect = Optional.ofNullable(replenishmentResultDTO.getLocalPurchaseDetails()).orElse(new ArrayList<>())
                .stream()
                .filter(v -> !ObjectUtils.isEmpty(v.getWarehouseId()))
                .collect(Collectors.groupingBy(ReplenishmentResultDTO.EstimatedPurchaseDetailDTO::getWarehouseId));
        for (Map.Entry<String, List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO>> entry : collect.entrySet()) {
            BigDecimal shopQty = localPurchaseDetail.stream()
                    .filter(v -> v.getWarehouseId().equals(entry.getKey()))
                    .map(ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO::getShopInventoryDetails)
                    .flatMap(Collection::stream)
                    .filter(v -> v.getShopId().equals(replenishmentResultDTO.getReplenishment().getShopId()))
                    .map(ReplenishmentResultDTO.ShopInventoryDetailDTO::getQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int totalQty = entry.getValue()
                    .stream()
                    .map(ReplenishmentResultDTO.EstimatedPurchaseDetailDTO::getQty)
                    .reduce(0, Math::addExact);
            int i = 0;
            BigDecimal countQty = BigDecimal.ZERO;
            List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> dtos = entry.getValue().stream()
                    .sorted(Comparator.comparing(ReplenishmentResultDTO.EstimatedPurchaseDetailDTO::getQty))
                    .collect(Collectors.toList());
            for (ReplenishmentResultDTO.EstimatedPurchaseDetailDTO dto : dtos) {
                BigDecimal currentQty = getCurrentDocQty(i == entry.getValue().size() - 1, totalQty, countQty, dto.getQty(), shopQty);
                dto.setShopPreQty(currentQty.intValue());
                countQty = countQty.add(currentQty);
                i++;
            }
        }
        replenishmentResultDTO.setLocalPurchaseDetail(localPurchaseDetail);
        return qty;
    }

    @Override
    public Map<String, Integer> getShopDemandQtyMap(List<ReplenishmentResultDTO> dtoList) {
        return dtoList.stream()
                .collect(Collectors.toMap(v -> v.getReplenishment().getShopId(), v -> {
                    int demandQty = v.getShopDemandQty() - Optional.ofNullable(v.getReplenishmentDetail().getFbaUsableQty()).orElse(0)
                            - Optional.ofNullable(v.getReplenishmentDetail().getFbaInTransitQty()).orElse(0)
                            - Optional.ofNullable(v.getReplenishmentDetail().getOverseasUsableQty()).orElse(0)
                            - Optional.ofNullable(v.getReplenishmentDetail().getOverseasInTransitQty()).orElse(0);
                    return Math.max(demandQty, 0);
                }));
    }


    @Override
    public void saveAllHistoryInventory(LocalDate calculationDate, String calcDate) {
        //清洗每日库存到历史表
        List<FbaInventoryEntity> inventoryEntities = inventoryMapper.getAllFbaHistoryInventory(getTableName(FBA_INVENTORY, calcDate));
        if (!CollectionUtils.isEmpty(inventoryEntities)) {
            fbaHistoryInventoryService.saveTodayInventory(inventoryEntities, calculationDate);
        }
        List<OverseasInventoryEntity> overseasHistoryInventory = inventoryMapper.getAllOverseasHistoryInventory(getTableName(OVERSEAS_INVENTORY, calcDate));
        if (!CollectionUtils.isEmpty(overseasHistoryInventory)) {
            overseasHistoryInventoryService.saveTodayInventory(overseasHistoryInventory, calculationDate);
        }
        List<InventoryEntity> localHistoryInventory = inventoryMapper.getAllLocalHistoryInventory(getTableName(INVENTORY, calcDate));
        if (!CollectionUtils.isEmpty(localHistoryInventory)) {
            localHistoryInventoryService.saveTodayInventory(localHistoryInventory, calculationDate);
        }
        List<VirtualInventoryEntity> virtualInventory = inventoryMapper.getAllVirtualHistoryInventory(getTableName(VIRTUAL_INVENTORY, calcDate));
        if (!CollectionUtils.isEmpty(virtualInventory)) {
            virtualInventoryHistoryService.saveTodayInventory(virtualInventory, calculationDate);
        }
    }

    @Override
    public void checkAllTableExists(String calcDate) {
        List<String> tableList = new ArrayList<>();
        for (SnapshotTableEnum value : values()) {
            boolean exist = inventoryMapper.isTableExist(getTableName(value, calcDate));
            if (Boolean.FALSE.equals(exist)) {
                tableList.add(value.getCode());
            }
        }
        if (!CollectionUtils.isEmpty(tableList)) {
            throw new ServiceException(ApiError.COMMON_TABLE_NOT_FOUND, String.join(",", tableList));
        }
    }

    @Override
    public int getInventory(ReplenishmentResultDTO replenishmentResultDTO, LocalDate endDate, Set<String> deliveryVolumeInventory) {
        return deliveryVolumeInventory.stream()
                .map(code -> getInventoryByCode(code, replenishmentResultDTO, endDate))
                .reduce(0, Math::addExact);
    }

    @Override
    public int getInventoryByPurchaseSuggest(ReplenishmentResultDTO.PurchaseSuggestDTO suggestDTO, ReplenishmentResultDTO replenishmentResultDTO, LocalDate endDate, Set<String> deliveryVolumeInventory) {
        return deliveryVolumeInventory.stream()
                .map(code -> getInventoryByCode(suggestDTO, code, replenishmentResultDTO, endDate))
                .reduce(0, Math::addExact);
    }

    /**
     * @param code                   库存类型
     * @param replenishmentResultDTO 建议
     * @param endDate                结束时间
     */
    private int getInventoryByCode(ReplenishmentResultDTO.PurchaseSuggestDTO suggestDTO, String code, ReplenishmentResultDTO replenishmentResultDTO, LocalDate endDate) {
        CfgRuleStrategyDTO cfgRuleStrategyDTO = replenishmentResultDTO.getCfgRuleStrategy();
        CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult = cfgRuleStrategyDTO.getWarehouseResult();
        //虚拟仓集合
        List<String> virtualWarehouseIdList = warehouseResult.getLocalWarehouseList().stream()
                .map(CfgRuleWarehouseDTO.StrategyDetailResultDTO::getVirtualWarehouseId).collect(Collectors.toList());
        //实体仓集合
        List<String> warehouseIdList = warehouseResult.getLocalWarehouseList().stream()
                .map(CfgRuleWarehouseDTO.StrategyDetailResultDTO::getWarehouseId).collect(Collectors.toList());

        if (CfgRuleSuggestedAmountNodeEnum.LOCAL_USABLE_QTY.getCode().equals(code)) {
            Integer inventoryQty;
            if (Boolean.TRUE.equals(warehouseResult.getIsEnableVirtual())) {
                inventoryQty = replenishmentResultDTO.getInventoryDTO().getVirtualUsableList()
                        .stream().filter(v -> v.getSkuId().equals(suggestDTO.getSkuId()))
                        .filter(v -> virtualWarehouseIdList.contains(v.getVirtualWarehouseId()))
                        .map(ReplenishmentInventoryDTO.VirtualUsableDTO::getQty)
                        .reduce(MathUtil.ZERO, Integer::sum);

            } else {
                inventoryQty = replenishmentResultDTO.getInventoryDTO().getLocalUsableList()
                        .stream().filter(v -> v.getSkuId().equals(suggestDTO.getSkuId()))
                        .filter(v -> warehouseIdList.contains(v.getWarehouseId()))
                        .map(ReplenishmentInventoryDTO.LocalUsableDTO::getQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
            }
            return inventoryQty;
        }
        if (CfgRuleSuggestedAmountNodeEnum.LOCAL_WAIT_QC.getCode().equals(code)) {
            return replenishmentResultDTO.getInventoryDTO().getLocalWaitQcList()
                    .stream().filter(v -> v.getSkuId().equals(suggestDTO.getSkuId()))
                    .filter(v -> warehouseIdList.contains(v.getWarehouseId()))
                    .map(ReplenishmentInventoryDTO.LocalWaitQcDTO::getQty)
                    .reduce(MathUtil.ZERO, Integer::sum);
        }
        if (CfgRuleSuggestedAmountNodeEnum.LOCAL_IN_TRANSIT_QTY.getCode().equals(code)) {
            return replenishmentResultDTO.getInventoryDTO().getLocalInTransitList()
                    .stream().filter(v -> v.getSkuId().equals(suggestDTO.getSkuId()))
                    .filter(v -> warehouseIdList.contains(v.getWarehouseId())
                            && (suggestDTO.getEstimateInstockDate().isAfter(suggestDTO.getSuggestPurchaseDate()) || suggestDTO.getEstimateInstockDate().isEqual(suggestDTO.getSuggestPurchaseDate()))
                            && (suggestDTO.getEstimateInstockDate().isBefore(endDate) || suggestDTO.getEstimateInstockDate().isEqual(endDate)))
                    .map(ReplenishmentInventoryDTO.LocalInTransitDTO::getQty)
                    .reduce(MathUtil.ZERO, Integer::sum);
        }
        if (CfgRuleSuggestedAmountNodeEnum.LOCAL_PLAN_PURCHASE_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getInventoryDTO().getEstimatedPurchaseList()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v -> CharSequenceUtil.equals(v.getSkuId(), suggestDTO.getSkuId()))
                    .filter(v -> warehouseIdList.contains(v.getWarehouseId())
                            && (suggestDTO.getEstimateInstockDate().isAfter(suggestDTO.getSuggestPurchaseDate()) || suggestDTO.getEstimateInstockDate().isEqual(suggestDTO.getSuggestPurchaseDate()))
                            && (suggestDTO.getEstimateInstockDate().isBefore(endDate) || suggestDTO.getEstimateInstockDate().isEqual(endDate)))
                    .map(ReplenishmentInventoryDTO.EstimatedPurchaseDTO::getQty)
                    .reduce(MathUtil.ZERO, Integer::sum);
        }
        if (CfgRuleSuggestedAmountNodeEnum.FBA_PLAN_DELIVERY_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getFbaDeliveryDetails()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v ->
                            (v.getPlanDeliveryDate().isAfter(suggestDTO.getSuggestPurchaseDate()) || v.getPlanDeliveryDate().isEqual(suggestDTO.getSuggestPurchaseDate()))
                            && (v.getPlanDeliveryDate().isBefore(endDate) || v.getPlanDeliveryDate().isEqual(endDate)))
                    .map(ReplenishmentResultDTO.EstimatedDeliveryDetailDTO::getQty)
                    .reduce(0, Math::addExact);
        }
        if (CfgRuleSuggestedAmountNodeEnum.OVERSEAS_PLAN_DELIVERY_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getOverseasDeliveryDetails()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v ->
                            (v.getPlanDeliveryDate().isAfter(suggestDTO.getSuggestPurchaseDate()) || v.getPlanDeliveryDate().isEqual(suggestDTO.getSuggestPurchaseDate()))
                                    && (v.getPlanDeliveryDate().isBefore(endDate) || v.getPlanDeliveryDate().isEqual(endDate)))
                    .map(ReplenishmentResultDTO.EstimatedDeliveryDetailDTO::getShopPreQty)
                    .reduce(0, Math::addExact);
        }
        return 0;
    }


    @Override
    public List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> getReplenishmentPlan(ReplenishmentResultDTO replenishmentResultDTO, Set<String> replenishmentPlan,
                                                                                        CfgRuleExpireTimeDTO.StrategyResultDTO expireTimeResult,
                                                                                        ReplenishmentInventoryTypeEnum inventoryTypeEnum) {
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> estimatedDeliveryDetails = inventoryMapper.getReplenishmentPlan(replenishmentPlan,
                replenishmentResultDTO, getTableName(DELIVERY_SUGGEST, calcDate), getTableName(WMS_DELIVERY_PLAN, calcDate),
                getTableName(WMS_DELIVERY_PLAN_DETAIL, calcDate));
        for (ReplenishmentResultDTO.EstimatedDeliveryDetailDTO detail : estimatedDeliveryDetails) {
            detail.setType(inventoryTypeEnum.getCode());
            if (ReplenishmentInventoryTypeEnum.FBA_ESTIMATED_DELIVERY.equals(inventoryTypeEnum)) {
                detail.setSourceType(CfgRuleInventoryNodeEnum.FBA_REPLENISHMENT_PLAN.getCode());
            } else {
                detail.setSourceType(CfgRuleInventoryNodeEnum.OVERSEAS_REPLENISHMENT_PLAN.getCode());
            }
            detail.setPlanDeliveryDate(detail.getEstimateSalesDate().minusDays(expireTimeResult.getLogisticsResult().getLogisticsDays()).minusDays(expireTimeResult.getInstockDays()));
        }
        return estimatedDeliveryDetails;
    }

    @Override
    public int getOverseasInTransit(ReplenishmentResultDTO replenishmentResultDTO, String code, CfgRuleStrategyDTO cfgRuleStrategyDTO, List<ReplenishmentResultDTO> dtoList) {
        Map<String, Integer> shopDemandQty = dtoList.stream()
                .collect(Collectors.toMap(v -> v.getReplenishment().getShopId(), ReplenishmentResultDTO::getShopDemandQty));
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> overseasInTransitDetail = new ArrayList<>();
        List<LocalInventoryDTO> invetoryList = getOverseasInTransitInventory(replenishmentResultDTO, cfgRuleStrategyDTO.getExpireTimeResult());
        int qty = getAllocateQty(replenishmentResultDTO, cfgRuleStrategyDTO.getWarehouseResult().getOverseasWarehouseList(), invetoryList,
                overseasInTransitDetail, shopDemandQty, ReplenishmentInventoryTypeEnum.OVERSEAS_IN_TRANSIT, CfgRuleWarehouseTypeEnum.OVERSEAS);
        Map<String, List<ReplenishmentResultDTO.OverseasInTransitDetailDTO>> collect = Optional.ofNullable(replenishmentResultDTO.getOverseasInTransitDetails()).orElse(new ArrayList<>())
                .stream()
                .filter(v -> !ObjectUtils.isEmpty(v.getWarehouseId()))
                .collect(Collectors.groupingBy(ReplenishmentResultDTO.OverseasInTransitDetailDTO::getWarehouseId));
        for (Map.Entry<String, List<ReplenishmentResultDTO.OverseasInTransitDetailDTO>> entry : collect.entrySet()) {
            BigDecimal shopQty = overseasInTransitDetail.stream()
                    .filter(v -> v.getWarehouseId().equals(entry.getKey()))
                    .map(ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO::getShopInventoryDetails)
                    .flatMap(Collection::stream)
                    .filter(v -> v.getShopId().equals(replenishmentResultDTO.getReplenishment().getShopId()))
                    .map(ReplenishmentResultDTO.ShopInventoryDetailDTO::getQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int totalQty = entry.getValue()
                    .stream()
                    .map(ReplenishmentResultDTO.OverseasInTransitDetailDTO::getInTransitQty)
                    .reduce(0, Math::addExact);
            int i = 0;
            BigDecimal countQty = BigDecimal.ZERO;
            List<ReplenishmentResultDTO.OverseasInTransitDetailDTO> dtos = entry.getValue().stream()
                    .sorted(Comparator.comparing(ReplenishmentResultDTO.OverseasInTransitDetailDTO::getInTransitQty))
                    .collect(Collectors.toList());
            for (ReplenishmentResultDTO.OverseasInTransitDetailDTO dto : dtos) {
                BigDecimal currentQty = getCurrentDocQty(i == entry.getValue().size() - 1, totalQty, countQty, dto.getInTransitQty(), shopQty);
                dto.setShopPreQty(currentQty.intValue());
                countQty = countQty.add(currentQty);
                i++;
            }
        }
        replenishmentResultDTO.setOverseasInTransitDetail(overseasInTransitDetail);
        return qty;
    }

    @Override
    public int getOverseasPlanDelivery(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO, List<ReplenishmentResultDTO> dtoList) {
        Map<String, Integer> shopDemandQty = dtoList.stream()
                .collect(Collectors.toMap(v -> v.getReplenishment().getShopId(), ReplenishmentResultDTO::getShopDemandQty));
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> overseasPlanDeliveryDetail = new ArrayList<>();
        List<LocalInventoryDTO> invetoryList = getOverseasPlanDeliveryInventory(replenishmentResultDTO, cfgRuleStrategyDTO);
        int qty = getAllocateQty(replenishmentResultDTO, cfgRuleStrategyDTO.getWarehouseResult().getOverseasWarehouseList(), invetoryList,
                overseasPlanDeliveryDetail, shopDemandQty, ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY, CfgRuleWarehouseTypeEnum.OVERSEAS);
        Map<String, List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO>> collect = Optional.ofNullable(replenishmentResultDTO.getOverseasDeliveryDetails()).orElse(new ArrayList<>())
                .stream()
                .filter(v -> !ObjectUtils.isEmpty(v.getWarehouseId()))
                .collect(Collectors.groupingBy(ReplenishmentResultDTO.EstimatedDeliveryDetailDTO::getWarehouseId));
        for (Map.Entry<String, List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO>> entry : collect.entrySet()) {
            BigDecimal shopQty = overseasPlanDeliveryDetail.stream()
                    .filter(v -> v.getWarehouseId().equals(entry.getKey()))
                    .map(ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO::getShopInventoryDetails)
                    .flatMap(Collection::stream)
                    .filter(v -> v.getShopId().equals(replenishmentResultDTO.getReplenishment().getShopId()))
                    .map(ReplenishmentResultDTO.ShopInventoryDetailDTO::getQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer totalQty = entry.getValue()
                    .stream()
                    .map(ReplenishmentResultDTO.EstimatedDeliveryDetailDTO::getQty)
                    .reduce(0, Math::addExact);
            int i = 0;
            BigDecimal countQty = BigDecimal.ZERO;
            List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> dtos = entry.getValue().stream()
                    .sorted(Comparator.comparing(ReplenishmentResultDTO.EstimatedDeliveryDetailDTO::getQty))
                    .collect(Collectors.toList());
            for (ReplenishmentResultDTO.EstimatedDeliveryDetailDTO dto : dtos) {
                BigDecimal currentQty = getCurrentDocQty(i == entry.getValue().size() - 1, totalQty, countQty, dto.getQty(), shopQty);
                dto.setShopPreQty(currentQty.intValue());
                countQty = countQty.add(currentQty);
                i++;
            }
        }
        replenishmentResultDTO.setOverseasDeliveryDetail(overseasPlanDeliveryDetail);
        return qty;
    }

    /**
     * 获取当前单据数据
     * @param isLast    是否最后一条数据
     * @param totalQty  总数量
     * @param countQty  统计数量
     * @param docQty    单据数量
     * @param shopQty   店铺分配数量
     */
    private static BigDecimal getCurrentDocQty(Boolean isLast, Integer totalQty, BigDecimal countQty, Integer docQty, BigDecimal shopQty) {
        BigDecimal currentQty;
        if (Boolean.TRUE.equals(isLast)) {
            currentQty = shopQty.subtract(countQty);
        } else {
            currentQty = new BigDecimal(docQty).multiply(shopQty).divide(new BigDecimal(totalQty), 0, RoundingMode.FLOOR);
        }
        return currentQty;
    }

    @Override
    public List<InventoryReportDTO.TransportPagingDTO> listLocalInTransit(LocalDate calculationDate) {
        return inventoryMapper.listLocalInTransit(getTableName(TRANSACTION_FLOW, calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE)));
    }

    private List<LocalInventoryDTO> getOverseasPlanDeliveryInventory(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO) {

        String baseKey = CfgRuleCommonTypeEnum.getBaseInventoryRedisKey();
        List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> estimatedDeliveryDetails = new ArrayList<>();
        //获取需要计算库存的FBA预计发货配置
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        //补货计划
        Set<String> replenishmentPlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getOverseasReplenishmentPlan());
        if (!CollectionUtils.isEmpty(replenishmentPlan)) {
            List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> planDelivery = getReplenishmentPlan(replenishmentResultDTO, replenishmentPlan, cfgRuleStrategyDTO.getExpireTimeResult(),
                    ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY);
            if (!CollectionUtils.isEmpty(planDelivery)) {
                estimatedDeliveryDetails.addAll(planDelivery);
            }
        }
        //发货计划_补货计划下推
        Set<String> replenishmentDeliveryPlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getOverseasDeliveryPlanByReplenishment());
        if (!CollectionUtils.isEmpty(replenishmentDeliveryPlan) && CollUtil.isNotEmpty(replenishmentResultDTO.getOverseasWarehouseId())) {
            List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> planDelivery = getPlanDelivery(replenishmentResultDTO, replenishmentDeliveryPlan, cfgRuleStrategyDTO.getExpireTimeResult(),
                    CfgRuleInventoryNodeEnum.OVERSEAS_DELIVERY_PLAN_BY_REPLENISHMENT.getCode(), ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY, DeliveryPlanTypeEnum.THIRD_WAREHOUSE.getCode());
            if (!CollectionUtils.isEmpty(planDelivery)) {
                estimatedDeliveryDetails.addAll(planDelivery);
            }
        }
        //发货计划_手动新增
        Set<String> codes = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getOverseasDeliveryPlanByManual());
        if (!CollectionUtils.isEmpty(codes) && CollUtil.isNotEmpty(replenishmentResultDTO.getOverseasWarehouseId())) {
            List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> planDelivery = getPlanDelivery(replenishmentResultDTO, codes, cfgRuleStrategyDTO.getExpireTimeResult(),
                    CfgRuleInventoryNodeEnum.OVERSEAS_DELIVERY_PLAN_BY_REPLENISHMENT.getCode(), ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY, DeliveryPlanTypeEnum.THIRD_WAREHOUSE.getCode());
            if (!CollectionUtils.isEmpty(planDelivery)) {
                estimatedDeliveryDetails.addAll(planDelivery);
            }
        }
        replenishmentResultDTO.setOverseasDeliveryDetails(estimatedDeliveryDetails);
        return new ArrayList<>(estimatedDeliveryDetails.parallelStream()
                .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty(), v.getSourceType()))
                .collect(Collectors.toMap(
                        v -> v.getWarehouseId() + ":" + v.getSourceType(),
                        v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty(), v.getSourceType()),
                        (existing, replacement) -> {
                            // 合并 qty
                            existing.setQty(existing.getQty() + replacement.getQty());
                            return existing;
                        }
                )).values());
    }

    /**
     * 获取海外仓在途逻辑
     *
     * @param replenishmentResultDTO 建议
     * @param expireTimeResult       时效配置
     */
    private List<LocalInventoryDTO> getOverseasInTransitInventory(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleExpireTimeDTO.StrategyResultDTO expireTimeResult) {
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        //海外仓未找到
        if (CollectionUtils.isEmpty(replenishmentResultDTO.getOverseasWarehouseId())) {
            return Collections.emptyList();
        }
        //1、若已生成头程物流单，货件--发货单--头程物流单：
        //已生成头程物流单，且已下单，则预计到货日期 = 头程物流单的下单时间 + 头程物流单上的预计时效
        //已生成头程物流单，但未下单，则预计到货时间 = 发货单的发货时间 + 本地发FBA时效 + 海外仓入库时间
        //2、若未生成头程物流单，已生成发货单，则预计到货时间 = 发货单的发货时间 + 本地发海外仓时效 + 海外仓入库时间
        List<ReplenishmentResultDTO.OverseasInTransitDetailDTO> inTransitDetails = inventoryMapper.getOverseasDelivery(replenishmentResultDTO,
                getTableName(FIRST_MILE_DELIVERY, calcDate),
                getTableName(FIRST_MILE_DELIVERY_DETAIL, calcDate),
                getTableName(OVERSEAS_WAREHOUSE_INBOUND, calcDate),
                getTableName(OVERSEAS_WAREHOUSE_INBOUND_DETAIL, calcDate)

        );
        if (CollectionUtils.isEmpty(inTransitDetails)) {
            return Collections.emptyList();
        }
        List<String> firstMileDeliveryIds = inTransitDetails.stream().map(ReplenishmentResultDTO.OverseasInTransitDetailDTO::getDeliveryPlanId).collect(Collectors.toList());
        //查询头程物流单
        List<LogisticsBillEntity> logisticsBills = inventoryMapper.getLogisticsBillBySourceIds(firstMileDeliveryIds, getTableName(LOGISTICS_BILL, calcDate));
        for (ReplenishmentResultDTO.OverseasInTransitDetailDTO detail : inTransitDetails) {
            LogisticsBillEntity logisticsBill = logisticsBills.stream().filter(v -> v.getSourceId().equals(detail.getDeliveryPlanId()))
                    .filter(v -> !ObjectUtils.isEmpty(v.getOrderTime()))
                    .findFirst()
                    .orElse(null);
            if (ObjectUtils.isEmpty(logisticsBill)) {
                detail.setEstimateSalesDate(detail.getDeliveryDate().plusDays(expireTimeResult.getLogisticsResult().getLogisticsDays()).plusDays(expireTimeResult.getInstockDays()));
            } else {
                detail.setEstimateSalesDate(logisticsBill.getOrderTime().toLocalDate().plusDays(expireTimeResult.getLogisticsResult().getLogisticsDays()).plusDays(expireTimeResult.getInstockDays()));
            }
        }
        replenishmentResultDTO.setOverseasInTransitDetails(inTransitDetails);
        return new ArrayList<>(inTransitDetails.parallelStream()
                .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getInTransitQty(), null))
                .collect(Collectors.toMap(
                        v -> v.getWarehouseId() + ":" + v.getSourceType(),
                        v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty(), v.getSourceType()),
                        (existing, replacement) -> {
                            // 合并 qty
                            existing.setQty(existing.getQty() + replacement.getQty());
                            return existing;
                        }
                )).values());
    }

    /**
     * @param code                   库存类型
     * @param replenishmentResultDTO 建议
     * @param endDate                结束时间
     */
    private int getInventoryByCode(String code, ReplenishmentResultDTO replenishmentResultDTO, LocalDate endDate) {
        if (CfgRuleSuggestedAmountNodeEnum.FBA_USABLE_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getReplenishmentDetail().getFbaUsableQty()).orElse(0);
        }
        if (CfgRuleSuggestedAmountNodeEnum.OVERSEAS_USABLE_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getReplenishmentDetail().getOverseasUsableQty()).orElse(0);
        }
        if (CfgRuleSuggestedAmountNodeEnum.LOCAL_USABLE_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getReplenishmentDetail().getLocalUsableQty()).orElse(0);
        }
        if (CfgRuleSuggestedAmountNodeEnum.LOCAL_WAIT_QC.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getReplenishmentDetail().getLocalWaitQcQty()).orElse(0);
        }
        if (CfgRuleSuggestedAmountNodeEnum.FBA_IN_TRANSIT_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getFbaInTransitDetails()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v -> !v.getEstimateSalesDate().isAfter(endDate))
                    .map(ReplenishmentResultDTO.FbaInTransitDetailDTO::getInTransitQty)
                    .reduce(0, Math::addExact);
        }
        if (CfgRuleSuggestedAmountNodeEnum.FBA_PLAN_DELIVERY_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getFbaDeliveryDetails()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v -> !v.getEstimateSalesDate().isAfter(endDate))
                    .map(ReplenishmentResultDTO.EstimatedDeliveryDetailDTO::getQty)
                    .reduce(0, Math::addExact);
        }
        if (CfgRuleSuggestedAmountNodeEnum.OVERSEAS_IN_TRANSIT_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getOverseasInTransitDetails()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v -> !v.getEstimateSalesDate().isAfter(endDate))
                    .map(ReplenishmentResultDTO.OverseasInTransitDetailDTO::getShopPreQty)
                    .reduce(0, Math::addExact);
        }
        if (CfgRuleSuggestedAmountNodeEnum.OVERSEAS_PLAN_DELIVERY_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getOverseasDeliveryDetails()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v -> !v.getEstimateSalesDate().isAfter(endDate))
                    .map(ReplenishmentResultDTO.EstimatedDeliveryDetailDTO::getShopPreQty)
                    .reduce(0, Math::addExact);
        }
        if (CfgRuleSuggestedAmountNodeEnum.LOCAL_IN_TRANSIT_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getLocalInTransitDetails()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v -> !v.getEstimateSalesDate().isAfter(endDate))
                    .map(ReplenishmentResultDTO.LocalInTransitDetailDTO::getShopPreQty)
                    .reduce(0, Math::addExact);
        }
        if (CfgRuleSuggestedAmountNodeEnum.LOCAL_PLAN_PURCHASE_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getLocalPurchaseDetails()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v -> !v.getEstimateSalesDate().isAfter(endDate))
                    .map(ReplenishmentResultDTO.EstimatedPurchaseDetailDTO::getShopPreQty)
                    .reduce(0, Math::addExact);
        }
        return 0;
    }

    /**
     * 获取预计采购库存
     *
     * @param replenishmentResultDTO 补货建议
     */
    private List<LocalInventoryDTO> getEstimatedPurchaseInventory(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        CfgRuleExpireTimeDTO.StrategyResultDTO expireTimeResult = cfgRuleStrategyDTO.getExpireTimeResult();
        List<String> localWarehouseIds = replenishmentResultDTO.getLocalWarehouseId();
        if (CollectionUtils.isEmpty(localWarehouseIds)) {
            return Collections.emptyList();
        }
        List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> detailList = new ArrayList<>();
        //采购计划和采购单的数据
        List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> purchaseList = replenishmentResultDTO.getInventoryDTO().getEstimatedPurchaseList()
                .stream().filter(v -> v.getSkuId().equals(replenishmentResultDTO.getReplenishment().getSkuId()))
                .filter(v -> localWarehouseIds.contains(v.getWarehouseId()))
                .map(ReplenishmentResultDTO.EstimatedPurchaseDetailDTO::buildEstimatedPurchaseDetailDTO)
                .collect(Collectors.toList());
        for (ReplenishmentResultDTO.EstimatedPurchaseDetailDTO detail : purchaseList) {
            detail.setEstimatedPutAwayDate(detail.getEstimatedPutAwayDate().plusDays(expireTimeResult.getPurchaseApproveDays())
                    .plusDays(expireTimeResult.getProductionDays()).plusDays(expireTimeResult.getSupplierDeliveryDays()).plusDays(expireTimeResult.getQcDays())
                    .plusDays(expireTimeResult.getPurchaseCycleDays()));
            if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType()) || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
                detail.setEstimateSalesDate(detail.getEstimatedPutAwayDate().plusDays(expireTimeResult.getLogisticsResult().getLogisticsDays()).plusDays(expireTimeResult.getInstockDays()));
            } else {
                detail.setEstimateSalesDate(detail.getEstimatedPutAwayDate());
            }
            detailList.add(detail);
        }
        //补货计划采购建议数据
        List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> purchaseSuggestList = replenishmentResultDTO.getInventoryDTO().getReplenishmentPurchaseList()
                .stream().filter(v -> replenishmentResultDTO.getReplenishment().getSkuId().equals(v.getSkuId()))
                .filter(v -> replenishmentResultDTO.getReplenishment().getShopId().equals(v.getShopId()))
                .map(ReplenishmentResultDTO.EstimatedPurchaseDetailDTO::buildEstimatedPurchaseDetailDTO)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(purchaseSuggestList)) {
            detailList.addAll(purchaseSuggestList);
        }
        replenishmentResultDTO.setLocalPurchaseDetails(detailList);
        return new ArrayList<>(detailList.parallelStream()
                .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty(), v.getSourceType()))
                .collect(Collectors.toMap(
                        v -> v.getWarehouseId() + ":" + v.getSourceType(),
                        v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty(), v.getSourceType()),
                        (existing, replacement) -> {
                            // 合并 qty
                            existing.setQty(existing.getQty() + replacement.getQty());
                            return existing;
                        }
                )).values());
    }

}
