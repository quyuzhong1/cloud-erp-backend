package com.erp.server.dmp.inout.handler.input.task.dmp.eccang;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpThirdInventoryEntity;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDoNextDmpHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class EccangReturnInventoryAgeDmpHandler extends DmpInputDoNextDmpHandler {

    public static final String INDEX_NUM = "indexNum";

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        Object refundsObj = dmpInputMongoEntity.get("batch_info");
        if (null == refundsObj) {
            return Collections.emptyList();
        }
        // 退货/退款信息
        List<Map<String, Object>> batchInfo = (List<Map<String, Object>>) refundsObj;
        if (CollectionUtils.isEmpty(batchInfo)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> resultList = new LinkedList<>();

        Map<String, Integer> uniqueFieldSetIndexMap = new HashMap<>();

        for (Map<String, Object> batchInfoMap : batchInfo) {
            String ibType = batchInfoMap.getOrDefault("ib_type", "").toString();
            String ibStatus = batchInfoMap.getOrDefault("ib_status", "").toString();
            // 汇总ib_status=1，且ib_type=0的数据
            if (!"1".equalsIgnoreCase(ibStatus)){
                continue;
            }
            if (!"0".equalsIgnoreCase(ibType)){
                continue;
            }
            // 批次单号
            String receivingCode = batchInfoMap.getOrDefault("receiving_code", "").toString();
            batchInfoMap.put("receivingCode", receivingCode);

            Integer indexNumber = uniqueFieldSetIndexMap.getOrDefault(receivingCode, 0);
            // 当前行号索引
            batchInfoMap.put(INDEX_NUM, indexNumber);
            // 累计次数
            uniqueFieldSetIndexMap.put(receivingCode, indexNumber + 1);

            String ibFifoTime = batchInfoMap.getOrDefault("ib_fifo_time", "1970-01-01 00:00:00").toString();

            LocalDateTime parse = LocalDateTime.parse(ibFifoTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            batchInfoMap.put("putAwayDate", parse.toLocalDate());

            // 补充主单信息定位mainId
            String warehouseCode = dmpInputMongoEntity.getOrDefault("warehouse_code", "").toString();
            batchInfoMap.put("warehouse_code", warehouseCode);
            batchInfoMap.put("warehouseCode", warehouseCode);
            String productSku = dmpInputMongoEntity.getOrDefault("product_sku", "").toString();
            batchInfoMap.put("product_sku", productSku);
            batchInfoMap.put("productSku", productSku);
            batchInfoMap.put("authId", dmpInputMongoEntity.getOrDefault("authId", "").toString());
            resultList.add(batchInfoMap);
        }
        return resultList;
    }

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        String parentTableName = SqlHelper.table(DmpThirdInventoryEntity.class).getTableName();
        ServiceImpl parentServiceImpl = this.getServiceImpl(parentTableName);
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq(INPUT_TASK_ID, inputTaskId);
        List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);

        Map<String, String> uniqueIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(listMaps)) {
            for (Map<String, Object> listMap : listMaps) {
                //,platformWarehouseCode,productSku,authId
                String platformWarehouseCode = listMap.getOrDefault("platform_warehouse_code", "").toString();
                String productSku = listMap.getOrDefault("product_sku", "").toString();
                String authId = listMap.getOrDefault("auth_id", "").toString();
                String uniqueId = CharSequenceUtil.format("{}_{}_{}", platformWarehouseCode, productSku, authId);
                uniqueIdMap.put(uniqueId, listMap.get(BaseEntity.FIELD_ID).toString());
            }
        }
        for (List<TreeMap<String, Object>> dmpInputMongoList : dmpInputDataDmpRelationMaps.values()) {
            for (TreeMap<String, Object> detailMap : dmpInputMongoList) {
                String platformWarehouseCode = detailMap.getOrDefault("warehouse_code", "").toString();
                String productSku = detailMap.getOrDefault("product_sku", "").toString();
                String authId = detailMap.getOrDefault("authId", "").toString();
                String uniqueId = CharSequenceUtil.format("{}_{}_{}", platformWarehouseCode, productSku, authId);
                String dmpId = uniqueIdMap.get(uniqueId);
                detailMap.put("mainId", dmpId);
            }
        }
    }

}
