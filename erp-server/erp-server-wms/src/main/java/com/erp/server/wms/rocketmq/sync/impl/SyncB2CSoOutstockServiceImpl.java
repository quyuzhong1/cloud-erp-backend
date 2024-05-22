package com.erp.server.wms.rocketmq.sync.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.item.KingdeeDeliveryDetailItemEntity;
import com.erp.model.dmp.wdt.WangDianOrderEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.SyncKingdeeDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockRuleDTO;
import com.erp.model.wms.dto.inventory.TransactionRuleDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.PackingStatusEnum;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;
import com.erp.server.wms.service.InventoryTransCoreService;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname SyncB2CSoOutstockServiceImpl
 * @Date 2023-06-27 10:54
 * @Created by yl
 */
@Service
public class SyncB2CSoOutstockServiceImpl implements SyncB2CSoOutstockService {

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private SyncKingdeeSoOutstockService syncKingdeeSoOutstockService;

    /**
     * 同步金蝶的销售出库单
     *
     * @param entity
     * @return void
     * @author yl
     * @date 2023-06-27 14:05
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncKingdeeSoOutstock(KingdeeDeliveryDetailEntity entity) {
        List<KingdeeDeliveryDetailItemEntity> kingdeeDetailList = entity.getKingdeeOutStockItemEntityList();
        if (CollectionUtils.isEmpty(kingdeeDetailList)) {
            return;
        }

        //不需要管的sku
        List<SkuVO> noInventorySkuList = plmTaskFeign.getNoInventorySku();
        //对应不需要的验证的sku no list
        List<String> noInventorySkuNoList = noInventorySkuList.stream().map(SkuVO::getSkuNo).collect(Collectors.toList());
        //获取到销售出库单的信息
        SyncKingdeeDTO.B2CSoOutstockDTO info = handleWmsSoOutstock(entity, noInventorySkuNoList);
        SoOutstockEntity soOutstock = info.getSoOutstockEntity();
        List<SoOutstockDetailEntity> detailList = soOutstock.getDetailList();
        String flagId = info.getFlagId();
        if (CollectionUtils.isNotEmpty(detailList)) {
            //当是审核通过的时候
            if ("C".equals(entity.getFDocumentStatus())) {
                //当已存在 就删除以前的  并回滚库存
                soOutstockService.handleKingdeeToErp(soOutstock, detailList, flagId);
                InventoryInOutStockRuleDTO inventoryInOutStockDTO = info.getInventoryInOutStockRuleDTO();
                if (CollectionUtils.isNotEmpty(inventoryInOutStockDTO.getParamList())) {
                    inventoryTransCoreService.approveByRule(inventoryInOutStockDTO);
                }
                //扣减库存成功后 更新状态
                boolean update = soOutstockService.lambdaUpdate()
                        .set(SoOutstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE)
                        .eq(SoOutstockEntity::getId, soOutstock.getId())
                        .update();
            }

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncWdtSoOutStock(WangDianOrderEntity entity) {
        //不需要管的sku
        List<SkuVO> noInventorySkuList = plmTaskFeign.getNoInventorySku();
        //对应不需要的验证的sku no list
        List<String> noInventorySkuNoList = noInventorySkuList.stream().map(SkuVO::getSkuNo).collect(Collectors.toList());
        //获取到销售出库单的信息
        SyncKingdeeDTO.B2CSoOutstockDTO info = handleWmsSoOutStock(entity, noInventorySkuNoList);
        SoOutstockEntity soOutstock = info.getSoOutstockEntity();
        List<SoOutstockDetailEntity> detailList = soOutstock.getDetailList();
        String flagId = info.getFlagId();
        if (CollectionUtils.isNotEmpty(detailList)) {
            //当已存在 就删除以前的  并回滚库存
            soOutstockService.handleKingdeeToErp(soOutstock, detailList, flagId);
            InventoryInOutStockRuleDTO inventoryInOutStockDTO = info.getInventoryInOutStockRuleDTO();
            if (CollectionUtils.isNotEmpty(inventoryInOutStockDTO.getParamList())) {
                inventoryTransCoreService.approveByRule(inventoryInOutStockDTO);
            }
            //扣减库存成功后 更新状态
            soOutstockService.lambdaUpdate()
                    .set(SoOutstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE)
                    .eq(SoOutstockEntity::getId, soOutstock.getId())
                    .update();
        }
        //推送金蝶
        sendPushTask(soOutstock);
    }

    private void sendPushTask(SoOutstockEntity obj) {
        //审核通过发送金蝶
        DmpPushTaskEntity pushTaskEntity = syncKingdeeSoOutstockService.syncB2cDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode());
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Collections.singletonList(pushTaskEntity));
            }
        });
    }

    private SyncKingdeeDTO.B2CSoOutstockDTO handleWmsSoOutStock(WangDianOrderEntity entity, List<String> noInventorySkuNoList) {
        SyncKingdeeDTO.B2CSoOutstockDTO result = new SyncKingdeeDTO.B2CSoOutstockDTO();
        String flagId = soOutstockService.getByCode(entity.getOrderNo());
        result.setFlagId(flagId);
        //查询旺店通对应系统店铺
        List<ThirdMappingEntity> shop = FeignQuery.list(FeignQuery.create(ThirdMappingEntity.class)
                .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.SHOP.getCode())
                .eq(ThirdMappingEntity::getThirdSysType, ThirdSysTypeEnum.WANGDIAN.getCode())
                .eq(ThirdMappingEntity::getThirdId, entity.getShopId()));
        if (CollectionUtils.isEmpty(shop)) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND_SHOP_MAPPING, entity.getShopId());
        }
        ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shop.get(0).getSysId());
        //查询旺店通对应系统仓库
        List<ThirdMappingEntity> warehouseList = FeignQuery.list(FeignQuery.create(ThirdMappingEntity.class)
                .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.WAREHOUSE.getCode())
                .eq(ThirdMappingEntity::getThirdSysType, ThirdSysTypeEnum.WANGDIAN.getCode())
                .eq(ThirdMappingEntity::getThirdId, entity.getWarehouseId()));
        if (CollectionUtils.isEmpty(shop)) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND_WAREHOUSE_MAPPING, entity.getWarehouseNo());
        }
        WarehouseEntity warehouse = FeignQuery.getById(WarehouseEntity.class, warehouseList.get(0).getSysId());
        CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());
        //组织信息
        SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouse.getOrgId());
        //sku no list
        List<String> skuNoList = entity.getDetailsList().stream().map(WangDianOrderEntity.DetailItem::getSpecNo).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);
        String sourceType = SourceTypeEnum.WDT_OUT_STOCK.getCode();
        //销售出库单
        InventorySourceTypeEnum sourceTypeEnum = InventorySourceTypeEnum.SO_OUTSTOCK;
        SoOutstockEntity soOutstock = new SoOutstockEntity();
        //单据编号
        soOutstock.setCode(entity.getOrderNo());
        //单据状态
        soOutstock.setApproveStatus(ApproveStatusEnum.APPROVE);
        //是否作废
        soOutstock.setInvalidStatus(false);
        //销售订单code
        soOutstock.setSoCode(entity.getSrcOrderNo());
        //库存组织
        soOutstock.setWarehouseOrgId(warehouse.getOrgId());
        if (ObjectUtil.isNotEmpty(company)){
            soOutstock.setWarehouseOrgName(company.getCompanyName());
        }
        //出库时间
        LocalDateTime outStockTime = LocalDateTime.parse(entity.getConsignTime(), DateTimeFormatter.ofPattern(DateUtil.fmt));
        soOutstock.setPlanDeliveryDate(outStockTime.toLocalDate());
        soOutstock.setPackDate(outStockTime.toLocalDate());
        soOutstock.setActualDeliveryDate(outStockTime);
        // 出库日期
        soOutstock.setBillDate(outStockTime.toLocalDate());
        //优惠金额
        soOutstock.setTotalDiscountAmount(entity.getDiscount());
        //运输单号
        soOutstock.setTrackNo(entity.getLogisticsNo());
        //来源信息
        soOutstock.setSourceId(entity.getStockoutId());
        soOutstock.setSourceType(sourceType);
        soOutstock.setSourceCode(entity.getTradeNo());
        soOutstock.setOrderType(OrderTypeEnum.B2C.getCode());
        //仓库
        soOutstock.setWarehouseId(warehouse.getId());
        soOutstock.setWarehouseName(warehouse.getName());
        //审核时间
        soOutstock.setApproveTime(outStockTime);
        //客户信息
        soOutstock.setCustomerId(shopInfo.getCustomerId());
        if (ObjectUtil.isNotEmpty(customerInfo)) {
            soOutstock.setCustomerName(customerInfo.getName());
        }
        //销售组织
        soOutstock.setSalesOrgId(shopInfo.getSalesOrgId());
        soOutstock.setSalesOrgName(shopInfo.getSalesOrgName());
        soOutstock.setCountry(entity.getReceiverCountry());
        //第三方单据编号
        soOutstock.setThirdCode(entity.getSrcOrderNo());
        soOutstock.setPackingStatus(PackingStatusEnum.NOT_PACKING.getCode());
        String id = IdWorker.getIdStr();
        soOutstock.setId(id);
        List<SoOutstockDetailEntity> addDetailList = new ArrayList<>(entity.getDetailsList().size());
        List<InOutStockDTO> inOutStockList = new ArrayList<>();
        for (WangDianOrderEntity.DetailItem detail : entity.getDetailsList()) {
            String skuNo = detail.getSpecNo();
            //是否扣减库存 true 就要
            Boolean isDeduction = !noInventorySkuNoList.contains(skuNo);
            String skuId = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).
                    findFirst().map(SkuVO::getSkuId).orElse("");
            if (StringUtils.isBlank(skuId)) {
                throw new ServiceException(ApiError.ERROR_92055, skuNo);
            }
            SoOutstockDetailEntity detailEntity = new SoOutstockDetailEntity();
            String detailId = IdWorker.getIdStr();
            detailEntity.setId(detailId);
            detailEntity.setMainId(id);
            detailEntity.setSkuNo(skuNo);
            detailEntity.setSkuId(skuId);
            //实发
            BigDecimal realQty = detail.getNum();
            Integer actualQty = realQty.intValue();
            detailEntity.setActualQty(actualQty);
            detailEntity.setPlanQty(actualQty);
            //仓库
            detailEntity.setWarehouseId(warehouse.getId());
            detailEntity.setWarehouseName(warehouse.getName());
            //单价
            detailEntity.setPrice(detail.getMarketPrice());
            //税率
            detailEntity.setTaxRate(detail.getTaxRate());
            //成交价
            detailEntity.setAmount(detail.getSellPrice());
            detailEntity.setCurrency(CurrencyEnum.RMB.getCurrencyCode());
            detailEntity.setCurrencySymbol(CurrencyEnum.RMB.getCurrencySymbol());
            detailEntity.setAllAmountLocalCurrency(detail.getSellPrice());
            detailEntity.setExchangeRate(new BigDecimal(1));
            detailEntity.setSoDetailId(detail.getSrcOrderDetailId());
            detailEntity.setRemark(detail.getRemark());
            detailEntity.setSourceDetailId(detail.getSrcOrderDetailId());
            detailEntity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
            detailEntity.setInvalidStatus(false);
            String warehouseId = "";
            String warehouseName = "";
            //表示要扣除库存
            if (Boolean.TRUE.equals(isDeduction)) {
                //并且扣库存 才执行
                warehouseId = warehouse.getId();
                warehouseName = warehouse.getName();
                //设置主表仓库和组织
                InOutStockDTO inOutStock = new InOutStockDTO();
                inOutStock.setSourceId(id);
                inOutStock.setSourceDetailId(detailId);
                inOutStock.setSourceType(sourceTypeEnum);
                inOutStock.setBillDate(outStockTime.toLocalDate());
                inOutStock.setQty(actualQty);
                inOutStock.setSkuId(skuId);
                inOutStock.setSkuNo(skuNo);
                inOutStock.setSourceCode(entity.getOrderNo());
                inOutStock.setWarehouseId(warehouseId);
                inOutStockList.add(inOutStock);
            }
            detailEntity.setWarehouseId(warehouseId);
            detailEntity.setWarehouseName(warehouseName);
            addDetailList.add(detailEntity);
        }
        soOutstock.setDetailList(addDetailList);
        result.setSoOutstockEntity(soOutstock);
        //扣库存
        InventoryInOutStockRuleDTO inventoryInOutStockRuleDTO = getInventoryInOutStockRuleDTO(inOutStockList);
        result.setInventoryInOutStockRuleDTO(inventoryInOutStockRuleDTO);
        return result;
    }

    /**
     * 构建库存相关数据
     */
    private static InventoryInOutStockRuleDTO getInventoryInOutStockRuleDTO(List<InOutStockDTO> inOutStockList) {
        InventoryInOutStockRuleDTO inventoryInOutStockRuleDTO = new InventoryInOutStockRuleDTO();
        inventoryInOutStockRuleDTO.setBusinessType(InventoryBusinessTypeEnum.SO_OUTSTOCK.getCode());
        inventoryInOutStockRuleDTO.setParamList(inOutStockList);
        List<TransactionRuleDTO> ruleList = new ArrayList<>(1);
        TransactionRuleDTO transactionRule = new TransactionRuleDTO();
        transactionRule.setInventoryStatus(InventoryStatusEnum.USABLE);
        transactionRule.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT);
        transactionRule.setTransactionMode(InventoryModeEnum.OUT_STOCK);
        ruleList.add(transactionRule);
        inventoryInOutStockRuleDTO.setRules(ruleList);
        return inventoryInOutStockRuleDTO;
    }


    /**
     * 处理wms 销售出库单需要的数据
     *
     * @param noInventorySkuNoList 不需要的校验的skuno
     * @return com.erp.model.wms.entity.SoOutstockEntity
     * @author yl
     * @date 2023-07-01 10:56
     */
    private SyncKingdeeDTO.B2CSoOutstockDTO handleWmsSoOutstock(KingdeeDeliveryDetailEntity entity, List<String> noInventorySkuNoList) {
        String customerNumber = entity.getFCustomerNumber();
        String customerName = entity.getFCustomerName();
        List<CustomerInfoEntity> customerInfoEntityList = customerFeign.getCustomerByCodeAndName(customerNumber, customerName);
//    	if(CollUtil.isEmpty(customerInfoEntityList)) {
//    		throw new ServiceException(String.format("通过客户编码：%s，客户名称：%s查询不到客户信息" , customerNumber , customerName));
//    	}else if(customerInfoEntityList.size() > 1){
//    		throw new ServiceException(String.format("通过客户编码：%s，客户名称：%s查询到多条客户信息" , customerNumber , customerName));
//    	}

        SyncKingdeeDTO.B2CSoOutstockDTO result = new SyncKingdeeDTO.B2CSoOutstockDTO();
        List<KingdeeDeliveryDetailItemEntity> kingdeeDetailList = entity.getKingdeeOutStockItemEntityList();
        //金蝶的仓库code
        List<String> kingdeeWarehouseCodeList = kingdeeDetailList.stream().map(KingdeeDeliveryDetailItemEntity::getFStockNumber).distinct().collect(Collectors.toList());
        //仓库的
        List<WarehouseEntity> warehouseList = warehouseService.listByKingdeeCodeList(kingdeeWarehouseCodeList);
        //组织信息
        List<String> warehouseOrgIdList = warehouseList.stream().map(req -> req.getOrgId()).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(warehouseOrgIdList);


        /**
         * sku no list
         */
        List<String> skuNoList = kingdeeDetailList.stream().map(KingdeeDeliveryDetailItemEntity::getFMaterialNumber).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);
        ApproveStatusEnum statusEnum = ApproveStatusEnum.WAIT_SUBMIT;
        String sourceType = SourceTypeEnum.SAL_OUTSTOCK.getCode();
        //销售出库单
        InventorySourceTypeEnum sourceTypeEnum = InventorySourceTypeEnum.SO_OUTSTOCK;
        String code = entity.getFBillNo();
        String flagId = soOutstockService.getByCode(code);
        result.setFlagId(flagId);
        SoOutstockEntity soOutstock = new SoOutstockEntity();
        if (CollUtil.isNotEmpty(customerInfoEntityList)) {
            soOutstock.setCustomerId(customerInfoEntityList.get(0).getId());
        }
        soOutstock.setCustomerName(customerName);
        //单据编号
        soOutstock.setCode(code);
        //运输单号
        soOutstock.setTrackNo(entity.getFCarriageNO());
        //第三方单据编号
        soOutstock.setThirdCode(entity.getFEThirdBillNo());
        //仓管员
        String warehouseKeeperName = entity.getFStockerName();
        soOutstock.setWarehouseKeeperName(warehouseKeeperName);
        soOutstock.setApproveStatus(statusEnum);
        soOutstock.setSourceType(sourceType);
        soOutstock.setOrderType(OrderTypeEnum.B2C.getCode());
        LocalDate billDate = null;
        String billDateStr = entity.getFDate();
        if (StringUtils.isNotBlank(billDateStr)) {
            LocalDateTime billDateTime = LocalDateUtil.strToLocalDateTime(billDateStr);
            billDate = billDateTime.toLocalDate();
        }
        if (Objects.isNull(billDate)) {
            billDate = LocalDate.now();
        }
        // 出库日期
        soOutstock.setBillDate(billDate);
        String id = IdWorker.getIdStr();
        soOutstock.setId(id);
        List<SoOutstockDetailEntity> addDetailList = new ArrayList<>(kingdeeDetailList.size());
        List<InOutStockDTO> inOutStockList = new ArrayList<>();
        for (KingdeeDeliveryDetailItemEntity detail : kingdeeDetailList) {
            String skuNo = detail.getFMaterialNumber();
            String fStockNumber = detail.getFStockNumber();
            //是否扣减库存 true 就要
            Boolean isDeduction = !noInventorySkuNoList.contains(skuNo);
            String skuId = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).
                    findFirst().map(SkuVO::getSkuId).orElse("");
            if (StringUtils.isBlank(skuId)) {
                throw new ServiceException(ApiError.ERROR_92055, skuNo);
            }
            SoOutstockDetailEntity detailEntity = new SoOutstockDetailEntity();
            String detailId = IdWorker.getIdStr();
            detailEntity.setId(detailId);
            detailEntity.setMainId(id);
            detailEntity.setSkuNo(skuNo);
            detailEntity.setSkuId(skuId);
            //实发
            String realQty = detail.getFRealQty();
            Integer actualQty = Integer.parseInt(realQty.split("\\.")[0]);
            detailEntity.setActualQty(actualQty);
            detailEntity.setPlanQty(actualQty);
            String note = detail.getFNote();
            if (StringUtils.isNotBlank(note)) {
                detailEntity.setPlatformCode(note.trim());
            }
            String warehouseId = "";
            String warehouseName = "";
            String warehouseOrgId = "";
            String warehouseOrgName = "";
            //表示要扣除库存
            if (isDeduction) {
                WarehouseEntity warehouse = warehouseList.stream().filter(w -> w.getKingdeeWarehouseCode().
                        equals(fStockNumber)).findFirst().orElse(null);
                //并且扣库存 才执行
                if (Objects.isNull(warehouse)) {
                    throw new ServiceException(ApiError.ERROR_92054, fStockNumber);
                } else {
                    warehouseId = warehouse.getId();
                    warehouseName = warehouse.getName();
                    warehouseOrgId = warehouse.getOrgId();

                    BaseIdDTO.CodeDTO codeDTO = accountingCompanyList.stream().filter(req -> req.getId().equals(warehouse.getOrgId())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(codeDTO)) {
                        warehouseOrgName = codeDTO.getName();
                    }

                    //设置主表仓库和组织
                    soOutstock.setWarehouseId(warehouseId);
                    soOutstock.setWarehouseName(warehouseName);
                    soOutstock.setWarehouseOrgId(warehouseOrgId);
                    soOutstock.setWarehouseOrgName(warehouseOrgName);
                }

                InOutStockDTO inOutStock = new InOutStockDTO();
                inOutStock.setSourceId(id);
                inOutStock.setSourceDetailId(detailId);
                inOutStock.setSourceType(sourceTypeEnum);
                inOutStock.setBillDate(billDate);
                inOutStock.setQty(actualQty);
                inOutStock.setSkuId(skuId);
                inOutStock.setSkuNo(skuNo);
                inOutStock.setSourceCode(code);
                inOutStock.setWarehouseId(warehouseId);
                inOutStockList.add(inOutStock);
            }
            detailEntity.setWarehouseId(warehouseId);
            detailEntity.setWarehouseName(warehouseName);
            addDetailList.add(detailEntity);
        }

        soOutstock.setDetailList(addDetailList);
        result.setSoOutstockEntity(soOutstock);

        //扣库存
        InventoryInOutStockRuleDTO inventoryInOutStockRuleDTO = getInventoryInOutStockRuleDTO(inOutStockList);
        result.setInventoryInOutStockRuleDTO(inventoryInOutStockRuleDTO);
        return result;
    }


}





