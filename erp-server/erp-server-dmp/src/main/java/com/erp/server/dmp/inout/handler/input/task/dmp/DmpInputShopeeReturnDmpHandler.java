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
 * Shopee 售后退货主表 DMP 转换。
 */
@Service
@Scope("prototype")
public class DmpInputShopeeReturnDmpHandler extends DmpInputDbConvertDmpHandler {

    public static final String SHOPEE_RETURN_LIST_DATA = "Shopee_returnList_data";
    public static final String SHOPEE_RETURN_DETAIL_DATA = "Shopee_returnDetail_data";

    private static final int RETURN_SOLUTION_RETURN_AND_REFUND = 0;

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
                if (!isReturnAndRefund(dmpDataMap)) {
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
        dmpDataMap.put("shopId", StringUtils.defaultString(Objects.toString(dmpDataMap.get("nextLevelId"), ""), nextLevelId));

        putIfPresent(dmpDataMap, "return_sn", "thirdCode");
        putIfPresent(dmpDataMap, "order_sn", "platformCode");
        putIfPresent(dmpDataMap, "order_sn", "platformOrderCode");
        putIfPresent(dmpDataMap, "text_reason", "remark");
        putIfPresent(dmpDataMap, "status", "platformStatus");
        putIfPresent(dmpDataMap, "tracking_number", "trackingNumber");

        Object refundAmount = dmpDataMap.get("refund_amount");
        if (refundAmount != null) {
            dmpDataMap.put("allAmount", new BigDecimal(String.valueOf(refundAmount)));
        }
        Object currency = dmpDataMap.get("currency");
        if (currency != null) {
            dmpDataMap.put("currencyCode", String.valueOf(currency));
        }

        LocalDateTime createTime = toLocalDateTime(dmpDataMap.get("create_time"));
        if (createTime != null) {
            dmpDataMap.put("platformCreateTime", createTime);
            dmpDataMap.put("returnTime", createTime);
            dmpDataMap.put("billDate", createTime);
        }
        LocalDateTime updateTime = toLocalDateTime(dmpDataMap.get("update_time"));
        if (updateTime != null) {
            dmpDataMap.put("platformUpdateTime", updateTime);
        }

        dmpDataMap.put("status", mapInternalStatus(String.valueOf(dmpDataMap.getOrDefault("platformStatus", ""))));
    }

    private boolean isReturnAndRefund(Map<String, Object> dmpDataMap) {
        Object returnSolution = dmpDataMap.get("return_solution");
        if (returnSolution == null) {
            return false;
        }
        return RETURN_SOLUTION_RETURN_AND_REFUND == Integer.parseInt(String.valueOf(returnSolution));
    }

    private String mapInternalStatus(String platformStatus) {
        if (StringUtils.isBlank(platformStatus)) {
            return "1";
        }
        if ("CANCELLED".equalsIgnoreCase(platformStatus)) {
            return "5";
        }
        return "1";
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
