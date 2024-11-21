package com.erp.server.mrp.calculation.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.enums.*;
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.model.wms.enums.DeliveryPlanTypeEnum;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
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
    private WmsOverseasWarehouseFeign wmsOverseasWarehouseFeign;

    @Override
    public int getFbaUsable(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes) {
        String code = String.join("+", codes);
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        return inventoryMapper.getFbaUsable(replenishmentResultDTO, code, SnapshotTableEnum.getTableName(SnapshotTableEnum.FBA_INVENTORY, calcDate));
    }

    @Override
    public int getFbaInTransit(ReplenishmentResultDTO replenishmentResultDTO, String code, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult) {
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<ReplenishmentResultDTO.FbaInTransitDetailDTO> inTransitDetails = new ArrayList<>();
        //1、若已生成头程物流单，货件--发货单--头程物流单：
        //已生成头程物流单，且已下单，则预计到货日期 = 头程物流单的下单时间 + 头程物流单上的预计时效
        //已生成头程物流单，但未下单，则预计到货时间 = 发货单的发货时间 + 本地发FBA时效 + FBA入库时间
        //2、若未生成头程物流单，已生成发货单，则预计到货时间 = 发货单的发货时间 + 本地发FBA时效 + FBA入库时间
        if (CfgRuleInventoryNodeEnum.FBA_DELIVERY.getCode().equals(code)) {
            //发FBA，签收数量取对应货件的签收数量 在途数量 = 发货单上的实发数量 - 签收数量；
            inTransitDetails = inventoryMapper.getFbaDelivery(replenishmentResultDTO,
                    SnapshotTableEnum.getTableName(FIRST_MILE_DELIVERY, calcDate),
                    SnapshotTableEnum.getTableName(FIRST_MILE_DELIVERY_DETAIL, calcDate),
                    SnapshotTableEnum.getTableName(FBA_SHIPMENT, calcDate),
                    SnapshotTableEnum.getTableName(FBA_SHIPMENT_DETAIL, calcDate)
            );
            if (CollectionUtils.isEmpty(inTransitDetails)) {
                return 0;
            }
            List<String> firstMileDeliveryIds = inTransitDetails.stream().map(ReplenishmentResultDTO.FbaInTransitDetailDTO::getSourceId).collect(Collectors.toList());
            //查询头程物流单
            List<LogisticsBillEntity> logisticsBills = inventoryMapper.getLogisticsBillBySourceIds(firstMileDeliveryIds, SnapshotTableEnum.getTableName(LOGISTICS_BILL, calcDate));
            for (ReplenishmentResultDTO.FbaInTransitDetailDTO detail : inTransitDetails) {
                LogisticsBillEntity logisticsBill = logisticsBills.stream().filter(v -> v.getSourceId().equals(detail.getSourceId()))
                        .filter(v -> !ObjectUtils.isEmpty(v.getOrderTime()))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isEmpty(logisticsBill)) {
                    detail.setEstimateSalesDate(detail.getDeliveryDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                } else {
                    detail.setEstimateSalesDate(logisticsBill.getOrderTime().toLocalDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                }
            }
        } else if (CfgRuleInventoryNodeEnum.FBA_SHIPMENT.getCode().equals(code)) {
            //FBA在途 =发货数量 - 签收数量
            inTransitDetails = inventoryMapper.getFbaShipment(replenishmentResultDTO,
                    SnapshotTableEnum.getTableName(FBA_SHIPMENT, calcDate),
                    SnapshotTableEnum.getTableName(FBA_SHIPMENT_DETAIL, calcDate)
            );
            if (CollectionUtils.isEmpty(inTransitDetails)) {
                return 0;
            }
            List<String> codes = inTransitDetails.stream().map(ReplenishmentResultDTO.FbaInTransitDetailDTO::getSourceCode).collect(Collectors.toList());
            List<FirstMileDeliveryDTO.FbaShipmentDTO> firstMileDeliveryList = inventoryMapper.listFirstMileDelivery(codes, SnapshotTableEnum.getTableName(FIRST_MILE_DELIVERY, calcDate),
                    SnapshotTableEnum.getTableName(FIRST_MILE_DELIVERY_DETAIL, calcDate));
            List<String> firstMileDeliveryIds = firstMileDeliveryList.stream().map(FirstMileDeliveryDTO.FbaShipmentDTO::getId).collect(Collectors.toList());
            List<LogisticsBillEntity> logisticsBills = inventoryMapper.getLogisticsBillBySourceIds(firstMileDeliveryIds, SnapshotTableEnum.getTableName(LOGISTICS_BILL, calcDate));
            for (ReplenishmentResultDTO.FbaInTransitDetailDTO detail : inTransitDetails) {
                FirstMileDeliveryDTO.FbaShipmentDTO fbaShipmentDTO = firstMileDeliveryList.stream()
                        .filter(v -> v.getFbaShipmentCode().equals(detail.getSourceCode()))
                        .findFirst()
                        .orElse(new FirstMileDeliveryDTO.FbaShipmentDTO());
                LogisticsBillEntity logisticsBill = logisticsBills.stream().filter(v -> v.getSourceId().equals(fbaShipmentDTO.getId()))
                        .filter(v -> !ObjectUtils.isEmpty(v.getOrderTime()))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isEmpty(logisticsBill)) {
                    detail.setEstimateSalesDate(fbaShipmentDTO.getApproveTime().toLocalDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                } else {
                    detail.setEstimateSalesDate(logisticsBill.getOrderTime().toLocalDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                }
            }

        }
        replenishmentResultDTO.setFbaInTransitDetails(inTransitDetails);
        return inTransitDetails.stream().map(ReplenishmentResultDTO.FbaInTransitDetailDTO::getInTransitQty)
                .reduce(0, Math::addExact);
    }

    public List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> getPlanDelivery(ReplenishmentResultDTO replenishmentResultDTO,
                                                                                   Set<String> strategyCodes,
                                                                                   CfgRuleStockUpDTO.StrategyResultDTO stockUpResult,
                                                                                   String sourceType,
                                                                                   ReplenishmentInventoryTypeEnum inventoryTypeEnum,
                                                                                   String type) {
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> estimatedDeliveryDetails = inventoryMapper.getPlanDelivery(type, strategyCodes,
                replenishmentResultDTO, SnapshotTableEnum.getTableName(WMS_DELIVERY_PLAN, calcDate),
                SnapshotTableEnum.getTableName(WMS_DELIVERY_PLAN_DETAIL, calcDate), sourceType);
        for (ReplenishmentResultDTO.EstimatedDeliveryDetailDTO detail : estimatedDeliveryDetails) {
            detail.setType(inventoryTypeEnum.getCode());
            detail.setEstimateSalesDate(detail.getEstimateSalesDate()
                    .plusDays(stockUpResult.getInstockDays())
                    .plusDays(stockUpResult.getLogisticsResult().getLogisticsDays())
                    .plusDays(stockUpResult.getLogisticsResult().getLogisticsCycleDays()));
            detail.setSourceType(SourceTypeEnum.DELIVERY_PLAN.getCode());
        }
        return estimatedDeliveryDetails;
    }

    @Override
    public int getOverseasUsable(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO) {

        List<OverseasProviderWarehouseDTO.ViewDTO> list = wmsOverseasWarehouseFeign.listByWarehouseIdList(replenishmentResultDTO.getOverseasWarehouseId());
        Map<String, String> codeMap = list.stream().collect(Collectors.toMap(OverseasProviderWarehouseDTO.ViewDTO::getPlatformWarehouseCode,
                OverseasProviderWarehouseDTO.ViewDTO::getWarehouseId, (o1, o2) -> o1));
        String code = String.join("+", codes);
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> overseasUsableDetail = new ArrayList<>();
        List<LocalInventoryDTO.OverseasInventoryDTO> invetoryOverseasList = inventoryMapper.getOverseasUsable(replenishmentResultDTO, code, getTableName(OVERSEAS_INVENTORY, calcDate), codeMap.keySet());
        List<LocalInventoryDTO> invetoryList = invetoryOverseasList.stream()
                .map(v -> new LocalInventoryDTO(codeMap.get(v.getWarehouseCode()), v.getQty()))
                .collect(Collectors.toList());
        int qty = getAllocateQty(replenishmentResultDTO, cfgRuleStrategyDTO.getWarehouseResult().getOverseasWarehouseList(), invetoryList, overseasUsableDetail,
                ReplenishmentInventoryTypeEnum.OVERSEAS_USABLE, CfgRuleWarehouseTypeEnum.OVERSEAS);
        replenishmentResultDTO.setOverseasUsableDetail(overseasUsableDetail);
        return qty;
    }

    /**
     * 分摊平台销量或店铺库存
     *
     * @param replenishmentResultDTO 建议
     * @param warehouseList          仓库
     * @param inventoryList          仓库库存
     * @param inventoryDetail        库存详情
     */
    private int getAllocateQty(ReplenishmentResultDTO replenishmentResultDTO,
                               List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> warehouseList,
                               List<LocalInventoryDTO> inventoryList,
                               List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> inventoryDetail,
                               ReplenishmentInventoryTypeEnum inventoryType,
                               CfgRuleWarehouseTypeEnum warehouseType) {
        inventoryList = inventoryList.stream()
                .filter(dto -> dto.getQty() > 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(inventoryList)) {
            return 0;
        }
        int qty = 0;
        for (LocalInventoryDTO dto : inventoryList) {
            if (ObjectUtils.isEmpty(dto.getWarehouseId())) {
                inventoryDetail.add(ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO.buildReplenishmentInventoryDetailDTO(
                        inventoryType.getCode(), dto.getQty(),
                        Collections.singletonList(new ReplenishmentResultDTO.ShopInventoryDetailDTO(replenishmentResultDTO.getReplenishment().getShopId(), new BigDecimal(dto.getQty())))));
                qty += dto.getQty();
            } else {
                // 获取平台对应的店铺和历史销量
                Map<String, List<String>> platformShop = getPlatformShop(warehouseList,
                        replenishmentResultDTO.getShopIdByPlatform(), dto.getWarehouseId());
                Map<String, Integer> platformShopSalesMap = getPlatformShopSalesMap(replenishmentResultDTO, platformShop);
                int totalSaleQty = platformShopSalesMap.values().stream().reduce(0, Math::addExact);
                List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> warehouses = filterWarehouseList(warehouseList, warehouseType, dto.getWarehouseId());
                BigDecimal platformQtyCount = BigDecimal.ZERO;
                int i = 0;
                for (CfgRuleWarehouseDTO.StrategyDetailResultDTO result : warehouses) {
                    int platformSaleQty = calculatePlatformSaleQty(result, platformShop, platformShopSalesMap);
                    BigDecimal platformQty = calculateInventoryDistribution(dto.getQty(), platformSaleQty, totalSaleQty, platformQtyCount, i == warehouses.size() - 1);

                    List<ReplenishmentResultDTO.ShopInventoryDetailDTO> shopSaleQtyList = createShopSaleQtyList(platformShop, result.getDictPlatform(), platformShopSalesMap);
                    List<ReplenishmentResultDTO.ShopInventoryDetailDTO> detailDTOS = new ArrayList<>();
                    if (CfgRuleInventoryAllocateTypeEnum.SHARE.getCode().equals(result.getInventoryAllocateType())) {
                        if (result.getDictPlatform().equals(replenishmentResultDTO.getReplenishment().getPlatform())) {
                            qty += platformQty.intValue();
                        }
                    } else {
                        qty += calculateInventoryQty(replenishmentResultDTO, platformQty, platformSaleQty, shopSaleQtyList, detailDTOS);
                    }
                    inventoryDetail.add(ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO.buildReplenishmentInventoryDetailDTO(
                            inventoryType.getCode(), result, dto.getQty(), platformQty.intValue(), detailDTOS));
                    platformQtyCount = platformQtyCount.add(platformQty);
                    i++;
                }
            }
        }
        return qty;
    }

    /**
     * 过滤仓库方法
     * @param warehouseList 仓库列表
     * @param warehouseType 仓库类型
     * @param warehouseId   仓库id
     */
    private List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> filterWarehouseList(List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> warehouseList,
                                                                                  CfgRuleWarehouseTypeEnum warehouseType,
                                                                                  String warehouseId) {
        return warehouseList.stream()
                .filter(v -> CfgRuleWarehouseTypeEnum.VIRTUAL.equals(warehouseType) ?
                        v.getVirtualWarehouseId().equals(warehouseId) :
                        v.getWarehouseId().equals(warehouseId))
                .collect(Collectors.toList());
    }

    /**
     * 获取平台对应店铺
     *
     * @param warehouseList    仓库配置
     * @param shopIdByPlatform 平台店铺
     */
    private Map<String, List<String>> getPlatformShop(List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> warehouseList,
                                                      Map<String, List<String>> shopIdByPlatform, String warehouseId) {
        return warehouseList.stream()
                .filter(v -> warehouseId.equals(v.getWarehouseId()))
                .collect(Collectors.toMap(CfgRuleWarehouseDTO.StrategyDetailResultDTO::getDictPlatform, v -> {
                    if (VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(v.getChannelType())) {
                        return shopIdByPlatform.get(v.getDictPlatform());
                    } else {
                        return v.getChannelIdJson().stream().map(Object::toString).collect(Collectors.toList());
                    }
                }));
    }

    /**
     * 获取店铺销量
     *
     * @param replenishmentResultDTO 建议
     * @param platformShop           平台店铺
     */
    private Map<String, Integer> getPlatformShopSalesMap(ReplenishmentResultDTO replenishmentResultDTO, Map<String, List<String>> platformShop) {
        Set<String> allShopIds = platformShop.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        return Optional.ofNullable(replenishmentResultDTO.getShopSalesMap()).orElse(new HashMap<>()).entrySet().stream()
                .filter(e -> allShopIds.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 计算平台销量
     *
     * @param result               海外仓配置
     * @param platformShop         平台店铺映射
     * @param platformShopSalesMap 平台店铺销量
     */
    private int calculatePlatformSaleQty(CfgRuleWarehouseDTO.StrategyDetailResultDTO result, Map<String, List<String>> platformShop, Map<String, Integer> platformShopSalesMap) {
        return Optional.ofNullable(platformShop.get(result.getDictPlatform())).orElse(new ArrayList<>()).stream()
                .map(v -> Optional.ofNullable(platformShopSalesMap.get(v)).orElse(0))
                .reduce(0, Math::addExact);
    }

    /**
     * 分摊平台库存
     *
     * @param totalQty         总库存
     * @param platformSaleQty  平台总销量
     * @param totalSaleQty     总销量
     * @param platformQtyCount 已分摊数量
     * @param isLastPlatform   是否最后一个平台
     */
    private BigDecimal calculateInventoryDistribution(int totalQty, int platformSaleQty, int totalSaleQty, BigDecimal platformQtyCount, boolean isLastPlatform) {
        if (isLastPlatform) {
            return new BigDecimal(totalQty).subtract(platformQtyCount);
        } else {
            return new BigDecimal(totalQty)
                    .multiply(new BigDecimal(platformSaleQty))
                    .divide(new BigDecimal(0 == totalSaleQty ? 1 : totalSaleQty), 2, RoundingMode.HALF_UP)
                    .setScale(0, RoundingMode.FLOOR);
        }
    }

    /**
     * 组装数据
     *
     * @param platformShop         平台店铺映射
     * @param dictPlatform         平台
     * @param platformShopSalesMap 平台店铺销量
     */
    private List<ReplenishmentResultDTO.ShopInventoryDetailDTO> createShopSaleQtyList(Map<String, List<String>> platformShop, String dictPlatform, Map<String, Integer> platformShopSalesMap) {
        return Optional.ofNullable(platformShop.get(dictPlatform)).orElse(new ArrayList<>()).stream()
                .map(v -> new ReplenishmentResultDTO.ShopInventoryDetailDTO(v, new BigDecimal(Optional.ofNullable(platformShopSalesMap.get(v)).orElse(0))))
                .collect(Collectors.toList());
    }

    /**
     * 分摊库存
     *
     * @param replenishmentResultDTO 建议
     * @param platformQty            平台数量
     * @param platformSaleQty        平台销量
     * @param shopSaleQtyList        店铺销量
     * @param detailDTOS             明细
     */
    private int calculateInventoryQty(ReplenishmentResultDTO replenishmentResultDTO, BigDecimal platformQty, int platformSaleQty,
                                      List<ReplenishmentResultDTO.ShopInventoryDetailDTO> shopSaleQtyList,
                                      List<ReplenishmentResultDTO.ShopInventoryDetailDTO> detailDTOS) {
        int qty = 0;
        BigDecimal otherSales = BigDecimal.ZERO;
        for (int i = 0; i < shopSaleQtyList.size(); i++) {
            BigDecimal shopQty;
            if (i == shopSaleQtyList.size() - 1) {
                shopQty = platformQty.subtract(otherSales);
            } else {
                shopQty = platformQty.multiply(shopSaleQtyList.get(i).getQty()).divide(new BigDecimal(0 == platformSaleQty ? 1 : platformSaleQty), 2, RoundingMode.HALF_UP).setScale(0, RoundingMode.FLOOR);
                otherSales = otherSales.add(shopQty);
            }
            detailDTOS.add(new ReplenishmentResultDTO.ShopInventoryDetailDTO(shopSaleQtyList.get(i).getShopId(), shopQty));
            if (shopSaleQtyList.get(i).getShopId().equals(replenishmentResultDTO.getReplenishment().getShopId())) {
                qty += shopQty.intValue();
            }
        }
        return qty;
    }


    @Override
    @SuppressWarnings("all")
    public int getLocalUsable(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult = cfgRuleStrategyDTO.getWarehouseResult();
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localUsableDetail = new ArrayList<>();
        // 根据是否启用虚拟库存获取对应的库存列表
        List<LocalInventoryDTO> inventoryList = Boolean.TRUE.equals(warehouseResult.getIsEnableVirtual())
                ? inventoryMapper.getVirtualUsable(replenishmentResultDTO.getReplenishment().getSkuId(), codes, getTableName(VIRTUAL_INVENTORY, calcDate))
                : inventoryMapper.getLocalUsable(replenishmentResultDTO.getReplenishment().getSkuId(), codes, getTableName(INVENTORY, calcDate));
        CfgRuleWarehouseTypeEnum warehouseType = Boolean.TRUE.equals(warehouseResult.getIsEnableVirtual()) ? CfgRuleWarehouseTypeEnum.VIRTUAL : CfgRuleWarehouseTypeEnum.LOCAL;
        int qty = getAllocateQty(replenishmentResultDTO, cfgRuleStrategyDTO.getWarehouseResult().getLocalWarehouseList(),
                inventoryList, localUsableDetail, ReplenishmentInventoryTypeEnum.LOCAL_USABLE, warehouseType);
        replenishmentResultDTO.setLocalUsableDetail(localUsableDetail);
        return qty;
    }

    @Override
    public int getLocalInTransit(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localInTransitDetail = new ArrayList<>();
        List<LocalInventoryDTO> invetoryList = getLocalInTransitInventory(replenishmentResultDTO, codes, cfgRuleStrategyDTO.getStockUpResult());
        int qty = getAllocateQty(replenishmentResultDTO, cfgRuleStrategyDTO.getWarehouseResult().getLocalWarehouseList(),
                invetoryList, localInTransitDetail, ReplenishmentInventoryTypeEnum.LOCAL_IN_TRANSIT, CfgRuleWarehouseTypeEnum.LOCAL);
        replenishmentResultDTO.setLocalInTransitDetail(localInTransitDetail);
        return qty;
    }

    /**
     * 获取本地在途库存
     *
     * @param replenishmentResultDTO 建议
     * @param codes                  单据状态
     * @param stockUpResult          备货配置
     */
    private List<LocalInventoryDTO> getLocalInTransitInventory(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult) {
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        String platformType = replenishmentResultDTO.getReplenishment().getPlatformType();
        boolean isPurchase = codes.contains(CfgRuleInventoryNodeEnum.LOCAL_IN_TRANSIT_PURCHASE.getCode());
        boolean isTransfer = codes.contains(CfgRuleInventoryNodeEnum.LOCAL_IN_TRANSIT_TRANSFER.getCode());
        List<String> localWarehouseIds = replenishmentResultDTO.getLocalWarehouseId();
        if (CollectionUtils.isEmpty(localWarehouseIds)) {
            return Collections.emptyList();
        }
        List<ReplenishmentResultDTO.LocalInTransitDetailDTO> localInTransitDetails = inventoryMapper.getLocalInTransitDetail(isPurchase, isTransfer, replenishmentResultDTO.getReplenishment().getSkuId(), localWarehouseIds, getTableName(TRANSACTION_FLOW, calcDate),
                getTableName(INSTOCK_FORCAST, calcDate), getTableName(PO_RECEIVE, calcDate), getTableName(PO_INSTOCK, calcDate), getTableName(PO_RETURN, calcDate),
                getTableName(TRANSFER_OUT, calcDate), getTableName(TRANSFER_IN, calcDate));
        for (ReplenishmentResultDTO.LocalInTransitDetailDTO dto : localInTransitDetails) {
            //预计入库日期 = 采购订单的审核日期 + 生产周期 + 供应商发货时长 + 质检入库时长
            dto.setEstimatedPutAwayDate(dto.getEstimatedPutAwayDate()
                    .plusDays(stockUpResult.getPurchaseApproveDays())
                    .plusDays(stockUpResult.getProductionDays())
                    .plusDays(stockUpResult.getSupplierDeliveryDays()).plusDays(stockUpResult.getQcDays()));
            if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(platformType) || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(platformType)) {
                //预计到货日期（Amazon） = 预计入库日期 + 本地发FBA时效 + FBA入库时间
                //预计到货日期（海外） = 预计入库日期 + 本地发海外时效 + 海外仓入库时间
                dto.setEstimateSalesDate(dto.getEstimatedPutAwayDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
            } else if (CfgRulePlatformTypeEnum.B2B.getCode().equals(platformType) || CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(platformType)) {
                //预计到货日期（本地）= 预计入库日期
                dto.setEstimateSalesDate(dto.getEstimatedPutAwayDate());
            }
        }
        replenishmentResultDTO.setLocalInTransitDetails(localInTransitDetails);
        return new ArrayList<>(localInTransitDetails.parallelStream()
                .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()))
                .collect(Collectors.toMap(
                        LocalInventoryDTO::getWarehouseId,
                        v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()),
                        (existing, replacement) -> {
                            // 合并 qty
                            existing.setQty(existing.getQty() + replacement.getQty());
                            return existing;
                        }
                )).values());
    }

    @Override
    public int getLocalPurchase(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localPurchaseDetail = new ArrayList<>();
        List<LocalInventoryDTO> invetoryList = getEstimatedPurchaseInventory(replenishmentResultDTO, cfgRuleStrategyDTO);
        int qty = getAllocateQty(replenishmentResultDTO, cfgRuleStrategyDTO.getWarehouseResult().getLocalWarehouseList(),
                invetoryList, localPurchaseDetail, ReplenishmentInventoryTypeEnum.LOCAL_ESTIMATED_DELIVERY, CfgRuleWarehouseTypeEnum.LOCAL);
        replenishmentResultDTO.setLocalPurchaseDetail(localPurchaseDetail);
        return qty;
    }

    @Override
    public void saveAllHistoryInventory(LocalDate calculationDate, String calcDate) {
        //清洗每日库存到历史表
        List<FbaInventoryEntity> inventoryEntities = inventoryMapper.getAllFbaHistoryInventory(SnapshotTableEnum.getTableName(SnapshotTableEnum.FBA_INVENTORY, calcDate));
        if (!CollectionUtils.isEmpty(inventoryEntities)) {
            fbaHistoryInventoryService.saveTodayInventory(inventoryEntities, calculationDate);
        }
        List<OverseasInventoryEntity> overseasHistoryInventory = inventoryMapper.getAllOverseasHistoryInventory(SnapshotTableEnum.getTableName(SnapshotTableEnum.OVERSEAS_INVENTORY, calcDate));
        if (!CollectionUtils.isEmpty(overseasHistoryInventory)) {
            overseasHistoryInventoryService.saveTodayInventory(overseasHistoryInventory, calculationDate);
        }
        List<InventoryEntity> localHistoryInventory = inventoryMapper.getAllLocalHistoryInventory(SnapshotTableEnum.getTableName(SnapshotTableEnum.INVENTORY, calcDate));
        if (!CollectionUtils.isEmpty(localHistoryInventory)) {
            localHistoryInventoryService.saveTodayInventory(localHistoryInventory, calculationDate);
        }
        List<VirtualInventoryEntity> virtualInventory = inventoryMapper.getAllVirtualHistoryInventory(SnapshotTableEnum.getTableName(SnapshotTableEnum.VIRTUAL_INVENTORY, calcDate));
        if (!CollectionUtils.isEmpty(virtualInventory)) {
            virtualInventoryHistoryService.saveTodayInventory(virtualInventory, calculationDate);
        }
    }

    @Override
    public void checkAllTableExists(String calcDate) {
        List<String> tableList = new ArrayList<>();
        for (SnapshotTableEnum value : SnapshotTableEnum.values()) {
            boolean exist = inventoryMapper.isTableExist(getTableName(value, calcDate));
            if (Boolean.FALSE.equals(exist)) {
                tableList.add(value.getCode());
            }
        }
        if (!CollectionUtils.isEmpty(tableList)) {
            throw new ServiceException(ApiError.ERROR_TABLE_NOT_EXIST, String.join(",", tableList));
        }
    }

    @Override
    public int getInventory(ReplenishmentResultDTO replenishmentResultDTO, LocalDate endDate, Set<String> deliveryVolumeInventory, CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult) {
        return deliveryVolumeInventory.stream()
                .map(code -> getInventoryByCode(code, replenishmentResultDTO, endDate, warehouseResult))
                .reduce(0, Math::addExact);
    }

    @Override
    public List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> getReplenishmentPlan(ReplenishmentResultDTO replenishmentResultDTO, Set<String> replenishmentPlan,
                                                                                        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult,
                                                                                        ReplenishmentInventoryTypeEnum inventoryTypeEnum) {
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> estimatedDeliveryDetails = inventoryMapper.getReplenishmentPlan(replenishmentPlan,
                replenishmentResultDTO, SnapshotTableEnum.getTableName(DELIVERY_SUGGEST, calcDate), SnapshotTableEnum.getTableName(WMS_DELIVERY_PLAN, calcDate),
                SnapshotTableEnum.getTableName(WMS_DELIVERY_PLAN_DETAIL, calcDate));
        for (ReplenishmentResultDTO.EstimatedDeliveryDetailDTO detail : estimatedDeliveryDetails) {
            detail.setType(inventoryTypeEnum.getCode());
            detail.setSourceType(SourceTypeEnum.REPLENISHMENT_PLAN.getCode());
        }
        return estimatedDeliveryDetails;
    }

    @Override
    public int getOverseasInTransit(ReplenishmentResultDTO replenishmentResultDTO, String code, CfgRuleStrategyDTO cfgRuleStrategyDTO) {

        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> overseasInTransitDetail = new ArrayList<>();
        List<LocalInventoryDTO> invetoryList = getOverseasInTransitInventory(replenishmentResultDTO, cfgRuleStrategyDTO.getStockUpResult());
        int qty = getAllocateQty(replenishmentResultDTO, cfgRuleStrategyDTO.getWarehouseResult().getOverseasWarehouseList(), invetoryList,
                overseasInTransitDetail, ReplenishmentInventoryTypeEnum.OVERSEAS_IN_TRANSIT, CfgRuleWarehouseTypeEnum.OVERSEAS);
        replenishmentResultDTO.setOverseasInTransitDetail(overseasInTransitDetail);
        return qty;
    }

    @Override
    public int getOverseasPlanDelivery(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> overseasPlanDeliveryDetail = new ArrayList<>();
        List<LocalInventoryDTO> invetoryList = getOverseasPlanDeliveryInventory(replenishmentResultDTO, cfgRuleStrategyDTO);
        int qty = getAllocateQty(replenishmentResultDTO, cfgRuleStrategyDTO.getWarehouseResult().getOverseasWarehouseList(), invetoryList,
                overseasPlanDeliveryDetail, ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY, CfgRuleWarehouseTypeEnum.OVERSEAS);
        replenishmentResultDTO.setOverseasDeliveryDetail(overseasPlanDeliveryDetail);
        return qty;
    }

    @Override
    public List<InventoryReportDTO.TransportPagingDTO> listLocalInTransit(LocalDate calculationDate) {
        return inventoryMapper.listLocalInTransit(getTableName(TRANSACTION_FLOW, calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE)));
    }

    private List<LocalInventoryDTO> getOverseasPlanDeliveryInventory(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO) {

        String baseKey = CfgRuleCommonTypeEnum.getBaseInventoryRedisKey(replenishmentResultDTO.getReplenishment().getPlatformType());
        List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> estimatedDeliveryDetails = new ArrayList<>();
        //获取需要计算库存的FBA预计发货配置
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        //补货计划
        Set<String> replenishmentPlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getOverseasReplenishmentPlan());
        if (!CollectionUtils.isEmpty(replenishmentPlan)) {
            List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> planDelivery = getReplenishmentPlan(replenishmentResultDTO, replenishmentPlan, cfgRuleStrategyDTO.getStockUpResult(),
                    ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY);
            if (!CollectionUtils.isEmpty(planDelivery)) {
                estimatedDeliveryDetails.addAll(planDelivery);
            }
        }
        //发货计划_补货计划下推
        Set<String> replenishmentDeliveryPlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getOverseasDeliveryPlanByReplenishment());
        if (!CollectionUtils.isEmpty(replenishmentDeliveryPlan)) {
            List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> planDelivery = getPlanDelivery(replenishmentResultDTO, replenishmentDeliveryPlan, cfgRuleStrategyDTO.getStockUpResult(),
                    SourceTypeEnum.REPLENISHMENT_PLAN.getCode(), ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY, DeliveryPlanTypeEnum.THIRD_WAREHOUSE.getCode());
            if (!CollectionUtils.isEmpty(planDelivery)) {
                estimatedDeliveryDetails.addAll(planDelivery);
            }
        }
        //发货计划_手动新增
        Set<String> codes = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getOverseasDeliveryPlanByManual());
        if (!CollectionUtils.isEmpty(codes)) {
            List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> planDelivery = getPlanDelivery(replenishmentResultDTO, codes, cfgRuleStrategyDTO.getStockUpResult(),
                    SourceTypeEnum.DELIVERY_PLAN.getCode(), ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY, DeliveryPlanTypeEnum.THIRD_WAREHOUSE.getCode());
            if (!CollectionUtils.isEmpty(planDelivery)) {
                estimatedDeliveryDetails.addAll(planDelivery);
            }
        }
        replenishmentResultDTO.setOverseasDeliveryDetails(estimatedDeliveryDetails);
        return new ArrayList<>(estimatedDeliveryDetails.parallelStream()
                .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()))
                .collect(Collectors.toMap(
                        LocalInventoryDTO::getWarehouseId,
                        v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()),
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
     * @param stockUpResult          备货配置
     */
    private List<LocalInventoryDTO> getOverseasInTransitInventory(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult) {
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        //1、若已生成头程物流单，货件--发货单--头程物流单：
        //已生成头程物流单，且已下单，则预计到货日期 = 头程物流单的下单时间 + 头程物流单上的预计时效
        //已生成头程物流单，但未下单，则预计到货时间 = 发货单的发货时间 + 本地发FBA时效 + 海外仓入库时间
        //2、若未生成头程物流单，已生成发货单，则预计到货时间 = 发货单的发货时间 + 本地发海外仓时效 + 海外仓入库时间
        List<ReplenishmentResultDTO.OverseasInTransitDetailDTO> inTransitDetails = inventoryMapper.getOverseasDelivery(replenishmentResultDTO,
                SnapshotTableEnum.getTableName(FIRST_MILE_DELIVERY, calcDate),
                SnapshotTableEnum.getTableName(FIRST_MILE_DELIVERY_DETAIL, calcDate),
                SnapshotTableEnum.getTableName(OVERSEAS_WAREHOUSE_INBOUND, calcDate),
                SnapshotTableEnum.getTableName(OVERSEAS_WAREHOUSE_INBOUND_DETAIL, calcDate)

        );
        if (CollectionUtils.isEmpty(inTransitDetails)) {
            return Collections.emptyList();
        }
        List<String> firstMileDeliveryIds = inTransitDetails.stream().map(ReplenishmentResultDTO.OverseasInTransitDetailDTO::getDeliveryPlanId).collect(Collectors.toList());
        //查询头程物流单
        List<LogisticsBillEntity> logisticsBills = inventoryMapper.getLogisticsBillBySourceIds(firstMileDeliveryIds, SnapshotTableEnum.getTableName(LOGISTICS_BILL, calcDate));
        for (ReplenishmentResultDTO.OverseasInTransitDetailDTO detail : inTransitDetails) {
            LogisticsBillEntity logisticsBill = logisticsBills.stream().filter(v -> v.getSourceId().equals(detail.getDeliveryPlanId()))
                    .filter(v -> !ObjectUtils.isEmpty(v.getOrderTime()))
                    .findFirst()
                    .orElse(null);
            if (ObjectUtils.isEmpty(logisticsBill)) {
                detail.setEstimateSalesDate(detail.getDeliveryDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
            } else {
                detail.setEstimateSalesDate(logisticsBill.getOrderTime().toLocalDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
            }
        }
        replenishmentResultDTO.setOverseasInTransitDetails(inTransitDetails);
        return new ArrayList<>(inTransitDetails.parallelStream()
                .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getInTransitQty()))
                .collect(Collectors.toMap(
                        LocalInventoryDTO::getWarehouseId,
                        v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()),
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
     * @param warehouseResult        仓库配置
     */
    private int getInventoryByCode(String code, ReplenishmentResultDTO replenishmentResultDTO, LocalDate endDate,
                                   CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult) {
        if (CfgRuleSuggestedAmountNodeEnum.FBA_USABLE_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getReplenishmentDetail().getFbaUsableQty()).orElse(0);
        }
        if (CfgRuleSuggestedAmountNodeEnum.OVERSEAS_USABLE_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getReplenishmentDetail().getOverseasUsableQty()).orElse(0);
        }
        if (CfgRuleSuggestedAmountNodeEnum.LOCAL_USABLE_QTY.getCode().equals(code)) {
            return Optional.ofNullable(replenishmentResultDTO.getReplenishmentDetail().getLocalUsableQty()).orElse(0);
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
        List<LocalInventoryDTO> invetoryList = new ArrayList<>();
        List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> warehouseList = new ArrayList<>();
        if (CfgRuleSuggestedAmountNodeEnum.OVERSEAS_IN_TRANSIT_QTY.getCode().equals(code)) {
            invetoryList = Optional.ofNullable(replenishmentResultDTO.getOverseasInTransitDetails()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v -> !v.getEstimateSalesDate().isAfter(endDate))
                    .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getInTransitQty()))
                    .collect(Collectors.toList());
            warehouseList = warehouseResult.getOverseasWarehouseList();
        }
        if (CfgRuleSuggestedAmountNodeEnum.OVERSEAS_PLAN_DELIVERY_QTY.getCode().equals(code)) {
            invetoryList = Optional.ofNullable(replenishmentResultDTO.getOverseasDeliveryDetails()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v -> !v.getEstimateSalesDate().isAfter(endDate))
                    .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()))
                    .collect(Collectors.toList());
            warehouseList = warehouseResult.getOverseasWarehouseList();
        }
        if (CfgRuleSuggestedAmountNodeEnum.LOCAL_IN_TRANSIT_QTY.getCode().equals(code)) {
            invetoryList = Optional.ofNullable(replenishmentResultDTO.getLocalInTransitDetails()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v -> !v.getEstimateSalesDate().isAfter(endDate))
                    .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()))
                    .collect(Collectors.toList());
            warehouseList = warehouseResult.getLocalWarehouseList();
        }
        if (CfgRuleSuggestedAmountNodeEnum.LOCAL_PLAN_PURCHASE_QTY.getCode().equals(code)) {
            invetoryList = Optional.ofNullable(replenishmentResultDTO.getLocalPurchaseDetails()).orElse(new ArrayList<>())
                    .stream()
                    .filter(v -> !v.getEstimateSalesDate().isAfter(endDate))
                    .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()))
                    .collect(Collectors.toList());
            warehouseList = warehouseResult.getLocalWarehouseList();
        }
        return getInventory(replenishmentResultDTO, invetoryList, warehouseList);
    }

    /**
     * 分摊
     *
     * @param replenishmentResultDTO 参数
     * @param invetoryList           库存
     * @param warehouseList          仓库
     */
    private int getInventory(ReplenishmentResultDTO replenishmentResultDTO, List<LocalInventoryDTO> invetoryList, List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> warehouseList) {
        int inventory = 0;
        if (!CollectionUtils.isEmpty(invetoryList)) {
            for (LocalInventoryDTO dto : invetoryList) {
                if (ObjectUtils.isEmpty(dto.getWarehouseId())) {
                    inventory += dto.getQty();
                } else {
                    Map<String, List<String>> platformShop = getPlatformShop(warehouseList, replenishmentResultDTO.getShopIdByPlatform(), dto.getWarehouseId());
                    Map<String, Integer> platformShopSalesMap = getPlatformShopSalesMap(replenishmentResultDTO, platformShop);
                    int totalSaleQty = platformShopSalesMap.values().stream().reduce(0, Math::addExact);
                    BigDecimal platformQtyCount = BigDecimal.ZERO;
                    int i = 0;
                    List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> newWarehouseList = warehouseList
                            .stream()
                            .filter(v -> v.getWarehouseId().equals(dto.getWarehouseId()))
                            .collect(Collectors.toList());
                    for (CfgRuleWarehouseDTO.StrategyDetailResultDTO result : newWarehouseList) {
                        int platformSaleQty = calculatePlatformSaleQty(result, platformShop, platformShopSalesMap);
                        BigDecimal platformQty = calculateInventoryDistribution(dto.getQty(), platformSaleQty, totalSaleQty, platformQtyCount, i == newWarehouseList.size() - 1);

                        List<ReplenishmentResultDTO.ShopInventoryDetailDTO> shopSaleQtyList = createShopSaleQtyList(platformShop, result.getDictPlatform(), platformShopSalesMap);
                        List<ReplenishmentResultDTO.ShopInventoryDetailDTO> detailDTOS = new ArrayList<>();
                        if (CfgRuleInventoryAllocateTypeEnum.SHARE.getCode().equals(result.getInventoryAllocateType())) {
                            if (result.getDictPlatform().equals(replenishmentResultDTO.getReplenishment().getPlatform())) {
                                inventory += platformQty.intValue();
                            }
                        } else {
                            inventory += calculateInventoryQty(replenishmentResultDTO, platformQty, platformSaleQty, shopSaleQtyList, detailDTOS);
                        }
                        platformQtyCount = platformQtyCount.add(platformQty);
                        i++;
                    }
                }
            }
        }
        return inventory;
    }

    /**
     * 获取预计采购库存
     *
     * @param replenishmentResultDTO 补货建议
     */
    private List<LocalInventoryDTO> getEstimatedPurchaseInventory(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> detailList = new ArrayList<>();
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        String baseKey = CfgRuleCommonTypeEnum.getBaseInventoryRedisKey(replenishmentResultDTO.getReplenishment().getPlatformType());
        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = cfgRuleStrategyDTO.getStockUpResult();
        List<String> localWarehouseIds = replenishmentResultDTO.getLocalWarehouseId();
        if (CollectionUtils.isEmpty(localWarehouseIds)) {
            return Collections.emptyList();
        }
        Set<String> localReplenishmentPlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalReplenishmentPlan());
        if (!CollectionUtils.isEmpty(localReplenishmentPlan)) {
            // todo 本地补货计划
        }

        Set<String> localPurchasePlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalPurchasePlan());
        if (!CollectionUtils.isEmpty(localPurchasePlan)) {
            List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> applications = inventoryMapper.listPurchasePlan(localPurchasePlan, replenishmentResultDTO.getReplenishment().getSkuId(), localWarehouseIds, getTableName(PURCHASE_APPLICATION, calcDate), getTableName(PURCHASE_APPLICATION_DETAIL, calcDate));
            //查询关联采购
            List<String> detailIds = applications.stream().map(ReplenishmentResultDTO.EstimatedPurchaseDetailDTO::getDetailId).collect(Collectors.toList());
            List<PurchaseApplicationRefPoDTO.ListDTO> refList = null;
            List<SubcontractOrderDetailEntity> subcontractOrderDetailList = null;
            if (!CollectionUtils.isEmpty(detailIds)) {
                refList = inventoryMapper.listPurchaseApplicationRefPo(detailIds, getTableName(PURCHASE_ORDER, calcDate), getTableName(PURCHASE_ORDER_DETAIL, calcDate), getTableName(PURCHASE_APPLICATION_REF_PO, calcDate));
                subcontractOrderDetailList = inventoryMapper.listSubcontractOrderDetail(detailIds, getTableName(SUBCONTRACT_ORDER, calcDate), getTableName(SUBCONTRACT_ORDER_DETAIL, calcDate), getTableName(PURCHASE_ORDER_DETAIL, calcDate));
            }
            //已下推委外订单的数量
            //处理已审核 & 部分生成 数据
            for (ReplenishmentResultDTO.EstimatedPurchaseDetailDTO application : applications) {
                if (ApproveStatusEnum.APPROVE.getCode().equals(application.getStatus()) && CreatePoTypeEnum.PARTIAL_GENERATED.getStatus().equals(application.getCreatePoType())) {
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
            if (!CollectionUtils.isEmpty(applications)) {
                detailList.addAll(applications);
            }
        }
        Set<String> localPurchaseOrder = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalPurchaseOrder());
        if (!CollectionUtils.isEmpty(localPurchaseOrder)) {
            List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> purchaseDetails = inventoryMapper.listPurchase(localPurchaseOrder, replenishmentResultDTO.getReplenishment().getSkuId(), localWarehouseIds, getTableName(PURCHASE_ORDER, calcDate), getTableName(PURCHASE_ORDER_DETAIL, calcDate));
            if (!CollectionUtils.isEmpty(purchaseDetails)) {
                detailList.addAll(purchaseDetails);
            }
        }
        for (ReplenishmentResultDTO.EstimatedPurchaseDetailDTO detail : detailList) {
            detail.setType(ReplenishmentInventoryTypeEnum.LOCAL_ESTIMATED_DELIVERY.getCode());
            detail.setEstimatedPutAwayDate(detail.getEstimatedPutAwayDate().plusDays(stockUpResult.getPurchaseApproveDays())
                    .plusDays(stockUpResult.getProductionDays()).plusDays(stockUpResult.getSupplierDeliveryDays()).plusDays(stockUpResult.getQcDays())
                    .plusDays(stockUpResult.getPurchaseCycleDays()));
            if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType()) || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
                detail.setEstimateSalesDate(detail.getEstimatedPutAwayDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
            } else {
                detail.setEstimateSalesDate(detail.getEstimatedPutAwayDate());
            }
        }
        replenishmentResultDTO.setLocalPurchaseDetails(detailList);
        return new ArrayList<>(detailList.parallelStream()
                .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()))
                .collect(Collectors.toMap(
                        LocalInventoryDTO::getWarehouseId,
                        v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()),
                        (existing, replacement) -> {
                            // 合并 qty
                            existing.setQty(existing.getQty() + replacement.getQty());
                            return existing;
                        }
                )).values());
    }

}
