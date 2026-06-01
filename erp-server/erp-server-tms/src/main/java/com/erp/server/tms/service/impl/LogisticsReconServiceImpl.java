package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.entity.LogisticsReconDetailEntity;
import com.erp.model.tms.entity.LogisticsReconEntity;
import com.erp.model.tms.entity.LogisticsReconRefLogisticsBillEntity;
import com.erp.model.tms.enums.LogisticsReconCheckStatusEnum;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconRefMatchTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.mapper.LogisticsReconMapper;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.LogisticsReconDetailService;
import com.erp.server.tms.service.LogisticsReconDetailSubService;
import com.erp.server.tms.service.LogisticsReconRefLogisticsBillService;
import com.erp.server.tms.service.LogisticsReconService;
import com.erp.server.tms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_RECON;

/**
 * <p>
 * 物流商对账单（主表） 服务实现类
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Slf4j
@Service
public class LogisticsReconServiceImpl
        extends SuperServiceImpl<LogisticsReconMapper, LogisticsReconEntity>
        implements LogisticsReconService {

    private static final String DOC_NAME = "物流商对账单";

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private LogisticsReconDetailService logisticsReconDetailService;

    @Resource
    private LogisticsReconDetailSubService logisticsReconDetailSubService;

    @Resource
    private LogisticsReconRefLogisticsBillService logisticsReconRefLogisticsBillService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;


    @Override
    public List<LogisticsReconDTO.TabListDTO> tabList(PermissionsDTO param) {
        LogisticsReconDTO.PagingParamDTO searchParam = new LogisticsReconDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<LogisticsReconDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        if (list == null) {
            list = new ArrayList<>();
        }
        // 不存在的状态补 0
        List<String> existStatus = list.stream()
                .map(LogisticsReconDTO.TabListDTO::getTabFlag)
                .collect(Collectors.toList());
        for (String status : LogisticsReconCheckStatusEnum.getStatusList()) {
            if (!existStatus.contains(status)) {
                list.add(new LogisticsReconDTO.TabListDTO(status, 0));
            }
        }
        list.add(new LogisticsReconDTO.TabListDTO("all",
                list.stream().mapToInt(LogisticsReconDTO.TabListDTO::getCount).sum()));
        return list;
    }

    @Override
    public PagingVO<LogisticsReconDTO.ListDTO> paging(PagingDTO<LogisticsReconDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<LogisticsReconDTO.ListDTO> query =
                new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<LogisticsReconDTO.ListDTO> pageData = baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public LogisticsReconDTO.ViewDTO view(String id) {
        LogisticsReconEntity entity = super.getByIdOpt(id)
                .orElseThrow(() -> new ServiceException("未找到" + DOC_NAME + "数据"));
        LogisticsReconDTO.ViewDTO data = new LogisticsReconDTO.ViewDTO();
        data.setId(entity.getId());
        data.setReconciliationMonth(formatReconciliationMonth(entity.getReconciliationMonth()));
        data.setSupplierName(entity.getSupplierName());
        data.setTotalAmountStr(formatAmount(entity.getTotalAmount(), currencySymbol(entity.getCurrency())));
        return data;
    }


    @Override
    public List<BatchResultDTO> preprocessingImportExcel(LogisticsReconDTO.PreprocessingDTO dto) {
        // TODO 预处理：调用重构后的 buildSupplierBillDetailList（仅 ETL 清洗 + 校验，不落库）
        //  现阶段不复用 ImportHistoryRecordServiceImpl#importFile，原方法需重构后再接入
        throw new ServiceException("预处理导入待重构后接入");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO importExcel(LogisticsReconDTO.ImportDTO dto) {
        // 1. 先创建主表占位（check_status = importing 导入中），便于异步任务进度回写
        LogisticsReconEntity entity = createImportingMain(dto);
        // 2. TODO 提交异步任务执行 Excel 解析 + 落 logistics_recon_detail / _detail_sub
        //  原 ImportHistoryRecordServiceImpl#importFile + handleImportSuccessList 的解析链路待重构；
        //  重构完成后在任务回调中：
        //    - 调 LogisticsReconDetailService / LogisticsReconDetailSubService 批量写入
        //    - 汇总主表 import_count / cost_count / total_amount（导入完成一次性写；match 不写，查询实时聚合）
        //    - 把 check_status 由 importing 置为 pending（待确认）；失败置 import_fail_reason，保持 importing 直到任务结束
        log.info("[importExcel] processingType=importOnly mainId={} fileUrl={}", entity.getId(), dto.getFileUrl());
        return new BaseResultDTO.AddDTO(entity.getId(), entity.getCode());
    }

    /**
     * 新建导入中主表（共用 importExcel / importExcelCheck）
     */
    private LogisticsReconEntity createImportingMain(LogisticsReconDTO.ImportDTO dto) {
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DZ);
        LogisticsReconEntity entity = new LogisticsReconEntity()
                .setCode(code)
                .setReconciliationMonth(dto.getReconciliationMonth())
                .setCfgImportId(dto.getCfgImportId())
                .setFileUrl(dto.getFileUrl())
                .setFileName(dto.getFileName())
                .setCheckStatus(LogisticsReconCheckStatusEnum.IMPORTING.getCode())
                .setImportCount(0)
                .setCostCount(0);
        boolean save = super.save(entity);
        if (!save) {
            throw new ServiceException(DOC_NAME + "保存失败");
        }
        return entity;
    }

    // ============================== 校验状态流转 ==============================

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchUpdateCheckStatus(LogisticsReconDTO.UpdateCheckStatusDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> results = new ArrayList<>(ids.size());
        List<LogisticsReconEntity> entities = lambdaQuery()
                .in(LogisticsReconEntity::getId, ids)
                .list();
        for (LogisticsReconEntity entity : entities) {
            try {
                validateCheckStatusTransition(entity, dto.getCheckStatus());
                LoginUser user = UserContext.getDefaultLoginUser();
                lambdaUpdate()
                        .eq(LogisticsReconEntity::getId, entity.getId())
                        .set(LogisticsReconEntity::getCheckStatus, dto.getCheckStatus())
                        .set(LogisticsReconEntity::getCheckUserId,
                                LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(dto.getCheckStatus())
                                        ? user.getUid() : "")
                        .set(LogisticsReconEntity::getCheckUserName,
                                LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(dto.getCheckStatus())
                                        ? user.getUserName() : "")
                        .set(LogisticsReconEntity::getCheckTime,
                                LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(dto.getCheckStatus())
                                        ? LocalDateTime.now() : null)
                        .update(new LogisticsReconEntity());
                results.add(BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE));
                String msg = StrUtil.format("用户【{}】将{}【{}】校验状态切换为【{}】，备注：{}",
                        user.getUserName(), DOC_NAME, entity.getCode(),
                        LogisticsReconCheckStatusEnum.getName(dto.getCheckStatus()), dto.getRemark());
                operateLogService.addModuleOperateLog(msg, null, entity.getId(), "校验状态切换");
            } catch (Exception e) {
                log.error("{}校验状态切换失败 id={}", DOC_NAME, entity.getId(), e);
                results.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return results;
    }

    private void validateCheckStatusTransition(LogisticsReconEntity entity, String targetStatus) {
        // 导入中不允许直接切换校验状态（需等导入完成进入待确认）
        if (LogisticsReconCheckStatusEnum.IMPORTING.getCode().equals(entity.getCheckStatus())) {
            throw new ServiceException("对账单导入中，暂不允许切换校验状态");
        }
        // 仅允许在 待确认 / 已确认 之间流转
        if (!LogisticsReconCheckStatusEnum.PENDING.getCode().equals(targetStatus)
                && !LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(targetStatus)) {
            throw new ServiceException("非法的校验状态目标值");
        }
        if (LogisticsReconCheckStatusEnum.PENDING.getCode().equals(targetStatus)) {
            int refCount = (int) logisticsReconRefLogisticsBillService.lambdaQuery()
                    .eq(com.erp.model.tms.entity.LogisticsReconRefLogisticsBillEntity::getMainId, entity.getId())
                    .count();
            if (refCount > 0) {
                throw new ServiceException("已存在有效匹配关系，不允许回退到待确认");
            }
        }
    }

    // ============================== 合并 & 匹配 ==============================

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchMatch(LogisticsReconDTO.BatchMatchDTO dto) {
        List<BatchResultDTO> results = new ArrayList<>(dto.getIds().size());
        for (String mainId : dto.getIds()) {
            try {
                LogisticsReconEntity entity = super.getByIdOpt(mainId)
                        .orElseThrow(() -> new ServiceException(DOC_NAME + "不存在"));
                if (!LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(entity.getCheckStatus())) {
                    throw new ServiceException("仅已确认的对账单允许触发合并匹配");
                }
                // TODO 合并 & 匹配：
                //  1. 把 detail + detail_sub 转 ImportDataDTO（依赖 ImportHistoryRecordServiceImpl#buildImportDataList 重构后的入口）
                //  2. 调 ImportHistoryRecordServiceImpl#importBatchAddOrUpdate(List<ImportDataDTO>, processingType, mainId) 重载
                //  3. 落 logistics_recon_ref_logistics_bill（按 detail 整批写）
                //  4. detail_sub.match_status 整批更新（主表匹配数/匹配状态由查询实时聚合，无需回写）
                results.add(BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE));
            } catch (Exception e) {
                log.error("[batchMatch] 失败 mainId={}", mainId, e);
                results.add(BatchResultDTO.fail(mainId, mainId, e.getMessage()));
            }
        }
        return results;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchConfirmBill(LogisticsReconDTO.BatchConfirmBillDTO dto) {
        String targetStatus = dto.getReconciliationStatus();
        if (!ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(targetStatus)
                && !ReconciliationStatusEnum.CONFIRMED.getCode().equals(targetStatus)) {
            throw new ServiceException("仅支持更新为待确认或账单确认");
        }
        List<BatchResultDTO> results = new ArrayList<>(dto.getIds().size());
        LocalDateTime confirmTime = ReconciliationStatusEnum.CONFIRMED.getCode().equals(targetStatus)
                ? LocalDateTime.now() : null;
        for (String mainId : dto.getIds()) {
            try {
                LogisticsReconEntity entity = super.getByIdOpt(mainId)
                        .orElseThrow(() -> new ServiceException(DOC_NAME + "不存在"));
                if (!LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(entity.getCheckStatus())) {
                    throw new ServiceException("仅已确认的对账单允许执行账单确认");
                }
                List<String> logisticsBillCostIds = logisticsReconRefLogisticsBillService.lambdaQuery()
                        .eq(LogisticsReconRefLogisticsBillEntity::getMainId, mainId)
                        .eq(LogisticsReconRefLogisticsBillEntity::getIsDeleted, false)
                        .list()
                        .stream()
                        .map(LogisticsReconRefLogisticsBillEntity::getLogisticsBillCostId)
                        .filter(StrUtil::isNotBlank)
                        .distinct()
                        .collect(Collectors.toList());
                if (CollUtil.isEmpty(logisticsBillCostIds)) {
                    throw new ServiceException("未找到已匹配的物流费用单");
                }
                for (String logisticsBillCostId : logisticsBillCostIds) {
                    logisticsBillCostService.updateReconciliationStatus(logisticsBillCostId, targetStatus, confirmTime);
                }
                String msg = StrUtil.format("用户【{}】将{}【{}】关联物流费用单对账状态更新为【{}】",
                        UserContext.getDefaultLoginUser().getUserName(), DOC_NAME, entity.getCode(),
                        ReconciliationStatusEnum.getName(targetStatus));
                operateLogService.addModuleOperateLog(msg, null, entity.getId(), "账单确认");
                results.add(BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE));
            } catch (Exception e) {
                log.error("[batchConfirmBill] 失败 mainId={}", mainId, e);
                results.add(BatchResultDTO.fail(mainId, mainId, e.getMessage()));
            }
        }
        return results;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchUnbindMatch(LogisticsReconDTO.BatchUnbindMatchDTO dto) {
        // 按 detail_sub 维度逻辑删 ref + 还原 detail_sub.match_status = unmatched
        logisticsReconRefLogisticsBillService.removeByDetailSubIds(dto.getDetailSubIds());
        logisticsReconDetailSubService.batchUpdateMatchStatus(dto.getDetailSubIds(),
                LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode(), null);
        // 解绑后匹配数由列表/详情查询实时聚合，无需回写主表

        List<BatchResultDTO> results = new ArrayList<>(dto.getDetailSubIds().size());
        for (String detailSubId : dto.getDetailSubIds()) {
            results.add(BatchResultDTO.success(detailSubId, detailSubId, OperationTypeEnum.UPDATE));
        }
        return results;
    }

    // ============================== 删除 / 导出 ==============================

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchDelete(List<String> ids) {
        List<BatchResultDTO> results = new ArrayList<>(ids.size());
        List<LogisticsReconEntity> entities = lambdaQuery()
                .in(LogisticsReconEntity::getId, ids)
                .list();
        List<String> deletableIds = new ArrayList<>();
        for (LogisticsReconEntity entity : entities) {
            // 已确认的对账单不允许删除，导入中 / 待确认均可删
            if (LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(entity.getCheckStatus())) {
                results.add(BatchResultDTO.fail(entity.getId(), entity.getCode(),
                        "已确认的对账单不允许删除"));
                continue;
            }
            deletableIds.add(entity.getId());
            results.add(BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE));
        }
        if (CollUtil.isNotEmpty(deletableIds)) {
            // 级联逻辑删 detail / detail_sub / ref
            logisticsReconRefLogisticsBillService.removeByMainIds(deletableIds);
            logisticsReconDetailSubService.removeByMainIds(deletableIds);
            logisticsReconDetailService.removeByMainIds(deletableIds);
            super.removeByIds(deletableIds);
        }
        return results;
    }

    @Override
    public void exportList(LogisticsReconDTO.ExportDTO param) {
        downloadTaskFeign.saveDownloadTask(DOC_NAME, EXPORT_TMS_LOGISTICS_RECON.getCode(), param);
    }

    // ============================== 主表汇总 ==============================

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshAggregate(String mainId) {
        if (ObjectUtil.isEmpty(mainId)) {
            return;
        }
        if (super.getById(mainId) == null) {
            return;
        }
        // 仅汇总导入行数（导入完成时调用一次）；匹配状态/匹配数不在主表存储，由查询实时聚合派生
        int importCount = (int) logisticsReconDetailService.lambdaQuery()
                .eq(LogisticsReconDetailEntity::getMainId, mainId)
                .count();
        lambdaUpdate()
                .eq(LogisticsReconEntity::getId, mainId)
                .set(LogisticsReconEntity::getImportCount, importCount)
                .update(new LogisticsReconEntity());
    }

    // ============================== private ==============================

    /**
     * 列表名称回填
     */
    private void fillList(List<LogisticsReconDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<String, String> currencySymbolMap = FeignQuery.list(DictCurrencyEntity.class).stream()
                .collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol, (first, second) -> first));
        for (LogisticsReconDTO.ListDTO data : list) {
            data.setCheckStatusName(LogisticsReconCheckStatusEnum.getName(data.getCheckStatus()));
            // match_count 由 paging 子查询实时聚合得到，match_status 在此按 cost_count（费用项数）派生
            int matchCount = data.getMatchCount() == null ? 0 : data.getMatchCount();
            int costCount = data.getCostCount() == null ? 0 : data.getCostCount();
            String matchStatus = LogisticsReconMatchStatusEnum.resolve(matchCount, costCount);
            data.setMatchStatus(matchStatus);
            data.setMatchStatusName(LogisticsReconMatchStatusEnum.getName(matchStatus));
            String symbol = currencySymbolMap.getOrDefault(data.getCurrency(), "¥");
            data.setCurrencySymbol(symbol);
            data.setTotalAmountStr(formatAmount(data.getTotalAmount(), symbol));
            data.setMatchSuccessAmountStr(formatAmount(data.getMatchSuccessAmount(), symbol));
            data.setMatchFailAmountStr(formatAmount(data.getMatchFailAmount(), symbol));
        }
    }

    /**
     * 根据币别获取符号
     * @author Will
     * @date 2026/6/1 15:40
     * @param currency 币别
     * @return String
     */
    private String currencySymbol(String currency) {
        return FeignQuery.list(DictCurrencyEntity.class).stream()
                .filter(item -> StrUtil.equals(item.getId(), currency))
                .findFirst()
                .map(DictCurrencyEntity::getSymbol)
                .orElse("¥");
    }

    /**
     * 格式化对账月份展示文本
     * @author Will
     * @date 2026/6/1 15:55
     * @param reconciliationMonth 对账月份 yyyy-MM
     * @return String
     */
    private String formatReconciliationMonth(String reconciliationMonth) {
        if (StrUtil.isBlank(reconciliationMonth)) {
            return "";
        }
        String[] parts = reconciliationMonth.split("-");
        if (parts.length != 2) {
            return reconciliationMonth;
        }
        try {
            return parts[0] + "年" + Integer.parseInt(parts[1]) + "月份";
        } catch (NumberFormatException e) {
            return reconciliationMonth;
        }
    }

    /**
     * 格式化金额展示文本（带币别符号）
     * @author Will
     * @date 2026/6/1 15:40
     * @param amount 金额
     * @param symbol 币别符号
     * @return String
     */
    private String formatAmount(BigDecimal amount, String symbol) {
        if (amount == null) {
            return symbol + "0";
        }
        return symbol + amount.stripTrailingZeros().toPlainString();
    }

    /**
     * 匹配类型默认值（手动 / 新建物流单匹配 在对应方法内显式指定）
     */
    @SuppressWarnings("unused")
    private static final String DEFAULT_MATCH_TYPE = LogisticsReconRefMatchTypeEnum.AUTO.getCode();
}
