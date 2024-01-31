package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 导出的供应商
 * @author
 * @Classname SupplierExportExcelDTO

 * @Date 2023-03-29 14:07
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierExportExcelDTO implements Serializable {

    /**
     * 编号
     */
    @ExcelProperty(value = "供应商编号", index = 0)
    @ColumnWidth(20)
    private String  code;


    /**
     * 名称
     */
    @ExcelProperty(value = "供应商名称", index = 1)
    @ColumnWidth(20)
    private String name;

    /**
     * 审核状态名
     */
    @ExcelProperty(value = "单据状态", index = 2)
    @ColumnWidth(10)
    private String approveStatusName;


    /**
     * 阶段名
     */
    @ExcelProperty(value = "供应商阶段", index = 3)
    @ColumnWidth(10)
    private String phaseName;


    /**
     * 供应商等级
     */
    @ExcelProperty(value = "供应商等级", index = 4)
    @ColumnWidth(20)
    private String gradeName;

    /**
     * 供应商分类
     */
    @ExcelProperty(value = "供应商分类", index = 5)
    @ColumnWidth(20)
    private String categoryName;


    /**
     * 启用状态
     */
    @ExcelProperty(value = "启用状态", index = 6)
    @ColumnWidth(10)
    private String enableStatus;


    /**
     * SRM协同 true 否 false 是
     */
    @ExcelProperty(value = "SRM协同", index = 7)
    @ColumnWidth(10)
    private String srmDisabled;

    /**
     * 订单接受规则
     */
    @ExcelProperty(value = "订单接受规则", index = 8)
    @ColumnWidth(10)
    private String orderAcceptRule;

    /**
     * 退货确认规则
     */
    @ExcelProperty(value = "退货确认规则", index = 9)
    @ColumnWidth(10)
    private String returnConfirmRule;

    /**
     * 结算方式
     */
    @ExcelProperty(value = "结算方式", index = 10)
    @ColumnWidth(20)
    private String payMethodName;

    /**
     * 付款条件
     */
    @ExcelProperty(value = "付款条件", index = 11)
    @ColumnWidth(20)
    private String paymentConditionName;

    /**
     * 采购员
     */
    @ExcelProperty(value = "采购员", index = 12)
    @ColumnWidth(10)
    private String purchaseUserName;


    /**
     * 联系人名
     */
    @ExcelProperty(value = "联系人", index = 13)
    @ColumnWidth(10)
    private String contactPerson="";


    /**
     * 联系人电话
     */
    @ExcelProperty(value = "联系人电话", index = 14)
    @ColumnWidth(20)
    private String contactTelNumber="";


    /**
     * 采购次数
     */
    @ExcelProperty(value = "采购次数", index = 15)
    @ColumnWidth(10)
    private Integer purchasesCount=0;

    /**
     * 退货率
     */
    @ExcelProperty(value = "退货率", index = 16)
    @ColumnWidth(10)
    private BigDecimal rejectRate;

    /**
     * 延期率
     */
    @ExcelProperty(value = "延期率", index = 17)
    @ColumnWidth(10)
    private BigDecimal delayRate;

    /**
     * 次品率
     */
    @ExcelProperty(value = "次品率", index = 18)
    @ColumnWidth(10)
    private BigDecimal defectiveRate;


    /**
     * 审核人（最新）
     */
    @ExcelProperty(value = "审核人（最新）", index = 19)
    @ColumnWidth(20)
    private String approveUserName;


    /**
     * 审核完成时间
     */
    @ExcelProperty(value = "审核完成时间", index = 20)
    @ColumnWidth(10)
    private LocalDateTime approveTime;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 21)
    @ColumnWidth(20)
    private LocalDateTime createTime;


    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人", index = 22)
    @ColumnWidth(10)
    private String createUserName;



}
