package com.erp.server.tms.constant;

/**
 * 物流商对账「导入匹配」分批参数（与主表匹配 {@code MATCH_ID_BATCH_SIZE}/{@code MATCH_CHUNK_SIZE} 对齐）。
 *
 * @author Will
 * @date 2026/6/12
 */
public final class LogisticsReconImportMatchConstant {

    private LogisticsReconImportMatchConstant() {
    }

    /**
     * 构建导入匹配上下文时，明细/费用项游标扫描每批条数。
     */
    public static final int CONTEXT_SCAN_BATCH_SIZE = 1000;

    /**
     * 单次 {@code submitManualMatch} 费用项上限（按完整识别组打包，不拆组）。
     */
    public static final int SUBMIT_CHUNK_SIZE = 500;
}
