package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Lambda
 * @Classname CfgSettingEnum

 * @Date 2025-03-20 14:15
 * @Created by ZDY
 */
public enum CfgSettingEnum implements EnumMessage {

    TIME_OUT_CONFIG("timeOutConfig",  "超时设置"),
    PAY_METHOD("payMethod",  "付款方式设置"),
    AMZ_AUTH_PRE_STATE("amzAuthPreState",  "亚马逊授权state前缀"),
    DHT_CUSTOMER_WHITELIST("dhtCustomerWhitelist",  "订货通客户白名单"),
    /**
     * 销售订单基础信息默认值（JSON 结构，按环境配置）
     * <pre>
     * {
     *   "transactionSubType": "offlineOrder",      // 单据子类型默认值（OrderSubTypeEnum.code）
     *   "warehouseId": "...",                       // 默认仓库 ID
     *   "salesOrgId": "...",                        // 默认销售组织 ID
     *   "receiveAccountMap": {                      // 销售组织 ID -> 收款账号 ID 联动映射
     *     "salesOrgId1": "receiveAccountId1",
     *     "salesOrgId2": "receiveAccountId2"
     *   }
     * }
     * </pre>
     * 业务方按环境在 cfg_setting 表配置；未配置则不兜底，前端继续以原表单为准。
     */
    SO_INFO_DEFAULT("soInfoDefault",  "销售订单基础信息默认值"),

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
