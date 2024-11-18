package com.erp.server.wms.schedule;

import cn.hutool.core.date.CalendarUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.entity.WarehouseLocationSafetyInventoryEntity;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.TransactionFlowService;
import com.erp.server.wms.service.WarehouseLocationSafetyInventoryService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 安全库存条目
 * @date 2024-07-02
 * @author tanmujin
 */
@Slf4j
@Component
public class SafetyInventoryJob {

    @Resource
    private TransactionFlowService flowService;
    @Resource
    private WarehouseLocationSafetyInventoryService safetyInventoryService;
    @Resource
    private ProductDetailFeign productDetailFeign;
    @Resource
    private WarehouseLocationService warehouseLocationService;

    /**
     * 刷新仓位安全库存条目
     */
    @XxlJob("refreshSafetyInventory")
    public ReturnT<String> refreshSafetyInventory() {
        DateTime dateTime = DateUtil.offsetHour(new Date(), -8);
        List<TransactionFlowEntity> flowList = flowService.getBaseMapper().selectList(new QueryWrapper<TransactionFlowEntity>().between("create_time", DateUtil.formatDateTime(dateTime), DateUtil.formatDateTime(new Date())));
        List<String> list = flowList.stream()
                .map(item -> item.getWarehouseId() + "#" + item.getWarehouseLocation() + "#" + item.getSkuId())
                .distinct()
                .collect(Collectors.toList());
        List<String> skuIds = flowList.stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productList = productDetailFeign.listByIds(skuIds);
        Map<String, ProductDetailEntity> productMap = productList.stream().collect(Collectors.toMap(item1 -> item1.getId(), item2 -> item2));

        List<WarehouseLocationSafetyInventoryEntity> saveList = new ArrayList<>();
        for (String str : list) {
            String[] split = str.split("#");
            String warehouseId = split[0];
            String warehouseLocation = split[1];
            String skuId = split[2];
            if(CharSequenceUtil.isBlank(warehouseId) || CharSequenceUtil.isBlank(warehouseLocation) || CharSequenceUtil.isBlank(skuId)){
                continue;
            }
            WarehouseLocationSafetyInventoryEntity one = safetyInventoryService.getOne(new QueryWrapper<WarehouseLocationSafetyInventoryEntity>()
                    .eq("warehouse_id", warehouseId)
                    .eq("warehouse_location", warehouseLocation)
                    .eq("sku_id", skuId));
            if(one == null){
                WarehouseLocationSafetyInventoryEntity entity = new WarehouseLocationSafetyInventoryEntity();
                entity.setWarehouseId(warehouseId);
                entity.setWarehouseLocation(warehouseLocation);
                entity.setSkuId(skuId);
                ProductDetailEntity product = productMap.get(skuId);
                entity.setSkuNo(product.getSkuNo());
                entity.setProductName(product.getName());
                WarehouseLocationEntity locationEntity = warehouseLocationService.findWarehouseArea(warehouseId, warehouseLocation);
                entity.setWarehouseArea(locationEntity.getCode());
                entity.setWarehouseAreaType(locationEntity.getAreaType());
                saveList.add(entity);
            }
        }
        safetyInventoryService.saveBatch(saveList);
        return ReturnT.SUCCESS;
    }
}
