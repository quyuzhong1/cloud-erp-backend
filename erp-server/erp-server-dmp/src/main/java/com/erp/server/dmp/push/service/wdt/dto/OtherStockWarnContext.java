package com.erp.server.dmp.push.service.wdt.dto;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.sdk.wangdian.sdk.api.wms.WdtOtherStockRemarkConstants;
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
    /** 旺店通推送单号 outerNo（对应旺店通 outer_no，改前告警称批次号） */
    private String outerNo;
    /** ERP 其他出入库单号（数大臣单号） */
    private String sourceCode;
    /** ERP 仓库 ID，用于从中台配置查找绑定的旺店通仓库 */
    private String sysWarehouseId;
    /** ERP 仓库名称，告警展示用，优先于远程查询 */
    private String erpWarehouseName;
    /** 请求中的旺店通仓库编码 warehouseNo，仅作兜底 */
    private String wdtWarehouseNo;

    public static OtherStockWarnContext fromInStockRequest(CreateOtherStockinRequest request) {
        return OtherStockWarnContext.builder()
                .apiModuleType(ApiModuleTypeEnum.WDT_OTHER_IN_STOCK)
                .outerNo(request.getOuterNo())
                .sourceCode(resolveSourceCode(request.getSourceCode(), request.getRemark()))
                .sysWarehouseId(request.getSysWarehouseId())
                .erpWarehouseName(request.getSysWarehouseName())
                .wdtWarehouseNo(request.getWarehouseNo())
                .dmpSyncTaskId(request.getDmpSyncTaskId())
                .sourceId(request.getSourceId())
                .build();
    }

    public static OtherStockWarnContext fromOutStockRequest(CreateOtherStockoutRequest request) {
        return OtherStockWarnContext.builder()
                .apiModuleType(ApiModuleTypeEnum.WDT_OTHER_OUT_STOCK)
                .outerNo(request.getOuterNo())
                .sourceCode(resolveSourceCode(request.getSourceCode(), request.getRemark()))
                .sysWarehouseId(request.getSysWarehouseId())
                .erpWarehouseName(request.getSysWarehouseName())
                .wdtWarehouseNo(request.getWarehouseNo())
                .dmpSyncTaskId(request.getDmpSyncTaskId())
                .sourceId(request.getSourceId())
                .build();
    }

    static String resolveSourceCode(String sourceCode, String remark) {
        if (CharSequenceUtil.isNotBlank(sourceCode)) {
            return sourceCode;
        }
        if (CharSequenceUtil.isNotBlank(remark) && remark.startsWith(WdtOtherStockRemarkConstants.SOURCE_CODE_PREFIX)) {
            return remark.substring(WdtOtherStockRemarkConstants.SOURCE_CODE_PREFIX.length()).trim();
        }
        return CharSequenceUtil.nullToEmpty(sourceCode);
    }
}
