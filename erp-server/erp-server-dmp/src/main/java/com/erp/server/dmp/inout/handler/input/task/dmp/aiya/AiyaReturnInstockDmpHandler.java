package com.erp.server.dmp.inout.handler.input.task.dmp.aiya;

import com.common.business.enums.WarehousePlatformTypeEnum;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import com.erp.server.dmp.inout.handler.output.task.mq.aiya.AiyaReturnInstockRocketMQTaskHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 爱亚退货入库主表 DMP 转换扩展 handler，对齐 {@code WegoReturnInstockDmpHandler}。
 * <p>
 * 在基类自动映射之后：
 * <ol>
 *   <li>强制 {@code warehousePlatformType=OVERSEAS_WAREHOUSE}；</li>
 *   <li>将爱亚 {@code putawayTime} 写入 {@code putAwayTime}。网关真实格式为
 *       {@code yyyy-MM-dd'T'HH:mm:ssZ}（如 {@code 2026-07-29T18:18:54+0800}），
 *       同时兼容方案文档的 {@code yyyy-MM-dd HH:mm:ss}。</li>
 * </ol>
 * 退货类型由下游 {@link AiyaReturnInstockRocketMQTaskHandler} 默认「其他」。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaReturnInstockDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final String DMP_KEY_WAREHOUSE_PLATFORM_TYPE = "warehousePlatformType";
    private static final String DMP_KEY_PUT_AWAY_TIME = "putAwayTime";
    private static final String MONGO_KEY_PUTAWAY_TIME = "putawayTime";
    /** 爱亚网关真实时间格式（与出库单 shippingTime 一致） */
    private static final DateTimeFormatter OFFSET_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    /** 方案文档标注格式，兼容保留 */
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            Map<String, Object> mongoData = (mongoDataList != null && !mongoDataList.isEmpty())
                    ? mongoDataList.get(0) : null;
            LocalDateTime putAwayTime = resolvePutAwayTime(mongoData);
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put(DMP_KEY_WAREHOUSE_PLATFORM_TYPE,
                        WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode());
                if (putAwayTime != null) {
                    dmpDataMap.put(DMP_KEY_PUT_AWAY_TIME, putAwayTime);
                }
            }
        }
    }

    /**
     * 解析爱亚上架时间：优先 {@code yyyy-MM-dd'T'HH:mm:ssZ}，再回退 {@code yyyy-MM-dd HH:mm:ss}。
     *
     * @param mongoData mongo 主记录
     * @return 本地时间；解析失败返回 null（下游 MQ 会跳过推送）
     */
    private LocalDateTime resolvePutAwayTime(Map<String, Object> mongoData) {
        if (mongoData == null) {
            return null;
        }
        Object putawayTimeObj = mongoData.get(MONGO_KEY_PUTAWAY_TIME);
        if (putawayTimeObj == null) {
            return null;
        }
        String putawayTime = putawayTimeObj.toString().trim();
        if (StringUtils.isBlank(putawayTime)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(putawayTime, OFFSET_DATETIME_FORMATTER).toLocalDateTime();
        } catch (Exception ignore) {
            // ignore and try "yyyy-MM-dd HH:mm:ss"
        }
        try {
            return LocalDateTime.parse(putawayTime, DATETIME_FORMATTER);
        } catch (Exception e) {
            Object asnNumber = mongoData.get("asnNumber");
            log.warn("[爱亚退货入库] putawayTime 解析失败，整单将跳过推送。退货单号={}，原始值={}",
                    asnNumber, putawayTime, e);
            return null;
        }
    }
}
