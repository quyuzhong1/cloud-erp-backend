package com.erp.model.sys.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 三方通知推送记录 通知节点类型
 * </p>
 *
 * @author jack
 * @since 2025-007-02
 */
public enum ThirdNoticePushRecordNoticeNodeEnum implements EnumMessage {
    WAIT_SUBMITTO_APPROVEING("waitSubmitToApproveIng", "提审"),
    ADD_RECORD("addRecord", "新增记录"),
    QC_BACK_FILL_PACKAGING ("qcBackFillPackaging ", "产品尺寸变更"),
    NOT_SUBSEQUENT_BATCH("notSubsequentBatch", "新品通知"),
    SUBSEQUENT_BATCH("subsequentBatch", "老品通知"),
    UNLOCK_VIRTUAL_INVENTORY("unLockVirtualInventory", "释放SKU虚拟仓库存"),
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

    ThirdNoticePushRecordNoticeNodeEnum(String code, String name) {
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
        for (ThirdNoticePushRecordNoticeNodeEnum statusEnum : ThirdNoticePushRecordNoticeNodeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
