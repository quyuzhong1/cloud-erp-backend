package com.erp.server.dmp.push.service.wdt.dto;

import lombok.Data;

/**
 * 旺店通其他出入库告警字段解析结果。
 */
@Data
public class ResolvedWarnFields {

    private String tableId;
    /** 批次号 outerNo，告警文案「数大臣单号」 */
    private String outerNo;
    /** 旺店通仓库编码 warehouseNo，告警文案「数大臣仓库编码」 */
    private String wdtWarehouseNo;
    private String docTypeName;
    private String bizName;
}
