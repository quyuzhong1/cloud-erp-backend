package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品违禁词库导出
 */
@Data
public class CfgProductForbiddenWordExportExcelDTO implements Serializable {

    @ExcelProperty("违禁词")
    private String forbiddenWord;

    @ExcelProperty("状态")
    private String disabledName;

    @ExcelProperty("创建人")
    private String createUserName;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
