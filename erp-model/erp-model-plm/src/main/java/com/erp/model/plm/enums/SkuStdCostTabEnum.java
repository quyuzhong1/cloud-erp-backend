package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * sku标准成本tab
 */
@Getter
@AllArgsConstructor
public enum SkuStdCostTabEnum implements EnumMessage {
    // 全部=业务要求最新
    ALL("all", "全部"),
    TO_BE_APPROVE("toBeApprove", "待我审核"),
    APPROVE("approve", "已审核"),
    REJECT("reject", "不通过"),
    HISTORY("history", "历史价格"),
    ;

    private final String code;
    private final String name;

    public static List<String> getCodeList() {
        return Arrays.stream(SkuStdCostTabEnum.values())
                .map(SkuStdCostTabEnum::getCode)
                .collect(Collectors.toList());
    }

    public static String getNameByCode(String code) {
        SkuStdCostTabEnum[] enums = values();
        for (SkuStdCostTabEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getCode().equals(code)) {
                return productTaskCategoryEnum.getName();
            }
        }
        return null;
    }

    public static SkuStdCostTabEnum getEnumByType(String code) {
        SkuStdCostTabEnum[] enums = values();
        for (SkuStdCostTabEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getCode().equals(code)) {
                return productTaskCategoryEnum;
            }
        }
        return null;
    }

    public static String getCodeByName(String name) {
        SkuStdCostTabEnum[] enums = values();
        for (SkuStdCostTabEnum productTaskCategoryEnum : enums) {
            if (productTaskCategoryEnum.getName().equals(name)) {
                return productTaskCategoryEnum.getCode();
            }
        }
        return null;
    }
}
