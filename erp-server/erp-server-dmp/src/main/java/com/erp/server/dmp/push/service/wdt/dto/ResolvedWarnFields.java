package com.erp.server.dmp.push.service.wdt.dto;

import lombok.Data;

/**
 * 旺店通其他出入库告警字段解析结果。
 */
@Data
public class ResolvedWarnFields {

    private String tableId;
    /** ERP 其他出入库单号，告警文案「数大臣单号」 */
    private String erpSourceCode;
    /** 旺店通推送单号 outerNo，告警文案「旺店通单号」 */
    private String outerNo;
    /** ERP 仓库名称，告警文案「数大臣仓库」 */
    private String erpWarehouseName;
    /** 中台配置绑定的旺店通仓库编码 */
    private String wdtWarehouseNo;
    private String docTypeName;
    private String bizName;
}
