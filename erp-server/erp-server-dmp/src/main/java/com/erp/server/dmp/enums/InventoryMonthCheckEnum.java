package com.erp.server.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author jack
 * @date 2025-04-06
 */
public enum InventoryMonthCheckEnum implements EnumMessage {
	ADS_ERP_OUTSTOCK_DIFF_FLOW("ads_erp_outstock_diff_flow","平台单据差异"),
    ADS_ERP_INVENTORY_DIFF_FLOW("ads_erp_inventory_diff_flow","平台流水差异"),
    ADS_ERP_DIFF_OUTSTOCK_SYNC("ads_erp_diff_outstock_sync","出库同步差异"),
    ADS_ERP_DIFF_RETURN_INSTOCK_SYNC("ads_erp_diff_return_instock_sync","退货同步差异"),
    ADS_ERP_RECEIVE_FLOW_DIFF("ads_erp_receive_flow_diff","签收流水差异"),
    ADS_ERP_INVENTORY_DIFF("ads_erp_inventory_diff","平台库存差异"),
    ADS_ERP_INVENTORY_DIFF_KINGDEE("ads_erp_inventory_diff_kingdee","金蝶库存差异"),
    ADS_ERP_FIRST_MILE_INTRANSIT_DIFF("ads_erp_first_mile_intransit_diff","平台在途报表"),
    
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    InventoryMonthCheckEnum(String code, String name) {
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
        if (StringUtils.isNotBlank(code)) {
            for (InventoryMonthCheckEnum item : InventoryMonthCheckEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static InventoryMonthCheckEnum getByCode(String code){
        return Arrays.stream(values()).filter(a -> a.getCode().equalsIgnoreCase(code))
                .findFirst().orElse(null);
    }

}
