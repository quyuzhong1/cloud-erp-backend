package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsReconBatchResultDTO;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.dto.LogisticsReconMatchDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.entity.LogisticsReconDetailEntity;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;
import com.erp.model.tms.entity.LogisticsReconEntity;

import java.math.BigDecimal;
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
     * 物流商对账单导入 Excel（processingType=importOnly，仅落对账单 + 明细）
     * TODO 当前依赖 ImportHistoryRecordServiceImpl 的 Excel 解析链路，后续重构后再接入
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return BaseResultDTO.AddDTO
     */
    BaseResultDTO.AddDTO importExcel(LogisticsReconDTO.ImportDTO dto);

    /**
     * 导入提交前预创建/更新主表（短事务，不含 Feign 任务创建）
     */
    void prepareImportExcelMains(LogisticsReconDTO.ImportDTO dto);

    /**
     * 单配置维度加锁后预创建/复用主表（prepareImportExcelMains 内部按 cfg 循环调用）
     */
    void prepareImportMainLocked(String lockKey, LogisticsReconDTO.ImportDTO dto,
                                 CfgLogisticsCostImportEntity importCfg,
                                 Map<String, String> mainIdMap, Map<String, Boolean> reimportUpdateMap,
                                 Map<String, String> logisticsSupplierNameMap,
                                 Map<String, String> salesPlatformNameMap);

    /**
     * 物流商对账单异步导入任务执行
     * @author Will
     * @date: 2026/06/02
     * @param dto
     * @return void
     */
    void executeImportTask(LogisticsReconDTO.ImportDTO dto);


    /**
     * 物流商对账单校验状态单条切换（待确认 ↔ 已确认）
     * 前置：非导入中（check_status != importing）；已存在有效 ref 的不允许回退到待确认
     * 单条独立事务 + 按对账单加分布式锁，批量切换由控制层循环调用
     * @author Will
     * @date: 2026/05/29
     * @param id 对账单 id
     * @param checkStatus 目标校验状态
     * @return BatchResultDTO
     */
    BatchResultDTO updateCheckStatus(String id, String checkStatus);

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
     * 按费用项 + ERP 业务单号匹配（手动匹配 / 导入匹配共用）。
     * 行 = 单个费用项，识别单号取用户输入的 ERP 单号，按"识别单号 + 物流商"复用导入匹配逻辑，
     * 命中唯一物流单则生成/更新物流费用并写对账关联，命中多张视为失败。
     * @author Will
     * @date 2026/6/11
     * @param mainId 对账单 id
     * @param inputs 费用项 + ERP 单号入参
     * @param matchType 关联匹配类型（manual / ...）
     * @return 逐费用项匹配结果
     */
    List<BatchResultDTO> matchDetailSubsByErp(String mainId, List<LogisticsReconMatchDTO.SubErpInputDTO> inputs, String matchType);

    /**
     * 对账单整批合并匹配的实际执行（异步线程内调用，加分布式锁 + 事务），处理已置"匹配中"的费用项。
     * @author Will
     * @date 2026/6/12
     * @param mainId 对账单 id
     * @param scopeSubIds 本次整批匹配认领的费用项 id（仅处理该集合，避免与手动/导入匹配交叉）
     * @param isConfirm 是否确认匹配上的物流费用数据
     * @return void
     */
    void doMatchByMain(String mainId, List<String> scopeSubIds, boolean isConfirm);

    /**
     * 对账单整批匹配的单批执行（分片小事务）：处理指定费用项 id 中仍处于匹配中的记录。
     *
     * @param mainId       对账单 id
     * @param detailSubIds 本批费用项 id
     * @param isConfirm    是否确认匹配上的物流费用数据
     * @param preload      整单级预加载上下文（导入配置 + 币别/汇率），各分片复用；为空则分片内自查（兼容旧调用）
     */
    void doMatchSubsChunk(String mainId, List<String> detailSubIds, boolean isConfirm,
                          LogisticsReconMatchDTO.ReconMatchPreloadDTO preload);

    /**
     * 提交手动匹配异步任务（按对账单分组、认领 matching 后提交线程池），立即返回。
     * @author Will
     * @date 2026/6/12
     * @param inputs 费用项 + ERP 单号入参
     * @param matchType 关联匹配类型（manual）
     * @return 提交结果（已提交 / 校验失败）
     */
    List<BatchResultDTO> submitManualMatch(List<LogisticsReconMatchDTO.SubErpInputDTO> inputs, String matchType);

    /**
     * 把仍处于"匹配中"的费用项回写为匹配失败（异步任务异常兜底）。
     * @author Will
     * @date 2026/6/12
     * @param mainId 对账单 id
     * @param detailSubIds 指定费用项 id（为空则按对账单下全部匹配中费用项）
     * @param reason 失败原因
     * @return void
     */
    void markReconMatchFailed(String mainId, List<String> detailSubIds, String reason);

    /**
     * 刷新费用项确认状态汇总（短事务）
     */
    void refreshDetailSubReconciliationStatusInTx(String mainId);

    /**
     * 刷新指定费用项范围的确认状态汇总（短事务），供分片匹配确认后按 scope 刷新，避免全单扫描。
     *
     * @param mainId 对账单 id
     * @param subIds 需要重算确认状态的费用项 id
     */
    void refreshDetailSubReconciliationStatusInTx(String mainId, java.util.Collection<String> subIds);

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
     * 物流商对账单账单确认（批量，异步）：HTTP 快速校验后提交后台任务执行 confirmBill，立即返回。
     *
     * @param ids                  对账单 id 集合
     * @param reconciliationStatus 目标对账状态（toBeConfirm / confirmed）
     * @param confirmTime          对账确认时间（状态为 confirmed 时有效）
     * @return 逐单提交结果（已提交 / 校验失败）
     */
    List<BatchResultDTO> submitConfirmBillAsync(List<String> ids, String reconciliationStatus, LocalDateTime confirmTime);

    /**
     * 账单确认单批 ref + 物流费用状态更新（独立短事务）
     */
    void confirmBillRefCostBatch(String mainId, List<String> batchCostIds, String reconciliationStatus,
                                 LocalDateTime confirmTime);

    /**
     * 物流费用单对账状态变更后反向同步：按费用单 id 将其对账状态回写到关联 ref 快照，
     * 并刷新受影响对账费用项（detail_sub）的确认状态聚合。
     * <p>供物流费用侧（手动改状态 / 作废 / 导入确认 / 回退重算）调用，保证三表状态一致。</p>
     *
     * @param logisticsBillCostIds 发生对账状态变更的物流费用单 id 集合
     */
    void syncReconStatusByCostIds(java.util.Collection<String> logisticsBillCostIds);

    /**
     * 物流费用单对账状态反向同步（可排除指定主单）。
     * <p>账单确认场景下当前主单的 ref 快照已在确认分片内直接更新，通过 {@code excludeMainId} 跳过当前主单，
     * 仅同步共享同一费用单的其他对账单，避免二次全量回查并重写当前主单 ref 快照。</p>
     *
     * @param logisticsBillCostIds 发生对账状态变更的物流费用单 id 集合
     * @param excludeMainId        需跳过的对账单主单 id（为空则不跳过）
     */
    void syncReconStatusByCostIds(java.util.Collection<String> logisticsBillCostIds, String excludeMainId);

    /**
     * 回写匹配结果（独立短事务，与 reconMatchAndGenerate 分离）
     */
    void commitReconMatchResult(String mainId, String matchType,
                                Map<String, String> rowKeyToDetailId,
                                Map<String, List<LogisticsReconDetailSubEntity>> rowKeyToSubs,
                                List<LogisticsReconMatchDTO.MatchResultDTO> matchResults);

    /**
     * 回写匹配结果（独立短事务，与 reconMatchAndGenerate 分离），支持传入 ERP 单号用于明细快照回填。
     */
    void commitReconMatchResult(String mainId, String matchType,
                                Map<String, String> rowKeyToDetailId,
                                Map<String, List<LogisticsReconDetailSubEntity>> rowKeyToSubs,
                                List<LogisticsReconMatchDTO.MatchResultDTO> matchResults,
                                List<LogisticsReconMatchDTO.SubErpInputDTO> erpInputs);

    /**
     * 物流商对账单导入分批处理（供 Excel 监听器分批回调）
     * 解析/校验/汇率换算在事务外完成，仅 detail+sub 落库走事务（{@link #saveImportDetailAndSub}）
     * @author Will
     * @date: 2026/06/03
     * @param rows 当前批次原始行数据
     * @param headMap Excel 表头
     * @param rowNoStart 当前批次首行行号（1 基，按 sheet 连续）
     * @param dto 导入参数（含 mainId）
     * @param importCfg 当前导入配置
     * @param cfgDetails 配置字段明细
     * @param rateCache 文件级本位币汇率缓存（按币别，跨批次复用，避免重复远程调用）
     * @return 当前批次落库结果（校验失败行 + 落库统计）
     */
    LogisticsReconBatchResultDTO handleReconImportBatch(List<Map<Integer, String>> rows,
                                                        Map<Integer, String> headMap,
                                                        int rowNoStart,
                                                        LogisticsReconDTO.ImportDTO dto,
                                                        CfgLogisticsCostImportEntity importCfg,
                                                        List<CfgLogisticsCostImportDetailEntity> cfgDetails,
                                                        Map<String, BigDecimal> rateCache);

    /**
     * 导入明细 + 费用项批量落库（单独事务，保证 detail/sub 原子写）
     * @author Will
     * @date: 2026/06/05
     * @param detailList 对账明细
     * @param subList 对账费用项
     * @return void
     */
    void saveImportDetailAndSub(List<LogisticsReconDetailEntity> detailList,
                                List<LogisticsReconDetailSubEntity> subList,
                                Map<String, Integer> detailCostCountDelta,
                                List<LogisticsReconDetailEntity> updateDetailList,
                                List<LogisticsReconDetailSubEntity> updateSubList);

    /**
     * 物流商对账明细批量解绑匹配（按 detail 维度，逻辑删 ref）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> batchUnbindMatch(LogisticsReconDTO.BatchUnbindMatchDTO dto);

    /**
     * 物流商对账单单条删除（导入中 / 待确认可删，已确认不可删；级联 detail / detail_sub / ref）
     * 单条独立事务 + 按对账单加分布式锁，批量删除由控制层循环调用
     * @author Will
     * @date: 2026/05/29
     * @param id 对账单 id
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);

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
