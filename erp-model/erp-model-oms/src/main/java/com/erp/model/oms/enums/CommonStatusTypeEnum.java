package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.erp.model.wms.enums.BillTypeEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lambda
 * @Classname CommenTypeStatusEnum
 * @Description TODO
 * @Date 2023-08-28 14:38
 * @Created by yl
 */
public enum CommonStatusTypeEnum implements EnumMessage {
    PROCESSING("processing","处理中","refundOrder"),
    CANCEL("cancel","已取消","refundOrder"),
    FINISH("finish","已退款","refundOrder"),



    ;

    CommonStatusTypeEnum(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    /**
     * 标识
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public static List<CommonStatusTypeEnum> listByType(String type) {
        List<CommonStatusTypeEnum> list = new ArrayList<>();
        for (CommonStatusTypeEnum item: CommonStatusTypeEnum.values()) {
            if (type.equals(item.getType())) {
                list.add(item);
            }
        }
        return list;
    }

    public static String getName(String code) {
        for (CommonStatusTypeEnum statusTypeEnum : CommonStatusTypeEnum.values()) {
            if (code.equals(statusTypeEnum.getCode())) {
                return statusTypeEnum.getName();
            }
        }
        return "";
    }
}
