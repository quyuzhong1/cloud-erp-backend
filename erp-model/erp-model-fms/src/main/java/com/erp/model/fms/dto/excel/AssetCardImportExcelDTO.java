package com.erp.model.fms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDate;

/**
 * 资产卡片导入Excel DTO
 * 
 * @author wuht
 * @date 2025-01-10
 */
@Data
public class AssetCardImportExcelDTO {

    /**
     * 序号
     */
    @ExcelProperty(value = "*序号", index = 0)
    private Integer no;

    /**
     * 资产组织
     */
    @ExcelProperty(value = "*资产组织", index = 1)
    private String orgName;

    /**
     * 计量单位
     */
    @ExcelProperty(value = "*计量单位", index = 2)
    private String unit;

    /**
     * 资产类别
     */
    @ExcelProperty(value = "*资产类别", index = 3)
    private String type;

    /**
     * 资产状态
     */
    @ExcelProperty(value = "*资产状态", index = 4)
    private String status;

    /**
     * 变动方式
     */
    @ExcelProperty(value = "*变动方式", index = 5)
    private String changeMethod;

    /**
     * 资产名称
     */
    @ExcelProperty(value = "*资产名称", index = 6)
    private String name;

    /**
     * 开始使用日期
     */
    @ExcelProperty(value = "*开始使用日期", index = 7)
    private LocalDate startUseDate;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 8)
    private String remark;

    /**
     * 资产位置
     */
    @ExcelProperty(value = "*资产位置", index = 9)
    private String assetLocationName;

    /**
     * 数量
     */
    @ExcelProperty(value = "*数量", index = 10)
    private Integer qty;

    /**
     * 使用部门
     */
    @ExcelProperty(value = "*使用部门", index = 11)
    private String useDeptName;

    /**
     * 费用项目
     */
    @ExcelProperty(value = "*费用项目", index = 12)
    private String costType;

    /**
     * 明细备注
     */
    @ExcelProperty(value = "备注", index = 13)
    private String detailRemark;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =14)
    @ColumnWidth(50)
    private String  errorMsg = "";
}
