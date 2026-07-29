package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * B2B三方发货单附件类型（attachment.type，与 ModuleTypeEnum 操作日志模块码无关）
 */
@Getter
public enum B2bThirdDeliveryAttachmentTypeEnum implements EnumMessage {

    ORDER_ATTACHMENT("b2b_third_delivery_order_attachment", "订单附件"),
    PRODUCT_LABEL("b2b_third_delivery_product_label", "产品标签"),
    OUTER_BOX_LABEL("b2b_third_delivery_outer_box_label", "外箱面单"),
    ;

    private final String code;
    private final String name;

    B2bThirdDeliveryAttachmentTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (B2bThirdDeliveryAttachmentTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "";
    }
}
