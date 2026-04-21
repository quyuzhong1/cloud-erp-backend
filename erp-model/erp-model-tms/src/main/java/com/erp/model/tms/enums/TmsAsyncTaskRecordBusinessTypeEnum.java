package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 异步任务记录 状态 枚举
 * </p>
 *
 * @author jack
 * @since 2026-01-28 12:16:20
 */
public enum TmsAsyncTaskRecordBusinessTypeEnum implements EnumMessage {
    FIRST_MILE_COST_ALLOCATION("firstMileCostAllocation", "头程费用分摊"),
    SMALL_BAG_COST_ALLOCATION("smallBagCostAllocation", "小包费用分摊"),
    TRANSFER_DECLARE_COST_ALLOCATION("transferDeclareCostAllocation", "中转费用分摊"),
    TMS_FIRST_MILE_RECONCILIATION("tmsFirstMileReconciliation", "头程对账单"),
    TMS_B2C_DECLARE_RECONCILIATION("tmsB2cDeclareReconciliation", "B2c报关对账单"),
    LOGISTICS_BILL_COST("logisticsBillCost", "自发货物流费用"),
    LAST_MILE_LOGISTICS_BILL_COST("lastMileLogisticsBillCost", "尾程物流费用"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    TmsAsyncTaskRecordBusinessTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (TmsAsyncTaskRecordBusinessTypeEnum statusEnum : TmsAsyncTaskRecordBusinessTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static List<String> getStatusList() {
        return Arrays.stream(TmsAsyncTaskRecordBusinessTypeEnum.values()).map(TmsAsyncTaskRecordBusinessTypeEnum::getCode).collect(Collectors.toList());
    }

    public static TmsAsyncTaskRecordBusinessTypeEnum getByCode(String code){
        return Arrays.stream(values()).filter(a -> a.getCode().equalsIgnoreCase(code))
                .findFirst().orElse(null);
    }
}
