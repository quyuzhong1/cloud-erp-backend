package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Will
 * @version 1.0
 * @description: 入库类型
 * @date 2023/5/15 18:12
 */
public enum InstockTypeEnum implements EnumMessage {


    SAMPLE_RETURN("sampleReturn", "样品归还"),
    THREE_NO_PRODUCT("threeNoProduct", "三无产品"),
    THREE_NO_PRODUCT_PRE_INSTOCK("threeNoProductPreInstock", "三无退货预入库"),
    REPORT_OVERFLOW("reportOverflow", "库存差异调整"),
    GIFT_INSTOCK("giftInstock", "赠品入库"),
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

    InstockTypeEnum(String code, String name) {
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

    public static String getByCode(String code) {

        InstockTypeEnum[] enumList = InstockTypeEnum.values();
        for (InstockTypeEnum item : enumList) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "";
    }
}
