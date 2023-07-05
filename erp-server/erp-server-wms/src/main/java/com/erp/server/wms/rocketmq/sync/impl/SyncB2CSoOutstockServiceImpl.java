package com.erp.server.wms.rocketmq.sync.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.RedisService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.item.KingdeeDeliveryDetailItemEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SyncKingdeeDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockRuleDTO;
import com.erp.model.wms.dto.inventory.TransactionRuleDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;
import com.erp.server.wms.service.InventoryTransCoreService;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname SyncB2CSoOutstockServiceImpl
 * @Description TODO
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
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private RedisService redisService;


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
        //根据单号检查能否同步 true 可以
        Boolean checkSyncResult = checkIsSync(entity.getFBillNo());
        if (!checkSyncResult) {
            return;
        }
        List<KingdeeDeliveryDetailItemEntity> kingdeeDetailList = entity.getKingdeeOutStockItemEntityList();
        if (CollectionUtils.isEmpty(kingdeeDetailList)) {
            return;
        }

        String code = entity.getFBillNo();
        String baseKey = RedisKeyConstant.KINGDEE_XSCK;
        String redisKey = code + baseKey;

        //根据仓库分组
        Map<String, List<KingdeeDeliveryDetailItemEntity>> map = kingdeeDetailList.stream().collect(Collectors.groupingBy(KingdeeDeliveryDetailItemEntity::getFStockNumber));
        for (Map.Entry<String, List<KingdeeDeliveryDetailItemEntity>> item : map.entrySet()) {
            SyncKingdeeDTO.B2CSoOutstockDTO info = handleWmsSoOutstock(entity, item.getKey(), item.getValue());
            SoOutstockEntity soOutstock = info.getSoOutstockEntity();
            List<SoOutstockDetailEntity> detailList = soOutstock.getDetailList();
            if (CollectionUtils.isNotEmpty(detailList)) {
                String flagId = info.getFlagId();

                //当是审核通过的时候
                if (entity.getFDocumentStatus().equals("C")) {
                    //当已存在 就删除以前的  并回滚库存
                    if (StringUtils.isNotBlank(flagId)) {
                        //回滚库存
                        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_OUTSTOCK, Arrays.asList(flagId));
                        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
                        soOutstockService.removeById(flagId);
                        soOutstockDetailService.removeByMainIdList(Arrays.asList(flagId));
                        redisService.deleteObject(redisKey);
                    }

                    //保存销售出库单
                    soOutstockService.save(soOutstock);
                    //保存销售出库单详情
                    soOutstockDetailService.saveBatch(detailList);
                    InventoryInOutStockRuleDTO inventoryInOutStockDTO = info.getInventoryInOutStockRuleDTO();
                    if (CollectionUtils.isNotEmpty(inventoryInOutStockDTO.getMembers())) {
                        inventoryTransCoreService.approveByRule(inventoryInOutStockDTO);
                    }

                    redisService.setCacheObject(redisKey, soOutstock.getId(), 7L, TimeUnit.DAYS);

                } else {
                    //当有的情况下
                    if (StringUtils.isNotBlank(flagId)) {
                        //回滚库存
                        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_OUTSTOCK, Arrays.asList(flagId));
                        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
                        soOutstockService.removeById(flagId);
                        soOutstockDetailService.removeByMainIdList(Arrays.asList(flagId));
                        redisService.deleteObject(redisKey);
                    }
                }

            }
        }


    }


    /**
     * 处理wms 销售出库单需要的数据
     *
     * @param detailList
     * @return com.erp.model.wms.entity.SoOutstockEntity
     * @author yl
     * @date 2023-07-01 10:56
     */
    private SyncKingdeeDTO.B2CSoOutstockDTO handleWmsSoOutstock(KingdeeDeliveryDetailEntity entity, String fStockNumber, List<KingdeeDeliveryDetailItemEntity> detailList) {
        SyncKingdeeDTO.B2CSoOutstockDTO result = new SyncKingdeeDTO.B2CSoOutstockDTO();
        List<KingdeeDeliveryDetailItemEntity> kingdeeDetailList = entity.getKingdeeOutStockItemEntityList();
        //金蝶的仓库code
        List<String> kingdeeWarehouseCodeList = kingdeeDetailList.stream().map(KingdeeDeliveryDetailItemEntity::getFStockNumber).distinct().collect(Collectors.toList());
        /**
         * sku no list
         */
        List<String> skuNoList = kingdeeDetailList.stream().map(KingdeeDeliveryDetailItemEntity::getFMaterialNumber).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);
        //仓库的
        List<WarehouseEntity> warehouseList = warehouseService.listByKingdeeCodeList(kingdeeWarehouseCodeList);

        WarehouseEntity warehouse = warehouseList.stream().filter(w -> w.getKingdeeWarehouseCode().
                equals(fStockNumber)).findFirst().orElse(null);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_92054, fStockNumber);
        }


        String b2c = BillTypeEnum.B2C.getCode();
        ApproveStatusEnum statusEnum = ApproveStatusEnum.APPROVE;
        String sourceType = SourceTypeEnum.SAL_OUTSTOCK.getCode();

        //销售出库单
        InventorySourceTypeEnum sourceTypeEnum = InventorySourceTypeEnum.SO_OUTSTOCK;
        String code = entity.getFBillNo();
        String baseKey = RedisKeyConstant.KINGDEE_XSCK;
        String redisKey = code + baseKey;
        String flagId = redisService.getCacheObject(redisKey);
        if (StringUtils.isBlank(flagId)) {
            flagId = soOutstockService.getByCode(code);
        }

        result.setFlagId(flagId);
        SoOutstockEntity soOutstock = new SoOutstockEntity();
        soOutstock.setOrderType(b2c);
        String warehouseId = warehouse.getId();

        //单据编号
        soOutstock.setCode(code);
        //运输单号
        soOutstock.setTrackNo(entity.getFCarriageNO());
        //仓管员
        String warehouseKeeperName = entity.getFStockerName();
        soOutstock.setWarehouseKeeperName(warehouseKeeperName);
        soOutstock.setApproveStatus(statusEnum);
        soOutstock.setSourceType(sourceType);
        soOutstock.setWarehouseName(warehouse.getName());
        soOutstock.setWarehouseId(warehouseId);
        soOutstock.setWarehouseOrgId(warehouse.getOrgId());
        String id = IdWorker.getIdStr();
        soOutstock.setId(id);
        List<SoOutstockDetailEntity> addDetailList = new ArrayList<>(detailList.size());
        List<InOutStockDTO> inOutStockList = new ArrayList<>();
        for (KingdeeDeliveryDetailItemEntity detail : detailList) {
            String skuNo = detail.getFMaterialNumber();
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
            addDetailList.add(detailEntity);

            InOutStockDTO inOutStock = new InOutStockDTO();
            inOutStock.setSourceId(id);
            inOutStock.setSourceDetailId(detailId);
            inOutStock.setSourceType(sourceTypeEnum);
            inOutStock.setBillDate(LocalDate.now());
            inOutStock.setQty(actualQty);
            inOutStock.setSkuId(skuId);
            inOutStock.setSkuNo(skuNo);
            inOutStock.setSourceCode(code);
            inOutStock.setWarehouseId(warehouseId);
            inOutStockList.add(inOutStock);

        }

        soOutstock.setDetailList(addDetailList);
        result.setSoOutstockEntity(soOutstock);

        //扣库存
        InventoryInOutStockRuleDTO inventoryInOutStockRuleDTO = new InventoryInOutStockRuleDTO();
        inventoryInOutStockRuleDTO.setBusinessType(InventoryBusinessTypeEnum.SO_OUTSTOCK.getCode());
        inventoryInOutStockRuleDTO.setMembers(inOutStockList);

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

    /**
     * 检查能否同步
     *
     * @param fBillNo
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-28 9:57
     */
    private Boolean checkIsSync(String fBillNo) {
        if (StringUtils.isBlank(fBillNo)) {
            throw new ServiceException(ApiError.ERROR_KINGDEE_CODE_NOT_EXIST);
        }
        if (fBillNo.length() == 15) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

    }
}
