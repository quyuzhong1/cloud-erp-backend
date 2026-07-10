package com.erp.server.dmp.inout.handler.input.task.dmp.wego;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import com.erp.server.dmp.inout.handler.input.task.dmp.jifeng.JiFengOutBoundDmpHandler;
import com.erp.server.dmp.inout.handler.input.task.init.api.wego.WegoOutboundInitHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * WEGO 2C 出库单 DMP 转换扩展 handler，与 {@link JiFengOutBoundDmpHandler} 等价。
 * <p>
 * 承接 {@link WegoOutboundInitHandler} 写入 mongo 的 {@link com.sdk.wms.wego.dto.response.WegoOutboundResp.OutboundOrderDTO}
 * 原始数据，在字段级 mapping 完成后做如下后处理：
 * <ol>
 *   <li>{@code finishDate}（"yyyy-MM-dd HH:mm:ss" 或 "yyyy-MM-dd"）→ {@code dateShipping}（{@link LocalDateTime}），
 *       供下游 {@code PlatformOutboundConsumerService} 写出库时间；</li>
 *   <li>{@code logisticsList[0].trackingNum} → {@code trackingNo}，取第一条非空物流跟踪号；</li>
 *   <li>{@code orderStatus}（mongo 中为 Integer）→ {@code orderStatus}（String），
 *       与 {@link com.sdk.wms.wego.enums.WegoEnums.OrderStatusEnum} 状态码保持一致；</li>
 *   <li>{@code createTime} → {@code platformCreateTime}；</li>
 *   <li>固定写入 {@code warehousePlatformType} = {@code overseasWarehouse}，防止接口 raw 字段污染路由分支；</li>
 *   <li>固定写入 {@code orderType} = {@code B2C}，标记订单类型。</li>
 * </ol>
 * <p>
 * 多例：因父类持有成员变量，Spring 管理为 {@link Scope}({@code prototype})。
 */
@Slf4j
@Service
@Scope("prototype")
public class WegoOutBoundDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String MONGO_KEY_NO = "no";
    private static final String MONGO_KEY_FINISH_DATE = "finishDate";
    private static final String MONGO_KEY_CREATE_TIME = "createTime";
    private static final String MONGO_KEY_LOGISTICS_LIST = "logisticsList";
    private static final String MONGO_KEY_TRACKING_NUM = "trackingNum";
    private static final String MONGO_KEY_ORDER_STATUS = "orderStatus";

    private static final String DMP_KEY_DATE_SHIPPING = "dateShipping";
    private static final String DMP_KEY_TRACKING_NO = "trackingNo";
    private static final String DMP_KEY_PLATFORM_CREATE_TIME = "platformCreateTime";
    private static final String DMP_KEY_WAREHOUSE_PLATFORM_TYPE = "warehousePlatformType";
    private static final String DMP_KEY_ORDER_STATUS = "orderStatus";
    private static final String DMP_KEY_ORDER_TYPE = "orderType";

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            if (dmpDataMaps == null || dmpDataMaps.isEmpty()) {
                continue;
            }
            List<Map<String, Object>> mongoDataMaps = entry.getKey();
            if (mongoDataMaps == null || mongoDataMaps.isEmpty()) {
                continue;
            }
            Map<String, Object> mongoData = mongoDataMaps.get(0);

            Object wegoNo = mongoData.get(MONGO_KEY_NO);
            LocalDateTime dateShipping = resolveDateTime(mongoData.get(MONGO_KEY_FINISH_DATE), MONGO_KEY_FINISH_DATE, wegoNo);
            LocalDateTime platformCreateTime = resolveDateTime(mongoData.get(MONGO_KEY_CREATE_TIME), MONGO_KEY_CREATE_TIME, wegoNo);
            String trackingNo = resolveTrackingNo(mongoData.get(MONGO_KEY_LOGISTICS_LIST));
            String orderStatus = mongoData.get(MONGO_KEY_ORDER_STATUS) != null
                    ? String.valueOf(mongoData.get(MONGO_KEY_ORDER_STATUS)) : null;

            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                if (dateShipping != null) {
                    dmpDataMap.put(DMP_KEY_DATE_SHIPPING, dateShipping);
                }
                if (platformCreateTime != null) {
                    dmpDataMap.put(DMP_KEY_PLATFORM_CREATE_TIME, platformCreateTime);
                }
                if (StringUtils.isNotBlank(trackingNo)) {
                    dmpDataMap.put(DMP_KEY_TRACKING_NO, trackingNo);
                }
                if (StringUtils.isNotBlank(orderStatus)) {
                    dmpDataMap.put(DMP_KEY_ORDER_STATUS, orderStatus);
                }
                dmpDataMap.put(DMP_KEY_WAREHOUSE_PLATFORM_TYPE, WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode());
                dmpDataMap.put(DMP_KEY_ORDER_TYPE, OrderTypeEnum.B2C.getCode());
            }
        }
    }

    /**
     * 解析日期字符串为 {@link LocalDateTime}，优先按 "yyyy-MM-dd HH:mm:ss" 解析，
     * 回退到 "yyyy-MM-dd"（取当日 00:00:00）。
     * <p>
     * 两种格式均解析失败时返回 null 并打印 warn，记录 WEGO 单号与原始值，
     * 避免发货时间等字段静默丢失且无法从日志定位。
     *
     * @param value     mongo 原始日期值
     * @param fieldName 字段名（用于日志定位，如 finishDate / createTime）
     * @param bizNo     WEGO 出库单号（{@code no}），用于日志定位
     */
    private LocalDateTime resolveDateTime(Object value, String fieldName, Object bizNo) {
        if (value == null) {
            return null;
        }
        String str = value.toString().trim();
        if (StringUtils.isBlank(str)) {
            return null;
        }
        try {
            return LocalDateTime.parse(str, DATETIME_FORMATTER);
        } catch (Exception ignore) {
            // ignore and try date-only format
        }
        try {
            return LocalDate.parse(str, DATE_FORMATTER).atStartOfDay();
        } catch (Exception e) {
            log.warn("[WEGO出库] {} 日期格式解析失败，该字段将不写入DMP。WEGO单号={}，{}原始值={}",
                    fieldName, bizNo, fieldName, str, e);
            return null;
        }
    }

    /**
     * 从 {@code logisticsList} 中提取第一条非空 {@code trackingNum}。
     * <p>
     * mongo 中 logisticsList 可能是 {@link java.util.List} 或 fastjson {@link com.alibaba.fastjson.JSONArray}，
     * 统一通过 JSON 序列化后再解析处理。
     */
    private String resolveTrackingNo(Object logisticsListObj) {
        if (logisticsListObj == null) {
            return null;
        }
        try {
            JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(logisticsListObj));
            if (jsonArray == null || jsonArray.isEmpty()) {
                return null;
            }
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject logistics = jsonArray.getJSONObject(i);
                if (logistics == null) {
                    continue;
                }
                String trackingNum = logistics.getString(MONGO_KEY_TRACKING_NUM);
                if (StringUtils.isNotBlank(trackingNum)) {
                    return trackingNum;
                }
            }
        } catch (Exception e) {
            // 解析失败时静默跳过，不影响主流程
        }
        return null;
    }
}
