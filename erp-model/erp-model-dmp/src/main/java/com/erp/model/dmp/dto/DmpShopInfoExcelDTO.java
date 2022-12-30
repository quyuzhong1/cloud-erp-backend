package com.erp.model.dmp.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 17:15
 */
@Data
@NoArgsConstructor
public class DmpShopInfoExcelDTO {

    /**
     * 店铺名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "店铺名称", index = 0)
    private String name;

    /**
     * 平台名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "平台名称", index = 1)
    private String platformName;

    /**
     * 店铺站点
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "店铺站点", index = 2)
    private String site;

    /**
     * 负责人名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "负责人名称", index = 3)
    private String chargeName;

    /**
     * 店铺状态
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "店铺状态", index = 4)
    private String statusName;

    /**
     * 店铺标识
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "店铺标识", index = 5)
    private String storeSign;

    /**
     * 创建时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "创建时间", index = 6)
    private String createTime;

    /**
     * 更新时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "更新时间", index = 7)
    private String updateTime;

    /**
     * 创建人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "创建人", index = 8)
    private String createUserName;

    /**
     * 更新人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "更新人", index = 9)
    private String updateUserName;

}
