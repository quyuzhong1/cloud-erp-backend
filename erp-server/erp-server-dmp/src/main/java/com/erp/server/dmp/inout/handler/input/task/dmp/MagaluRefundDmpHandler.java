package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Magalu 售后活动 -> 退款单 (dmp_so_refund_info / detail)
 * 只处理 activity.type = "refunded" 且 ticket.closed == true 的数据。
 * 与退货 (return) 使用不同存储表和独立的 Output Handler。
 */
@Service
@Scope("prototype")
public class MagaluRefundDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final String STORAGE_REFUND_INFO = "dmp_so_refund_info";
    private static final String STORAGE_REFUND_DETAIL = "dmp_so_refund_detail";
    private static final String MAGALU_PLATFORM = "Magalu";

    @Override
    public List<Map<String, Object>> convertMongoToDmp(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse) {
        List<Map<String, Object>> payloadList = super.convertMongoToDmp(dmpRequest, dmpResponse);
        if (CollUtil.isEmpty(payloadList)) {
            return Collections.emptyList();
        }
        if (STORAGE_REFUND_INFO.equals(storageName)) {
            return buildRefundInfoRows(payloadList);
        }
        if (STORAGE_REFUND_DETAIL.equals(storageName)) {
            return buildRefundDetailRows(payloadList);
        }
        return payloadList;
    }

    private List<Map<String, Object>> buildRefundInfoRows(List<Map<String, Object>> payloadList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> payload : payloadList) {
            Map<String, Object> ticket = mapValue(payload.get("ticket"));
            Map<String, Object> activity = mapValue(payload.get("activity"));
            if (ticket.isEmpty() && activity.isEmpty()) {
                ticket = payload;
            }

            String actType = stringValue(activity.get("type"));
            if (!"refunded".equalsIgnoreCase(actType)) {
                continue;
            }
            // 已在 ApiInitHandler 过滤 closed，这里再兜底一次
            if (!isTicketClosed(ticket)) {
                continue;
            }

            Map<String, Object> row = baseRow(ticket);
            String ticketCode = stringValue(ticket.get("code"));
            String thirdCode = firstNotBlank(ticketCode, stringValue(ticket.get("id")));

            Map<String, Object> order = mapValue(ticket.get("order"));
            String platformOrderCode = stringValue(order.get("code"));

            Map<String, Object> amounts = mapValue(order.get("amounts"));

            row.put("platformCreateTime", parseTime(ticket.get("created_at")));
            row.put("platformUpdateTime", parseTime(ticket.get("updated_at")));
            row.put("refundTime", parseTime(ticket.get("updated_at"))); // 或活动时间
            row.put("sourceSystem", MAGALU_PLATFORM);
            row.put("sourcePlatform", MAGALU_PLATFORM);
            row.put("thirdCode", thirdCode);
            row.put("platformCode", platformOrderCode);
            row.put("platformRefundNo", ticketCode);
            row.put("shopId", row.get("nextLevelId"));
            row.put("shopName", "");
            row.put("status", "1"); // 成功
            row.put("platformOriginalStatus", actType);
            row.put("reason", stringValue(ticket.get("reason")));
            row.put("remark", stringValue(ticket.get("reason")));
            row.put("currencyCode", firstNotBlank(stringValue(amounts.get("currency")), "BRL"));
            row.put("amount", money(amounts.get("total"), amounts.get("normalizer")));
            row.put("exchangeRate", BigDecimal.ONE);

            Map<String, Object> ext = buildExtend(ticket, activity);
            row.put("extendData", JSONUtil.toJsonStr(ext));
            resultList.add(row);
        }
        return resultList;
    }

    private List<Map<String, Object>> buildRefundDetailRows(List<Map<String, Object>> payloadList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> payload : payloadList) {
            Map<String, Object> ticket = mapValue(payload.get("ticket"));
            Map<String, Object> activity = mapValue(payload.get("activity"));
            if (ticket.isEmpty() && activity.isEmpty()) {
                ticket = payload;
            }

            String actType = stringValue(activity.get("type"));
            if (!"refunded".equalsIgnoreCase(actType)) {
                continue;
            }
            if (!isTicketClosed(ticket)) {
                continue;
            }

            Map<String, Object> order = mapValue(ticket.get("order"));
            Map<String, Object> delivery = mapValue(order.get("delivery"));
            List<Map<String, Object>> items = listMap(delivery.get("items"));

            String ticketCode = stringValue(ticket.get("code"));
            String ticketId = stringValue(ticket.get("id"));
            String thirdCode = firstNotBlank(ticketCode, ticketId);

            for (Map<String, Object> item : items) {
                Map<String, Object> row = baseRow(ticket);
                Map<String, Object> info = mapValue(item.get("info"));
                String sku = firstNotBlank(stringValue(info.get("sku")), stringValue(item.get("sku")));
                String lineId = firstNotBlank(stringValue(item.get("id")), sku);

                row.put("thirdCode", thirdCode); // for linking if needed
                row.put("mainId", thirdCode); // will be resolved by framework or use thirdCode as temp
                row.put("thirdDetailId", buildDetailId(ticketId, lineId, sku));
                row.put("platformDetailId", buildDetailId(ticketId, lineId, sku));
                row.put("skuId", "");
                row.put("skuNo", sku);
                row.put("qty", intValue(item.get("quantity")));
                row.put("amount", BigDecimal.ZERO);
                row.put("platformSku", sku);
                row.put("extendData", JSONUtil.toJsonStr(item));
                resultList.add(row);
            }
        }
        return resultList;
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

    private Map<String, Object> buildExtend(Map<String, Object> ticket, Map<String, Object> activity) {
        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("magaluTicketId", ticket.get("id"));
        ext.put("magaluTicketCode", ticket.get("code"));
        ext.put("activityType", activity.get("type"));
        ext.put("magaluOrder", ticket.get("order"));
        return ext;
    }

    private String buildDetailId(String prefix, String lineNo, String sku) {
        return firstNotBlank(prefix, "") + "_" + firstNotBlank(lineNo, "") + "_" + firstNotBlank(sku, "");
    }

    private boolean isTicketClosed(Map<String, Object> ticket) {
        if (ticket == null) return false;
        Object closed = ticket.get("closed");
        if (closed == null) return false;
        String s = String.valueOf(closed).trim().toLowerCase();
        return "true".equals(s) || "1".equals(s);
    }

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

    private BigDecimal money(Object value, Object normalizer) {
        BigDecimal amount = decimalValue(value);
        BigDecimal divisor = decimalValue(normalizer);
        if (BigDecimal.ZERO.compareTo(divisor) == 0) divisor = BigDecimal.ONE;
        return amount.divide(divisor, 6, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal decimalValue(Object value) {
        if (value == null || StringUtils.isBlank(value.toString())) return BigDecimal.ZERO;
        try { return new BigDecimal(value.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private int intValue(Object value) {
        if (value == null || StringUtils.isBlank(value.toString())) return 0;
        try { return new BigDecimal(value.toString()).intValue(); } catch (Exception e) { return 0; }
    }
}
