package com.sdk.oms.dht.enums;


import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import io.seata.common.util.StringUtils;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DhtEnums {
    ;

    private final String fieldName;
    private final Class<?> enumClz;

    DhtEnums(String fieldName, Class<?> enumClz) {
        this.fieldName = fieldName;
        this.enumClz = enumClz;
    }
    /**
     * 客户状态
     */
    @Getter
    public enum CustomerStatusEnum {
        PENDING("1","报备中"),
        NOT_ASSIGNED("2","未分配"),
        ASSIGNED("3","已分配"),
        NOT_IN_EFFECT("4","未生效"),
        CHANGING("5","变更中"),
        DEPOSED("99","已作废"),
        ;
        private final String code;
        private final String name;

        CustomerStatusEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
}
