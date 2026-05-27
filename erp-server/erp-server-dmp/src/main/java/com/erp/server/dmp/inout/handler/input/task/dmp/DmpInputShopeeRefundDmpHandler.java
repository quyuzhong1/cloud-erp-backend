package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.business.enums.PlatformDictEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Shopee 仅退款主表 DMP 转换：return_solution=1 且 status=CANCELLED（测试环境暂无 CLOSED 样本）。
 */
@Service
@Scope("prototype")
public class DmpInputShopeeRefundDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final int RETURN_SOLUTION_REFUND_ONLY = 1;
    private static final String STATUS_REFUND = "CANCELLED";
    private static final int TEXT_MAX_LENGTH = 255;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        Iterator<Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>>> iterator =
                dmpInputDataDmpRelationMaps.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry = iterator.next();
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            Iterator<TreeMap<String, Object>> dataIterator = dmpDataMaps.iterator();
            while (dataIterator.hasNext()) {
                TreeMap<String, Object> dmpDataMap = dataIterator.next();
                if (!isRefundOnlyClosed(dmpDataMap)) {
                    dataIterator.remove();
                    continue;
                }
                fillHeader(dmpDataMap);
            }
            if (dmpDataMaps.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private void fillHeader(TreeMap<String, Object> dmpDataMap) {
        dmpDataMap.put("sourceSystem", PlatformDictEnum.SHOPEE.getCode());
        dmpDataMap.put("sourcePlatform", PlatformDictEnum.SHOPEE.getCode());
        dmpDataMap.put("shopId", StringUtils.defaultString(Objects.toString(dmpDataMap.get("nextLevelId"), ""), nextLevelId));

        putIfPresent(dmpDataMap, "return_sn", "thirdCode");
        putIfPresent(dmpDataMap, "order_sn", "platformCode");
        putIfPresent(dmpDataMap, "order_sn", "platformOrderCode");

        Object textReason = dmpDataMap.get("text_reason");
        if (textReason != null) {
            String reason = StringUtils.left(String.valueOf(textReason), TEXT_MAX_LENGTH);
            dmpDataMap.put("reason", reason);
            dmpDataMap.put("remark", reason);
        }

        String platformStatus = resolvePlatformStatus(dmpDataMap);
        if (StringUtils.isNotBlank(platformStatus)) {
            dmpDataMap.put("platformOriginalStatus", platformStatus);
        }
        dmpDataMap.put("status", "1");

        Object refundAmount = dmpDataMap.get("refund_amount");
        if (refundAmount != null) {
            dmpDataMap.put("amount", new BigDecimal(String.valueOf(refundAmount)));
        }
        Object currency = dmpDataMap.get("currency");
        if (currency != null) {
            dmpDataMap.put("currencyCode", String.valueOf(currency));
        }

        LocalDateTime createTime = toLocalDateTime(dmpDataMap.get("create_time"));
        if (createTime != null) {
            dmpDataMap.put("platformCreateTime", createTime);
        }
        LocalDateTime updateTime = toLocalDateTime(dmpDataMap.get("update_time"));
        if (updateTime != null) {
            dmpDataMap.put("platformUpdateTime", updateTime);
            dmpDataMap.put("refundTime", updateTime);
        } else if (createTime != null) {
            dmpDataMap.put("refundTime", createTime);
        }
    }

    private boolean isRefundOnlyClosed(Map<String, Object> dmpDataMap) {
        Object returnSolution = dmpDataMap.get("return_solution");
        if (returnSolution == null
                || RETURN_SOLUTION_REFUND_ONLY != Integer.parseInt(String.valueOf(returnSolution))) {
            return false;
        }
        return STATUS_REFUND.equalsIgnoreCase(resolvePlatformStatus(dmpDataMap));
    }

    private String resolvePlatformStatus(Map<String, Object> dmpDataMap) {
        Object platformStatus = dmpDataMap.get("platformOriginalStatus");
        if (platformStatus == null) {
            platformStatus = dmpDataMap.get("platform_original_status");
        }
        if (platformStatus == null) {
            platformStatus = dmpDataMap.get("platformStatus");
        }
        if (platformStatus == null) {
            platformStatus = dmpDataMap.get("status");
        }
        return String.valueOf(platformStatus == null ? "" : platformStatus);
    }

    private LocalDateTime toLocalDateTime(Object epochSecondObj) {
        if (epochSecondObj == null || StringUtils.isBlank(String.valueOf(epochSecondObj))) {
            return null;
        }
        long epochSecond = Long.parseLong(String.valueOf(epochSecondObj));
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault());
    }

    private void putIfPresent(Map<String, Object> source, String sourceKey, String targetKey) {
        Object value = source.get(sourceKey);
        if (value != null) {
            source.put(targetKey, String.valueOf(value));
        }
    }
}
