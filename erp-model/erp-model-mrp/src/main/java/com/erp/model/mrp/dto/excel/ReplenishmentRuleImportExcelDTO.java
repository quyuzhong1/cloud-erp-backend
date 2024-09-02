package com.erp.model.mrp.dto.excel;

import lombok.Data;

import java.io.Serializable;

/**
 * 其他出库单导入
 *
 * @author Jim
 * {@code @date:} 2024/03/22
 */
@Data
public class ReplenishmentRuleImportExcelDTO implements Serializable {

    /**
     * 错误数据
     */
    private String errorMsg;
}
