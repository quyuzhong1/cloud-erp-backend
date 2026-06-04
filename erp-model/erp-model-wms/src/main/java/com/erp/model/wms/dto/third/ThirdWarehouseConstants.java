package com.erp.model.wms.dto.third;

/**
 * 第三方仓通用常量。
 */
public final class ThirdWarehouseConstants {

    public static final String MODULE_ORDER_LABEL = "order_label";
    public static final String MODULE_OTHER_DOCUMENTS_INVOICE = "other_documents_invoice";
    /** 发票 PDF base64 长度限制统一放在第三方仓常量，OMS/WMS 共用同一阈值。 */
    public static final int MAX_INVOICE_PDF_BASE64_LENGTH = 20 * 1024 * 1024;

    private ThirdWarehouseConstants() {
    }
}
