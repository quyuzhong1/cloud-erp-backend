package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 物流商对账「导入匹配」模板（固定列，与 logisticsReconDetailTemplate.xlsx 一致）。
 * <p>识别列固定包含销售单号、平台订单号、物流跟踪号、发货单号（均可配置为识别单号）；
 * 实际匹配时仅按导入模板配置的唯一识别字段取值分组，未配置为识别单的列不参与识别。
 * ERP 四单号与手动匹配一致。</p>
 *
 * @author Will
 * @since 2026-06-11
 */
@Data
public class LogisticsReconMatchImportExcelDTO implements Serializable {

  /**
     * 第三方-销售单号（识别列，对应 sourceCode）
     */
    @ExcelProperty("第三方-销售单号")
    private String soCode;

    /**
     * 第三方-平台订单号（识别列，对应 platformCode）
     */
    @ExcelProperty("第三方-平台订单号")
    private String platformOrderNo;

    /**
     * 第三方-物流运单号（识别列，对应 transportNo）
     */
    @ExcelProperty("第三方-物流运单号")
    private String transportNo;

    /**
     * 第三方-物流跟踪号（识别列，对应 trackNo）
     */
    @ExcelProperty("第三方-物流跟踪号")
    private String trackNo;

    /**
     * 第三方-发货单号（识别列，对应 soDeliveryCode）
     */
    @ExcelProperty("第三方-发货单号")
    private String soDeliveryCode;

    /**
     * ERP-销售单号
     */
    @ExcelProperty("ERP-销售单号")
    private String erpSoCode;

    /**
     * ERP-平台订单号
     */
    @ExcelProperty("ERP-平台订单号")
    private String erpPlatformOrderNo;

    /**
     * ERP-物流跟踪号
     */
    @ExcelProperty("ERP-物流跟踪号")
    private String erpTrackNo;

    /**
     * ERP-发货单号
     */
    @ExcelProperty("ERP-发货单号")
    private String erpSoDeliveryCode;

    /**
     * 匹配结果（导出回写）
     */
    @ExcelProperty("匹配结果")
    private String matchResult;

    /**
     * 失败原因（导出回写）
     */
    @ExcelProperty("失败原因")
    private String errorMsg;
}
