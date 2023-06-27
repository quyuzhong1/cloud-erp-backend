package com.erp.server.wms.rocketmq.sync.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.item.KingdeeDeliveryDetailItemEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private PlmTaskFeign plmTaskFeign;


    /**
     * 同步金蝶的销售出库单
     *
     * @param entity
     * @return void
     * @author yl
     * @date 2023-06-27 14:05
     */
    @Override
    public void syncKingdeeSoOutstock(KingdeeDeliveryDetailEntity entity) {

        List<KingdeeDeliveryDetailItemEntity> kingdeeDetailList = entity.getKingdeeOutStockItemEntityList();

        if (CollectionUtils.isEmpty(kingdeeDetailList)) {
            return;
        }
        //金蝶的仓库code
        List<String> kingdeeWarehouseCodeList = kingdeeDetailList.stream().map(KingdeeDeliveryDetailItemEntity::getFStockNumber).distinct().collect(Collectors.toList());

        /**
         * sku no list
         */
        List<String> skuNoList = kingdeeDetailList.stream().map(KingdeeDeliveryDetailItemEntity::getFMaterialNumber).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);
        //仓库的
        List<WarehouseEntity> warehouseList = warehouseService.listByKingdeeCodeList(kingdeeWarehouseCodeList);
        //根据仓库分组
        Map<String, List<KingdeeDeliveryDetailItemEntity>> map = kingdeeDetailList.stream().collect(Collectors.groupingBy(KingdeeDeliveryDetailItemEntity::getFStockNumber));
        List<SoOutstockEntity> addList = new ArrayList<>(map.size());
        List<SoOutstockDetailEntity> addDetailList = new ArrayList<>(map.size() * 2);
        String b2c = BillTypeEnum.B2C.getCode();
        ApproveStatusEnum statusEnum = ApproveStatusEnum.APPROVE;
        String sourceType = SourceTypeEnum.KINGDEE.getCode();

        for (Map.Entry<String, List<KingdeeDeliveryDetailItemEntity>> item : map.entrySet()) {
            //金蝶的仓库编号
            String fStockNumber = item.getKey();
            WarehouseEntity warehouse = warehouseList.stream().filter(w -> w.getKingdeeWarehouseCode().
                    equals(fStockNumber)).findFirst().orElse(null);
            //仓库存在的
            if (warehouse != null) {
                //销售出库
                SoOutstockEntity soOutstock = new SoOutstockEntity();
                soOutstock.setOrderType(b2c);
                //单据编号
                soOutstock.setCode(entity.getFBillNo());
                //运输单号
                soOutstock.setTrackNo(entity.getFCarriageNO());
                //仓管员
                String warehouseKeeperName = entity.getFStockerName();
                soOutstock.setWarehouseKeeperName(warehouseKeeperName);
                soOutstock.setApproveStatus(statusEnum);
                soOutstock.setSourceType(sourceType);
                soOutstock.setWarehouseName(warehouse.getName());
                soOutstock.setWarehouseId(warehouse.getId());
                soOutstock.setWarehouseOrgId(warehouse.getOrgId());
                String id = IdWorker.getIdStr();
                soOutstock.setId(id);
                addList.add(soOutstock);
                List<KingdeeDeliveryDetailItemEntity> detailList = item.getValue();
                for (KingdeeDeliveryDetailItemEntity detail : detailList) {
                    String skuNo = detail.getFMaterialNumber();
                    String skuId = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).
                            findFirst().map(SkuVO::getSkuId).orElse("");
                    if(StringUtils.isBlank(skuId)){
                        continue;
                    }
                    SoOutstockDetailEntity detailEntity = new SoOutstockDetailEntity();
                    detailEntity.setMainId(id);
                    detailEntity.setSkuNo(skuNo);
                    detailEntity.setSkuId(skuId);
                    //实发
                    String realQty = detail.getFRealQty();
                    Integer actualQty = Integer.parseInt(realQty.split("\\.")[0]);
                    detailEntity.setActualQty(actualQty);
                    detailEntity.setPlanQty(actualQty);

                    addDetailList.add(detailEntity);
                }


            }


        }

    }
}
