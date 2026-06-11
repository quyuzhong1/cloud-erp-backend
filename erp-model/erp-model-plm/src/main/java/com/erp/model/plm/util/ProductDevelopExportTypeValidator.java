package com.erp.model.plm.util;

import com.common.core.exception.ServiceException;
import com.erp.model.plm.enums.ProductDevelopExportTypeEnum;
import org.apache.commons.collections4.CollectionUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 产品开发导出类型校验，供 PLM 创建任务与 file 侧 Handler 共用，保证规则单一数据源。
 */
public final class ProductDevelopExportTypeValidator {

    private static final Integer EXPORT_PRODUCT = ProductDevelopExportTypeEnum.PRODUCT.getCode();
    private static final Integer EXPORT_TASK = ProductDevelopExportTypeEnum.TASK.getCode();

    private ProductDevelopExportTypeValidator() {
    }

    /**
     * 校验枚举合法性、去重，仅允许单一类型或 PRODUCT+TASK 组合。
     */
    public static List<Integer> validate(List<Integer> exportDataList) {
        if (CollectionUtils.isEmpty(exportDataList)) {
            throw new ServiceException("导出数据类型不能为空");
        }
        Set<Integer> distinctTypes = new LinkedHashSet<>();
        for (Integer flag : exportDataList) {
            if (!ProductDevelopExportTypeEnum.isValid(flag)) {
                throw new ServiceException("导出数据类型不合法：" + flag);
            }
            if (!distinctTypes.add(flag)) {
                throw new ServiceException("导出数据类型存在重复：" + flag);
            }
        }
        boolean validCombination = distinctTypes.size() == 1
                || (distinctTypes.size() == 2
                    && distinctTypes.contains(EXPORT_PRODUCT)
                    && distinctTypes.contains(EXPORT_TASK));
        if (!validCombination) {
            throw new ServiceException("导出数据类型不合法：" + exportDataList);
        }
        return exportDataList;
    }
}
