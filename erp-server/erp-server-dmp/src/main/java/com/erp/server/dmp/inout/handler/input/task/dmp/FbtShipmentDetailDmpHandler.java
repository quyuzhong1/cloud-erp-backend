package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpFbtShipmentEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputDmpResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FBT货件明细DMP处理器
 */
@Slf4j
@Service
@Scope("prototype")
public class FbtShipmentDetailDmpHandler extends DmpInputBaseDmpHandler {

    @Override
    public List<Map<String, Object>> convertMongoToDmp(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse) {
        List<Map<String, Object>> mongoRows = new ArrayList<>();
        Collection<List<Map<String, Object>>> values = dmpResponse.getConvertInputMongoEntityListMaps().values();
        for (List<Map<String, Object>> value : values) {
            if (CollUtil.isNotEmpty(value)) {
                mongoRows.addAll(value);
            }
        }
        if (CollUtil.isEmpty(mongoRows)) {
            return Collections.emptyList();
        }

        Map<String, String> mainIdMap = buildMainIdMap((DmpInputDmpResponse) dmpResponse);
        List<Map<String, Object>> detailRows = new ArrayList<>();
        for (Map<String, Object> mongoRow : mongoRows) {
            String authId = stringVal(mongoRow.get("authId"));
            String inboundOrderId = stringVal(mongoRow.get("inboundOrderId"));
            String mainId = mainIdMap.get(buildMainKey(authId, inboundOrderId));
            if (StringUtils.isBlank(mainId)) {
                log.warn("FBT货件明细转换跳过，未找到主表记录, authId={}, inboundOrderId={}", authId, inboundOrderId);
                continue;
            }
            Object plannedGoodsObj = mongoRow.get("plannedGoods");
            if (!(plannedGoodsObj instanceof List)) {
                continue;
            }
            for (Object plannedGoodObj : (List<?>) plannedGoodsObj) {
                if (!(plannedGoodObj instanceof Map)) {
                    continue;
                }
                Map<String, Object> plannedGood = (Map<String, Object>) plannedGoodObj;
                Map<String, Object> detailRow = new LinkedHashMap<>();
                detailRow.put("mainId", mainId);
                detailRow.put("nextLevelId", authId);
                detailRow.put("inboundOrderId", inboundOrderId);
                detailRow.put("goodsId", stringVal(plannedGood.get("goodsId")));
                detailRow.put("referenceCode", stringVal(plannedGood.get("referenceCode")));
                detailRow.put("productName", stringVal(plannedGood.get("name")));
                detailRow.put("declareQty", intVal(plannedGood.get("quantity")));
                detailRow.put("skuIdsJson", JSON.toJSONString(plannedGood.get("skuIds")));
                detailRows.add(detailRow);
            }
        }
        return detailRows;
    }

    private Map<String, String> buildMainIdMap(DmpInputDmpResponse dmpResponse) {
        Map<String, String> mainIdMap = new LinkedHashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : dmpResponse.getConvertInputDmpBaseEntityListMaps().entrySet()) {
            if (!"dmp_fbt_shipment".equals(entry.getKey().getStorageName())) {
                continue;
            }
            for (BaseEntity entity : entry.getValue()) {
                DmpFbtShipmentEntity mainEntity = (DmpFbtShipmentEntity) entity;
                mainIdMap.put(buildMainKey(mainEntity.getAuthId(), mainEntity.getInboundOrderId()), mainEntity.getId());
            }
        }
        return mainIdMap;
    }

    private String buildMainKey(String authId, String inboundOrderId) {
        return StringUtils.defaultString(authId) + "_" + StringUtils.defaultString(inboundOrderId);
    }

    private Integer intVal(Object value) {
        String raw = stringVal(value);
        if (StringUtils.isBlank(raw)) {
            return 0;
        }
        try {
            return Integer.parseInt(raw);
        } catch (Exception ignore) {
            return 0;
        }
    }

    private String stringVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @Override
    protected boolean needDealDetailDelete() {
        return true;
    }
}
