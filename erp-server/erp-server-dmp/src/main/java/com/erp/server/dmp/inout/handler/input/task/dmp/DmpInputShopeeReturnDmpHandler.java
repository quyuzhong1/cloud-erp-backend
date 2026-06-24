package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@Scope("prototype")
public class DmpInputShopeeReturnDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final int RETURN_SOLUTION_RETURN_AND_REFUND = 0;

    private boolean parentTaskShopIdLoaded;
    private String parentTaskShopId;

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
        String shopId = resolveShopeeShopId(dmpDataMap);
        dmpDataMap.put("shopId", shopId);
        dmpDataMap.put("nextLevelId", shopId);

        putIfPresent(dmpDataMap, "return_sn", "thirdCode");
        putIfPresent(dmpDataMap, "order_sn", "platformCode");
        putIfPresent(dmpDataMap, "order_sn", "platformOrderCode");
        putIfPresent(dmpDataMap, "text_reason", "remark");
        putIfPresent(dmpDataMap, "status", "platformStatus");
        putIfPresent(dmpDataMap, "tracking_number", "trackingNumber");

        Object refundAmount = dmpDataMap.get("refund_amount");
        if (refundAmount != null) {
            BigDecimal amount = parseBigDecimal(refundAmount);
            if (amount != null) {
                dmpDataMap.put("allAmount", amount);
            }
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
        Integer returnSolutionValue = parseInteger(returnSolution);
        return returnSolutionValue != null && RETURN_SOLUTION_RETURN_AND_REFUND == returnSolutionValue;
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
        Long epochSecond = parseLong(epochSecondObj);
        if (epochSecond == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault());
    }

    private void putIfPresent(Map<String, Object> source, String sourceKey, String targetKey) {
        Object value = source.get(sourceKey);
        if (value != null) {
            source.put(targetKey, String.valueOf(value));
        }
    }

    /**
     * 子任务 nextLevelId 为拆单 snowflake，不是 OMS shopId；需继承父任务店铺 ID。
     */
    private String resolveShopeeShopId(Map<String, Object> dmpDataMap) {
        String parentShopId = resolveParentTaskShopId();
        if (StringUtils.isNotBlank(parentShopId)) {
            return parentShopId;
        }
        String mongoShopId = Objects.toString(dmpDataMap.get("nextLevelId"), "");
        if (StringUtils.isNotBlank(mongoShopId)) {
            return mongoShopId;
        }
        return StringUtils.defaultString(nextLevelId, "");
    }

    private String resolveParentTaskShopId() {
        if (parentTaskShopIdLoaded) {
            return parentTaskShopId;
        }
        parentTaskShopIdLoaded = true;
        if (dmpInputTaskEntity == null || StringUtils.isBlank(dmpInputTaskEntity.getParentTaskId())) {
            return null;
        }
        DmpInputTaskEntity parentTask = dmpInputTaskService.getById(dmpInputTaskEntity.getParentTaskId());
        if (parentTask == null || StringUtils.isBlank(parentTask.getNextLevelId())) {
            return null;
        }
        parentTaskShopId = parentTask.getNextLevelId();
        return parentTaskShopId;
    }

    private Integer parseInteger(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            log.warn("退货主表字段解析失败,value:{}", value);
            return null;
        }
    }

    private Long parseLong(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            log.warn("退货主表时间字段解析失败,value:{}", value);
            return null;
        }
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            log.warn("退货主表金额字段解析失败,value:{}", value);
            return null;
        }
    }
}
