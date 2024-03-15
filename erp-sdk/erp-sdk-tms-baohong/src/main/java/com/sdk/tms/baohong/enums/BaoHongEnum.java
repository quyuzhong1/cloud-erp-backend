package com.sdk.tms.baohong.enums;

import com.common.business.enums.BatteryTypeEnum;
import com.common.core.constant.EnumMessage;
import com.erp.model.tms.enums.ProductRegistrationEnum;
import io.seata.common.util.StringUtils;
import jnr.ffi.annotations.In;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BaoHongEnum {
    ;
    /**
     * 产品状态枚举
     */
    @Getter
    public enum ProductStatusEnum implements EnumMessage {

        DRAFT("2","草稿", ProductRegistrationEnum.StatusEnum.DRAFT),
        REGISTERING("0","备案中", ProductRegistrationEnum.StatusEnum.REGISTERING),
        REGISTERED("1","已备案", ProductRegistrationEnum.StatusEnum.REGISTERED),
        FREEZE("3","冻结", ProductRegistrationEnum.StatusEnum.FREEZE),
        ;
        private final String code;
        private final String name;
        private final ProductRegistrationEnum.StatusEnum productRegistrationStatusEnum;
        ProductStatusEnum(String code, String name, ProductRegistrationEnum.StatusEnum productRegistrationStatusEnum) {
            this.code = code;
            this.name = name;
            this.productRegistrationStatusEnum = productRegistrationStatusEnum;
        }
    }

    /**
     * 电池类型
     */
    @Getter
    public enum BatteryEnum implements EnumMessage {

        BUILT_IN_BATTERY(2,"内置电池", BatteryTypeEnum.BUILT_AND_NON_REMOVABLE),
        BUILT_IN_BATTERY2(2,"内置电池", BatteryTypeEnum.BUILT_AND_REMOVABLE),
        PURE_BATTERY(7,"纯电池", BatteryTypeEnum.PURE_ELECTRICITY),
        ;
        private final Integer code;
        private final String name;
        private final BatteryTypeEnum erpEnum;
        BatteryEnum(Integer code, String name, BatteryTypeEnum erpEnum) {
            this.code = code;
            this.name = name;
            this.erpEnum = erpEnum;
        }


        public static Integer getCodeByErp(String erpName){
            if(StringUtils.isBlank(erpName)){
                return null;
            }
            return Arrays.stream(BatteryEnum.values())
                    .filter(item -> erpName.equals(item.getErpEnum().getName()))
                    .findFirst()
                    .map(BatteryEnum::getCode)
                    .orElse(null);
        }
    }
}
