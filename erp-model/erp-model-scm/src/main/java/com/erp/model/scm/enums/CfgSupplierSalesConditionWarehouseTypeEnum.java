package com.erp.model.scm.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 销量设置条件明细 仓库类型 枚举
 * </p>
 *
 * @author jack
 * @since 2025-06-13 10:54:36
 */
public enum CfgSupplierSalesConditionWarehouseTypeEnum implements EnumMessage {
	VIRTUALWAREHOUSE("virtualWarehouse", "虚拟仓"),
	PHYSICALWAREHOUSE("physicalWarehouse", "实体仓"),
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

    CfgSupplierSalesConditionWarehouseTypeEnum(String code, String name) {
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
        for (CfgSupplierSalesConditionWarehouseTypeEnum statusEnum : CfgSupplierSalesConditionWarehouseTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
