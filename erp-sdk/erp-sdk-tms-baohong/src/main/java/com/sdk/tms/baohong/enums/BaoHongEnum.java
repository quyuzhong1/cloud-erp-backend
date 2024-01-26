package com.sdk.tms.baohong.enums;

import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.core.constant.EnumMessage;
import com.erp.model.tms.enums.ProductRegistrationStatusEnum;
import lombok.Getter;

@Getter
public enum BaoHongEnum {
    ;
    /**
     * 产品状态枚举
     */
    @Getter
    public enum ProductStatusEnum implements EnumMessage {

        DRAFT("0","草稿",ProductRegistrationStatusEnum.DRAFT),
        REGISTERING("1","备案中",ProductRegistrationStatusEnum.REGISTERING),
        REGISTERED("2","已备案",ProductRegistrationStatusEnum.REGISTERED),
        FREEZE("3","冻结",ProductRegistrationStatusEnum.FREEZE),
        ;
        private final String code;
        private final String name;
        private final ProductRegistrationStatusEnum productRegistrationStatusEnum;
        ProductStatusEnum(String code, String name,ProductRegistrationStatusEnum productRegistrationStatusEnum) {
            this.code = code;
            this.name = name;
            this.productRegistrationStatusEnum = productRegistrationStatusEnum;
        }
    }
}
