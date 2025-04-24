package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
/**
 * 通知节点类型
 * @Auther will
 * @Date 2025/2/12 16:13
 */
public enum CfgVirtualNoticeNodeTypeEnum implements EnumMessage {

    INVENTORY_DIFF("inventoryDIff","库存分配差异通知"),
    INVENTORY_DETAIL_DIFF("inventoryDetailDiff","库龄差异通知"),
    FROZEN_INVENTORY_DIFF("frozenInventoryDiff","冻结库存差异通知"),
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


    CfgVirtualNoticeNodeTypeEnum(String code, String name) {
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
        for (CfgVirtualNoticeNodeTypeEnum settingEnum : CfgVirtualNoticeNodeTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgVirtualNoticeNodeTypeEnum getEnum(String code) {
        for (CfgVirtualNoticeNodeTypeEnum settingEnum : CfgVirtualNoticeNodeTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
