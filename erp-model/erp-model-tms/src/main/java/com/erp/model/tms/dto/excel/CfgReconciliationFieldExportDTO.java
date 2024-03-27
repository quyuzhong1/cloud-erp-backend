package com.erp.model.tms.dto.excel;

import lombok.Data;

import java.io.Serializable;

/**
 * 对账字段导出
 *
 * @author Jim
 * {@code @date:} 2024-03-25
 */
@Data
public class CfgReconciliationFieldExportDTO implements Serializable {

    /**
     * 核对类型名称
     */
    private String reconciliationType;

    /**
     * 第三方名称
     */
    private String thirdName;

    /**
     * 第三方字段名称
     */
    private String thirdFieldName;

    /**
     * ERP字段名称
     */
    private String erpFieldName;

    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private String createTime;

    private String sourceType;

    private String sourceId;
}
