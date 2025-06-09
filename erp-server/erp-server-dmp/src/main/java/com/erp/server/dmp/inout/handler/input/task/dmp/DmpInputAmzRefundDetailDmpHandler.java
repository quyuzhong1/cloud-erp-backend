package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpSoRefundInfoEntity;
import com.erp.sdk.oms.amz.spapi.model.finances.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzRefundDetailDmpHandler extends DmpInputDoNextDmpHandler {


    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntityList) {
        List<Map<String, Object>> refundDetailList = new LinkedList<>();
        Object shipmentItemAdjustmentListObj = dmpInputMongoEntityList.get("shipmentItemAdjustmentList");
        if (null == shipmentItemAdjustmentListObj) {
            log.warn("DmpInputAmzRefundDetailDmpHandler 数据异常无明细：{}", JSONUtil.toJsonStr(dmpInputMongoEntityList));
            return refundDetailList;
        }

        log.debug("DmpInputAmzRefundDetailDmpHandler afterConvertData：");
        String parentTableName = SqlHelper.table(DmpSoRefundInfoEntity.class).getTableName();
        ServiceImpl parentServiceImpl = this.getServiceImpl(parentTableName);
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq(INPUT_TASK_ID, inputTaskId);
        List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
        // Map<>
        Map<String, String> dmpRefundIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(listMaps)) {
            for (Map<String, Object> listMap : listMaps) {
                dmpRefundIdMap.put(listMap.get("third_code").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
            }
        }

        List<Map<String, Object>> shipmentItemAdjustmentList = (List<Map<String, Object>>) shipmentItemAdjustmentListObj;
        for (Map<String, Object> itemMap : shipmentItemAdjustmentList) {
            ShipmentItem shipmentItem = JSONUtil.toBean(JSONUtil.toJsonStr(itemMap), ShipmentItem.class);
            if (null == shipmentItem) {
                continue;
            }
            BigDecimal taxAmount = BigDecimal.ZERO;
            BigDecimal amount = BigDecimal.ZERO;
            ChargeComponentList itemChargeAdjustmentList = shipmentItem.getItemChargeAdjustmentList();
            if (CollectionUtils.isNotEmpty(itemChargeAdjustmentList)) {
                for (ChargeComponent component : itemChargeAdjustmentList) {
                    if ("Principal".equalsIgnoreCase(component.getChargeType())) {
                        amount = component.getChargeAmount().getCurrencyAmount();
                    }
                    if ("Tax".equalsIgnoreCase(component.getChargeType())) {
                        taxAmount = component.getChargeAmount().getCurrencyAmount();
                    }
                }
            }

            String returnOrderId = dmpInputMongoEntityList.getOrDefault("amazonOrderId", "").toString();
            String dmpId = dmpRefundIdMap.get(returnOrderId);
            itemMap.put("mainId", dmpId);

            // 计算税费
            itemMap.put("taxAmount", taxAmount.abs());
            // 商品价格
            itemMap.put("amount", amount.abs());

            String marketplaceName = dmpInputMongoEntityList.getOrDefault("marketplaceName", "").toString();
            // 补充原信息
            itemMap.put("marketplaceName", marketplaceName);
            itemMap.put("amazonOrderId", returnOrderId);
            // 添加导结果
            refundDetailList.add(itemMap);
        }
        return refundDetailList;
    }
}
