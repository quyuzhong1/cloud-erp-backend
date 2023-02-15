package com.erp.model.plm.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname BomExportExcelVO
 * @Description TODO
 * @Date 2023-01-29 17:49
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BomExportExcelVO  implements Serializable {



    /**
     * bom 编号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "BOM编号", index = 0)
    private String serialNumber;

    /**
     * 版本
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "版本号", index = 1)
    private Integer version;

    /**
     * sku 编号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "SKU", index = 2)
    private String skuNo;

    /**
     * sku 名称
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "SKU名称", index = 3)
    private String skuName = "";

    /**
     * 数量
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "用量", index = 4)
    private Integer quantity;



    /**
     * spu 名称
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "SPU(型号/model)", index = 5)
    private String spuNo = "";


    /**
     * 类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "BOM类型", index = 6)
    private String typeName;

    /**
     * 状态名
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "状态", index = 7)
    private String stateName;


    /**
     * 备注
     */
    @ColumnWidth(60)
    @ExcelProperty(value = "备注", index = 8)
    private String remark;



    /**
     * 创建人 名
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "创建人", index = 9)
    private String createUserName = "";

    /**
     * 创建时间
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "创建时间", index = 10)
    private Date createTime;

    /**
     * 更新时间
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "更新时间", index = 11)
    private Date updateTime;



}
