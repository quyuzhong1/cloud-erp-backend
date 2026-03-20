package com.erp.model.wms.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @Author: wtr
 * @Date: 2026/3/12 20:10
 * @Param:
 * @Return:
 * @Description:
 **/
@Getter
public enum  ZhongBaoB2BDeliveryStatusEnum {

    EXCEPTION(-2,"异常"),
    CANCEL(-1,"已取消"),
    DRAFT(1,"草稿"),
    APPROVING(2,"待审核"),
    APPROVE(3,"已审核"),
    WAIT_OUTSTOCK(4,"待出库"),
    OUTSTOCK(5,"已出库"),

    CREATE_FAIR(999,"创建失败"),
    ;

    /**
     * 类型
     */
    private final Integer code;
    /**
     * 名称
     */
    private final String name;

    ZhongBaoB2BDeliveryStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }


    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (ZhongBaoB2BDeliveryStatusEnum item : ZhongBaoB2BDeliveryStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static ZhongBaoB2BDeliveryStatusEnum getByCode(String code) {
        return Arrays.stream(ZhongBaoB2BDeliveryStatusEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
