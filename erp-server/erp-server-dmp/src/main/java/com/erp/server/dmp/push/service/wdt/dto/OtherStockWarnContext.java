package com.erp.server.dmp.push.service.wdt.dto;

import com.common.message.enums.ApiModuleTypeEnum;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import lombok.Builder;
import lombok.Data;

/**
 * 旺店通其他出入库推送失败告警上下文。
 */
@Data
@Builder
public class OtherStockWarnContext {

    /** 兜底模块类型 */
    private ApiModuleTypeEnum apiModuleType;
    /** 告警关联推送任务 ID */
    private String dmpSyncTaskId;
    /** 来源单据 ID，tableId 兜底 */
    private String sourceId;
    /** 批次号 outerNo，作为数大臣单号 */
    private String outerNo;
    /** 旺店通仓库编码 warehouseNo，作为数大臣仓库编码 */
    private String wdtWarehouseNo;

    public static OtherStockWarnContext fromInStockRequest(CreateOtherStockinRequest request) {
        return OtherStockWarnContext.builder()
                .apiModuleType(ApiModuleTypeEnum.WDT_OTHER_IN_STOCK)
                .outerNo(request.getOuterNo())
                .wdtWarehouseNo(request.getWarehouseNo())
                .dmpSyncTaskId(request.getDmpSyncTaskId())
                .sourceId(request.getSourceId())
                .build();
    }

    public static OtherStockWarnContext fromOutStockRequest(CreateOtherStockoutRequest request) {
        return OtherStockWarnContext.builder()
                .apiModuleType(ApiModuleTypeEnum.WDT_OTHER_OUT_STOCK)
                .outerNo(request.getOuterNo())
                .wdtWarehouseNo(request.getWarehouseNo())
                .dmpSyncTaskId(request.getDmpSyncTaskId())
                .sourceId(request.getSourceId())
                .build();
    }
}
