package com.erp.model.oms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.constant.EnumMessage;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * <p>
 * 销售订单-tiktok全托管属性表 平台订单来源 枚举
 * </p>
 *
 * @author zdy
 * @since 2025-03-24 17:08:06
 */
public enum FullyManagedPlatformStatusEnum implements EnumMessage {
    WAIT_CONFIRM("waitConfirm", "待确认"),
    CONFIRMED("confirmed", "已确认"),
    SHIPPED("shipped", "已发货"),
    WAIT_RECEIVE("waitReceive", "待收货"),
    RECEIVED("received", "已收货"),
    WAIT_QC("waitQc", "待质检"),
    WAIT_INSTOCK("waitInstock", "待上架"),
    RETURN("return", "已退供"),
    SOTOCK_IN("stockIn", "已入库"),
    INVAILD("invalid", "已作废"),
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

    FullyManagedPlatformStatusEnum(String code, String name) {
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
        for (FullyManagedPlatformStatusEnum statusEnum : FullyManagedPlatformStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }


    @Getter
    public enum TikTokStatusEnum {
        WAIT_CONFIRM("WAIT_CONFIRM","已建单待商家确认", FullyManagedPlatformStatusEnum.WAIT_CONFIRM),
        WAIT_SEND("WAIT_SEND","已确认待商家发货", FullyManagedPlatformStatusEnum.CONFIRMED),
        SENDED("SENDED","商家已发货", FullyManagedPlatformStatusEnum.SHIPPED),
        SIGNED("SIGNED","已签收待仓库收货", FullyManagedPlatformStatusEnum.WAIT_RECEIVE),
        RECEIVED("RECEIVED","已收货完成待装箱", FullyManagedPlatformStatusEnum.RECEIVED),
        IN_QUALITY_CHECK("IN_QUALITY_CHECK","已装箱完成待质检", FullyManagedPlatformStatusEnum.WAIT_QC),
        QUALITY_CHECK_COMPLETED("QUALITY_CHECK_COMPLETED","已质检完成待上架", FullyManagedPlatformStatusEnum.WAIT_INSTOCK),
        RETURN_COMPLETED("RETURN_COMPLETED","已退供", FullyManagedPlatformStatusEnum.RETURN),
        INBOUND("INBOUND","已入库上架", FullyManagedPlatformStatusEnum.SOTOCK_IN),
        INVAILD("INVAILD","已作废", FullyManagedPlatformStatusEnum.INVAILD),
        ;
                ;
        private final String code;
        private final String name;
        private final FullyManagedPlatformStatusEnum erpEnum;

        TikTokStatusEnum(String code, String name, FullyManagedPlatformStatusEnum erpEnum) {
            this.code = code;
            this.name = name;
            this.erpEnum = erpEnum;
        }
    }

    public static String getErpCodeByCode(String platform,String code) {
        if (StringUtils.isBlank(platform) || StringUtils.isBlank(code)) {
            return "";
        }
        if(PlatformDictEnum.TIK_TOK_FULLY.getCode().equals(platform)){
            for (TikTokStatusEnum statusEnum : TikTokStatusEnum.values()) {
                if (code.equals(statusEnum.getCode())) {
                    return statusEnum.getErpEnum().getCode();
                }
            }
        }
        return "";
    }

    public static String getErpNameByCode(String platform,String code) {
        if (StringUtils.isBlank(platform) || StringUtils.isBlank(code)) {
            return "";
        }
        if(PlatformDictEnum.TIK_TOK_FULLY.getCode().equals(platform)){
            for (TikTokStatusEnum statusEnum : TikTokStatusEnum.values()) {
                if (code.equals(statusEnum.getCode())) {
                    return statusEnum.getErpEnum().getName();
                }
            }
        }
        return "";
    }
}
