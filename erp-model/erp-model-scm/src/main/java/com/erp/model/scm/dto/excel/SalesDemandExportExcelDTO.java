package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 备货申请单导出DTO
 * @date 2023/3/20 11:07
 */
@Data
public class SalesDemandExportExcelDTO implements Serializable {

    /**
     * 备货编号
     */
    @ExcelProperty(value = "备货编号", index = 0)
    private String  code;

    /**
     * 备货店铺
     */
    @ExcelProperty(value = "备货店铺", index = 1)
    private String  shopName;

    /**
     * SKU
     */
    @ExcelProperty(value = "SKU", index = 2)
    private String  skuNo;

    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称", index = 3)
    private String  productName;


    /**
     * 计划备货数量
     */
    @ExcelProperty(value = "计划备货数量", index = 4)
    private String  planStockQty;

    /**
     * 计划交期
     */
    @ExcelProperty(value = "计划交期", index = 5)
    private String  planDeliveryDate;

    /**
     * 目的仓库名称
     */
    @ExcelProperty(value = "目的仓库名称", index = 6)
    private String destWarehouseName;

    /**
     * 新品首批
     */
    @ExcelProperty(value = "新品首批", index = 7)
    private String isFirstMassProduct;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 8)
    private String remark;


    /**
     * 备货原因
     */
    @ExcelProperty(value = "备货原因", index = 9)
    private String stockReason;

    /**
     * 审核人
     */
    @ExcelProperty(value = "审核人", index = 10)
    private String approveUserName;

    /**
     * 单据状态
     */
    @ExcelProperty(value = "单据状态", index = 11)
    private String  approveStatusName;

    /**
     * 作废状态
     */
    @ExcelProperty(value = "作废状态", index = 12)
    private String  invalidStatusName;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人", index = 13)
    private String  createUserName;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 14)
    private String  createTime;
}
