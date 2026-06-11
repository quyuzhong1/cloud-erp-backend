package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 产品开发导出数据类型
 *
 * @author jack
 */
@Getter
public enum ProductDevelopExportTypeEnum implements EnumMessage {

    PRODUCT(0, "产品列表"),
    TASK(1, "任务列表");

    private static final Integer EXPORT_PRODUCT = PRODUCT.getCode();
    private static final Integer EXPORT_TASK = TASK.getCode();

    private final Integer code;

    private final String name;

    ProductDevelopExportTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static boolean isValid(Integer code) {
        return getEnum(code) != null;
    }

    public static ProductDevelopExportTypeEnum getEnum(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductDevelopExportTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 校验导出类型组合是否合法（纯判断，不抛异常、不依赖异常体系）：
     * 非空、每个 code 均为合法枚举、无重复，且仅允许单一类型或 PRODUCT+TASK 组合。
     * 业务异常由调用方（server 层）按需抛出，避免 model 层承载业务异常逻辑。
     */
    public static boolean isValidCombination(List<Integer> exportDataList) {
        if (CollectionUtils.isEmpty(exportDataList)) {
            return false;
        }
        Set<Integer> distinctTypes = new LinkedHashSet<>();
        for (Integer flag : exportDataList) {
            if (!isValid(flag)) {
                return false;
            }
            if (!distinctTypes.add(flag)) {
                return false;
            }
        }
        return distinctTypes.size() == 1
                || (distinctTypes.size() == 2
                    && distinctTypes.contains(EXPORT_PRODUCT)
                    && distinctTypes.contains(EXPORT_TASK));
    }
}
