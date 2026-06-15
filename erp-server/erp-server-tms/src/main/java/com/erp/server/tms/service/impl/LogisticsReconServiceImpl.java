package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.message.constant.DistributeKeyConstant;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import org.springframework.beans.factory.annotation.Qualifier;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.dto.LogisticsReconBatchResultDTO;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.dto.LogisticsReconMatchDTO;
import com.erp.model.tms.dto.excel.LogisticsReconImportExcelDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.entity.LogisticsReconDetailEntity;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;
import com.erp.model.tms.entity.LogisticsReconEntity;
import com.erp.model.tms.entity.LogisticsReconRefLogisticsBillEntity;
import com.erp.model.tms.enums.LogisticsReconCheckStatusEnum;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconReconciliationStatusEnum;
import com.erp.model.tms.enums.LogisticsReconRefMatchTypeEnum;
import com.erp.model.tms.enums.ImportHistoryRecordProcessingTypeEnum;
import com.erp.model.tms.enums.ImportHistoryRecordStatusEnum;
import com.erp.model.tms.enums.ImportHistoryRecordTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.tms.mapper.LogisticsReconMapper;
import com.erp.server.tms.listener.LogisticsReconExcelListener;
import com.erp.server.tms.service.CfgLogisticsCostImportDetailService;
import com.erp.server.tms.service.CfgLogisticsCostImportService;
import com.erp.server.tms.service.ImportHistoryRecordService;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.LogisticsReconDetailService;
import com.erp.server.tms.service.LogisticsReconDetailSubService;
import com.erp.server.tms.service.LogisticsReconRefLogisticsBillService;
import com.erp.server.tms.service.LogisticsReconService;
import com.erp.server.tms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_RECON;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_TMS_LOGISTICS_RECON;

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

    @Resource
    private FileFeign fileFeign;

    @Resource
    private CfgLogisticsCostImportService cfgLogisticsCostImportService;

    @Resource
    private CfgLogisticsCostImportDetailService cfgLogisticsCostImportDetailService;

    @Resource
    private ImportHistoryRecordService importHistoryRecordService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    /**
     * 自注入：用于让分批落库的 saveImportDetailAndSub 走 Spring 事务代理
     */
    @Resource
    private LogisticsReconService self;

    /**
     * 合并匹配 / 手动匹配异步线程池（本模块自管）
     */
    @Resource
    @Qualifier("logisticsReconMatchPool")
    private java.util.concurrent.ExecutorService logisticsReconMatchPool;


    @Override
    public List<LogisticsReconDTO.TabListDTO> tabList(PermissionsDTO param) {
        LogisticsReconDTO.PagingParamDTO searchParam = new LogisticsReconDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<LogisticsReconDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        if (list == null) {
            list = new ArrayList<>();
        }
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(obj -> obj.setTabFlagName(LogisticsReconCheckStatusEnum.getName(obj.getTabFlag())));
        }
        // 不存在的状态补 0
        List<String> existStatus = list.stream()
                .map(LogisticsReconDTO.TabListDTO::getTabFlag)
                .collect(Collectors.toList());
        for (String status : LogisticsReconCheckStatusEnum.getStatusList()) {
            if (!existStatus.contains(status)) {
                list.add(new LogisticsReconDTO.TabListDTO(status,
                        LogisticsReconCheckStatusEnum.getName(status), 0));
            }
        }
        list.add(new LogisticsReconDTO.TabListDTO("all", "全部",
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
                .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));
        LogisticsReconDTO.ViewDTO data = new LogisticsReconDTO.ViewDTO();
        data.setId(entity.getId());
        data.setReconciliationMonth(DateUtil.formatCnYearMonth(entity.getReconciliationMonth()));
        data.setSupplierName(entity.getSupplierName());
        data.setTotalAmountStr(formatAmount(entity.getTotalAmount(), currencySymbol(entity.getCurrency())));
        return data;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO importExcel(LogisticsReconDTO.ImportDTO dto) {
        List<CfgLogisticsCostImportEntity> cfgList =
                cfgLogisticsCostImportService.listByImport(dto.getFileName(), dto.getBusinessType(), dto.getCostType());
        if (CollUtil.isEmpty(cfgList)) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORT_TEMPLATE_NOT_RECOGNIZED);
        }
        List<String> cfgIds = cfgList.stream().map(CfgLogisticsCostImportEntity::getId).collect(Collectors.toList());
        List<CfgLogisticsCostImportDetailEntity> cfgDetails = cfgLogisticsCostImportDetailService.listByMainIdList(cfgIds);
        if (CollUtil.isEmpty(cfgDetails)) {
            throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
        }
        dto.setCfgLogisticsCostImportList(cfgList);
        dto.setImportDetailList(cfgDetails);
        // 提交导入时捕获操作人，异步回调落导入历史记录时使用
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        // 按配置预创建主表（一个配置一条），列表立即可见「导入中」状态；
        // mainId/code 为异步任务内按配置逐条处理时使用的临时字段，提交阶段不在此设置
        Map<String, String> mainIdMap = new HashMap<>(cfgList.size());
        Map<String, Boolean> reimportUpdateMap = new HashMap<>(cfgList.size());
        for (CfgLogisticsCostImportEntity importCfg : cfgList) {
            LogisticsReconEntity pending = findPendingReimportMain(dto, importCfg);
            if (pending != null) {
                lambdaUpdate()
                        .eq(LogisticsReconEntity::getId, pending.getId())
                        .set(LogisticsReconEntity::getCheckStatus, LogisticsReconCheckStatusEnum.IMPORTING.getCode())
                        .set(LogisticsReconEntity::getFileUrl, dto.getFileUrl())
                        .update(new LogisticsReconEntity());
                mainIdMap.put(importCfg.getId(), pending.getId());
                reimportUpdateMap.put(importCfg.getId(), Boolean.TRUE);
            } else {
                LogisticsReconEntity entity = createImportingMain(dto, importCfg);
                mainIdMap.put(importCfg.getId(), entity.getId());
                reimportUpdateMap.put(importCfg.getId(), Boolean.FALSE);
            }
        }
        dto.setMainIdMap(mainIdMap);
        dto.setReimportUpdateMap(reimportUpdateMap);
        String taskId = downloadTaskFeign.saveImportTask(DOC_NAME + "导入", IMPORT_TMS_LOGISTICS_RECON.getCode(), dto);
        log.info("[importExcel] submit taskId={} fileName={} cfgCount={} mainIds={}",
                taskId, dto.getFileName(), cfgList.size(), mainIdMap.values());
        return new BaseResultDTO.AddDTO(taskId, dto.getFileName());
    }

    @Override
    public void executeImportTask(LogisticsReconDTO.ImportDTO dto) {
        List<String> createdMainIds = CollUtil.isNotEmpty(dto.getMainIdMap())
                ? new ArrayList<>(dto.getMainIdMap().values()) : new ArrayList<>();
        try {
            List<CfgLogisticsCostImportEntity> cfgList = dto.getCfgLogisticsCostImportList();
            if (CollUtil.isEmpty(cfgList)) {
                cfgList = cfgLogisticsCostImportService.listByImport(dto.getFileName(), dto.getBusinessType(), dto.getCostType());
            }
            if (CollUtil.isEmpty(cfgList)) {
                throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORT_TEMPLATE_NOT_RECOGNIZED);
            }
            List<CfgLogisticsCostImportDetailEntity> allCfgDetails = dto.getImportDetailList();
            if (CollUtil.isEmpty(allCfgDetails)) {
                List<String> cfgIds = cfgList.stream().map(CfgLogisticsCostImportEntity::getId).collect(Collectors.toList());
                allCfgDetails = cfgLogisticsCostImportDetailService.listByMainIdList(cfgIds);
            }
            if (CollUtil.isEmpty(allCfgDetails)) {
                throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
            }
            Map<String, List<CfgLogisticsCostImportDetailEntity>> detailMap =
                    allCfgDetails.stream().collect(Collectors.groupingBy(CfgLogisticsCostImportDetailEntity::getMainId));
            byte[] fileBytes = fileFeign.downloadFile(dto.getFileUrl());

            int totalCount = 0;
            List<LogisticsReconImportExcelDTO> errorList = new ArrayList<>();
            String configErrorUrl = "";
            for (CfgLogisticsCostImportEntity importCfg : cfgList) {
                List<CfgLogisticsCostImportDetailEntity> cfgDetails = detailMap.get(importCfg.getId());
                if (CollUtil.isEmpty(cfgDetails)) {
                    throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
                }
                LogisticsReconEntity entity = resolveImportMain(dto, importCfg, createdMainIds);
                dto.setMainId(entity.getId());
                dto.setCode(entity.getCode());
                boolean reimportUpdate = CollUtil.isNotEmpty(dto.getReimportUpdateMap())
                        && Boolean.TRUE.equals(dto.getReimportUpdateMap().get(importCfg.getId()));
                dto.setReimportUpdate(reimportUpdate);
                if (reimportUpdate) {
                    initImportDetailCacheFromDb(dto, cfgDetails);
                } else {
                    dto.setImportDetailKeyMap(null);
                    dto.setImportDetailMaxSeqMap(null);
                    dto.setImportDetailSubKeyMap(null);
                }
                // 边解析边分批落库（监听器内 BATCH_COUNT 达阈值回调 handleReconImportBatch）
                LogisticsReconExcelListener excelListener =
                        new LogisticsReconExcelListener(dto, importCfg, cfgDetails);
                EasyExcel.read(new ByteArrayInputStream(fileBytes), excelListener)
                        .headRowNumber(importCfg.getHeaderRow())
                        .sheet(importCfg.getSheetName())
                        .doRead();
                if (excelListener.isHeadEmpty()) {
                    throw new ServiceException(ApiError.LOGISTICS_RECON_EXCEL_HEAD_NOT_FOUND);
                }
                totalCount += excelListener.getTotalRowCount();
                errorList.addAll(excelListener.getErrorList());
                finalizeImportMain(dto.getMainId(), excelListener);
                configErrorUrl = uploadErrorFile(excelListener.getErrorList());
                addImportHistoryRecord(dto, importCfg, excelListener, configErrorUrl);
            }
            BaseDTO.ImportResultDTO resultDTO = new BaseDTO.ImportResultDTO();
            resultDTO.setTaskId(dto.getTaskId());
            resultDTO.setCount(totalCount);
            // 单配置直接复用配置级错误文件，多配置再合并上传一次
            resultDTO.setErrorUrl(cfgList.size() == 1 ? configErrorUrl : uploadErrorFile(errorList));
            resultDTO.setFinishTime(LocalDateTime.now());
            resultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
            resultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
            downloadTaskFeign.updateTask(resultDTO);
        } catch (Exception e) {
            markImportFail(createdMainIds, e.getMessage());
            throw e;
        }
    }

    @Override
    public LogisticsReconBatchResultDTO handleReconImportBatch(List<Map<Integer, String>> rows,
                                                               Map<Integer, String> headMap,
                                                               int rowNoStart,
                                                               LogisticsReconDTO.ImportDTO dto,
                                                               CfgLogisticsCostImportEntity importCfg,
                                                               List<CfgLogisticsCostImportDetailEntity> cfgDetails,
                                                               Map<String, BigDecimal> rateCache) {
        Map<String, Integer> headerIndexMap = buildHeaderIndexMap(headMap);
        List<LogisticsReconDetailEntity> detailList = new ArrayList<>();
        List<LogisticsReconDetailSubEntity> subList = new ArrayList<>();
        List<LogisticsReconDetailEntity> updateDetailList = new ArrayList<>();
        List<LogisticsReconDetailSubEntity> updateSubList = new ArrayList<>();
        List<LogisticsReconImportExcelDTO> errorList = new ArrayList<>();
        // 与物流费用导入一致：区分纵向（费用项按行展开、按识别单号分组）/ 横向（费用项多列、一行多费用）
        Map<String, Integer> detailCostCountDelta = new HashMap<>();
        if (isVerticalReconCostItem(cfgDetails)) {
            processVerticalReconRows(rows, headerIndexMap, rowNoStart, dto, cfgDetails, rateCache,
                    detailList, subList, updateDetailList, updateSubList, errorList, detailCostCountDelta);
        } else {
            processHorizontalReconRows(rows, headerIndexMap, rowNoStart, dto, cfgDetails, rateCache,
                    detailList, subList, updateDetailList, updateSubList, errorList, detailCostCountDelta);
        }
        // detail + sub 落库走独立事务（经自注入代理生效），保证两表原子写
        self.saveImportDetailAndSub(detailList, subList, detailCostCountDelta, updateDetailList, updateSubList);
        // 主表合计取费用项本位币金额（local_amount）之和
        List<LogisticsReconDetailSubEntity> amountSubs = new ArrayList<>(subList.size() + updateSubList.size());
        amountSubs.addAll(subList);
        amountSubs.addAll(updateSubList);
        BigDecimal batchAmount = amountSubs.stream()
                .map(LogisticsReconDetailSubEntity::getLocalAmount)
                .filter(ObjectUtil::isNotEmpty)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
        // 主表币别取费用项本位币（local_currency），默认人民币
        Set<String> currencies = amountSubs.stream()
                .map(LogisticsReconDetailSubEntity::getLocalCurrency)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toCollection(HashSet::new));
        return new LogisticsReconBatchResultDTO(errorList, detailList.size(),
                subList.size() + updateSubList.size(), batchAmount, currencies);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveImportDetailAndSub(List<LogisticsReconDetailEntity> detailList,
                                       List<LogisticsReconDetailSubEntity> subList,
                                       Map<String, Integer> detailCostCountDelta,
                                       List<LogisticsReconDetailEntity> updateDetailList,
                                       List<LogisticsReconDetailSubEntity> updateSubList) {
        if (CollUtil.isNotEmpty(detailList)) {
            logisticsReconDetailService.saveBatch(detailList);
        }
        if (CollUtil.isNotEmpty(updateDetailList)) {
            logisticsReconDetailService.updateBatchById(updateDetailList);
        }
        if (CollUtil.isNotEmpty(subList)) {
            logisticsReconDetailSubService.saveBatch(subList);
        }
        if (CollUtil.isNotEmpty(updateSubList)) {
            logisticsReconDetailSubService.updateBatchById(updateSubList);
        }
        if (CollUtil.isNotEmpty(detailCostCountDelta)) {
            for (Map.Entry<String, Integer> entry : detailCostCountDelta.entrySet()) {
                if (StrUtil.isBlank(entry.getKey()) || entry.getValue() == null || entry.getValue() <= 0) {
                    continue;
                }
                logisticsReconDetailService.lambdaUpdate()
                        .eq(LogisticsReconDetailEntity::getId, entry.getKey())
                        .setSql("cost_count = cost_count + " + entry.getValue())
                        .update(new LogisticsReconDetailEntity());
            }
        }
    }

    /**
     * 取原币 → 本位币（人民币）汇率，按对账月份查 DMP；按币别缓存避免重复远程调用
     * @author Will
     * @date: 2026/06/05
     * @param currency 原币别（空按人民币处理）
     * @param reconciliationMonth 对账月份 yyyy-MM
     * @param cache 币别 → 汇率缓存
     * @return 汇率；本位币为人民币时恒为 1；查不到返回 null
     */
    private BigDecimal resolveLocalRate(String currency, String reconciliationMonth, Map<String, BigDecimal> cache) {
        String code = StrUtil.blankToDefault(currency, CurrencyEnum.CNY.getCurrencyCode());
        if (cache.containsKey(code)) {
            return cache.get(code);
        }
        BigDecimal rate;
        if (CurrencyEnum.CNY.getCurrencyCode().equals(code)) {
            rate = BigDecimal.ONE;
        } else if (StrUtil.isBlank(reconciliationMonth)) {
            rate = null;
        } else {
            rate = dmpTaskFeign.getMonthRate(reconciliationMonth + "-01", code);
        }
        cache.put(code, rate);
        return rate;
    }

    /**
     * 导入完成后回写主表汇总字段（行数 / 费用项数 / 金额 / 币别 / 校验状态）
     * @author Will
     * @date: 2026/06/03
     * @param mainId 主表 id
     * @param excelListener 导入监听器（已累积统计）
     * @return void
     */
    private void finalizeImportMain(String mainId, LogisticsReconExcelListener excelListener) {
        int detailCount = (int) logisticsReconDetailService.lambdaQuery()
                .eq(LogisticsReconDetailEntity::getMainId, mainId)
                .count();
        int subCount = (int) logisticsReconDetailSubService.lambdaQuery()
                .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                .count();
        lambdaUpdate()
                .eq(LogisticsReconEntity::getId, mainId)
                .set(LogisticsReconEntity::getCheckStatus, LogisticsReconCheckStatusEnum.PENDING.getCode())
                .set(LogisticsReconEntity::getImportCount, detailCount)
                .set(LogisticsReconEntity::getCostCount, subCount)
                .set(LogisticsReconEntity::getTotalAmount, excelListener.getTotalAmount())
                .set(LogisticsReconEntity::getCurrency, excelListener.resolveCurrency())
                .update(new LogisticsReconEntity());
    }

    /**
     * 落导入历史记录（仿 ImportHistoryRecordExcelListener#addMatchExcelResult）
     * @author Will
     * @date: 2026/06/03
     * @param dto 导入参数
     * @param importCfg 当前导入配置
     * @param excelListener 导入监听器（已累积统计）
     * @param cleanFileUrl 清洗/错误结果文件 url
     * @return void
     */
    private void addImportHistoryRecord(LogisticsReconDTO.ImportDTO dto, CfgLogisticsCostImportEntity importCfg,
                                        LogisticsReconExcelListener excelListener, String cleanFileUrl) {
        ImportHistoryRecordDTO.AddOrUpdateDTO addOrUpdateDTO = new ImportHistoryRecordDTO.AddOrUpdateDTO();
        addOrUpdateDTO.setReconciliationMonth(dto.getReconciliationMonth());
        addOrUpdateDTO.setBusinessType(importCfg.getBusinessType());
        addOrUpdateDTO.setFileUrl(dto.getFileUrl());
        addOrUpdateDTO.setFileName(dto.getFileName());
        addOrUpdateDTO.setSheetName(importCfg.getSheetName());
        addOrUpdateDTO.setCleanFileUrl(cleanFileUrl);
        addOrUpdateDTO.setCleanFileName(dto.getFileName());
        // 对账单导入仅基础落库，匹配在后续合并匹配阶段进行，故匹配数为 0
        addOrUpdateDTO.setImportCount(excelListener.getTotalRowCount());
        addOrUpdateDTO.setMatchCount(0);
        addOrUpdateDTO.setStatus(ImportHistoryRecordStatusEnum.HANDLE.getStatus());
        addOrUpdateDTO.setType(ImportHistoryRecordTypeEnum.SELF.getCode());
        addOrUpdateDTO.setOperationUserId(dto.getUserId());
        importHistoryRecordService.addOrUpdate(addOrUpdateDTO);
    }

    /**
     * 导入失败时回写主表失败原因
     * @author Will
     * @date: 2026/06/02
     * @param mainIds 主表 id 集合
     * @param failReason 失败原因
     * @return void
     */
    private void markImportFail(List<String> mainIds, String failReason) {
        if (CollUtil.isEmpty(mainIds) || StrUtil.isBlank(failReason)) {
            return;
        }
        String reason = failReason.length() > 490 ? failReason.substring(0, 490) : failReason;
        for (String mainId : mainIds) {
            lambdaUpdate()
                    .eq(LogisticsReconEntity::getId, mainId)
                    .set(LogisticsReconEntity::getImportFailReason, reason)
                    .update(new LogisticsReconEntity());
        }
    }

    /**
     * 构建表头索引
     * @author Will
     * @date: 2026/06/02
     * @param headMap
     * @return Map<String, Integer>
     */
    private Map<String, Integer> buildHeaderIndexMap(Map<Integer, String> headMap) {
        return headMap.entrySet().stream()
                .filter(entry -> StrUtil.isNotBlank(entry.getValue()))
                .collect(Collectors.toMap(entry -> entry.getValue().trim(), Map.Entry::getKey, (first, second) -> first));
    }

    /**
     * 构建导入中间对象并执行字段映射
     * @author Will
     * @date: 2026/06/02
     * @param rowNo
     * @param row
     * @param headerIndexMap
     * @param cfgDetails
     * @return LogisticsReconImportExcelDTO
     */
    private LogisticsReconImportExcelDTO buildReconImportExcel(int rowNo, Map<Integer, String> row,
                                                              Map<String, Integer> headerIndexMap,
                                                              List<CfgLogisticsCostImportDetailEntity> cfgDetails) {
        LogisticsReconImportExcelDTO excelDTO = new LogisticsReconImportExcelDTO();
        excelDTO.setNo(String.valueOf(rowNo));
        for (CfgLogisticsCostImportDetailEntity cfg : cfgDetails) {
            String target = StrUtil.blankToDefault(cfg.getTargetField(), cfg.getTargetDetailField());
            String value = getCellValue(row, headerIndexMap, cfg);
            if (StrUtil.isBlank(target) || StrUtil.isBlank(value)) {
                continue;
            }
            applyImportExcelField(excelDTO, target, value, cfg);
        }
        if (StrUtil.isNotBlank(excelDTO.getActualAmount())) {
            // 原币金额统一按 numeric(16,4) 规整
            excelDTO.setActualAmountValue(
                    parseAmount(excelDTO.getActualAmount(), false).setScale(4, RoundingMode.HALF_UP));
        }
        return excelDTO;
    }

    /**
     * 构建对账行明细
     * @author Will
     * @date: 2026/06/02
     * @param dto
     * @param rowNo
     * @param excelDTO
     * @return LogisticsReconDetailEntity
     */
    private LogisticsReconDetailEntity buildReconDetail(LogisticsReconDTO.ImportDTO dto, int rowNo,
                                                        LogisticsReconImportExcelDTO excelDTO) {
        LogisticsReconDetailEntity detail = new LogisticsReconDetailEntity()
                .setMainId(dto.getMainId())
                .setRowNo(rowNo)
                .setSoCode(excelDTO.getSoCode())
                .setPlatformOrderNo(excelDTO.getPlatformOrderNo())
                .setTrackNo(excelDTO.getTrackNo())
                .setTransportNo(excelDTO.getTransportNo())
                .setSoDeliveryCode(excelDTO.getSoDeliveryCode())
                .setCurrency(excelDTO.getCurrency())
                .setPayType(excelDTO.getPayType())
                .setWeightLogistics(parseAmount(excelDTO.getWeightLogistics(), false))
                .setVolumeWeightLogistics(parseAmount(excelDTO.getVolumeWeightLogistics(), false))
                .setBillingWeightLogistics(parseAmount(excelDTO.getBillingWeightLogistics(), false))
                .setWeightUnit(excelDTO.getWeightUnit())
                .setThirdLength(parseAmount(excelDTO.getThirdLength(), false))
                .setThirdWidth(parseAmount(excelDTO.getThirdWidth(), false))
                .setThirdHeight(parseAmount(excelDTO.getThirdHeight(), false))
                .setCostCount(1);
        detail.setId(IdWorker.getIdStr());
        return detail;
    }

    /**
     * 构建费用项明细
     * @author Will
     * @date: 2026/06/02
     * @param dto
     * @param detail
     * @param seqNo
     * @return LogisticsReconDetailSubEntity
     */
    private LogisticsReconDetailSubEntity buildReconDetailSub(LogisticsReconDTO.ImportDTO dto,
                                                              LogisticsReconDetailEntity detail,
                                                              int seqNo, String costName,
                                                              BigDecimal actualAmount, BigDecimal estimatedAmount) {
        LogisticsReconDetailSubEntity sub = new LogisticsReconDetailSubEntity()
                .setMainId(dto.getMainId())
                .setDetailId(detail.getId())
                .setSeqNo(seqNo)
                .setCostName(costName)
                .setCfgCostName(costName)
                .setActualAmount(actualAmount)
                .setEstimatedAmount(estimatedAmount == null ? BigDecimal.ZERO : estimatedAmount)
                .setCurrency(StrUtil.blankToDefault(detail.getCurrency(), ""))
                .setLocalCurrency(CurrencyEnum.CNY.getCurrencyCode())
                .setMatchStatus(LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode())
                .setReconciliationStatus(LogisticsReconReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
        if (StrUtil.isBlank(sub.getId())) {
            sub.setId(IdWorker.getIdStr());
        }
        return sub;
    }

    /**
     * 是否纵向费用项模板（targetField=costItem 且配置了物流商明细字段），与导入 isVerticalCostItem 一致。
     * @author Will
     * @date: 2026/06/12
     */
    private boolean isVerticalReconCostItem(List<CfgLogisticsCostImportDetailEntity> cfgDetails) {
        return cfgDetails.stream().anyMatch(detail ->
                StrUtil.equals("costItem", detail.getTargetField())
                        && StrUtil.isNotBlank(detail.getSourceDetailField()));
    }

    /**
     * 费用项配置列表：targetField=costItem，或（targetField 为空但配置了费用项 targetDetailField），兼容历史配置。
     * @author Will
     * @date: 2026/06/12
     */
    private List<CfgLogisticsCostImportDetailEntity> reconCostItemCfgList(List<CfgLogisticsCostImportDetailEntity> cfgDetails) {
        return cfgDetails.stream()
                .filter(detail -> StrUtil.equals("costItem", detail.getTargetField())
                        || (StrUtil.isBlank(detail.getTargetField()) && StrUtil.isNotBlank(detail.getTargetDetailField())))
                .collect(Collectors.toList());
    }

    /**
     * 横向费用项：一行一个明细，每个费用列生成一个费用项。
     * @author Will
     * @date: 2026/06/12
     */
    private void processHorizontalReconRows(List<Map<Integer, String>> rows, Map<String, Integer> headerIndexMap,
                                            int rowNoStart, LogisticsReconDTO.ImportDTO dto,
                                            List<CfgLogisticsCostImportDetailEntity> cfgDetails,
                                            Map<String, BigDecimal> rateCache,
                                            List<LogisticsReconDetailEntity> detailList,
                                            List<LogisticsReconDetailSubEntity> subList,
                                            List<LogisticsReconDetailEntity> updateDetailList,
                                            List<LogisticsReconDetailSubEntity> updateSubList,
                                            List<LogisticsReconImportExcelDTO> errorList,
                                            Map<String, Integer> detailCostCountDelta) {
        List<CfgLogisticsCostImportDetailEntity> costCfgList = reconCostItemCfgList(cfgDetails);
        List<CfgLogisticsCostImportDetailEntity> uniqueKeyList = cfgDetails.stream()
                .filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey)
                .collect(Collectors.toList());
        int rowNo = rowNoStart;
        for (Map<Integer, String> row : rows) {
            int currentRowNo = rowNo++;
            LogisticsReconImportExcelDTO excelDTO = null;
            try {
                excelDTO = buildReconImportExcel(currentRowNo, row, headerIndexMap, cfgDetails);
                List<String> errorMsgList = FieldValidUtil.fieldValid(excelDTO);
                if (CollUtil.isNotEmpty(errorMsgList)) {
                    excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(excelDTO);
                    continue;
                }
                String groupKey = reconUniqueGroupKey(row, headerIndexMap, uniqueKeyList);
                DetailResolveResult resolveResult = resolveImportDetail(dto, groupKey, currentRowNo, excelDTO);
                LogisticsReconDetailEntity detail = resolveResult.getDetail();
                List<LogisticsReconDetailSubEntity> rowNewSubs = new ArrayList<>();
                List<LogisticsReconDetailSubEntity> rowUpdateSubs = new ArrayList<>();
                String localRateError = null;
                int seqNo = resolveResult.getNextSeqNo();
                for (CfgLogisticsCostImportDetailEntity costCfg : costCfgList) {
                    String value = getCellValue(row, headerIndexMap, costCfg);
                    if (StrUtil.isBlank(value)) {
                        continue;
                    }
                    BigDecimal amount = parseAmount(value, false).setScale(4, RoundingMode.HALF_UP);
                    String costName = StrUtil.blankToDefault(costCfg.getTargetDetailFieldName(), costCfg.getTargetDetailField());
                    ImportSubResolveResult subResult = resolveOrBuildImportSub(dto, detail, seqNo, costName,
                            amount, BigDecimal.ZERO, rateCache);
                    if (subResult.getRateError() != null) {
                        localRateError = subResult.getRateError();
                        break;
                    }
                    if (subResult.isUpdateExisting()) {
                        rowUpdateSubs.add(subResult.getSub());
                    } else {
                        rowNewSubs.add(subResult.getSub());
                        seqNo = subResult.getNextSeqNo();
                    }
                }
                if (localRateError != null) {
                    excelDTO.setErrorMsg(localRateError);
                    errorList.add(excelDTO);
                    continue;
                }
                if (rowNewSubs.isEmpty() && rowUpdateSubs.isEmpty()) {
                    excelDTO.setErrorMsg("费用项不能为空");
                    errorList.add(excelDTO);
                    continue;
                }
                finishImportDetailSubs(dto, resolveResult, rowNewSubs, rowUpdateSubs, currentRowNo, excelDTO,
                        detailList, subList, updateDetailList, updateSubList, detailCostCountDelta);
            } catch (NumberFormatException e) {
                if (excelDTO == null) {
                    excelDTO = new LogisticsReconImportExcelDTO();
                    excelDTO.setNo(String.valueOf(currentRowNo));
                }
                excelDTO.setErrorMsg("金额或数字字段格式不正确");
                errorList.add(excelDTO);
            }
        }
    }

    /**
     * 纵向费用项：费用名称在单元格、金额在固定列；按识别单号分组合并为一个明细 + 多个费用项。
     * @author Will
     * @date: 2026/06/12
     */
    private void processVerticalReconRows(List<Map<Integer, String>> rows, Map<String, Integer> headerIndexMap,
                                          int rowNoStart, LogisticsReconDTO.ImportDTO dto,
                                          List<CfgLogisticsCostImportDetailEntity> cfgDetails,
                                          Map<String, BigDecimal> rateCache,
                                          List<LogisticsReconDetailEntity> detailList,
                                          List<LogisticsReconDetailSubEntity> subList,
                                          List<LogisticsReconDetailEntity> updateDetailList,
                                          List<LogisticsReconDetailSubEntity> updateSubList,
                                          List<LogisticsReconImportExcelDTO> errorList,
                                          Map<String, Integer> detailCostCountDelta) {
        List<CfgLogisticsCostImportDetailEntity> costCfgList = reconCostItemCfgList(cfgDetails);
        List<CfgLogisticsCostImportDetailEntity> uniqueKeyList = cfgDetails.stream()
                .filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey)
                .collect(Collectors.toList());
        // 费用名称列（费用项配置的物流商抬头字段）+ 实际/预估金额列
        String costNameHeader = costCfgList.stream()
                .map(CfgLogisticsCostImportDetailEntity::getSourceField)
                .filter(StrUtil::isNotBlank)
                .findFirst().orElse(null);
        CfgLogisticsCostImportDetailEntity actualCfg = cfgDetails.stream()
                .filter(detail -> StrUtil.equals("actualAmount", detail.getTargetField())).findFirst().orElse(null);
        CfgLogisticsCostImportDetailEntity estimatedCfg = cfgDetails.stream()
                .filter(detail -> StrUtil.equals("estimatedAmount", detail.getTargetField())).findFirst().orElse(null);

        // 先解析 + 校验每行，再按识别单号分组
        Map<String, List<ReconRowContext>> grouped = new LinkedHashMap<>();
        int rowNo = rowNoStart;
        for (Map<Integer, String> row : rows) {
            int currentRowNo = rowNo++;
            LogisticsReconImportExcelDTO excelDTO = buildReconImportExcel(currentRowNo, row, headerIndexMap, cfgDetails);
            List<String> errorMsgList = FieldValidUtil.fieldValid(excelDTO);
            if (CollUtil.isNotEmpty(errorMsgList)) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            String groupKey = reconUniqueGroupKey(row, headerIndexMap, uniqueKeyList);
            grouped.computeIfAbsent(groupKey, key -> new ArrayList<>())
                    .add(new ReconRowContext(currentRowNo, row, excelDTO));
        }

        for (List<ReconRowContext> group : grouped.values()) {
            ReconRowContext first = group.get(0);
            String groupKey = reconUniqueGroupKey(first.row, headerIndexMap, uniqueKeyList);
            DetailResolveResult resolveResult = resolveImportDetail(dto, groupKey, first.rowNo, first.excelDTO);
            LogisticsReconDetailEntity detail = resolveResult.getDetail();
            List<LogisticsReconDetailSubEntity> groupNewSubs = new ArrayList<>();
            List<LogisticsReconDetailSubEntity> groupUpdateSubs = new ArrayList<>();
            int seqNo = resolveResult.getNextSeqNo();
            String rowError = null;
            LogisticsReconImportExcelDTO errorExcel = first.excelDTO;
            for (ReconRowContext ctx : group) {
                errorExcel = ctx.excelDTO;
                String costNameCell = StrUtil.isBlank(costNameHeader) ? "" : readCellByHeader(ctx.row, headerIndexMap, costNameHeader);
                CfgLogisticsCostImportDetailEntity matched = costCfgList.stream()
                        .filter(costCfg -> StrUtil.equals(costCfg.getSourceDetailField(), costNameCell))
                        .findFirst().orElse(null);
                String costName = matched != null
                        ? StrUtil.blankToDefault(matched.getTargetDetailFieldName(), matched.getSourceDetailField())
                        : costNameCell;
                if (StrUtil.isBlank(costName)) {
                    rowError = "费用名称为空，无法识别费用项";
                    break;
                }
                try {
                    BigDecimal actual = actualCfg == null ? BigDecimal.ZERO
                            : parseAmount(getCellValue(ctx.row, headerIndexMap, actualCfg), false).setScale(4, RoundingMode.HALF_UP);
                    BigDecimal estimated = estimatedCfg == null ? BigDecimal.ZERO
                            : parseAmount(getCellValue(ctx.row, headerIndexMap, estimatedCfg), false).setScale(4, RoundingMode.HALF_UP);
                    ImportSubResolveResult subResult = resolveOrBuildImportSub(dto, detail, seqNo, costName, actual, estimated, rateCache);
                    if (subResult.getRateError() != null) {
                        rowError = subResult.getRateError();
                        break;
                    }
                    if (subResult.isUpdateExisting()) {
                        groupUpdateSubs.add(subResult.getSub());
                    } else {
                        groupNewSubs.add(subResult.getSub());
                        seqNo = subResult.getNextSeqNo();
                    }
                } catch (NumberFormatException e) {
                    rowError = "金额或数字字段格式不正确";
                    break;
                }
            }
            if (rowError != null) {
                errorExcel.setErrorMsg(rowError);
                errorList.add(errorExcel);
                continue;
            }
            if (groupNewSubs.isEmpty() && groupUpdateSubs.isEmpty()) {
                first.excelDTO.setErrorMsg("费用项不能为空");
                errorList.add(first.excelDTO);
                continue;
            }
            finishImportDetailSubs(dto, resolveResult, groupNewSubs, groupUpdateSubs, first.rowNo, first.excelDTO,
                    detailList, subList, updateDetailList, updateSubList, detailCostCountDelta);
        }
    }

    /**
     * 导入明细解析结果：新建或合并到已有明细。
     */
    private static class DetailResolveResult {
        private final LogisticsReconDetailEntity detail;
        private final boolean newDetail;
        private final int nextSeqNo;

        private DetailResolveResult(LogisticsReconDetailEntity detail, boolean newDetail, int nextSeqNo) {
            this.detail = detail;
            this.newDetail = newDetail;
            this.nextSeqNo = nextSeqNo;
        }

        private LogisticsReconDetailEntity getDetail() {
            return detail;
        }

        private boolean isNewDetail() {
            return newDetail;
        }

        private int getNextSeqNo() {
            return nextSeqNo;
        }
    }

    /**
     * 确保导入分组缓存已初始化。
     */
    private void ensureImportDetailCache(LogisticsReconDTO.ImportDTO dto) {
        if (dto.getImportDetailKeyMap() == null) {
            dto.setImportDetailKeyMap(new HashMap<>());
        }
        if (dto.getImportDetailMaxSeqMap() == null) {
            dto.setImportDetailMaxSeqMap(new HashMap<>());
        }
    }

    /**
     * 按识别单号分组键解析明细：已存在则合并到原明细，否则新建。
     */
    private DetailResolveResult resolveImportDetail(LogisticsReconDTO.ImportDTO dto, String groupKey, int rowNo,
                                                    LogisticsReconImportExcelDTO excelDTO) {
        ensureImportDetailCache(dto);
        String existingDetailId = dto.getImportDetailKeyMap().get(groupKey);
        if (StrUtil.isNotBlank(existingDetailId)) {
            LogisticsReconDetailEntity detail = new LogisticsReconDetailEntity();
            detail.setId(existingDetailId);
            detail.setMainId(dto.getMainId());
            detail.setCurrency(excelDTO.getCurrency());
            int nextSeqNo = dto.getImportDetailMaxSeqMap().getOrDefault(existingDetailId, 0) + 1;
            return new DetailResolveResult(detail, false, nextSeqNo);
        }
        LogisticsReconDetailEntity detail = buildReconDetail(dto, rowNo, excelDTO);
        dto.getImportDetailKeyMap().put(groupKey, detail.getId());
        dto.getImportDetailMaxSeqMap().put(detail.getId(), 0);
        return new DetailResolveResult(detail, true, 1);
    }

    /**
     * 费用项落库：新建明细入 detailList，合并明细按唯一键更新并累加新增费用项 cost_count。
     */
    private void finishImportDetailSubs(LogisticsReconDTO.ImportDTO dto, DetailResolveResult resolveResult,
                                        List<LogisticsReconDetailSubEntity> newSubs,
                                        List<LogisticsReconDetailSubEntity> updateSubs,
                                        int rowNo, LogisticsReconImportExcelDTO excelDTO,
                                        List<LogisticsReconDetailEntity> newDetailList,
                                        List<LogisticsReconDetailSubEntity> newSubList,
                                        List<LogisticsReconDetailEntity> updateDetailList,
                                        List<LogisticsReconDetailSubEntity> updateSubList,
                                        Map<String, Integer> detailCostCountDelta) {
        LogisticsReconDetailEntity detail = resolveResult.getDetail();
        if (resolveResult.isNewDetail()) {
            detail.setCostCount(newSubs.size());
            newDetailList.add(detail);
        } else {
            LogisticsReconDetailEntity updateDetail = buildReconDetail(dto, rowNo, excelDTO);
            updateDetail.setId(detail.getId());
            updateDetailList.add(updateDetail);
            if (CollUtil.isNotEmpty(newSubs)) {
                detailCostCountDelta.merge(detail.getId(), newSubs.size(), Integer::sum);
            }
        }
        int maxSeq = dto.getImportDetailMaxSeqMap().getOrDefault(detail.getId(), 0);
        if (CollUtil.isNotEmpty(newSubs)) {
            maxSeq = Math.max(maxSeq, resolveResult.getNextSeqNo() + newSubs.size() - 1);
        }
        dto.getImportDetailMaxSeqMap().put(detail.getId(), maxSeq);
        newSubList.addAll(newSubs);
        updateSubList.addAll(updateSubs);
    }

    /**
     * 导入费用项解析结果：新建或按费用名覆盖更新。
     */
    private static class ImportSubResolveResult {
        private final LogisticsReconDetailSubEntity sub;
        private final boolean updateExisting;
        private final int nextSeqNo;
        private final String rateError;

        private ImportSubResolveResult(LogisticsReconDetailSubEntity sub, boolean updateExisting,
                                       int nextSeqNo, String rateError) {
            this.sub = sub;
            this.updateExisting = updateExisting;
            this.nextSeqNo = nextSeqNo;
            this.rateError = rateError;
        }

        private LogisticsReconDetailSubEntity getSub() {
            return sub;
        }

        private boolean isUpdateExisting() {
            return updateExisting;
        }

        private int getNextSeqNo() {
            return nextSeqNo;
        }

        private String getRateError() {
            return rateError;
        }
    }

    private ImportSubResolveResult resolveOrBuildImportSub(LogisticsReconDTO.ImportDTO dto,
                                                           LogisticsReconDetailEntity detail, int seqNo,
                                                           String costName, BigDecimal actualAmount,
                                                           BigDecimal estimatedAmount,
                                                           Map<String, BigDecimal> rateCache) {
        String subKey = buildImportSubKey(detail.getId(), costName);
        if (Boolean.TRUE.equals(dto.getReimportUpdate())) {
            ensureImportDetailSubKeyMap(dto);
            String existingSubId = dto.getImportDetailSubKeyMap().get(subKey);
            if (StrUtil.isNotBlank(existingSubId)) {
                LogisticsReconDetailSubEntity sub = buildReconDetailSub(dto, detail, seqNo, costName,
                        actualAmount, estimatedAmount);
                sub.setId(existingSubId);
                String rateError = fillSubLocalAmount(sub, dto, rateCache);
                return new ImportSubResolveResult(sub, true, seqNo, rateError);
            }
        }
        LogisticsReconDetailSubEntity sub = buildReconDetailSub(dto, detail, seqNo, costName,
                actualAmount, estimatedAmount);
        String rateError = fillSubLocalAmount(sub, dto, rateCache);
        if (Boolean.TRUE.equals(dto.getReimportUpdate())) {
            ensureImportDetailSubKeyMap(dto);
            dto.getImportDetailSubKeyMap().put(subKey, sub.getId());
        }
        return new ImportSubResolveResult(sub, false, seqNo + 1, rateError);
    }

    private void ensureImportDetailSubKeyMap(LogisticsReconDTO.ImportDTO dto) {
        if (dto.getImportDetailSubKeyMap() == null) {
            dto.setImportDetailSubKeyMap(new HashMap<>());
        }
    }

    private String buildImportSubKey(String detailId, String costName) {
        return detailId + "::" + StrUtil.trim(costName);
    }

    /**
     * 查找同 Excel 待确认对账单（允许覆盖更新导入）。
     */
    private LogisticsReconEntity findPendingReimportMain(LogisticsReconDTO.ImportDTO dto,
                                                         CfgLogisticsCostImportEntity importCfg) {
        return lambdaQuery()
                .eq(LogisticsReconEntity::getReconciliationMonth, dto.getReconciliationMonth())
                .eq(LogisticsReconEntity::getFileName, dto.getFileName())
                .eq(LogisticsReconEntity::getCfgImportId, importCfg.getId())
                .eq(LogisticsReconEntity::getCheckStatus, LogisticsReconCheckStatusEnum.PENDING.getCode())
                .eq(LogisticsReconEntity::getIsDeleted, false)
                .orderByDesc(LogisticsReconEntity::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    /**
     * 重导更新：从库内预加载唯一键明细映射与费用名费用项映射。
     */
    private void initImportDetailCacheFromDb(LogisticsReconDTO.ImportDTO dto,
                                             List<CfgLogisticsCostImportDetailEntity> cfgDetails) {
        ensureImportDetailCache(dto);
        ensureImportDetailSubKeyMap(dto);
        List<CfgLogisticsCostImportDetailEntity> uniqueKeyList = cfgDetails.stream()
                .filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(uniqueKeyList)) {
            return;
        }
        String lastDetailId = null;
        while (true) {
            LambdaQueryChainWrapper<LogisticsReconDetailEntity> detailQuery = logisticsReconDetailService.lambdaQuery()
                    .eq(LogisticsReconDetailEntity::getMainId, dto.getMainId())
                    .orderByAsc(LogisticsReconDetailEntity::getId)
                    .last("LIMIT " + MATCH_ID_BATCH_SIZE);
            if (lastDetailId != null) {
                detailQuery.gt(LogisticsReconDetailEntity::getId, lastDetailId);
            }
            List<LogisticsReconDetailEntity> detailBatch = detailQuery.list();
            if (CollUtil.isEmpty(detailBatch)) {
                break;
            }
            lastDetailId = detailBatch.get(detailBatch.size() - 1).getId();
            for (LogisticsReconDetailEntity detail : detailBatch) {
                String groupKey = buildDetailGroupKey(detail, uniqueKeyList);
                if (StrUtil.isNotBlank(groupKey)) {
                    dto.getImportDetailKeyMap().putIfAbsent(groupKey, detail.getId());
                }
            }
        }
        String lastSubId = null;
        while (true) {
            LambdaQueryChainWrapper<LogisticsReconDetailSubEntity> subQuery = logisticsReconDetailSubService.lambdaQuery()
                    .eq(LogisticsReconDetailSubEntity::getMainId, dto.getMainId())
                    .orderByAsc(LogisticsReconDetailSubEntity::getId)
                    .last("LIMIT " + MATCH_ID_BATCH_SIZE);
            if (lastSubId != null) {
                subQuery.gt(LogisticsReconDetailSubEntity::getId, lastSubId);
            }
            List<LogisticsReconDetailSubEntity> subBatch = subQuery.list();
            if (CollUtil.isEmpty(subBatch)) {
                break;
            }
            lastSubId = subBatch.get(subBatch.size() - 1).getId();
            for (LogisticsReconDetailSubEntity sub : subBatch) {
                if (StrUtil.isBlank(sub.getDetailId()) || StrUtil.isBlank(sub.getCostName())) {
                    continue;
                }
                dto.getImportDetailSubKeyMap().putIfAbsent(buildImportSubKey(sub.getDetailId(), sub.getCostName()), sub.getId());
                dto.getImportDetailMaxSeqMap().merge(sub.getDetailId(),
                        sub.getSeqNo() == null ? 0 : sub.getSeqNo(), Math::max);
            }
        }
    }

    private String buildDetailGroupKey(LogisticsReconDetailEntity detail,
                                     List<CfgLogisticsCostImportDetailEntity> uniqueKeyList) {
        return uniqueKeyList.stream()
                .map(uniqueKey -> detailIdentifyValue(detail, uniqueKey.getTargetField()))
                .collect(Collectors.joining("_"));
    }

    /**
     * 计算费用项本位币金额（原币 × 本位币汇率），汇率缺失返回错误文案，否则返回 null。
     * @author Will
     * @date: 2026/06/12
     */
    private String fillSubLocalAmount(LogisticsReconDetailSubEntity sub, LogisticsReconDTO.ImportDTO dto,
                                      Map<String, BigDecimal> rateCache) {
        BigDecimal localRate = resolveLocalRate(sub.getCurrency(), dto.getReconciliationMonth(), rateCache);
        if (localRate == null) {
            return StrUtil.format("未找到币别【{}】在对账月份【{}】的汇率",
                    StrUtil.blankToDefault(sub.getCurrency(), CurrencyEnum.CNY.getCurrencyCode()),
                    dto.getReconciliationMonth());
        }
        sub.setLocalExchangeRate(localRate);
        BigDecimal actualAmount = ObjectUtil.isEmpty(sub.getActualAmount()) ? BigDecimal.ZERO : sub.getActualAmount();
        sub.setLocalAmount(actualAmount.multiply(localRate).setScale(4, RoundingMode.HALF_UP));
        return null;
    }

    /**
     * 按表头名称读取单元格值。
     * @author Will
     * @date: 2026/06/12
     */
    private String readCellByHeader(Map<Integer, String> row, Map<String, Integer> headerIndexMap, String header) {
        Integer index = headerIndexMap.get(header);
        return index == null ? "" : StrUtil.trimToEmpty(row.get(index));
    }

    /**
     * 识别单号分组键（按唯一键字段值拼接）。
     * @author Will
     * @date: 2026/06/12
     */
    private String reconUniqueGroupKey(Map<Integer, String> row, Map<String, Integer> headerIndexMap,
                                       List<CfgLogisticsCostImportDetailEntity> uniqueKeyList) {
        return uniqueKeyList.stream()
                .map(uniqueKey -> getCellValue(row, headerIndexMap, uniqueKey))
                .collect(Collectors.joining("_"));
    }

    /**
     * 纵向解析行上下文。
     */
    private static class ReconRowContext {
        private final int rowNo;
        private final Map<Integer, String> row;
        private final LogisticsReconImportExcelDTO excelDTO;

        private ReconRowContext(int rowNo, Map<Integer, String> row, LogisticsReconImportExcelDTO excelDTO) {
            this.rowNo = rowNo;
            this.row = row;
            this.excelDTO = excelDTO;
        }
    }

    /**
     * 获取单元格值
     * @author Will
     * @date: 2026/06/02
     * @param row
     * @param headerIndexMap
     * @param cfg
     * @return String
     */
    private String getCellValue(Map<Integer, String> row, Map<String, Integer> headerIndexMap,
                                CfgLogisticsCostImportDetailEntity cfg) {
        String source = StrUtil.blankToDefault(cfg.getSourceDetailField(), cfg.getSourceField());
        Integer index = headerIndexMap.get(source);
        if (index == null) {
            return "";
        }
        return StrUtil.trimToEmpty(row.get(index));
    }

    /**
     * 映射导入字段到 Excel 中间对象
     * @author Will
     * @date: 2026/06/02
     * @param excelDTO
     * @param target
     * @param value
     * @param cfg
     * @return void
     */
    private void applyImportExcelField(LogisticsReconImportExcelDTO excelDTO, String target, String value,
                                       CfgLogisticsCostImportDetailEntity cfg) {
        if ("sourceCode".equals(target) || "soCode".equals(target)) {
            excelDTO.setSoCode(value);
        } else if ("platformCode".equals(target) || "platformOrderNo".equals(target)) {
            excelDTO.setPlatformOrderNo(value);
        } else if ("trackNo".equals(target)) {
            excelDTO.setTrackNo(value);
        } else if ("transportNo".equals(target)) {
            excelDTO.setTransportNo(value);
        } else if ("soDeliveryCode".equals(target)) {
            excelDTO.setSoDeliveryCode(value);
        } else if ("currency".equals(target)) {
            excelDTO.setCurrency(value);
        } else if ("payType".equals(target)) {
            excelDTO.setPayType(value);
        } else if ("thirdActualWeight".equals(target) || "weightLogistics".equals(target)) {
            excelDTO.setWeightLogistics(value);
        } else if ("volumeWeightLogistics".equals(target)) {
            excelDTO.setVolumeWeightLogistics(value);
        } else if ("billingWeightLogistics".equals(target)) {
            excelDTO.setBillingWeightLogistics(value);
        } else if ("logisticsWeightUnit".equals(target) || "weightUnit".equals(target)) {
            excelDTO.setWeightUnit(value);
        } else if ("thirdLength".equals(target)) {
            excelDTO.setThirdLength(value);
        } else if ("thirdWidth".equals(target)) {
            excelDTO.setThirdWidth(value);
        } else if ("thirdHeight".equals(target)) {
            excelDTO.setThirdHeight(value);
        } else if ("actualAmount".equals(target) || StrUtil.isNotBlank(cfg.getTargetDetailField())) {
            excelDTO.setCostName(StrUtil.blankToDefault(cfg.getTargetDetailFieldName(), cfg.getSourceDetailField()));
            excelDTO.setActualAmount(value);
        }
    }

    /**
     * 解析金额/数字
     * @author Will
     * @date: 2026/06/02
     * @param value
     * @param absolute
     * @return BigDecimal
     */
    private BigDecimal parseAmount(String value, Boolean absolute) {
        if (StrUtil.isBlank(value)) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = new BigDecimal(value.replace(",", "").trim());
        return Boolean.TRUE.equals(absolute) ? amount.abs() : amount;
    }

    /**
     * 导出并上传错误文件
     * @author Will
     * @date: 2026/06/02
     * @param errorList
     * @return String
     */
    private String uploadErrorFile(List<LogisticsReconImportExcelDTO> errorList) {
        if (CollUtil.isEmpty(errorList)) {
            return "";
        }
        List<LogisticsReconImportExcelDTO> sortedErrorList = errorList.stream()
                .sorted(Comparator.comparingInt(item -> {
                    try {
                        return Integer.parseInt(item.getNo());
                    } catch (Exception e) {
                        return 0;
                    }
                }))
                .collect(Collectors.toList());
        String fileName = "物流商对账单导入错误信息.xlsx";
        File file = ExcelUtil.exportFile(fileName, "error", sortedErrorList, LogisticsReconImportExcelDTO.class);
        return file.isDirectory() ? "" : FastDFSClientUtil.uploadFile(file, fileName);
    }

    /**
     * 获取或创建导入主表（优先使用 importExcel 预创建的记录，兼容历史异步任务）
     * @author Will
     * @date: 2026/06/03
     * @param dto 导入参数
     * @param importCfg 导入配置
     * @param createdMainIds 本次任务涉及的主表 id 集合（兜底创建时追加）
     * @return LogisticsReconEntity
     */
    private LogisticsReconEntity resolveImportMain(LogisticsReconDTO.ImportDTO dto,
                                                   CfgLogisticsCostImportEntity importCfg,
                                                   List<String> createdMainIds) {
        String mainId = CollUtil.isNotEmpty(dto.getMainIdMap())
                ? dto.getMainIdMap().get(importCfg.getId()) : null;
        if (StrUtil.isNotBlank(mainId)) {
            LogisticsReconEntity entity = super.getById(mainId);
            if (entity != null) {
                return entity;
            }
        }
        LogisticsReconEntity entity = createImportingMain(dto, importCfg);
        if (!createdMainIds.contains(entity.getId())) {
            createdMainIds.add(entity.getId());
        }
        return entity;
    }

    /**
     * 新建导入中主表
     * @author Will
     * @date: 2026/06/02
     * @param dto 导入参数
     * @param importCfg 导入配置
     * @return LogisticsReconEntity
     */
    private LogisticsReconEntity createImportingMain(LogisticsReconDTO.ImportDTO dto,
                                                     CfgLogisticsCostImportEntity importCfg) {
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DZ);
        LogisticsReconEntity entity = new LogisticsReconEntity()
                .setCode(code)
                .setReconciliationMonth(dto.getReconciliationMonth())
                .setBusinessType(importCfg.getBusinessType())
                .setCfgImportId(importCfg.getId())
                .setCfgType(importCfg.getCfgType())
                .setSupplierId(importCfg.getDictPlatform())
                .setSupplierName(importCfg.getName())
                .setSheetName(importCfg.getSheetName())
                .setFileUrl(dto.getFileUrl())
                .setFileName(dto.getFileName())
                .setCheckStatus(LogisticsReconCheckStatusEnum.IMPORTING.getCode())
                .setImportCount(0)
                .setCostCount(0);
        boolean save = super.save(entity);
        if (!save) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_SAVE_FAILED);
        }
        return entity;
    }

    // ============================== 校验状态流转 ==============================

    @DistributeLocker(businessType = DistributeKeyConstant.TMS_LOGISTICS_RECON_KEY, keyName = "id", unlockAfterTx = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO updateCheckStatus(String id, String checkStatus) {
        LogisticsReconEntity entity = super.getByIdOpt(id)
                .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));
        validateCheckStatusTransition(entity, checkStatus);
        LoginUser user = UserContext.getDefaultLoginUser();
        boolean confirmed = LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(checkStatus);
        boolean updated = lambdaUpdate()
                .eq(LogisticsReconEntity::getId, entity.getId())
                .eq(LogisticsReconEntity::getVersion, entity.getVersion())
                .set(LogisticsReconEntity::getCheckStatus, checkStatus)
                .set(LogisticsReconEntity::getCheckUserId, confirmed ? user.getUid() : "")
                .set(LogisticsReconEntity::getCheckUserName, confirmed ? user.getUserName() : "")
                .set(LogisticsReconEntity::getCheckTime, confirmed ? LocalDateTime.now() : null)
                .update(new LogisticsReconEntity());
        if (!updated) {
            throw new ServiceException(ApiError.BILL_DATA_LOCKED);
        }
        String msg = StrUtil.format("用户【{}】将{}【{}】校验状态切换为【{}】",
                user.getUserName(), DOC_NAME, entity.getCode(),
                LogisticsReconCheckStatusEnum.getName(checkStatus));
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "校验状态切换");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    private void validateCheckStatusTransition(LogisticsReconEntity entity, String targetStatus) {
        // 导入中不允许直接切换校验状态（需等导入完成进入待确认）
        if (LogisticsReconCheckStatusEnum.IMPORTING.getCode().equals(entity.getCheckStatus())) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORTING_CHECK_STATUS_FORBIDDEN);
        }
        // 仅允许在 待确认 / 已确认 之间流转
        if (!LogisticsReconCheckStatusEnum.PENDING.getCode().equals(targetStatus)
                && !LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(targetStatus)) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_CHECK_STATUS_INVALID);
        }
        if (LogisticsReconCheckStatusEnum.PENDING.getCode().equals(targetStatus)) {
            int refCount = (int) logisticsReconRefLogisticsBillService.lambdaQuery()
                    .eq(com.erp.model.tms.entity.LogisticsReconRefLogisticsBillEntity::getMainId, entity.getId())
                    .count();
            if (refCount > 0) {
                throw new ServiceException(ApiError.LOGISTICS_RECON_MATCH_REF_EXISTS_ROLLBACK_FORBIDDEN);
            }
        }
    }

    // ============================== 合并 & 匹配 ==============================

    @Override
    public List<BatchResultDTO> batchMatch(LogisticsReconDTO.BatchMatchDTO dto) {
        // 异步：仅做校验 + 认领（置 matching）+ 提交线程池，立即返回；匹配结果异步写回 detail_sub
        LoginUser user = UserContext.getLoginUser();
        List<BatchResultDTO> results = new ArrayList<>(dto.getIds().size());
        for (String mainId : dto.getIds()) {
            try {
                LogisticsReconEntity entity = super.getByIdOpt(mainId)
                        .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));
                if (!LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(entity.getCheckStatus())) {
                    throw new ServiceException(ApiError.LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_MATCH);
                }
                // 超时兜底：重置长时间卡在匹配中的费用项，避免异步任务异常后无法再次触发
                resetStaleMatchingSubs(mainId);
                // 原子认领：未匹配/失败且未确认 → 匹配中
                if (!claimMainSubsMatching(mainId)) {
                    long matchingCount = logisticsReconDetailSubService.lambdaQuery()
                            .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                            .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                                    LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                            .count();
                    if (matchingCount > 0) {
                        results.add(BatchResultDTO.fail(entity.getId(), entity.getCode(),
                                "存在匹配中的费用项，请等待当前匹配完成"));
                    } else {
                        results.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "无待匹配的费用项"));
                    }
                    continue;
                }
                logisticsReconMatchPool.submit(() -> asyncMatchByMain(mainId, user));
                results.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "已提交匹配，请稍后查看明细匹配结果"));
            } catch (Exception e) {
                log.error("[batchMatch] 提交失败 mainId={}", mainId, e);
                results.add(BatchResultDTO.fail(mainId, mainId, e.getMessage()));
            }
        }
        return results;
    }

    /**
     * 认领对账单下未匹配/失败的费用项为"匹配中"（条件更新，避免大数据量下 IN id 集合超长）。
     * @author Will
     * @date 2026/6/12
     * @return 是否存在被认领的费用项
     */
    private boolean claimMainSubsMatching(String mainId) {
        return logisticsReconDetailSubService.lambdaUpdate()
                .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                .in(LogisticsReconDetailSubEntity::getMatchStatus,
                        LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode(),
                        LogisticsReconDetailMatchStatusEnum.FAILED.getCode())
                .ne(LogisticsReconDetailSubEntity::getReconciliationStatus,
                        LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode())
                .set(LogisticsReconDetailSubEntity::getMatchStatus,
                        LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                .set(LogisticsReconDetailSubEntity::getMatchFailReason, "")
                .update(new LogisticsReconDetailSubEntity());
    }

    /**
     * 异步执行对账单整批匹配：传递登录人上下文，失败时把仍处于匹配中的费用项回写失败。
     * @author Will
     * @date 2026/6/12
     */
    private void asyncMatchByMain(String mainId, LoginUser user) {
        LoginUser prev = UserContext.getLoginUser();
        try {
            UserContext.setLoginUser(user);
            self.doMatchByMain(mainId);
        } catch (Exception e) {
            log.error("[asyncMatchByMain] 匹配失败 mainId={}", mainId, e);
            try {
                self.markReconMatchFailed(mainId, null, resolveMatchFailReason(e));
            } catch (Exception ex) {
                log.error("[asyncMatchByMain] 回写失败状态异常 mainId={}", mainId, ex);
            }
        } finally {
            if (prev != null) {
                UserContext.setLoginUser(prev);
            } else {
                UserContext.clear();
            }
        }
    }

    /**
     * 截断匹配失败原因，避免超过字段长度。
     * @author Will
     * @date 2026/6/12
     */
    private String resolveMatchFailReason(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        return msg.length() > 490 ? msg.substring(0, 490) : msg;
    }

    @Override
    public void doMatchByMain(String mainId) {
        LogisticsReconEntity entity = super.getByIdOpt(mainId)
                .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));
        if (!LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(entity.getCheckStatus())) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_MATCH);
        }
        // 游标分片处理匹配中费用项，每片独立小事务，避免整单 30 万条单事务 OOM / 超时
        String lastId = null;
        while (true) {
            LambdaQueryChainWrapper<LogisticsReconDetailSubEntity> query =
                    logisticsReconDetailSubService.lambdaQuery()
                    .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                    .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                            LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                    .orderByAsc(LogisticsReconDetailSubEntity::getId)
                    .last("LIMIT " + MATCH_CHUNK_SIZE);
            if (lastId != null) {
                query.gt(LogisticsReconDetailSubEntity::getId, lastId);
            }
            List<LogisticsReconDetailSubEntity> subBatch = query.list();
            if (CollUtil.isEmpty(subBatch)) {
                break;
            }
            lastId = subBatch.get(subBatch.size() - 1).getId();
            List<String> subIds = subBatch.stream()
                    .map(LogisticsReconDetailSubEntity::getId)
                    .collect(Collectors.toList());
            self.doMatchSubsChunk(mainId, subIds);
        }
    }

    @DistributeLocker(businessType = DistributeKeyConstant.TMS_LOGISTICS_RECON_KEY, keyName = "mainId", unlockAfterTx = true)
    @Override
    public void doMatchSubsChunk(String mainId, List<String> detailSubIds) {
        if (CollUtil.isEmpty(detailSubIds)) {
            return;
        }
        touchMatchingSubsUpdateTime(detailSubIds);
        LogisticsReconEntity entity = super.getByIdOpt(mainId)
                .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));
        List<LogisticsReconDetailSubEntity> subList = logisticsReconDetailSubService.lambdaQuery()
                .in(LogisticsReconDetailSubEntity::getId, detailSubIds)
                .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                        LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                .list();
        if (CollUtil.isEmpty(subList)) {
            return;
        }
        Set<String> detailIds = subList.stream()
                .map(LogisticsReconDetailSubEntity::getDetailId)
                .collect(Collectors.toSet());
        Map<String, LogisticsReconDetailEntity> detailMap = logisticsReconDetailService.lambdaQuery()
                .in(LogisticsReconDetailEntity::getId, detailIds)
                .list().stream()
                .collect(Collectors.toMap(LogisticsReconDetailEntity::getId, detail -> detail, (a, b) -> a));
        List<ReconMatchUnit> units = new ArrayList<>(subList.size());
        for (LogisticsReconDetailSubEntity sub : subList) {
            LogisticsReconDetailEntity detail = detailMap.get(sub.getDetailId());
            if (detail != null) {
                units.add(new ReconMatchUnit(sub.getId(), detail, sub, null));
            }
        }
        MatchExecutionResult executionResult = executeReconMatch(entity, units,
                LogisticsReconRefMatchTypeEnum.AUTO.getCode(), false, false);
        self.commitReconMatchResult(mainId, LogisticsReconRefMatchTypeEnum.AUTO.getCode(),
                executionResult.getRowKeyToDetailId(), executionResult.getRowKeyToSubs(),
                executionResult.getMatchResults());
    }

    /**
     * 刷新匹配中费用项的更新时间，避免长任务被 resetStaleMatchingSubs 误判超时。
     */
    private void touchMatchingSubsUpdateTime(List<String> detailSubIds) {
        for (int i = 0; i < detailSubIds.size(); i += MATCH_ID_BATCH_SIZE) {
            List<String> batch = detailSubIds.subList(i, Math.min(detailSubIds.size(), i + MATCH_ID_BATCH_SIZE));
            logisticsReconDetailSubService.lambdaUpdate()
                    .in(LogisticsReconDetailSubEntity::getId, batch)
                    .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                            LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                    .update(new LogisticsReconDetailSubEntity());
        }
    }

    /**
     * 重置长时间处于匹配中且未更新的费用项（异步任务异常兜底）。
     */
    private void resetStaleMatchingSubs(String mainId) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(MATCHING_STALE_MINUTES);
        logisticsReconDetailSubService.lambdaUpdate()
                .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                        LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                .lt(LogisticsReconDetailSubEntity::getUpdateTime, threshold)
                .set(LogisticsReconDetailSubEntity::getMatchStatus,
                        LogisticsReconDetailMatchStatusEnum.FAILED.getCode())
                .set(LogisticsReconDetailSubEntity::getMatchFailReason, "匹配超时，请重新匹配")
                .update(new LogisticsReconDetailSubEntity());
    }

    /**
     * 三个匹配入口（主表整批 / 手动 / 导入）共用的核心编排：
     * 加载导入模板配置 → 构造匹配上下文 → 调用 reconMatchAndGenerate 生成物流费用 → 回写 detail_sub 状态与 ref。
     * 三者差异仅在“待匹配数据怎么来 + 识别号来源 + 是否要求唯一 + matchType”，均由入参 units / 标志位表达。
     * @author Will
     * @date 2026/6/12
     * @param entity 对账单主表
     * @param units 待匹配单元（费用项 + 所属明细 + 识别号覆盖，override=null 表示取明细三方单号）
     * @param matchType 关联匹配类型（auto / manual）
     * @param requireUnique 是否要求识别号唯一命中一张物流单
     * @param matchByProvided 是否按 units 提供的识别字段匹配（手动/导入为 true）
     * @return 逐单元匹配结果
     */
    /**
     * 匹配编排结果（Feign 匹配与结果落库分离）。
     */
    private static class MatchExecutionResult {
        private final List<LogisticsReconMatchDTO.MatchResultDTO> matchResults;
        private final Map<String, String> rowKeyToDetailId;
        private final Map<String, List<LogisticsReconDetailSubEntity>> rowKeyToSubs;

        private MatchExecutionResult(List<LogisticsReconMatchDTO.MatchResultDTO> matchResults,
                                     Map<String, String> rowKeyToDetailId,
                                     Map<String, List<LogisticsReconDetailSubEntity>> rowKeyToSubs) {
            this.matchResults = matchResults;
            this.rowKeyToDetailId = rowKeyToDetailId;
            this.rowKeyToSubs = rowKeyToSubs;
        }

        private List<LogisticsReconMatchDTO.MatchResultDTO> getMatchResults() {
            return matchResults;
        }

        private Map<String, String> getRowKeyToDetailId() {
            return rowKeyToDetailId;
        }

        private Map<String, List<LogisticsReconDetailSubEntity>> getRowKeyToSubs() {
            return rowKeyToSubs;
        }
    }

    private static final List<String> MATCH_CLAIM_FROM_STATUSES = Arrays.asList(
            LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode(),
            LogisticsReconDetailMatchStatusEnum.FAILED.getCode());

    private static final List<String> MATCHING_FROM_STATUS = Collections.singletonList(
            LogisticsReconDetailMatchStatusEnum.MATCHING.getCode());

    private MatchExecutionResult executeReconMatch(LogisticsReconEntity entity,
                                                   List<ReconMatchUnit> units,
                                                   String matchType,
                                                   boolean requireUnique,
                                                   boolean matchByProvided) {
        if (CollUtil.isEmpty(units)) {
            return new MatchExecutionResult(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
        }
        CfgLogisticsCostImportEntity costImportEntity = cfgLogisticsCostImportService.getById(entity.getCfgImportId());
        if (ObjectUtil.isEmpty(costImportEntity)) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORT_TEMPLATE_NOT_RECOGNIZED);
        }
        List<CfgLogisticsCostImportDetailEntity> cfgDetails =
                cfgLogisticsCostImportDetailService.listByMainIdList(Collections.singletonList(costImportEntity.getId()));
        if (CollUtil.isEmpty(cfgDetails)) {
            throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
        }
        List<CfgLogisticsCostImportDetailEntity> uniqueKeyList = cfgDetails.stream()
                .filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey)
                .collect(Collectors.toList());

        // 行 = 单个费用项；按 detail_sub 粒度覆盖 ref，避免误删同明细下已匹配费用项
        List<LogisticsReconMatchDTO.MatchRowDTO> rows = new ArrayList<>(units.size());
        Map<String, String> rowKeyToDetailId = new HashMap<>();
        Map<String, List<LogisticsReconDetailSubEntity>> rowKeyToSubs = new HashMap<>();
        for (ReconMatchUnit unit : units) {
            rows.add(buildMatchRow(unit.rowKey, unit.detail, Collections.singletonList(unit.sub),
                    uniqueKeyList, unit.identifyOverride));
            rowKeyToDetailId.put(unit.rowKey, unit.detail.getId());
            rowKeyToSubs.put(unit.rowKey, Collections.singletonList(unit.sub));
        }

        LogisticsReconMatchDTO.MatchContextDTO ctx = new LogisticsReconMatchDTO.MatchContextDTO();
        ctx.setCostImportEntity(costImportEntity);
        ctx.setCfgImportDetailList(cfgDetails);
        ctx.setReconciliationMonth(entity.getReconciliationMonth());
        ctx.setProcessingType(ImportHistoryRecordProcessingTypeEnum.IMPORT.getCode());
        ctx.setRequireUnique(requireUnique);
        ctx.setMatchByProvidedIdentifyKeys(matchByProvided);
        ctx.setRows(rows);

        List<LogisticsReconMatchDTO.MatchResultDTO> matchResults = importHistoryRecordService.reconMatchAndGenerate(ctx);
        return new MatchExecutionResult(matchResults, rowKeyToDetailId, rowKeyToSubs);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void commitReconMatchResult(String mainId, String matchType,
                                       Map<String, String> rowKeyToDetailId,
                                       Map<String, List<LogisticsReconDetailSubEntity>> rowKeyToSubs,
                                       List<LogisticsReconMatchDTO.MatchResultDTO> matchResults) {
        writeReconMatchResult(mainId, matchType, rowKeyToDetailId, rowKeyToSubs, matchResults);
    }

    /**
     * 待匹配单元：费用项 + 所属明细 + 识别号覆盖（null 表示取明细三方单号）。
     */
    private static class ReconMatchUnit {
        private final String rowKey;
        private final LogisticsReconDetailEntity detail;
        private final LogisticsReconDetailSubEntity sub;
        private final Map<String, String> identifyOverride;

        private ReconMatchUnit(String rowKey, LogisticsReconDetailEntity detail,
                               LogisticsReconDetailSubEntity sub, Map<String, String> identifyOverride) {
            this.rowKey = rowKey;
            this.detail = detail;
            this.sub = sub;
            this.identifyOverride = identifyOverride;
        }
    }

    /**
     * 把仍处于"匹配中"的费用项回写为匹配失败（异步任务异常兜底，避免状态卡在匹配中）。
     * @author Will
     * @date 2026/6/12
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void markReconMatchFailed(String mainId, List<String> detailSubIds, String reason) {
        // 主表整批兜底：按 mainId + 匹配中 条件更新，避免大数据量下构造超长 id 集合
        if (CollUtil.isEmpty(detailSubIds)) {
            logisticsReconDetailSubService.lambdaUpdate()
                    .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                    .eq(LogisticsReconDetailSubEntity::getMatchStatus, LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                    .set(LogisticsReconDetailSubEntity::getMatchStatus, LogisticsReconDetailMatchStatusEnum.FAILED.getCode())
                    .set(LogisticsReconDetailSubEntity::getMatchFailReason, reason)
                    .update(new LogisticsReconDetailSubEntity());
            return;
        }
        // 指定费用项（手动匹配，量小）：仅回写仍处于匹配中的，按 id 分片更新
        List<String> ids = logisticsReconDetailSubService.lambdaQuery()
                .in(LogisticsReconDetailSubEntity::getId, detailSubIds)
                .eq(LogisticsReconDetailSubEntity::getMatchStatus, LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                .list().stream().map(LogisticsReconDetailSubEntity::getId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(ids)) {
            logisticsReconDetailSubService.batchUpdateMatchStatus(ids,
                    LogisticsReconDetailMatchStatusEnum.FAILED.getCode(), reason, MATCHING_FROM_STATUS);
        }
    }

    /**
     * 提交手动匹配异步任务：按对账单分组，认领（置 matching）后提交线程池，立即返回。
     * @author Will
     * @date 2026/6/12
     */
    @Override
    public List<BatchResultDTO> submitManualMatch(List<LogisticsReconMatchDTO.SubErpInputDTO> inputs, String matchType) {
        LoginUser user = UserContext.getLoginUser();
        List<String> detailSubIds = inputs.stream()
                .map(LogisticsReconMatchDTO.SubErpInputDTO::getDetailSubId)
                .distinct().collect(Collectors.toList());
        Map<String, LogisticsReconDetailSubEntity> subMap = logisticsReconDetailSubService.lambdaQuery()
                .in(LogisticsReconDetailSubEntity::getId, detailSubIds)
                .list().stream()
                .collect(Collectors.toMap(LogisticsReconDetailSubEntity::getId, s -> s, (a, b) -> a));

        Map<String, List<LogisticsReconMatchDTO.SubErpInputDTO>> inputsByMain = new LinkedHashMap<>();
        Map<String, List<String>> claimByMain = new LinkedHashMap<>();
        List<BatchResultDTO> results = new ArrayList<>(inputs.size());
        for (LogisticsReconMatchDTO.SubErpInputDTO input : inputs) {
            String detailSubId = input.getDetailSubId();
            LogisticsReconDetailSubEntity sub = subMap.get(detailSubId);
            if (sub == null) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "对账费用项不存在"));
                continue;
            }
            if (LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode().equals(sub.getReconciliationStatus())) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "费用项已确认，不允许匹配更新"));
                continue;
            }
            if (LogisticsReconDetailMatchStatusEnum.MATCHING.getCode().equals(sub.getMatchStatus())) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "费用项匹配进行中，请稍后"));
                continue;
            }
            if (LogisticsReconDetailMatchStatusEnum.MATCHED.getCode().equals(sub.getMatchStatus())) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "费用项已匹配，请先解绑后再匹配"));
                continue;
            }
            if (!LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode().equals(sub.getMatchStatus())
                    && !LogisticsReconDetailMatchStatusEnum.FAILED.getCode().equals(sub.getMatchStatus())) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "费用项状态不允许匹配"));
                continue;
            }
            inputsByMain.computeIfAbsent(sub.getMainId(), key -> new ArrayList<>()).add(input);
            claimByMain.computeIfAbsent(sub.getMainId(), key -> new ArrayList<>()).add(detailSubId);
        }
        for (Map.Entry<String, List<LogisticsReconMatchDTO.SubErpInputDTO>> entry : inputsByMain.entrySet()) {
            String mainId = entry.getKey();
            List<LogisticsReconMatchDTO.SubErpInputDTO> mainInputs = entry.getValue();
            List<String> claimIds = claimByMain.get(mainId);
            logisticsReconDetailSubService.batchUpdateMatchStatus(claimIds,
                    LogisticsReconDetailMatchStatusEnum.MATCHING.getCode(), null, MATCH_CLAIM_FROM_STATUSES);
            logisticsReconMatchPool.submit(() -> asyncMatchDetailSubsByErp(mainId, mainInputs, matchType, claimIds, user));
            for (LogisticsReconMatchDTO.SubErpInputDTO input : mainInputs) {
                results.add(BatchResultDTO.success(input.getDetailSubId(), input.getDetailSubId(), "已提交匹配，请稍后查看匹配结果"));
            }
        }
        return results;
    }

    /**
     * 异步执行手动匹配：传递登录人上下文，失败时把认领的费用项回写失败。
     * @author Will
     * @date 2026/6/12
     */
    private void asyncMatchDetailSubsByErp(String mainId, List<LogisticsReconMatchDTO.SubErpInputDTO> inputs,
                                           String matchType, List<String> claimIds, LoginUser user) {
        LoginUser prev = UserContext.getLoginUser();
        try {
            UserContext.setLoginUser(user);
            self.matchDetailSubsByErp(mainId, inputs, matchType);
        } catch (Exception e) {
            log.error("[asyncMatchDetailSubsByErp] 匹配失败 mainId={}", mainId, e);
            try {
                self.markReconMatchFailed(mainId, claimIds, resolveMatchFailReason(e));
            } catch (Exception ex) {
                log.error("[asyncMatchDetailSubsByErp] 回写失败状态异常 mainId={}", mainId, ex);
            }
        } finally {
            if (prev != null) {
                UserContext.setLoginUser(prev);
            } else {
                UserContext.clear();
            }
        }
    }

    @DistributeLocker(businessType = DistributeKeyConstant.TMS_LOGISTICS_RECON_KEY, keyName = "mainId", unlockAfterTx = true)
    @Override
    public List<BatchResultDTO> matchDetailSubsByErp(String mainId, List<LogisticsReconMatchDTO.SubErpInputDTO> inputs, String matchType) {
        List<BatchResultDTO> results = new ArrayList<>(inputs.size());
        LogisticsReconEntity entity = super.getByIdOpt(mainId)
                .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));

        List<String> detailSubIds = inputs.stream()
                .map(LogisticsReconMatchDTO.SubErpInputDTO::getDetailSubId)
                .distinct().collect(Collectors.toList());
        Map<String, LogisticsReconDetailSubEntity> subMap = logisticsReconDetailSubService.lambdaQuery()
                .in(LogisticsReconDetailSubEntity::getId, detailSubIds)
                .list().stream()
                .collect(Collectors.toMap(LogisticsReconDetailSubEntity::getId, s -> s, (a, b) -> a));
        Set<String> relatedDetailIds = subMap.values().stream()
                .map(LogisticsReconDetailSubEntity::getDetailId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, LogisticsReconDetailEntity> detailMap = CollUtil.isEmpty(relatedDetailIds)
                ? Collections.emptyMap()
                : logisticsReconDetailService.lambdaQuery()
                .in(LogisticsReconDetailEntity::getId, relatedDetailIds)
                .list().stream()
                .collect(Collectors.toMap(LogisticsReconDetailEntity::getId, d -> d, (a, b) -> a));

        // 识别单号取用户输入的 ERP 单号；逐条做前置校验，组装待匹配单元
        List<ReconMatchUnit> units = new ArrayList<>(inputs.size());
        for (LogisticsReconMatchDTO.SubErpInputDTO input : inputs) {
            String detailSubId = input.getDetailSubId();
            LogisticsReconDetailSubEntity sub = subMap.get(detailSubId);
            if (sub == null || !mainId.equals(sub.getMainId())) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "对账费用项不存在"));
                continue;
            }
            // 校验状态：已确认的费用项不允许匹配更新
            if (LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode().equals(sub.getReconciliationStatus())) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "费用项已确认，不允许匹配更新"));
                continue;
            }
            if (LogisticsReconDetailMatchStatusEnum.MATCHED.getCode().equals(sub.getMatchStatus())) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "费用项已匹配，请先解绑后再匹配"));
                continue;
            }
            if (!LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode().equals(sub.getMatchStatus())
                    && !LogisticsReconDetailMatchStatusEnum.FAILED.getCode().equals(sub.getMatchStatus())
                    && !LogisticsReconDetailMatchStatusEnum.MATCHING.getCode().equals(sub.getMatchStatus())) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "费用项状态不允许匹配"));
                continue;
            }
            LogisticsReconDetailEntity detail = detailMap.get(sub.getDetailId());
            if (detail == null) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "对账明细不存在"));
                continue;
            }
            Map<String, String> identifyOverride = buildErpIdentifyOverride(input);
            if (identifyOverride.isEmpty()) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "请至少填写一个 ERP 业务单号"));
                continue;
            }
            units.add(new ReconMatchUnit(detailSubId, detail, sub, identifyOverride));
        }

        // 复用统一核心编排（手动/导入：按提供的 ERP 识别号、要求唯一）；Feign 匹配与落库分离
        MatchExecutionResult executionResult = executeReconMatch(entity, units, matchType, true, true);
        self.commitReconMatchResult(mainId, matchType, executionResult.getRowKeyToDetailId(),
                executionResult.getRowKeyToSubs(), executionResult.getMatchResults());
        writeErpSnapshot(inputs, executionResult.getMatchResults());
        for (LogisticsReconMatchDTO.MatchResultDTO matchResult : executionResult.getMatchResults()) {
            if (matchResult.isSuccess()) {
                results.add(BatchResultDTO.success(matchResult.getRowKey(), matchResult.getRowKey(), OperationTypeEnum.UPDATE));
            } else {
                results.add(BatchResultDTO.fail(matchResult.getRowKey(), matchResult.getRowKey(), matchResult.getFailReason()));
            }
        }
        return results;
    }

    /**
     * 构造匹配行：识别单号优先取 override（手动/导入匹配的 ERP 单号），否则取明细三方单号。
     * @author Will
     * @date 2026/6/11
     */
    private LogisticsReconMatchDTO.MatchRowDTO buildMatchRow(String rowKey,
                                                             LogisticsReconDetailEntity detail,
                                                             List<LogisticsReconDetailSubEntity> subs,
                                                             List<CfgLogisticsCostImportDetailEntity> uniqueKeyList,
                                                             Map<String, String> identifyOverride) {
        LogisticsReconMatchDTO.MatchRowDTO row = new LogisticsReconMatchDTO.MatchRowDTO();
        row.setRowKey(rowKey);
        row.setPayType(detail.getPayType());
        row.setCurrency(detail.getCurrency());
        row.setBillingWeightLogistics(detail.getBillingWeightLogistics());
        row.setThirdActualWeight(detail.getWeightLogistics());
        row.setWeightUnit(detail.getWeightUnit());
        row.setThirdLength(detail.getThirdLength());
        row.setThirdWidth(detail.getThirdWidth());
        row.setThirdHeight(detail.getThirdHeight());
        Map<String, String> identifyValues = new HashMap<>();
        if (identifyOverride != null) {
            identifyOverride.forEach((field, value) -> {
                if (StrUtil.isNotBlank(field) && StrUtil.isNotBlank(value)) {
                    identifyValues.put(field, value);
                }
            });
        } else {
            for (CfgLogisticsCostImportDetailEntity uniqueKey : uniqueKeyList) {
                String field = uniqueKey.getTargetField();
                if (StrUtil.isBlank(field)) {
                    continue;
                }
                String value = detailIdentifyValue(detail, field);
                if (StrUtil.isNotBlank(value)) {
                    identifyValues.put(field, value);
                }
            }
        }
        row.setIdentifyValues(identifyValues);
        List<LogisticsReconMatchDTO.MatchCostItemDTO> costItems = subs.stream().map(sub -> {
            LogisticsReconMatchDTO.MatchCostItemDTO item = new LogisticsReconMatchDTO.MatchCostItemDTO();
            item.setDetailSubId(sub.getId());
            item.setCfgCostId(sub.getCfgCostId());
            item.setCostName(StrUtil.blankToDefault(sub.getCfgCostName(), sub.getCostName()));
            item.setActualAmount(sub.getActualAmount());
            item.setEstimatedAmount(sub.getEstimatedAmount());
            return item;
        }).collect(Collectors.toList());
        row.setCostItems(costItems);
        return row;
    }

    /**
     * 取明细对应识别字段值（targetField 对齐 LogisticsBillVo 字段）。
     * @author Will
     * @date 2026/6/11
     */
    private String detailIdentifyValue(LogisticsReconDetailEntity detail, String targetField) {
        switch (targetField) {
            case "sourceCode":
                return detail.getSoCode();
            case "platformCode":
                return detail.getPlatformOrderNo();
            case "trackNo":
                return detail.getTrackNo();
            case "transportNo":
                return detail.getTransportNo();
            case "soDeliveryCode":
                return detail.getSoDeliveryCode();
            default:
                return null;
        }
    }

    /**
     * ERP 单号 → 识别字段（targetField）覆盖映射，仅保留模板唯一键覆盖到的字段。
     * @author Will
     * @date 2026/6/11
     */
    private Map<String, String> buildErpIdentifyOverride(LogisticsReconMatchDTO.SubErpInputDTO input) {
        Map<String, String> override = new HashMap<>();
        if (StrUtil.isNotBlank(input.getErpSoCode())) {
            override.put("sourceCode", input.getErpSoCode().trim());
        }
        if (StrUtil.isNotBlank(input.getErpPlatformOrderNo())) {
            override.put("platformCode", input.getErpPlatformOrderNo().trim());
        }
        if (StrUtil.isNotBlank(input.getErpTrackNo())) {
            override.put("trackNo", input.getErpTrackNo().trim());
        }
        if (StrUtil.isNotBlank(input.getErpSoDeliveryCode())) {
            override.put("soDeliveryCode", input.getErpSoDeliveryCode().trim());
        }
        return override;
    }

    /**
     * 匹配成功后回填明细 ERP 单号快照（用于第三方/ERP 对照展示）。
     * @author Will
     * @date 2026/6/11
     */
    private void writeErpSnapshot(List<LogisticsReconMatchDTO.SubErpInputDTO> inputs,
                                  List<LogisticsReconMatchDTO.MatchResultDTO> matchResults) {
        Map<String, Boolean> successMap = matchResults.stream()
                .collect(Collectors.toMap(LogisticsReconMatchDTO.MatchResultDTO::getRowKey,
                        LogisticsReconMatchDTO.MatchResultDTO::isSuccess, (a, b) -> a));
        List<String> successSubIds = inputs.stream()
                .filter(input -> Boolean.TRUE.equals(successMap.get(input.getDetailSubId())))
                .map(LogisticsReconMatchDTO.SubErpInputDTO::getDetailSubId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(successSubIds)) {
            return;
        }
        Map<String, LogisticsReconDetailSubEntity> subEntityMap = new HashMap<>();
        for (int i = 0; i < successSubIds.size(); i += MATCH_ID_BATCH_SIZE) {
            List<String> batch = successSubIds.subList(i, Math.min(successSubIds.size(), i + MATCH_ID_BATCH_SIZE));
            logisticsReconDetailSubService.lambdaQuery()
                    .in(LogisticsReconDetailSubEntity::getId, batch)
                    .list()
                    .forEach(sub -> subEntityMap.put(sub.getId(), sub));
        }
        Map<String, LogisticsReconMatchDTO.SubErpInputDTO> detailSnapshotMap = new HashMap<>();
        for (LogisticsReconMatchDTO.SubErpInputDTO input : inputs) {
            if (!Boolean.TRUE.equals(successMap.get(input.getDetailSubId()))) {
                continue;
            }
            LogisticsReconDetailSubEntity sub = subEntityMap.get(input.getDetailSubId());
            if (sub == null || StrUtil.isBlank(sub.getDetailId())) {
                continue;
            }
            detailSnapshotMap.putIfAbsent(sub.getDetailId(), input);
        }
        for (Map.Entry<String, LogisticsReconMatchDTO.SubErpInputDTO> entry : detailSnapshotMap.entrySet()) {
            LogisticsReconMatchDTO.SubErpInputDTO input = entry.getValue();
            logisticsReconDetailService.lambdaUpdate()
                    .eq(LogisticsReconDetailEntity::getId, entry.getKey())
                    .set(LogisticsReconDetailEntity::getErpSoCode, input.getErpSoCode())
                    .set(LogisticsReconDetailEntity::getErpPlatformOrderNo, input.getErpPlatformOrderNo())
                    .set(LogisticsReconDetailEntity::getErpTrackNo, input.getErpTrackNo())
                    .set(LogisticsReconDetailEntity::getErpSoDeliveryCode, input.getErpSoDeliveryCode())
                    .update(new LogisticsReconDetailEntity());
        }
    }

    /**
     * 回写匹配结果：成功行写关联关系并置费用项 matched，失败行置 failed + 失败原因。
     * 行键即 detailId 时按 detail 整批覆盖关联关系，否则（按费用项匹配）按 detail_sub 覆盖。
     * @author Will
     * @date 2026/6/11
     */
    private void writeReconMatchResult(String mainId, String matchType,
                                       Map<String, String> rowKeyToDetailId,
                                       Map<String, List<LogisticsReconDetailSubEntity>> rowKeyToSubs,
                                       List<LogisticsReconMatchDTO.MatchResultDTO> matchResults) {
        // rowKey 即 detailId 时按 detail 整批覆盖关联关系，否则（按费用项匹配）按 detail_sub 覆盖
        boolean byDetail = rowKeyToDetailId.entrySet().stream()
                .allMatch(e -> StrUtil.equals(e.getKey(), e.getValue()));
        Map<String, LogisticsReconMatchDTO.MatchResultDTO> resultMap = matchResults.stream()
                .collect(Collectors.toMap(LogisticsReconMatchDTO.MatchResultDTO::getRowKey, r -> r, (a, b) -> a));
        List<LogisticsReconRefLogisticsBillEntity> refList = new ArrayList<>();
        List<String> matchedSubIds = new ArrayList<>();
        for (Map.Entry<String, List<LogisticsReconDetailSubEntity>> entry : rowKeyToSubs.entrySet()) {
            String rowKey = entry.getKey();
            String detailId = rowKeyToDetailId.get(rowKey);
            List<LogisticsReconDetailSubEntity> subs = entry.getValue();
            LogisticsReconMatchDTO.MatchResultDTO result = resultMap.get(rowKey);
            if (result == null) {
                continue;
            }
            if (!result.isSuccess()) {
                logisticsReconDetailSubService.batchUpdateMatchStatus(
                        subs.stream().map(LogisticsReconDetailSubEntity::getId).collect(Collectors.toList()),
                        LogisticsReconDetailMatchStatusEnum.FAILED.getCode(), result.getFailReason(),
                        MATCHING_FROM_STATUS);
                continue;
            }
            for (LogisticsReconDetailSubEntity sub : subs) {
                boolean subMatched = false;
                for (LogisticsReconMatchDTO.BillRefDTO billRef : result.getBillRefs()) {
                    if (StrUtil.isNotBlank(billRef.getDetailSubId()) && !StrUtil.equals(billRef.getDetailSubId(), sub.getId())) {
                        continue;
                    }
                    LogisticsReconRefLogisticsBillEntity ref = new LogisticsReconRefLogisticsBillEntity()
                            .setMainId(mainId)
                            .setDetailId(detailId)
                            .setDetailSubId(sub.getId())
                            .setLogisticsBillId(billRef.getLogisticsBillId())
                            .setLogisticsBillDetailId(billRef.getLogisticsBillDetailId())
                            .setLogisticsBillCostId(billRef.getLogisticsBillCostId())
                            .setTmsCostDetailId(billRef.getTmsCostDetailId())
                            .setMatchType(matchType)
                            .setImportType(result.getImportType())
                            .setReconciliationStatus(billRef.getReconciliationStatus());
                    refList.add(ref);
                    subMatched = true;
                }
                if (subMatched) {
                    matchedSubIds.add(sub.getId());
                }
            }
        }
        if (byDetail) {
            logisticsReconRefLogisticsBillService.saveBatchByDetail(refList);
        } else {
            logisticsReconRefLogisticsBillService.saveBatchByDetailSub(refList);
        }
        logisticsReconDetailSubService.batchUpdateMatchStatus(matchedSubIds,
                LogisticsReconDetailMatchStatusEnum.MATCHED.getCode(), null, MATCHING_FROM_STATUS);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void confirmBillRefCostBatch(String mainId, List<String> batchCostIds, String reconciliationStatus,
                                        LocalDateTime confirmTime) {
        if (CollUtil.isEmpty(batchCostIds)) {
            return;
        }
        logisticsBillCostService.batchUpdateReconciliationStatus(batchCostIds, reconciliationStatus, confirmTime);
        for (int i = 0; i < batchCostIds.size(); i += MATCH_ID_BATCH_SIZE) {
            List<String> costIdBatch = batchCostIds.subList(i,
                    Math.min(batchCostIds.size(), i + MATCH_ID_BATCH_SIZE));
            logisticsReconRefLogisticsBillService.lambdaUpdate()
                    .eq(LogisticsReconRefLogisticsBillEntity::getMainId, mainId)
                    .in(LogisticsReconRefLogisticsBillEntity::getLogisticsBillCostId, costIdBatch)
                    .set(LogisticsReconRefLogisticsBillEntity::getReconciliationStatus, reconciliationStatus)
                    .update(new LogisticsReconRefLogisticsBillEntity());
        }
    }

    @DistributeLocker(businessType = DistributeKeyConstant.TMS_LOGISTICS_RECON_KEY, keyName = "mainId", unlockAfterTx = true)
    @Override
    public BatchResultDTO confirmBill(String mainId, String reconciliationStatus, LocalDateTime confirmTime) {
        if (!ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(reconciliationStatus)
                && !ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus)) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_RECONCILIATION_STATUS_INVALID);
        }
        LocalDateTime effectiveConfirmTime = ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus)
                ? confirmTime : null;
        LogisticsReconEntity entity = super.getByIdOpt(mainId)
                .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));
        if (!LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(entity.getCheckStatus())) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_BILL_CONFIRM);
        }
        boolean hasBillCost = false;
        String lastRefId = null;
        while (true) {
            LambdaQueryChainWrapper<LogisticsReconRefLogisticsBillEntity> refQuery =
                    logisticsReconRefLogisticsBillService.lambdaQuery()
                            .eq(LogisticsReconRefLogisticsBillEntity::getMainId, mainId)
                            .eq(LogisticsReconRefLogisticsBillEntity::getIsDeleted, false)
                            .select(LogisticsReconRefLogisticsBillEntity::getId,
                                    LogisticsReconRefLogisticsBillEntity::getLogisticsBillCostId)
                            .orderByAsc(LogisticsReconRefLogisticsBillEntity::getId)
                            .last("LIMIT " + MATCH_ID_BATCH_SIZE);
            if (lastRefId != null) {
                refQuery.gt(LogisticsReconRefLogisticsBillEntity::getId, lastRefId);
            }
            List<LogisticsReconRefLogisticsBillEntity> refBatch = refQuery.list();
            if (CollUtil.isEmpty(refBatch)) {
                break;
            }
            lastRefId = refBatch.get(refBatch.size() - 1).getId();
            List<String> batchCostIds = refBatch.stream()
                    .map(LogisticsReconRefLogisticsBillEntity::getLogisticsBillCostId)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(batchCostIds)) {
                continue;
            }
            hasBillCost = true;
            self.confirmBillRefCostBatch(mainId, batchCostIds, reconciliationStatus, effectiveConfirmTime);
        }
        if (!hasBillCost) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_MATCHED_BILL_COST_NOT_FOUND);
        }
        refreshDetailSubReconciliationStatus(mainId);
        String msg = StrUtil.format("用户【{}】将{}【{}】关联物流费用单对账状态更新为【{}】",
                UserContext.getDefaultLoginUser().getUserName(), DOC_NAME, entity.getCode(),
                ReconciliationStatusEnum.getName(reconciliationStatus));
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "账单确认");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    @DistributeLocker(businessType = DistributeKeyConstant.TMS_LOGISTICS_RECON_KEY, keyName = "dto.mainId", unlockAfterTx = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchUnbindMatch(LogisticsReconDTO.BatchUnbindMatchDTO dto) {
        // 按 detail_sub 维度逻辑删 ref + 还原 detail_sub.match_status = unmatched（内部均已分片）
        logisticsReconRefLogisticsBillService.removeByDetailSubIds(dto.getDetailSubIds());
        logisticsReconDetailSubService.batchUpdateMatchStatus(dto.getDetailSubIds(),
                LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode(), null);
        // 还原费用项确认状态：分片更新，避免一次性 IN 过多 id 超出 SQL 长度限制
        List<String> unbindSubIds = new ArrayList<>(dto.getDetailSubIds());
        for (int i = 0; i < unbindSubIds.size(); i += MATCH_ID_BATCH_SIZE) {
            List<String> batch = unbindSubIds.subList(i, Math.min(unbindSubIds.size(), i + MATCH_ID_BATCH_SIZE));
            logisticsReconDetailSubService.lambdaUpdate()
                    .in(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getId, batch)
                    .set(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getReconciliationStatus,
                            LogisticsReconReconciliationStatusEnum.TO_BE_CONFIRM.getCode())
                    .update(new com.erp.model.tms.entity.LogisticsReconDetailSubEntity());
        }
        // 解绑入参已带 mainId，直接按对账单刷新费用项确认状态，避免再 load 全部费用项取 mainId
        refreshDetailSubReconciliationStatus(dto.getMainId());
        // 解绑后匹配数由列表/详情查询实时聚合，无需回写主表

        List<BatchResultDTO> results = new ArrayList<>(dto.getDetailSubIds().size());
        for (String detailSubId : dto.getDetailSubIds()) {
            results.add(BatchResultDTO.success(detailSubId, detailSubId, OperationTypeEnum.UPDATE));
        }
        return results;
    }

    // ============================== 删除 / 导出 ==============================

    @DistributeLocker(businessType = DistributeKeyConstant.TMS_LOGISTICS_RECON_KEY, keyName = "id", unlockAfterTx = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        LogisticsReconEntity entity = super.getByIdOpt(id)
                .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));
        // 已确认的对账单不允许删除，导入中 / 待确认均可删
        if (LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(entity.getCheckStatus())) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_CONFIRMED_DELETE_FORBIDDEN);
        }
        List<String> mainIds = Collections.singletonList(id);
        // 级联逻辑删 detail / detail_sub / ref
        logisticsReconRefLogisticsBillService.removeByMainIds(mainIds);
        logisticsReconDetailSubService.removeByMainIds(mainIds);
        logisticsReconDetailService.removeByMainIds(mainIds);
        super.removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
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

    /**
     * 刷新费用项确认状态汇总（ref → detail_sub）
     * @author Will
     * @date 2026/6/2 16:30
     * @param mainId 对账单 id
     * @return void
     */
    private void refreshDetailSubReconciliationStatus(String mainId) {
        String lastId = null;
        while (true) {
            LambdaQueryChainWrapper<com.erp.model.tms.entity.LogisticsReconDetailSubEntity> query =
                    logisticsReconDetailSubService.lambdaQuery()
                    .eq(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getMainId, mainId)
                    .orderByAsc(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getId)
                    .last("LIMIT " + MATCH_ID_BATCH_SIZE);
            if (lastId != null) {
                query.gt(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getId, lastId);
            }
            List<com.erp.model.tms.entity.LogisticsReconDetailSubEntity> subBatch = query.list();
            if (CollUtil.isEmpty(subBatch)) {
                break;
            }
            lastId = subBatch.get(subBatch.size() - 1).getId();
            List<String> subIds = subBatch.stream()
                    .map(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getId)
                    .collect(Collectors.toList());
            Map<String, List<LogisticsReconRefLogisticsBillEntity>> refMap =
                    logisticsReconRefLogisticsBillService.lambdaQuery()
                            .eq(LogisticsReconRefLogisticsBillEntity::getMainId, mainId)
                            .eq(LogisticsReconRefLogisticsBillEntity::getIsDeleted, false)
                            .in(LogisticsReconRefLogisticsBillEntity::getDetailSubId, subIds)
                            .list()
                            .stream()
                            .collect(Collectors.groupingBy(LogisticsReconRefLogisticsBillEntity::getDetailSubId));

            List<String> confirmedSubIds = new ArrayList<>();
            List<String> partialSubIds = new ArrayList<>();
            List<String> unconfirmedSubIds = new ArrayList<>();
            for (com.erp.model.tms.entity.LogisticsReconDetailSubEntity sub : subBatch) {
                List<LogisticsReconRefLogisticsBillEntity> refs = refMap.get(sub.getId());
                int total = CollUtil.isEmpty(refs) ? 0 : refs.size();
                int confirmed = CollUtil.isEmpty(refs) ? 0 : (int) refs.stream()
                        .filter(ref -> ReconciliationStatusEnum.CONFIRMED.getCode().equals(ref.getReconciliationStatus()))
                        .count();
                String status = LogisticsReconReconciliationStatusEnum.resolve(confirmed, total);
                if (LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode().equals(status)) {
                    confirmedSubIds.add(sub.getId());
                } else if (LogisticsReconReconciliationStatusEnum.PARTIAL_CONFIRM.getCode().equals(status)) {
                    partialSubIds.add(sub.getId());
                } else {
                    unconfirmedSubIds.add(sub.getId());
                }
            }
            updateDetailSubReconciliationStatus(confirmedSubIds, LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode());
            updateDetailSubReconciliationStatus(partialSubIds, LogisticsReconReconciliationStatusEnum.PARTIAL_CONFIRM.getCode());
            updateDetailSubReconciliationStatus(unconfirmedSubIds, LogisticsReconReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
        }
    }

    /**
     * 批量更新费用项确认状态
     * @author Will
     * @date: 2026/06/02
     * @param ids
     * @param status
     * @return void
     */
    private void updateDetailSubReconciliationStatus(List<String> ids, String status) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 分片更新，避免一次性 IN 过多 id 超出 SQL 长度限制
        for (int i = 0; i < ids.size(); i += MATCH_ID_BATCH_SIZE) {
            List<String> batch = ids.subList(i, Math.min(ids.size(), i + MATCH_ID_BATCH_SIZE));
            logisticsReconDetailSubService.lambdaUpdate()
                    .in(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getId, batch)
                    .set(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getReconciliationStatus, status)
                    .update(new com.erp.model.tms.entity.LogisticsReconDetailSubEntity());
        }
    }

    /**
     * 按 id 集合更新/删除时的分片大小，避免一次性 IN 过多 id 超出 SQL 长度限制。
     */
    private static final int MATCH_ID_BATCH_SIZE = 1000;

    /**
     * 整单匹配时每批处理的费用项数量（小事务分片）。
     */
    private static final int MATCH_CHUNK_SIZE = 500;

    /**
     * 匹配中状态超时分钟数，超时后允许重置为失败并重新触发匹配。
     */
    private static final long MATCHING_STALE_MINUTES = 120;

    // ============================== private ==============================

    /**
     * 列表名称回填
     * @author Will
     * @date: 2026/06/02
     * @param list 列表数据
     * @return void
     */
    private void fillList(List<LogisticsReconDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<String, String> currencySymbolMap = FeignQuery.list(DictCurrencyEntity.class).stream()
                .collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol, (first, second) -> first));
        for (LogisticsReconDTO.ListDTO data : list) {
            data.setReconciliationMonth(DateUtil.formatCnYearMonth(data.getReconciliationMonth()));
            data.setCheckStatusName(LogisticsReconCheckStatusEnum.getName(data.getCheckStatus()));
            data.setReconciliationStatusName(
                    LogisticsReconReconciliationStatusEnum.getName(data.getReconciliationStatus()));
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
