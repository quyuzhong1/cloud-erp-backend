package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
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
 * Shopee 仅退款主表 DMP 转换：return_solution=1 且 status=ACCEPTED。
 */
@Service
@Scope("prototype")
public class DmpInputShopeeRefundDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final int RETURN_SOLUTION_REFUND_ONLY = 1;
    private static final String STATUS_REFUND_ACCEPTED = "ACCEPTED";
    private static final String INTERNAL_STATUS_NORMAL = "1";
    private static final int TEXT_MAX_LENGTH = 255;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        // 父任务店铺在同一批次内固定，循环外解析一次，避免每条仅退款记录重复查父任务。
        String parentShopId = resolveParentTaskShopId();
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
                fillHeader(dmpDataMap, parentShopId);
            }
            if (dmpDataMaps.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private void fillHeader(TreeMap<String, Object> dmpDataMap, String parentShopId) {
        dmpDataMap.put("sourceSystem", PlatformDictEnum.SHOPEE.getCode());
        dmpDataMap.put("sourcePlatform", PlatformDictEnum.SHOPEE.getCode());
        String shopId = resolveShopeeShopId(dmpDataMap, parentShopId);
        dmpDataMap.put("shopId", shopId);
        dmpDataMap.put("nextLevelId", shopId);

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
        dmpDataMap.put("status", INTERNAL_STATUS_NORMAL);

        Object refundAmount = dmpDataMap.get("refund_amount");
        BigDecimal amount = parseBigDecimal(refundAmount);
        if (amount == null) {
            throw new ServiceException("Shopee仅退款金额解析失败,returnSn:"
                    + Objects.toString(dmpDataMap.get("return_sn"), "")
                    + ",orderSn:" + Objects.toString(dmpDataMap.get("order_sn"), ""));
        }
        dmpDataMap.put("amount", amount);
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
        Integer returnSolutionValue = parseInteger(returnSolution);
        if (returnSolutionValue == null || RETURN_SOLUTION_REFUND_ONLY != returnSolutionValue) {
            return false;
        }
        return STATUS_REFUND_ACCEPTED.equalsIgnoreCase(resolvePlatformStatus(dmpDataMap));
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
        Long epochSecond = parseLong(epochSecondObj);
        if (epochSecond == null) {
            return null;
        }
        // Shopee 订单/退款链路的 epoch 秒统一按 ERP 服务默认时区转换，保持同平台 DMP 处理口径一致。
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
    private String resolveShopeeShopId(Map<String, Object> dmpDataMap, String parentShopId) {
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
        if (dmpInputTaskEntity == null || StringUtils.isBlank(dmpInputTaskEntity.getParentTaskId())) {
            return null;
        }
        DmpInputTaskEntity parentTask = dmpInputTaskService.getById(dmpInputTaskEntity.getParentTaskId());
        if (parentTask == null || StringUtils.isBlank(parentTask.getNextLevelId())) {
            return null;
        }
        return parentTask.getNextLevelId();
    }

    private Integer parseInteger(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
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
            return null;
        }
    }
}
