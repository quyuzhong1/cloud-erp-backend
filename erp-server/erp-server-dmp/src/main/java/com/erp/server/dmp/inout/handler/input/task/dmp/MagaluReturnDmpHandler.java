package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputDmpResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Magalu售后(工单)转换为DMP退货主表、明细。
 * 通过 getTicketActivities 只拉取 type=return_info_sent（生成退货单）和 refunded（生成退款单）的数据，
 * 并根据 activity.type 区分退货/退款。
 */
@Service
@Scope("prototype")
public class MagaluReturnDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final String STORAGE_RETURN_INFO = "dmp_so_return_info";
    private static final String STORAGE_RETURN_DETAIL = "dmp_so_return_detail";
    private static final String MAGALU_PLATFORM = "Magalu";

    @Override
    public List<Map<String, Object>> convertMongoToDmp(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse) {
        List<Map<String, Object>> ticketList = super.convertMongoToDmp(dmpRequest, dmpResponse);
        if (CollUtil.isEmpty(ticketList)) {
            return Collections.emptyList();
        }
        if (STORAGE_RETURN_INFO.equals(storageName)) {
            return buildReturnInfoRows(ticketList);
        }
        Map<String, String> mainIdMap = buildMainIdMap(dmpResponse);
        if (STORAGE_RETURN_DETAIL.equals(storageName)) {
            return buildReturnDetailRows(ticketList, mainIdMap);
        }
        return ticketList;
    }

    private List<Map<String, Object>> buildReturnInfoRows(List<Map<String, Object>> payloadList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> payload : payloadList) {
            Map<String, Object> ticket = mapValue(payload.get("ticket"));
            Map<String, Object> activity = mapValue(payload.get("activity"));
            if (ticket.isEmpty() && activity.isEmpty()) {
                ticket = payload;
            }

            String actType = stringValue(activity.get("type"));
            // 只处理 return_info_sent 生成退货单；refunded 走独立的 refund handler
            if ("refunded".equalsIgnoreCase(actType)) {
                continue;
            }

            Map<String, Object> row = baseRow(ticket);
            String ticketCode = stringValue(ticket.get("code"));
            String ticketId = stringValue(ticket.get("id"));
            String thirdCode = firstNotBlank(ticketCode, ticketId);

            Map<String, Object> order = mapValue(ticket.get("order"));
            String platformOrderCode = stringValue(order.get("code"));

            String origin = stringValue(ticket.get("origin"));

            row.put("platformCreateTime", parseTime(ticket.get("created_at")));
            row.put("platformUpdateTime", parseTime(ticket.get("updated_at")));
            row.put("returnTime", parseTime(ticket.get("created_at")));
            row.put("sourceSystem", MAGALU_PLATFORM);
            row.put("sourcePlatform", MAGALU_PLATFORM);
            row.put("thirdCode", thirdCode);
            row.put("platformCode", platformOrderCode);
            row.put("platformOrderCode", platformOrderCode);
            row.put("platformReturnNo", ticketCode);
            row.put("shopId", row.get("nextLevelId"));
            row.put("shopName", "");
            row.put("status", mapReturnStatus(ticket.get("states")));
            row.put("platformStatus", firstNotBlank(actType, stringValue(ticket.get("states")), stringValue(ticket.get("type"))));
            row.put("remark", stringValue(ticket.get("reason")));
            row.put("returnLogisticsNo", stringValue(ticket.get("protocol")));
            row.put("returnType", mapReturnType(origin));
            row.put("currencyCode", "BRL");
            row.put("exchangeRate", BigDecimal.ONE);
            row.put("allAmount", BigDecimal.ZERO);

            Map<String, Object> ext = buildExtend(ticket);
            ext.put("activityType", actType);
            row.put("extendData", JSONUtil.toJsonStr(ext));
            resultList.add(row);
        }
        return resultList;
    }

    private List<Map<String, Object>> buildReturnDetailRows(List<Map<String, Object>> payloadList, Map<String, String> mainIdMap) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> payload : payloadList) {
            Map<String, Object> ticket = mapValue(payload.get("ticket"));
            Map<String, Object> activity = mapValue(payload.get("activity"));
            if (ticket.isEmpty() && activity.isEmpty()) {
                ticket = payload;
            }

            String actType = stringValue(activity.get("type"));
            if ("refunded".equalsIgnoreCase(actType)) {
                continue; // 退款走独立的 refund handler
            }

            String ticketCode = stringValue(ticket.get("code"));
            String ticketId = stringValue(ticket.get("id"));
            String thirdCode = firstNotBlank(ticketCode, ticketId);
            String mainId = mainIdMap.get(thirdCode);
            if (StringUtils.isBlank(mainId)) {
                mainId = mainIdMap.get(ticketCode);
            }
            if (StringUtils.isBlank(mainId)) {
                mainId = thirdCode;
            }

            Map<String, Object> order = mapValue(ticket.get("order"));
            Map<String, Object> delivery = mapValue(order.get("delivery"));
            List<Map<String, Object>> items = listMap(delivery.get("items"));

            for (Map<String, Object> item : items) {
                Map<String, Object> row = baseRow(ticket);
                String sku = stringValue(item.get("sku"));
                String lineId = firstNotBlank(stringValue(item.get("id")), sku);
                row.put("mainId", mainId);
                row.put("thirdDetailId", buildDetailId(ticketId != null ? ticketId : ticketCode, lineId, sku));
                row.put("platformDetailId", buildDetailId(ticketId != null ? ticketId : ticketCode, lineId, sku));
                row.put("skuId", "");
                row.put("skuNo", sku);
                row.put("platformSku", sku);
                row.put("qty", intValue(item.get("quantity")));
                row.put("sellPrice", BigDecimal.ZERO);
                row.put("amount", BigDecimal.ZERO);
                row.put("reason", stringValue(ticket.get("reason")));
                row.put("platformStatus", firstNotBlank(actType, stringValue(delivery.get("status"))));
                Map<String, Object> itemExt = new LinkedHashMap<>(item);
                if (StringUtils.isNotBlank(actType)) {
                    itemExt.put("activityType", actType);
                }
                row.put("extendData", JSONUtil.toJsonStr(itemExt));
                resultList.add(row);
            }
        }
        return resultList;
    }

    private Map<String, String> buildMainIdMap(DmpInputMongoResponse dmpResponse) {
        if (!(dmpResponse instanceof DmpInputDmpResponse)) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        DmpInputDmpResponse response = (DmpInputDmpResponse) dmpResponse;
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : response.getConvertInputDmpBaseEntityListMaps().entrySet()) {
            if (!STORAGE_RETURN_INFO.equals(entry.getKey().getStorageName())) {
                continue;
            }
            for (BaseEntity entity : entry.getValue()) {
                DmpSoReturnInfoEntity returnInfo = (DmpSoReturnInfoEntity) entity;
                result.put(returnInfo.getThirdCode(), returnInfo.getId());
            }
        }
        return result;
    }

    private Map<String, Object> baseRow(Map<String, Object> source) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (String key : DmpInputMongoHandler.mongoBaseFiledList) {
            if (source.containsKey(key)) {
                row.put(key, source.get(key));
            }
        }
        Object nextLevelId = source.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID);
        if (nextLevelId != null) {
            row.put("nextLevelId", nextLevelId.toString());
        }
        return row;
    }

    private String buildDetailId(String prefix, String lineNo, String sku) {
        return firstNotBlank(prefix, "") + "_" + firstNotBlank(lineNo, "") + "_" + firstNotBlank(sku, "");
    }

    private String mapReturnType(String origin) {
        if ("channel".equalsIgnoreCase(origin)) {
            return "平台退货";
        } else if ("customer".equalsIgnoreCase(origin)) {
            return "买家退货";
        } else if ("seller".equalsIgnoreCase(origin)) {
            return "卖家退货";
        }
        return origin;
    }

    private String mapReturnStatus(Object statesObj) {
        // 简化映射，可根据实际 states 细化；默认待处理
        String s = stringValue(statesObj);
        if (StringUtils.isBlank(s)) {
            return "1";
        }
        // 示例：可根据 closed 或具体状态调整
        return "1";
    }

    private Map<String, Object> buildExtend(Map<String, Object> ticket) {
        Map<String, Object> extend = new LinkedHashMap<>();
        extend.put("magaluTicketId", ticket.get("id"));
        extend.put("magaluTicketCode", ticket.get("code"));
        extend.put("magaluTicketType", ticket.get("type"));
        extend.put("magaluTicketOrigin", ticket.get("origin"));
        extend.put("magaluOrder", ticket.get("order"));
        return extend;
    }

    // 复用 MagaluOrderDmpHandler 里的小工具（简化版）
    private String stringValue(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private String firstNotBlank(String... vals) {
        for (String v : vals) {
            if (StringUtils.isNotBlank(v)) return v;
        }
        return "";
    }

    private Map<String, Object> mapValue(Object o) {
        if (o instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) o;
            return m;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listMap(Object o) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (o instanceof List) {
            for (Object it : (List<?>) o) {
                if (it instanceof Map) res.add((Map<String, Object>) it);
            }
        }
        return res;
    }

    private LocalDateTime parseTime(Object value) {
        String text = stringValue(value);
        if (StringUtils.isBlank(text)) return null;
        try {
            return OffsetDateTime.parse(text).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    private int intValue(Object value) {
        if (value == null || StringUtils.isBlank(value.toString())) return 0;
        try { return new BigDecimal(value.toString()).intValue(); } catch (Exception e) { return 0; }
    }
}
