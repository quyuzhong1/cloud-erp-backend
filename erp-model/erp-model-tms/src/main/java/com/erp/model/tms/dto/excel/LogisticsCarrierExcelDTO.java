package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 公司信息导入DTO
 * @author zdy
 * @date: 2024/5/8 14:19
 */
@Data
public class LogisticsCarrierExcelDTO implements Serializable {


    /**
     * 物流商编码
     */
    @ExcelProperty(value = "物流商编码", index = 0)
    @FieldValid(fieldName = "物流商编码", isNotBlank = true ,maxLength = 64)
    private String carrierCode;

    /**
     * 物流商英文名称
     */
    @ExcelProperty(value = "物流商英文名称", index = 1)
    @FieldValid(fieldName = "物流商英文名称", isNotBlank = true ,maxLength = 64)
    private String carrierEn;

    /**
     * 物流商中文名称
     */
    @ExcelProperty(value = "物流商中文名称", index = 2)
    @FieldValid(fieldName = "物流商中文名称",isNotBlank = true ,maxLength = 64)
    private String carrierCn;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 3)
    private String  errorMsg;

}
