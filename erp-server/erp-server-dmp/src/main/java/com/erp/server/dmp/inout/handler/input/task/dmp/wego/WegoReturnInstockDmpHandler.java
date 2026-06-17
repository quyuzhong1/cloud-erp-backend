package com.erp.server.dmp.inout.handler.input.task.dmp.wego;

import com.common.business.enums.WarehousePlatformTypeEnum;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import com.erp.server.dmp.inout.handler.output.task.mq.wego.WegoReturnInstockRocketMQTaskHandler;
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
 * WEGO 退货入库单 DMP 转换扩展 handler。
 * <p>
 * 在基类自动映射（Mongo 字段 → {@code dmp_third_return_inbound} 列）之后执行自定义修正：
 * <ol>
 *   <li>将 {@code warehousePlatformType} 强制覆盖为
 *       {@link WarehousePlatformTypeEnum#OVERSEAS_WAREHOUSE}，
 *       保证下游消费端路由至海外仓分支；</li>
 *   <li>将 WEGO 的 {@code arrivalDate}（yyyy-MM-dd 字符串）转换为
 *       {@code putAwayTime}（LocalDateTime），作为退货入库时间写入主表；</li>
 *   <li>将 WEGO 的 {@code date}（订单创建日期）转换为 {@code platformCreateTime}。</li>
 * </ol>
 * <p>
 * 注：WEGO 退货订单无显式 returnType 枚举，{@code returnType} 字段留空，
 * 由下游 {@link WegoReturnInstockRocketMQTaskHandler} 跳过该字段校验直接推送。
 */
@Service
@Scope("prototype")
public class WegoReturnInstockDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final String DMP_KEY_WAREHOUSE_PLATFORM_TYPE = "warehousePlatformType";
    private static final String DMP_KEY_PUT_AWAY_TIME = "putAwayTime";
    private static final String DMP_KEY_PLATFORM_CREATE_TIME = "platformCreateTime";

    /** WEGO API 返回的到仓日期字段名 */
    private static final String MONGO_KEY_ARRIVAL_DATE = "arrivalDate";
    /** WEGO API 返回的订单创建日期字段名 */
    private static final String MONGO_KEY_DATE = "date";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected void afterConvertData(
            Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry
                : dmpInputDataDmpRelationMaps.entrySet()) {

            List<Map<String, Object>> mongoDataList = entry.getKey();
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            if (dmpDataMaps == null || dmpDataMaps.isEmpty()) {
                continue;
            }

            // 从 mongo 原始数据中取到仓日期 / 订单创建日期
            Map<String, Object> mongoData = (mongoDataList != null && !mongoDataList.isEmpty())
                    ? mongoDataList.get(0) : null;
            LocalDateTime putAwayTime = resolveArrivalDateTime(mongoData);
            LocalDateTime platformCreateTime = resolveCreateDateTime(mongoData);

            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                // 强制覆盖为海外仓类型，与 WegoInBoundDmpHandler 策略一致
                dmpDataMap.put(DMP_KEY_WAREHOUSE_PLATFORM_TYPE,
                        WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode());

                if (putAwayTime != null) {
                    dmpDataMap.put(DMP_KEY_PUT_AWAY_TIME, putAwayTime);
                }
                if (platformCreateTime != null) {
                    dmpDataMap.put(DMP_KEY_PLATFORM_CREATE_TIME, platformCreateTime);
                }
            }
        }
    }

    /**
     * 将 WEGO 到仓日期（{@code arrivalDate}，yyyy-MM-dd）转换为 LocalDateTime（当天 00:00:00）。
     */
    private LocalDateTime resolveArrivalDateTime(Map<String, Object> mongoData) {
        if (mongoData == null) {
            return null;
        }
        Object arrivalDateObj = mongoData.get(MONGO_KEY_ARRIVAL_DATE);
        if (arrivalDateObj == null) {
            return null;
        }
        String arrivalDate = arrivalDateObj.toString();
        if (StringUtils.isBlank(arrivalDate)) {
            return null;
        }
        try {
            return LocalDate.parse(arrivalDate.trim(), DATE_FORMATTER).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将 WEGO 订单创建日期（{@code date}，yyyy-MM-dd）转换为 LocalDateTime（当天 00:00:00）。
     */
    private LocalDateTime resolveCreateDateTime(Map<String, Object> mongoData) {
        if (mongoData == null) {
            return null;
        }
        Object dateObj = mongoData.get(MONGO_KEY_DATE);
        if (dateObj == null) {
            return null;
        }
        String date = dateObj.toString();
        if (StringUtils.isBlank(date)) {
            return null;
        }
        try {
            return LocalDate.parse(date.trim(), DATE_FORMATTER).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }
}
