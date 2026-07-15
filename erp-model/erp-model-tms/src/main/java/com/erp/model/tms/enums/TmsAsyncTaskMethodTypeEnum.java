package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 异步任务方法类型枚举：在同一 business_type 下区分不同执行方法
 * </p>
 *
 * @author jack
 * @since 2026-05-30
 */
public enum TmsAsyncTaskMethodTypeEnum implements EnumMessage {
    SELFDELIVER_PUSH_ALLOCATION("selfDeliverPushAllocation", "自发货下推分摊"),
    LASTMILE_PUSH_ALLOCATION("lastMilePushAllocation", "尾程下推分摊"),
    SELFDELIVER_UPDATE_RECONCILIATION_STATUS("selfDeliverUpdateReconciliationStatus", "自发货批量更新对账状态"),
    LASTMILE_UPDATE_RECONCILIATION_STATUS("lastMileUpdateReconciliationStatus", "尾程批量更新对账状态"),
	PUSH_ALLOCATION("pushAllocation", "下推分摊"),
	UPDATE_REPORT_STATUS("updateReportStatus", "批量更新核算状态"),
	RE_ALLOCATION("reAllocation", "重新分摊"),
	DELETE("delete", "批量删除"),
	LOGISTICS_RECON_MATCH("logisticsReconMatch", "对账单合并匹配"),
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

    TmsAsyncTaskMethodTypeEnum(String code, String name) {
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
        for (TmsAsyncTaskMethodTypeEnum methodTypeEnum : TmsAsyncTaskMethodTypeEnum.values()) {
            if (code.equals(methodTypeEnum.getCode())) {
                return methodTypeEnum.getName();
            }
        }
        return "";
    }
}
