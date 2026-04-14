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

    EXCEPTION(-2,"异常", ThirdDeliveryStatusEnum.EXCEPTION_ORDER),
    CANCEL(-1,"已取消", ThirdDeliveryStatusEnum.CANCEL_DELIVERY),
    DRAFT(1,"草稿", ThirdDeliveryStatusEnum.WAIT_SHIPPED),
    APPROVING(2,"待审核", ThirdDeliveryStatusEnum.WAIT_SHIPPED),
    APPROVE(3,"已审核", ThirdDeliveryStatusEnum.WAIT_SHIPPED),
    WAIT_OUTSTOCK(4,"待出库", ThirdDeliveryStatusEnum.WAIT_SHIPPED),
    OUTSTOCK(5,"已出库", ThirdDeliveryStatusEnum.SHIPPED),

    CREATE_FAIR(999,"创建失败", ThirdDeliveryStatusEnum.FAILED),
    ;

    /**
     * 类型
     */
    private final Integer code;
    /**
     * 名称
     */
    private final String name;
    /**
     * erp状态
     */
    private final ThirdDeliveryStatusEnum erpStatus;

    ZhongBaoB2BDeliveryStatusEnum(Integer code, String name, ThirdDeliveryStatusEnum erpStatus) {
        this.code = code;
        this.name = name;
        this.erpStatus = erpStatus;
    }


    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (ZhongBaoB2BDeliveryStatusEnum item : ZhongBaoB2BDeliveryStatusEnum.values()) {
                if (code.equals(String.valueOf(item.getCode()))) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static ZhongBaoB2BDeliveryStatusEnum getByCode(String code) {
        return Arrays.stream(ZhongBaoB2BDeliveryStatusEnum.values())
                .filter(e -> String.valueOf(e.getCode()).equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public static String getErpOrderStatus(String code) {
        return Arrays.stream(ZhongBaoB2BDeliveryStatusEnum.values())
                .filter(item -> String.valueOf(item.getCode()).equalsIgnoreCase(code))
                .findFirst()
                .map(ZhongBaoB2BDeliveryStatusEnum::getErpStatus)
                .map(ThirdDeliveryStatusEnum::getCode)
                .orElse("");
    }
}
