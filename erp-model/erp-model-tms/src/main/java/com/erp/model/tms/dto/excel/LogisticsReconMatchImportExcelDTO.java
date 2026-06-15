package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 物流商对账"导入匹配"模板。
 * 字段与手动匹配一致：以对账侧物流跟踪号定位对账明细，填入 ERP 业务单号执行匹配。
 * </p>
 *
 * @author Will
 * @since 2026-06-11
 */
@Data
public class LogisticsReconMatchImportExcelDTO implements Serializable {

    /**
     * 物流跟踪号（对账侧，定位对账明细 logistics_recon_detail.track_no）
     */
    @ExcelProperty("物流跟踪号")
    private String trackNo;

    /**
     * ERP 销售单号
     */
    @ExcelProperty("ERP销售单号")
    private String erpSoCode;

    /**
     * ERP 平台订单号
     */
    @ExcelProperty("ERP平台订单号")
    private String erpPlatformOrderNo;

    /**
     * ERP 物流跟踪号
     */
    @ExcelProperty("ERP物流跟踪号")
    private String erpTrackNo;

    /**
     * ERP 发货单号
     */
    @ExcelProperty("ERP发货单号")
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
