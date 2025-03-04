package com.erp.server.dmp.inout.handler.input.task.dmp.spt;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpThirdInventoryEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.server.dmp.inout.handler.input.task.dmp.eccang.EccangReturnInventoryAgeDmpHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class SptReturnInventoryAgeDmpHandler extends EccangReturnInventoryAgeDmpHandler {
    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("SptReturnInventoryAgeDmpHandler afterConvertData：");
        String parentTableName = SqlHelper.table(DmpThirdInventoryEntity.class).getTableName();
        ServiceImpl parentServiceImpl = this.getServiceImpl(parentTableName);
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq(INPUT_TASK_ID, inputTaskId);
        List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);

        Map<String, String> uniqueIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(listMaps)) {
            for (Map<String, Object> listMap : listMaps) {
                //,platformWarehouseCode,productSku,authId
                String sourcePlatform = DmpBasicSystemCodeEnum.SPT.getCode();
                String platformWarehouseCode = listMap.getOrDefault("platform_warehouse_code", "").toString();
                String productSku = listMap.getOrDefault("product_sku", "").toString();
                String authId = listMap.getOrDefault("auth_id", "").toString();
                String uniqueId = CharSequenceUtil.format("{}_{}_{}_{}", sourcePlatform, platformWarehouseCode, productSku, authId);
                uniqueIdMap.put(uniqueId, listMap.get(BaseEntity.FIELD_ID).toString());
            }
        }
        for (List<TreeMap<String, Object>> dmpInputMongoList : dmpInputDataDmpRelationMaps.values()) {
            for (TreeMap<String, Object> detailMap : dmpInputMongoList) {
                String sourcePlatform = DmpBasicSystemCodeEnum.SPT.getCode();
                String platformWarehouseCode = detailMap.getOrDefault("warehouse_code", "").toString();
                String productSku = detailMap.getOrDefault("product_sku", "").toString();
                String authId = detailMap.getOrDefault("authId", "").toString();
                String uniqueId = CharSequenceUtil.format("{}_{}_{}_{}", sourcePlatform, platformWarehouseCode, productSku, authId);
                String dmpId = uniqueIdMap.get(uniqueId);
                detailMap.put("mainId", dmpId);
            }
        }
        log.debug("SptReturnInventoryAgeDmpHandler afterConvertData end：");
    }
}
