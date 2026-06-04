package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.dto.excel.LogisticsReconImportExcelDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.entity.LogisticsReconEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物流商对账单（主表） 服务类
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
public interface LogisticsReconService extends SuperService<LogisticsReconEntity> {

    /**
     * 物流商对账单列表数量合计
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return List<LogisticsReconDTO.TabListDTO>
     */
    List<LogisticsReconDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 物流商对账单分页列表查询
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return PagingVO<LogisticsReconDTO.ListDTO>
     */
    PagingVO<LogisticsReconDTO.ListDTO> paging(PagingDTO<LogisticsReconDTO.PagingParamDTO> dto);

    /**
     * 物流商对账单查看详情
     * @author Will
     * @date: 2026/05/29
     * @param id
     * @return LogisticsReconDTO.ViewDTO
     */
    LogisticsReconDTO.ViewDTO view(String id);

    /**
     * 物流商对账单预处理导入（试解析，不落库；用于前端预览校验）
     * TODO 当前依赖 ImportHistoryRecordServiceImpl 的 Excel 解析链路，后续重构后再接入
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> preprocessingImportExcel(LogisticsReconDTO.PreprocessingDTO dto);

    /**
     * 物流商对账单导入 Excel（processingType=importOnly，仅落对账单 + 明细）
     * TODO 当前依赖 ImportHistoryRecordServiceImpl 的 Excel 解析链路，后续重构后再接入
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return BaseResultDTO.AddDTO
     */
    BaseResultDTO.AddDTO importExcel(LogisticsReconDTO.ImportDTO dto);

    /**
     * 物流商对账单异步导入任务执行
     * @author Will
     * @date: 2026/06/02
     * @param dto
     * @return void
     */
    void executeImportTask(LogisticsReconDTO.ImportDTO dto);


    /**
     * 物流商对账单校验状态批量切换（待确认 ↔ 已确认）
     * 前置：非导入中（check_status != importing）；已存在有效 ref 的不允许回退到待确认
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> batchUpdateCheckStatus(LogisticsReconDTO.UpdateCheckStatusDTO dto);

    /**
     * 物流商对账单合并并匹配（按对账单整批触发）
     * TODO 内部需复用 buildImportDataListFromSupplierBillDetail + importBatchAddOrUpdate 重载入口
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> batchMatch(LogisticsReconDTO.BatchMatchDTO dto);

    /**
     * 物流商对账单账单确认（单条，更新已匹配物流费用单对账状态）
     * @author Will
     * @date: 2026/06/02
     * @param mainId 对账单 id
     * @param reconciliationStatus 目标对账状态
     * @param confirmTime 对账确认时间（状态为 confirmed 时有效）
     * @return BatchResultDTO
     */
    BatchResultDTO confirmBill(String mainId, String reconciliationStatus, LocalDateTime confirmTime);

    /**
     * 物流商对账单导入分批落库（供 Excel 监听器分批回调，每批单独事务）
     * @author Will
     * @date: 2026/06/03
     * @param rows 当前批次原始行数据
     * @param headMap Excel 表头
     * @param rowNoStart 当前批次首行行号（1 基，按 sheet 连续）
     * @param dto 导入参数（含 mainId）
     * @param importCfg 当前导入配置
     * @param cfgDetails 配置字段明细
     * @return 当前批次落库结果（校验失败行 + 落库统计）
     */
    ReconBatchResult handleReconImportBatch(List<Map<Integer, String>> rows,
                                            Map<Integer, String> headMap,
                                            int rowNoStart,
                                            LogisticsReconDTO.ImportDTO dto,
                                            CfgLogisticsCostImportEntity importCfg,
                                            List<CfgLogisticsCostImportDetailEntity> cfgDetails);

    /**
     * 单批导入落库结果
     */
    class ReconBatchResult {
        private final List<LogisticsReconImportExcelDTO> errorList;
        private final int detailCount;
        private final int subCount;
        private final java.math.BigDecimal totalAmount;
        private final java.util.Set<String> currencies;

        public ReconBatchResult(List<LogisticsReconImportExcelDTO> errorList, int detailCount, int subCount,
                                java.math.BigDecimal totalAmount, java.util.Set<String> currencies) {
            this.errorList = errorList;
            this.detailCount = detailCount;
            this.subCount = subCount;
            this.totalAmount = totalAmount;
            this.currencies = currencies;
        }

        public List<LogisticsReconImportExcelDTO> getErrorList() {
            return errorList;
        }

        public int getDetailCount() {
            return detailCount;
        }

        public int getSubCount() {
            return subCount;
        }

        public java.math.BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public java.util.Set<String> getCurrencies() {
            return currencies;
        }
    }

    /**
     * 物流商对账明细批量解绑匹配（按 detail 维度，逻辑删 ref）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> batchUnbindMatch(LogisticsReconDTO.BatchUnbindMatchDTO dto);

    /**
     * 物流商对账单批量删除（导入中 / 待确认可删，已确认不可删；级联 detail / detail_sub / ref）
     * @author Will
     * @date: 2026/05/29
     * @param ids
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> batchDelete(List<String> ids);

    /**
     * 物流商对账单导出（异步：提交文件中心下载任务）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return void
     */
    void exportList(LogisticsReconDTO.ExportDTO dto);

    /**
     * 导入完成后刷新主表导入汇总字段（import_count 等）
     * 匹配数 / 匹配状态不回写主表，由查询实时聚合明细派生
     * @author Will
     * @date: 2026/05/29
     * @param mainId
     * @return void
     */
    void refreshAggregate(String mainId);
}
