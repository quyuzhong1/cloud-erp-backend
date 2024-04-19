package com.erp.server.wms.rocketmq.sync.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.item.KingdeeDeliveryDetailItemEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SyncKingdeeDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockRuleDTO;
import com.erp.model.wms.dto.inventory.TransactionRuleDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;
import com.erp.server.wms.service.InventoryTransCoreService;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.WarehouseService;

import cn.hutool.core.collection.CollUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    	if(CollUtil.isEmpty(customerInfoEntityList)) {
    		throw new ServiceException(String.format("通过客户编码：%s，客户名称：%s查询不到客户信息" , customerNumber , customerName));
    	}else if(customerInfoEntityList.size() > 1){
    		throw new ServiceException(String.format("通过客户编码：%s，客户名称：%s查询到多条客户信息" , customerNumber , customerName));
    	}
    	
    	SyncKingdeeDTO.B2CSoOutstockDTO result = new SyncKingdeeDTO.B2CSoOutstockDTO();
        List<KingdeeDeliveryDetailItemEntity> kingdeeDetailList = entity.getKingdeeOutStockItemEntityList();
        //金蝶的仓库code
        List<String> kingdeeWarehouseCodeList = kingdeeDetailList.stream().map(KingdeeDeliveryDetailItemEntity::getFStockNumber).distinct().collect(Collectors.toList());
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
        soOutstock.setCustomerId(customerInfoEntityList.get(0).getId());
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
        String warehouseOrgId = "";
        soOutstock.setWarehouseOrgId(warehouseOrgId);
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
            if(StringUtils.isNotBlank(note)) {
            	detailEntity.setPlatformCode(note.trim());
            }
            String warehouseId = "";
            String warehouseName = "";
            //表示要
            if (isDeduction) {
                //仓库的
                List<WarehouseEntity> warehouseList = warehouseService.listByKingdeeCodeList(kingdeeWarehouseCodeList);
                WarehouseEntity warehouse = warehouseList.stream().filter(w -> w.getKingdeeWarehouseCode().
                        equals(fStockNumber)).findFirst().orElse(null);
                //并且扣库存 才执行
                if (Objects.isNull(warehouse)) {
                    throw new ServiceException(ApiError.ERROR_92054, fStockNumber);
                } else {
                    warehouseId = warehouse.getId();
                    warehouseName = warehouse.getName();
                }
            }
            detailEntity.setWarehouseId(warehouseId);
            detailEntity.setWarehouseName(warehouseName);
            addDetailList.add(detailEntity);
            //要扣除库存
            if (isDeduction) {
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

        }

        soOutstock.setDetailList(addDetailList);
        result.setSoOutstockEntity(soOutstock);

        //扣库存
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
        result.setInventoryInOutStockRuleDTO(inventoryInOutStockRuleDTO);
        return result;
    }


}





