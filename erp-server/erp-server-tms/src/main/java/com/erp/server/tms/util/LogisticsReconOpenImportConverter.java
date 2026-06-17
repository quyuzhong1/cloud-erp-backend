package com.erp.server.tms.util;

import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.enums.ImportHistoryRecordProcessingTypeEnum;

/**
 * 开放接口（汇创等）物流费用导入请求 → 物流商对账单导入参数转换。
 *
 * @author Will
 * @date 2026/6/12
 */
public final class LogisticsReconOpenImportConverter {

    /**
     * 对账单导入：仅落库，校验状态待确认。
     */
    public static final String RECON_PROCESSING_IMPORT_ONLY = "importOnly";

    /**
     * 对账单导入：落库并直接已确认（对应外部导入确认）。
     */
    public static final String RECON_PROCESSING_IMPORT_CHECK = "importCheck";

    private LogisticsReconOpenImportConverter() {
    }

    /**
     * 将开放接口物流费用导入参数转为对账单批量导入参数。
     *
     * @author Will
     * @date 2026/6/12
     * @param openImportDTO 开放接口导入请求（原 ImportHistoryRecordDTO.ImportDTO）
     * @return 物流商对账单批量导入参数
     */
    public static LogisticsReconDTO.ImportBatchDTO toReconImportBatch(ImportHistoryRecordDTO.ImportDTO openImportDTO) {
        LogisticsReconDTO.ImportBatchDTO batch = new LogisticsReconDTO.ImportBatchDTO();
        batch.setList(openImportDTO.getList());
        batch.setBusinessType(openImportDTO.getBusinessType());
        batch.setCostType(openImportDTO.getCostType());
        batch.setReconciliationMonth(openImportDTO.getReconciliationMonth());
        batch.setProcessingType(mapOpenProcessingType(openImportDTO.getProcessingType()));
        return batch;
    }

    /**
     * 开放接口 processingType → 对账单导入 processingType。
     * <p>preprocessing / import → importOnly；confirmImport → importCheck。</p>
     *
     * @author Will
     * @date 2026/6/12
     * @param openProcessingType 开放接口处理类型
     * @return 对账单导入处理类型
     */
    public static String mapOpenProcessingType(String openProcessingType) {
        if (ImportHistoryRecordProcessingTypeEnum.CONFIRM_IMPORT.getCode().equals(openProcessingType)) {
            return RECON_PROCESSING_IMPORT_CHECK;
        }
        return RECON_PROCESSING_IMPORT_ONLY;
    }
}
