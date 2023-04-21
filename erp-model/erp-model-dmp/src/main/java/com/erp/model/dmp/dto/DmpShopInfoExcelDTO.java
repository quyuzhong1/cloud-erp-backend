package com.erp.model.dmp.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.business.service.LocalDateStringConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
     * 店铺负责人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "店铺负责人", index = 3)
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
     * 创建人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "创建人", index = 6)
    private String createUserName;

    /**
     * 创建时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "创建时间", index = 7,converter= LocalDateStringConverter.class)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "更新人", index = 8)
    private String updateUserName;

    /**
     * 更新时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "更新时间", index = 9,converter= LocalDateStringConverter.class)
    private LocalDateTime updateTime;

}
