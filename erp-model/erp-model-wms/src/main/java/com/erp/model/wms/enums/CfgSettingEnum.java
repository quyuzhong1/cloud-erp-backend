package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CfgSettingEnum implements EnumMessage {

    SUBCONTRACT_ISSUE("subcontractIssue","委外发料单设置"),
    PO_RETURN("poReturn","采购退货单设置"),
    PO_RECONCILIATION("poReconciliation","采购对账单设置"),
    FS_QC_NOTICE("fsQcNotice","飞书通知配置"),
    DELIVERY_INTERCEPT("b2cDeliveryIntercept","发货拦截设置"),
    PACKAGE_SETTING("packageSetting","组包设置"),
    TRANSIT_SETTING("transitSetting","中转设置"),
    FINISH_PACKING_NOTICE("finishPackingNotice","装箱完成通知"),
    CFG_PRINT("cfgPrint", "打印配置"),

    WAREHOUSE_LOCATION_MOVE_BLACKLIST("warehouseLocationMoveBlacklist","库位移动同步旺店通黑名单"),
    FS_REQUISITION_NOTICE("fsRequisitionNotice","飞书要货申请通知配置")
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


    CfgSettingEnum(String code, String name) {
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
        for (CfgSettingEnum settingEnum : CfgSettingEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgSettingEnum getEnum(String code) {
        for (CfgSettingEnum settingEnum : CfgSettingEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
