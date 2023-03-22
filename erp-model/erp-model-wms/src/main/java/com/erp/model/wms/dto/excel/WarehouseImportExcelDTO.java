package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Lambda
 * @Classname WarehouseImportExcelDTO
 * @Description TODO
 * @Date 2023-03-22 15:49
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WarehouseImportExcelDTO implements Serializable {


    /**
     * 金蝶仓库编号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "金蝶仓库编号", index = 0)
    private String kingdeeWarehouseCode;

    /**
     * 名称
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "仓库名称", index = 1)
    private String name;


    /**
     * 类型名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "仓库类型", index = 2)
    private String typeName;


    /**
     * 组织名称
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "仓库组织", index = 3)
    private String orgName;


    /**
     * 是否虚拟仓
     * true 是
     */
    @ColumnWidth(5)
    @ExcelProperty(value = "仓库组织", index = 4)
    private String isVirtual;

    /**
     * 负责人名
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "仓库负责人", index = 5)
    private String chargeName;


    /**
     * 联系人
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "联系人", index = 6)
    private String contacts;

    /**
     * 联系人电话
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "联系人电话", index = 7)
    private String contactTelNumber;

    /**
     * 地址
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "仓库地址", index = 8)
    private String address;

    /**
     * 状态
     */
    @ColumnWidth(5)
    @ExcelProperty(value = "启用状态", index = 9)
    private String enabled;

    @ColumnWidth(10)
    @ExcelProperty(value = "数据状态", index = 10)
    private String approveStatusName;

    @ColumnWidth(10)
    @ExcelProperty(value = "创建人", index = 11)
    private String createUserName;

    /**
     * 创建时间
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "创建时间", index = 12)
    private LocalDateTime createTime;




}
