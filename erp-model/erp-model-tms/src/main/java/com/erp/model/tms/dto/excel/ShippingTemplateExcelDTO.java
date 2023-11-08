package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 运费模板导入DTO
 * @date 2023/11/8 16:11
 */
@Data
public class ShippingTemplateExcelDTO implements Serializable {


    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 60)
    private String errorMsg;

}
