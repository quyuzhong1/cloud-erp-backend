package com.erp.server.dmp.inout.handler.input.task.dmp.aiya;

import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import com.erp.server.dmp.inout.handler.input.task.dmp.wego.WegoOutBoundDmpHandler;
import com.erp.server.dmp.inout.handler.input.task.init.api.aiya.AiyaOutboundInitHandler;
import com.sdk.wms.aiya.enums.AiyaEnums;
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
 * AIYA 2C 出库单 DMP 转换扩展 handler，与 {@link WegoOutBoundDmpHandler} 等价。
 * <p>
 * 承接 {@link AiyaOutboundInitHandler} 写入 mongo 的
 * {@code com.sdk.wms.aiya.dto.response.AiyaOutboundResp.OutboundOrderDTO} 原始数据，
 * 与 WEGO 不同的是本类原始数据已是扁平结构（orderNumber/warehouseCode/status/trackingNumber
 * 等均为顶层标量字段），大部分字段可直接由 {@code dmp_cfg_input_convert} 数据库字段映射完成，
 * 无需本类介入；本类仅做以下无法由字段映射直接表达的后处理：
 * <ol>
 *   <li>{@code shippingTime}（文档发运时间，格式 {@code yyyy-MM-dd HH:mm:ss}）
 *       → {@code dateShipping}（{@link LocalDateTime}）；</li>
 *   <li>{@code status}（官方 VALID/HELD/CANCELLED；兼容历史 {@code orderStatus}）→ 统一转 String；</li>
 *   <li>{@code status=VALID} 时联合 {@code stage} 二次确认：仅 {@code stage=SHIPPED}（2026-07-29
 *       联调实测确认）才视为已发货终态写入 {@code orderStatus}；否则视为尚在履约中（PICKING/PACKING 等），
 *       本轮跳过 {@code orderStatus} 推送，避免 ERP 侧被提前标记为已发货，详见
 *       {@link AiyaEnums.StageEnum}；</li>
 *   <li>固定写入 {@code warehousePlatformType} = {@code overseasWarehouse}；</li>
 *   <li>固定写入 {@code orderType} = {@code B2C}。</li>
 * </ol>
 * <p>
 * 多例：因父类持有成员变量，Spring 管理为 {@link Scope}({@code prototype})。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaOutBoundDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String MONGO_KEY_ORDER_NUMBER = "orderNumber";
    private static final String MONGO_KEY_SHIPPING_TIME = "shippingTime";
    /** 官方字段 status；兼容历史误写 orderStatus */
    private static final String MONGO_KEY_STATUS = "status";
    private static final String MONGO_KEY_ORDER_STATUS_LEGACY = "orderStatus";
    /** 官方字段 stage：VALID 是否等价"已发货"需联合此字段二次确认，见类注释 */
    private static final String MONGO_KEY_STAGE = "stage";

    private static final String DMP_KEY_DATE_SHIPPING = "dateShipping";
    private static final String DMP_KEY_ORDER_STATUS = "orderStatus";
    private static final String DMP_KEY_WAREHOUSE_PLATFORM_TYPE = "warehousePlatformType";
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

            Object orderNumber = mongoData.get(MONGO_KEY_ORDER_NUMBER);
            LocalDateTime dateShipping = resolveDateTime(mongoData.get(MONGO_KEY_SHIPPING_TIME), orderNumber);
            Object statusRaw = mongoData.get(MONGO_KEY_STATUS);
            if (statusRaw == null) {
                statusRaw = mongoData.get(MONGO_KEY_ORDER_STATUS_LEGACY);
            }
            String orderStatus = statusRaw != null ? String.valueOf(statusRaw) : null;
            if (AiyaEnums.OrderStatusEnum.VALID.getCode().equalsIgnoreCase(orderStatus)) {
                Object stageRaw = mongoData.get(MONGO_KEY_STAGE);
                String stage = stageRaw != null ? String.valueOf(stageRaw) : null;
                if (!AiyaEnums.StageEnum.isShipped(stage)) {
                    // VALID 只代表未被拦截，stage 非 SHIPPED 说明尚在履约中，本轮不推送状态，避免 ERP 侧被提前标记已发货
                    log.warn("[AIYA出库] status=VALID但stage非SHIPPED，暂不视为已发货终态，本轮跳过orderStatus推送。AIYA单号={}，stage原始值={}",
                            orderNumber, stage);
                    orderStatus = null;
                }
            }

            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                if (dateShipping != null) {
                    dmpDataMap.put(DMP_KEY_DATE_SHIPPING, dateShipping);
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
     * 解析发运时间字符串为 {@link LocalDateTime}，依次尝试 {@code yyyy-MM-dd HH:mm:ss}
     * （文档 {@code shippingTime} 格式）/ {@code yyyy-MM-dd}（取当日 00:00:00）。
     */
    private LocalDateTime resolveDateTime(Object value, Object bizNo) {
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
            log.warn("[AIYA出库] shippingTime 日期格式解析失败，该字段将不写入DMP。AIYA单号={}，shippingTime原始值={}",
                    bizNo, str, e);
            return null;
        }
    }
}
