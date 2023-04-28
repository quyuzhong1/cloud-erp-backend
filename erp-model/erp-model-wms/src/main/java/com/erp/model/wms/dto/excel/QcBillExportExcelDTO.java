package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.business.service.LocalDateStringConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Lambda
 * @Classname QcBillExportExcelDTO
 * @Description TODO
 * @Date 2023-04-19 18:55
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcBillExportExcelDTO  implements Serializable {




    /**
     * code
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检单号", index = 0)
    private String code;


    /**
     * 采购订单code
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "采购单号", index = 1)
    private String purchaseOrderCode;

    /**
     * 质检状态名
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检状态", index = 2)
    private String qcStatusName;

    /**
     * 质检员
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检员", index = 3)
    private String qcUserName;


    /**
     * 质检类型名
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "质检类型", index = 4)
    private String qcTypeName;



    /**
     * 内检 类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "内外检类型", index = 5)
    private String insideType;


    /**
     *供应商名
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "供应商名称", index = 6)
    private String supplierName;



    /**
     * sku
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "SKU", index = 7)
    private String skuNo;



    /**
     * sku id
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "SKU名称", index = 8)
    private String skuName;

    /**
     * 总量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "总数量", index = 9)
    private Integer totalQty;


    /**
     * 质检量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "质检量", index = 10)
    private Integer qcQty;


    /**
     * 质检合格量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "质检合格量", index = 11)
    private Integer qcGoodQty;

    /**
     * 质检不良量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "质检不良量", index = 12)
    private Integer qcBadQty;



    /**
     * 质检结果名
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "检验结果", index = 13)
    private String qcResultName;

    /**
     * 处理措施
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "处理措施", index = 14)
    private String handleModeName;



    /**
     * 不良现象
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "不良现象", index = 15)
    private String badDescription;


    /**
     * 质检合格率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "质检合格率(%)", index = 16)
    private String qcGoodRate;

    /**
     * 质检不良率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "质检不良率(%)", index = 17)
    private String qcBadRate;


    /**
     * 仓库
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "仓库", index = 18)
    private String warehouseName;

    /**
     * remark
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "备注", index = 19)
    private String remark;


    /**
     * 创建人
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "创建人", index = 20)
    private String createUserName;


    /**
     * 创建时间
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "创建时间", index = 21,converter= LocalDateStringConverter.class)
    private LocalDateTime createTime;

}
