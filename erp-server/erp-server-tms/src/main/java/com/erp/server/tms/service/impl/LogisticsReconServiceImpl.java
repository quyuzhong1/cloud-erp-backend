package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.message.constant.DistributeKeyConstant;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseDropDownDTO;
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
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.dto.LogisticsReconBatchResultDTO;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.dto.LogisticsReconMatchDTO;
import com.erp.model.tms.dto.LogisticsReconMatchExecutionResultDTO;
import com.erp.model.tms.dto.excel.LogisticsReconImportExcelDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.LogisticsReconDetailEntity;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;
import com.erp.model.tms.entity.LogisticsReconEntity;
import com.erp.model.tms.entity.LogisticsReconRefLogisticsBillEntity;
import com.erp.model.tms.enums.CfgLogisticsCostImportCfgTypeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportIdentifyTypeEnum;
import com.erp.model.tms.enums.LogisticsReconCheckStatusEnum;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconReconciliationStatusEnum;
import com.erp.model.tms.enums.LogisticsReconRefMatchTypeEnum;
import com.erp.model.tms.enums.ImportHistoryRecordProcessingTypeEnum;
import com.erp.model.tms.enums.ImportHistoryRecordStatusEnum;
import com.erp.model.tms.enums.ImportHistoryRecordTypeEnum;
import com.erp.model.tms.enums.LogisticsBillCostCheckStatusEnum;
import com.erp.model.tms.enums.LogisticsBillCostPayStateEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.tms.mapper.LogisticsReconMapper;
import com.erp.server.tms.listener.LogisticsReconExcelListener;
import com.erp.server.tms.service.CfgLogisticsCostImportDetailService;
import com.erp.server.tms.service.CfgLogisticsCostImportService;
import com.erp.server.tms.service.DictBasicService;
import com.erp.server.tms.service.ImportHistoryRecordService;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.LogisticsReconDetailService;
import com.erp.server.tms.service.LogisticsReconDetailSubService;
import com.erp.server.tms.service.LogisticsReconRefLogisticsBillService;
import com.erp.server.tms.service.LogisticsReconService;
import com.erp.server.tms.constant.LogisticsCostImportTargetFieldConstant;
import com.erp.server.tms.util.LogisticsReconMatchGroupHelper;
import com.erp.server.tms.util.LogisticsReconOpenImportConverter;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.support.LogisticsReconMatchFailReasonSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
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

    private static final String IMPORT_ERROR_MSG_HEADER = "错误信息";

    private static final String IMPORT_DETAIL_ROW_CACHE_PREFIX = "row:";

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
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    private DictBasicService dictBasicService;

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
        Map<String, Integer> countMap = list.stream()
                .collect(Collectors.toMap(LogisticsReconDTO.TabListDTO::getTabFlag,
                        LogisticsReconDTO.TabListDTO::getCount, Integer::sum));
        List<LogisticsReconDTO.TabListDTO> result = new ArrayList<>();
        int totalCount = LogisticsReconCheckStatusEnum.getStatusList().stream()
                .mapToInt(status -> countMap.getOrDefault(status, 0))
                .sum();
        result.add(new LogisticsReconDTO.TabListDTO("all", "全部", totalCount));
        for (String status : LogisticsReconCheckStatusEnum.getStatusList()) {
            result.add(new LogisticsReconDTO.TabListDTO(status,
                    LogisticsReconCheckStatusEnum.getName(status),
                    countMap.getOrDefault(status, 0)));
        }
        return result;
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


    @Override
    public BaseResultDTO.AddDTO importExcel(LogisticsReconDTO.ImportDTO dto) {
        self.prepareImportExcelMains(dto);
        String taskId = downloadTaskFeign.saveImportTask(DOC_NAME + "导入", IMPORT_TMS_LOGISTICS_RECON.getCode(), dto);
        log.info("[importExcel] submit taskId={} fileName={} cfgCount={} mainIds={}",
                taskId, dto.getFileName(), dto.getCfgLogisticsCostImportList().size(), dto.getMainIdMap().values());
        return new BaseResultDTO.AddDTO(taskId, dto.getFileName());
    }

    /**
     * 导入提交前预创建/复用各配置对应的主表（pending 复用 / 新建 importing），不含 Feign 任务创建。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void prepareImportExcelMains(LogisticsReconDTO.ImportDTO dto) {
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
        Map<String, String> logisticsSupplierNameMap = buildLogisticsSupplierNameMap();
        Map<String, String> salesPlatformNameMap = buildSalesPlatformNameMap();
        for (CfgLogisticsCostImportEntity importCfg : cfgList) {
            self.prepareImportMainLocked(buildReimportLockKey(dto, importCfg), dto, importCfg,
                    mainIdMap, reimportUpdateMap, logisticsSupplierNameMap, salesPlatformNameMap);
        }
        dto.setMainIdMap(mainIdMap);
        dto.setReimportUpdateMap(reimportUpdateMap);
    }

    /**
     * 单配置维度加锁后预创建/复用主表：pending 复用并置 importing，否则新建 importing 主表。
     */
    @DistributeLocker(businessType = DistributeKeyConstant.TMS_LOGISTICS_RECON_KEY, keyName = "lockKey",
            unlockAfterTx = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void prepareImportMainLocked(String lockKey, LogisticsReconDTO.ImportDTO dto,
                                        CfgLogisticsCostImportEntity importCfg,
                                        Map<String, String> mainIdMap, Map<String, Boolean> reimportUpdateMap,
                                        Map<String, String> logisticsSupplierNameMap,
                                        Map<String, String> salesPlatformNameMap) {
        String supplierName = resolveImportSupplierName(importCfg, logisticsSupplierNameMap, salesPlatformNameMap);
        if (findImportingReimportMain(dto, importCfg) != null) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORTING_DUPLICATE);
        }
        LogisticsReconEntity pending = lockPendingReimportMain(dto, importCfg);
        if (pending != null) {
            boolean updated = lambdaUpdate()
                    .eq(LogisticsReconEntity::getId, pending.getId())
                    .eq(LogisticsReconEntity::getCheckStatus, LogisticsReconCheckStatusEnum.PENDING.getCode())
                    .set(LogisticsReconEntity::getCheckStatus, LogisticsReconCheckStatusEnum.IMPORTING.getCode())
                    .set(LogisticsReconEntity::getFileUrl, dto.getFileUrl())
                    .set(LogisticsReconEntity::getFileName, dto.getFileName())
                    .set(LogisticsReconEntity::getImportFailReason, "")
                    .set(LogisticsReconEntity::getSupplierName, supplierName)
                    .update();
            if (!updated) {
                if (findImportingReimportMain(dto, importCfg) != null) {
                    throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORTING_DUPLICATE);
                }
                throw new ServiceException(ApiError.LOGISTICS_RECON_SAVE_FAILED);
            }
            mainIdMap.put(importCfg.getId(), pending.getId());
            reimportUpdateMap.put(importCfg.getId(), Boolean.TRUE);
            return;
        }
        try {
            LogisticsReconEntity entity = createImportingMain(dto, importCfg, supplierName);
            mainIdMap.put(importCfg.getId(), entity.getId());
            reimportUpdateMap.put(importCfg.getId(), Boolean.FALSE);
        } catch (DataIntegrityViolationException e) {
            if (isImportDimensionDuplicateException(e)) {
                throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORTING_DUPLICATE);
            }
            log.warn("创建导入主表数据完整性异常, lockKey={}, cfgImportId={}", lockKey, importCfg.getId(), e);
            throw new ServiceException(ApiError.LOGISTICS_RECON_SAVE_FAILED);
        }
    }

    /**
     * 对账维度（月份+物流商+Sheet）pending/importing 部分唯一索引名。
     */
    private static final String DIM_UNIQUE_CONSTRAINT = "uniq_logistics_recon_month_supplier_sheet";

    /**
     * 判断是否因对账维度唯一约束冲突（pending/importing 同维度）导致的数据完整性异常。
     */
    private boolean isImportDimensionDuplicateException(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            String msg = cur.getMessage();
            if (msg != null && isDimensionUniqueConstraintMessage(msg)) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    /**
     * 判断异常消息是否命中对账维度部分唯一索引（月份+物流商+Sheet）。
     */
    private boolean isDimensionUniqueConstraintMessage(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        return lower.contains(DIM_UNIQUE_CONSTRAINT);
    }

    /**
     * 预加载物流商下拉（code → 名称），用于导入主表 supplier_name 回填。
     */
    private Map<String, String> buildLogisticsSupplierNameMap() {
        return logisticsSupplierService.listAllShort(false).stream()
                .filter(item -> StrUtil.isNotBlank(item.getCode()))
                .collect(Collectors.toMap(BaseDropDownDTO.DisabledDTO::getCode,
                        BaseDropDownDTO.DisabledDTO::getValue, (first, second) -> first));
    }

    /**
     * 预加载销售平台字典（code → 名称），用于平台类导入配置的主表 supplier_name 回填。
     */
    private Map<String, String> buildSalesPlatformNameMap() {
        List<com.erp.model.tms.entity.DictBasicEntity> salesPlatformList =
                dictBasicService.getByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());
        if (CollUtil.isEmpty(salesPlatformList)) {
            return Collections.emptyMap();
        }
        return salesPlatformList.stream()
                .filter(item -> StrUtil.isNotBlank(item.getCode()))
                .collect(Collectors.toMap(com.erp.model.tms.entity.DictBasicEntity::getCode,
                        com.erp.model.tms.entity.DictBasicEntity::getName, (first, second) -> first));
    }

    /**
     * 按 cfg_type 解析主表 supplier_name：平台取平台名称，物流商取供应商名称。
     */
    private String resolveImportSupplierName(CfgLogisticsCostImportEntity importCfg,
                                             Map<String, String> logisticsSupplierNameMap,
                                             Map<String, String> salesPlatformNameMap) {
        if (importCfg == null || StrUtil.isBlank(importCfg.getDictPlatform())) {
            return "";
        }
        if (Objects.equals(CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(), importCfg.getCfgType())) {
            return logisticsSupplierNameMap.getOrDefault(importCfg.getDictPlatform(), importCfg.getDictPlatform());
        }
        return salesPlatformNameMap.getOrDefault(importCfg.getDictPlatform(), importCfg.getDictPlatform());
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
                LogisticsReconEntity entity = resolveImportMain(dto, importCfg);
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
                    dto.setImportDetailSubSnapshotMap(null);
                    dto.setImportDetailSnapshotMap(null);
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
                finalizeImportMain(dto, excelListener);
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
        // 纵向/横向导入均为「一行一条明细」；仅匹配阶段按识别单号合并费用项
        Map<String, Integer> detailCostCountDelta = new HashMap<>();
        if (isVerticalReconCostItem(cfgDetails)) {
            processVerticalReconRows(rows, headMap, headerIndexMap, rowNoStart, dto, cfgDetails, rateCache,
                    detailList, subList, updateDetailList, updateSubList, errorList, detailCostCountDelta);
        } else {
            processHorizontalReconRows(rows, headMap, headerIndexMap, rowNoStart, dto, cfgDetails, rateCache,
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
                        .update();
            }
        }
    }

    /**
     * 导入原币别：取模板映射值，空则默认人民币（不阻断导入）。
     */
    private String resolveImportCurrency(String mappedCurrency) {
        return StrUtil.blankToDefault(StrUtil.trim(mappedCurrency), CurrencyEnum.CNY.getCurrencyCode());
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
     * @param dto 导入参数（processingType 决定待确认或已确认）
     * @param excelListener 导入监听器（已累积统计）
     */
    private void finalizeImportMain(LogisticsReconDTO.ImportDTO dto, LogisticsReconExcelListener excelListener) {
        String mainId = dto.getMainId();
        int detailCount = (int) logisticsReconDetailService.lambdaQuery()
                .eq(LogisticsReconDetailEntity::getMainId, mainId)
                .count();
        int subCount = (int) logisticsReconDetailSubService.lambdaQuery()
                .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                .count();
        String checkStatus = LogisticsReconOpenImportConverter.RECON_PROCESSING_IMPORT_CHECK.equals(dto.getProcessingType())
                ? LogisticsReconCheckStatusEnum.CONFIRMED.getCode()
                : LogisticsReconCheckStatusEnum.PENDING.getCode();
        lambdaUpdate()
                .eq(LogisticsReconEntity::getId, mainId)
                .set(LogisticsReconEntity::getCheckStatus, checkStatus)
                .set(LogisticsReconEntity::getImportCount, detailCount)
                .set(LogisticsReconEntity::getCostCount, subCount)
                .set(LogisticsReconEntity::getTotalAmount, excelListener.getTotalAmount())
                .set(LogisticsReconEntity::getCurrency, excelListener.resolveCurrency())
                .set(LogisticsReconEntity::getImportFailReason, "")
                .update();
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
                    .set(LogisticsReconEntity::getCheckStatus, LogisticsReconCheckStatusEnum.PENDING.getCode())
                    .set(LogisticsReconEntity::getImportFailReason, reason)
                    .update();
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
                                                              Map<Integer, String> headMap,
                                                              Map<String, Integer> headerIndexMap,
                                                              List<CfgLogisticsCostImportDetailEntity> cfgDetails) {
        LogisticsReconImportExcelDTO excelDTO = new LogisticsReconImportExcelDTO();
        excelDTO.setNo(String.valueOf(rowNo));
        attachImportErrorRow(excelDTO, row, headMap);
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
     * 绑定原始 Excel 行与表头，供错误文件按原表头导出。
     */
    private void attachImportErrorRow(LogisticsReconImportExcelDTO excelDTO, Map<Integer, String> row,
                                      Map<Integer, String> headMap) {
        if (excelDTO == null) {
            return;
        }
        excelDTO.setRawRow(row == null ? Collections.emptyMap() : new HashMap<>(row));
        excelDTO.setHeadMap(headMap == null ? Collections.emptyMap() : new HashMap<>(headMap));
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
                .setCurrency(resolveImportCurrency(excelDTO.getCurrency()))
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
                .setActualAmount(actualAmount)
                .setEstimatedAmount(estimatedAmount == null ? BigDecimal.ZERO : estimatedAmount)
                .setCurrency(resolveImportCurrency(detail.getCurrency()))
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
     * 横向费用项：一行一条明细，同一行内各费用列生成该明细下多个费用项（导入不合并行）。
     * @author Will
     * @date: 2026/06/12
     */
    private void processHorizontalReconRows(List<Map<Integer, String>> rows, Map<Integer, String> headMap,
                                            Map<String, Integer> headerIndexMap,
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
        int rowNo = rowNoStart;
        for (Map<Integer, String> row : rows) {
            int currentRowNo = rowNo++;
            LogisticsReconImportExcelDTO excelDTO = null;
            try {
                excelDTO = buildReconImportExcel(currentRowNo, row, headMap, headerIndexMap, cfgDetails);
                List<String> errorMsgList = FieldValidUtil.fieldValid(excelDTO);
                if (CollUtil.isNotEmpty(errorMsgList)) {
                    excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(excelDTO);
                    continue;
                }
                DetailResolveResult resolveResult = resolveImportDetail(dto, currentRowNo, excelDTO);
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
                    attachImportErrorRow(excelDTO, row, headMap);
                }
                excelDTO.setErrorMsg("金额或数字字段格式不正确");
                errorList.add(excelDTO);
            }
        }
    }

    /**
     * 纵向费用项：一行一条明细 + 该行一个费用项（识别号相同也不合并；匹配阶段再按识别号合并）。
     * @author Will
     * @date: 2026/06/12
     */
    private void processVerticalReconRows(List<Map<Integer, String>> rows, Map<Integer, String> headMap,
                                          Map<String, Integer> headerIndexMap,
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
        String costNameHeader = costCfgList.stream()
                .map(CfgLogisticsCostImportDetailEntity::getSourceField)
                .filter(StrUtil::isNotBlank)
                .findFirst().orElse(null);
        CfgLogisticsCostImportDetailEntity actualCfg = cfgDetails.stream()
                .filter(detail -> StrUtil.equals("actualAmount", detail.getTargetField())).findFirst().orElse(null);
        CfgLogisticsCostImportDetailEntity estimatedCfg = cfgDetails.stream()
                .filter(detail -> StrUtil.equals("estimatedAmount", detail.getTargetField())).findFirst().orElse(null);

        int rowNo = rowNoStart;
        for (Map<Integer, String> row : rows) {
            int currentRowNo = rowNo++;
            LogisticsReconImportExcelDTO excelDTO = null;
            try {
                excelDTO = buildReconImportExcel(currentRowNo, row, headMap, headerIndexMap, cfgDetails);
                List<String> errorMsgList = FieldValidUtil.fieldValid(excelDTO);
                if (CollUtil.isNotEmpty(errorMsgList)) {
                    excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(excelDTO);
                    continue;
                }
                DetailResolveResult resolveResult = resolveImportDetail(dto, currentRowNo, excelDTO);
                LogisticsReconDetailEntity detail = resolveResult.getDetail();
                List<LogisticsReconDetailSubEntity> rowNewSubs = new ArrayList<>();
                List<LogisticsReconDetailSubEntity> rowUpdateSubs = new ArrayList<>();
                int seqNo = resolveResult.getNextSeqNo();

                String costNameCell = StrUtil.isBlank(costNameHeader) ? "" : readCellByHeader(row, headerIndexMap, costNameHeader);
                CfgLogisticsCostImportDetailEntity matched = costCfgList.stream()
                        .filter(costCfg -> StrUtil.equals(costCfg.getSourceDetailField(), costNameCell))
                        .findFirst().orElse(null);
                String costName = matched != null
                        ? StrUtil.blankToDefault(matched.getTargetDetailFieldName(), matched.getSourceDetailField())
                        : costNameCell;
                if (StrUtil.isBlank(costName)) {
                    excelDTO.setErrorMsg("费用名称为空，无法识别费用项");
                    errorList.add(excelDTO);
                    continue;
                }
                BigDecimal actual = actualCfg == null ? BigDecimal.ZERO
                        : parseAmount(getCellValue(row, headerIndexMap, actualCfg), false).setScale(4, RoundingMode.HALF_UP);
                BigDecimal estimated = estimatedCfg == null ? BigDecimal.ZERO
                        : parseAmount(getCellValue(row, headerIndexMap, estimatedCfg), false).setScale(4, RoundingMode.HALF_UP);
                ImportSubResolveResult subResult = resolveOrBuildImportSub(dto, detail, seqNo, costName, actual, estimated, rateCache);
                if (subResult.getRateError() != null) {
                    excelDTO.setErrorMsg(subResult.getRateError());
                    errorList.add(excelDTO);
                    continue;
                }
                if (subResult.isUpdateExisting()) {
                    rowUpdateSubs.add(subResult.getSub());
                } else {
                    rowNewSubs.add(subResult.getSub());
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
                    attachImportErrorRow(excelDTO, row, headMap);
                }
                excelDTO.setErrorMsg("金额或数字字段格式不正确");
                errorList.add(excelDTO);
            }
        }
    }

    /**
     * 导入明细解析结果：新建或按 rowNo 重导更新已有明细。
     */
    private static class DetailResolveResult {
        /** 目标明细（新建或已存在） */
        private final LogisticsReconDetailEntity detail;
        /** 是否为本次导入新建明细 */
        private final boolean newDetail;
        /** 下一费用项 seq_no（同一明细内多费用列时递增） */
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
     * 解析导入明细：一律按 Excel 行号 rowNo 对应库内 (main_id, row_no)；导入阶段不按识别单号合并。
     */
    private DetailResolveResult resolveImportDetail(LogisticsReconDTO.ImportDTO dto, int rowNo,
                                                    LogisticsReconImportExcelDTO excelDTO) {
        ensureImportDetailCache(dto);
        String cacheKey = buildImportDetailRowCacheKey(rowNo);
        String existingDetailId = StrUtil.isBlank(cacheKey) ? null : dto.getImportDetailKeyMap().get(cacheKey);
        if (StrUtil.isNotBlank(existingDetailId)) {
            LogisticsReconDetailEntity detail = new LogisticsReconDetailEntity();
            detail.setId(existingDetailId);
            detail.setMainId(dto.getMainId());
            detail.setCurrency(resolveImportCurrency(excelDTO.getCurrency()));
            int nextSeqNo = dto.getImportDetailMaxSeqMap().getOrDefault(existingDetailId, 0) + 1;
            return new DetailResolveResult(detail, false, nextSeqNo);
        }
        LogisticsReconDetailEntity detail = buildReconDetail(dto, rowNo, excelDTO);
        if (StrUtil.isNotBlank(cacheKey)) {
            dto.getImportDetailKeyMap().put(cacheKey, detail.getId());
        }
        dto.getImportDetailMaxSeqMap().put(detail.getId(), 0);
        return new DetailResolveResult(detail, true, 1);
    }

    private String buildImportDetailRowCacheKey(Integer rowNo) {
        return rowNo == null ? "" : IMPORT_DETAIL_ROW_CACHE_PREFIX + rowNo;
    }

    /**
     * 费用项落库：新建明细入 detailList；重导时按 rowNo 更新已有明细并写入/更新该行下费用项。
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
            if (dto.getImportDetailSnapshotMap() != null) {
                LogisticsReconDetailEntity snapshot = dto.getImportDetailSnapshotMap().get(detail.getId());
                if (snapshot != null) {
                    updateDetail.setVersion(snapshot.getVersion());
                }
            }
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
        /** 目标费用项实体 */
        private final LogisticsReconDetailSubEntity sub;
        /** 是否为重导覆盖更新已有费用项 */
        private final boolean updateExisting;
        /** 下一费用项 seq_no */
        private final int nextSeqNo;
        /** 本位币汇率换算错误文案（无错误时为 null） */
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

    /**
     * 解析或构建导入费用项：重导时按明细 id + 费用名命中则覆盖更新，否则新建并写入缓存。
     *
     * @param dto              导入上下文
     * @param detail           所属对账明细
     * @param seqNo            当前费用项序号
     * @param costName         费用名称
     * @param actualAmount     实付金额
     * @param estimatedAmount  暂估金额
     * @param rateCache        本位币汇率缓存
     * @return 解析结果（新建 / 更新 + 汇率错误）
     */
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
                applyExistingSubSnapshot(sub, dto, subKey);
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

    /**
     * 确保重导费用项键缓存已初始化（detailId + 费用名 → subId）。
     */
    private void ensureImportDetailSubKeyMap(LogisticsReconDTO.ImportDTO dto) {
        if (dto.getImportDetailSubKeyMap() == null) {
            dto.setImportDetailSubKeyMap(new HashMap<>());
        }
    }

    private void ensureImportDetailSubSnapshotMap(LogisticsReconDTO.ImportDTO dto) {
        if (dto.getImportDetailSubSnapshotMap() == null) {
            dto.setImportDetailSubSnapshotMap(new HashMap<>());
        }
    }

    private void ensureImportDetailSnapshotMap(LogisticsReconDTO.ImportDTO dto) {
        if (dto.getImportDetailSnapshotMap() == null) {
            dto.setImportDetailSnapshotMap(new HashMap<>());
        }
    }

    /**
     * 重导覆盖更新费用项时保留库内 seq_no、version 及匹配/配置快照，避免 batch update 冲突。
     */
    private void applyExistingSubSnapshot(LogisticsReconDetailSubEntity sub, LogisticsReconDTO.ImportDTO dto,
                                          String subKey) {
        if (sub == null || dto.getImportDetailSubSnapshotMap() == null) {
            return;
        }
        LogisticsReconDetailSubEntity snapshot = dto.getImportDetailSubSnapshotMap().get(subKey);
        if (snapshot == null) {
            return;
        }
        if (snapshot.getSeqNo() != null) {
            sub.setSeqNo(snapshot.getSeqNo());
        }
        sub.setVersion(snapshot.getVersion());
        sub.setCfgCostId(snapshot.getCfgCostId());
        sub.setCfgCostName(snapshot.getCfgCostName());
        sub.setMatchStatus(snapshot.getMatchStatus());
        sub.setMatchFailReason(snapshot.getMatchFailReason());
        sub.setReconciliationStatus(snapshot.getReconciliationStatus());
    }

    private LogisticsReconDetailSubEntity copySubReimportSnapshot(LogisticsReconDetailSubEntity source) {
        LogisticsReconDetailSubEntity snapshot = new LogisticsReconDetailSubEntity();
        snapshot.setId(source.getId());
        snapshot.setSeqNo(source.getSeqNo());
        snapshot.setVersion(source.getVersion());
        snapshot.setCfgCostId(source.getCfgCostId());
        snapshot.setCfgCostName(source.getCfgCostName());
        snapshot.setMatchStatus(source.getMatchStatus());
        snapshot.setMatchFailReason(source.getMatchFailReason());
        snapshot.setReconciliationStatus(source.getReconciliationStatus());
        return snapshot;
    }

    private LogisticsReconDetailEntity copyDetailReimportSnapshot(LogisticsReconDetailEntity source) {
        LogisticsReconDetailEntity snapshot = new LogisticsReconDetailEntity();
        snapshot.setId(source.getId());
        snapshot.setVersion(source.getVersion());
        return snapshot;
    }

    /**
     * 构建导入费用项唯一键：明细 id + 费用名称（trim 后）。
     */
    private String buildImportSubKey(String detailId, String costName) {
        return detailId + "::" + StrUtil.trim(costName);
    }

    /**
     * 按对账维度（月份 + 物流商 + Sheet）构建查重条件，与唯一索引
     * uniq_logistics_recon_month_supplier_sheet_active 一致。
     */
    private LambdaQueryChainWrapper<LogisticsReconEntity> buildReimportDimensionQuery(
            LogisticsReconDTO.ImportDTO dto, CfgLogisticsCostImportEntity importCfg) {
        LambdaQueryChainWrapper<LogisticsReconEntity> query = lambdaQuery()
                .eq(LogisticsReconEntity::getReconciliationMonth, dto.getReconciliationMonth())
                .eq(LogisticsReconEntity::getSupplierId, importCfg.getDictPlatform())
                .eq(LogisticsReconEntity::getIsDeleted, false);
        if (StrUtil.isBlank(importCfg.getSheetName())) {
            query.and(w -> w.isNull(LogisticsReconEntity::getSheetName)
                    .or().eq(LogisticsReconEntity::getSheetName, ""));
        } else {
            query.eq(LogisticsReconEntity::getSheetName, importCfg.getSheetName());
        }
        return query;
    }

    /**
     * 构建重导维度分布式锁键：对账月份 | 物流商/平台 id | Sheet 名。
     */
    private String buildReimportLockKey(LogisticsReconDTO.ImportDTO dto, CfgLogisticsCostImportEntity importCfg) {
        return dto.getReconciliationMonth() + "|"
                + StrUtil.blankToDefault(importCfg.getDictPlatform(), "") + "|"
                + StrUtil.blankToDefault(importCfg.getSheetName(), "");
    }

    /**
     * 查找同维度待确认对账单并加行锁（允许覆盖更新导入；已确认单不在此命中，将新建）。
     */
    private LogisticsReconEntity lockPendingReimportMain(LogisticsReconDTO.ImportDTO dto,
                                                       CfgLogisticsCostImportEntity importCfg) {
        return buildReimportDimensionQuery(dto, importCfg)
                .eq(LogisticsReconEntity::getCheckStatus, LogisticsReconCheckStatusEnum.PENDING.getCode())
                .orderByDesc(LogisticsReconEntity::getCreateTime)
                .last("LIMIT 1 FOR UPDATE")
                .one();
    }

    /**
     * 查找同维度导入中对账单（不允许重复提交导入任务）。
     */
    private LogisticsReconEntity findImportingReimportMain(LogisticsReconDTO.ImportDTO dto,
                                                           CfgLogisticsCostImportEntity importCfg) {
        return buildReimportDimensionQuery(dto, importCfg)
                .eq(LogisticsReconEntity::getCheckStatus, LogisticsReconCheckStatusEnum.IMPORTING.getCode())
                .orderByDesc(LogisticsReconEntity::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    /**
     * 重导更新：从库内预加载 rowNo → 明细 id、明细 id + 费用名 → 费用项 id 映射。
     */
    private void initImportDetailCacheFromDb(LogisticsReconDTO.ImportDTO dto,
                                             List<CfgLogisticsCostImportDetailEntity> cfgDetails) {
        ensureImportDetailCache(dto);
        ensureImportDetailSubKeyMap(dto);
        ensureImportDetailSubSnapshotMap(dto);
        ensureImportDetailSnapshotMap(dto);
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
                String cacheKey = buildImportDetailRowCacheKey(detail.getRowNo());
                if (StrUtil.isNotBlank(cacheKey)) {
                    dto.getImportDetailKeyMap().putIfAbsent(cacheKey, detail.getId());
                }
                dto.getImportDetailSnapshotMap().putIfAbsent(detail.getId(), copyDetailReimportSnapshot(detail));
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
                dto.getImportDetailSubSnapshotMap().putIfAbsent(buildImportSubKey(sub.getDetailId(), sub.getCostName()),
                        copySubReimportSnapshot(sub));
                dto.getImportDetailMaxSeqMap().merge(sub.getDetailId(),
                        sub.getSeqNo() == null ? 0 : sub.getSeqNo(), Math::max);
            }
        }
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
        if (LogisticsCostImportTargetFieldConstant.SOURCE_CODE.equals(target)
                || LogisticsCostImportTargetFieldConstant.SO_CODE.equals(target)) {
            excelDTO.setSoCode(value);
        } else if (LogisticsCostImportTargetFieldConstant.PLATFORM_CODE.equals(target)
                || LogisticsCostImportTargetFieldConstant.PLATFORM_ORDER_NO.equals(target)) {
            excelDTO.setPlatformOrderNo(value);
        } else if (LogisticsCostImportTargetFieldConstant.TRACK_NO.equals(target)) {
            excelDTO.setTrackNo(value);
        } else if (LogisticsCostImportTargetFieldConstant.TRANSPORT_NO.equals(target)) {
            excelDTO.setTransportNo(value);
        } else if (LogisticsCostImportTargetFieldConstant.SO_DELIVERY_CODE.equals(target)) {
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
        File file;
        if (sortedErrorList.stream().anyMatch(item -> CollUtil.isNotEmpty(item.getRawRow()) && CollUtil.isNotEmpty(item.getHeadMap()))) {
            List<String> headers = buildImportErrorExportHeaders(sortedErrorList);
            List<List<Object>> exportRows = sortedErrorList.stream()
                    .map(item -> buildImportErrorExportRow(item, headers))
                    .collect(Collectors.toList());
            file = ExcelUtil.exportFile(fileName, "error", exportRows, headers);
        } else {
            file = ExcelUtil.exportFile(fileName, "error", sortedErrorList, LogisticsReconImportExcelDTO.class);
        }
        return file.isDirectory() ? "" : FastDFSClientUtil.uploadFile(file, fileName);
    }

    /**
     * 按原 Excel 表头顺序拼接导出列，末尾追加错误信息列。
     */
    private List<String> buildImportErrorExportHeaders(List<LogisticsReconImportExcelDTO> errorList) {
        LinkedHashSet<String> headerSet = new LinkedHashSet<>();
        for (LogisticsReconImportExcelDTO error : errorList) {
            Map<Integer, String> headMap = error.getHeadMap();
            if (CollUtil.isEmpty(headMap)) {
                continue;
            }
            headMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .filter(StrUtil::isNotBlank)
                    .forEach(headerSet::add);
        }
        List<String> headers = new ArrayList<>(headerSet);
        headers.add(IMPORT_ERROR_MSG_HEADER);
        return headers;
    }

    /**
     * 按表头名从原始行取值，最后一列写入错误信息。
     */
    private List<Object> buildImportErrorExportRow(LogisticsReconImportExcelDTO error, List<String> headers) {
        List<Object> exportRow = new ArrayList<>(headers.size());
        Map<Integer, String> headMap = error.getHeadMap();
        Map<Integer, String> rawRow = error.getRawRow() == null ? Collections.emptyMap() : error.getRawRow();
        Map<String, Integer> indexByHeader = CollUtil.isEmpty(headMap) ? Collections.emptyMap() : buildHeaderIndexMap(headMap);
        int errorColumnIndex = headers.size() - 1;
        for (int i = 0; i < headers.size(); i++) {
            if (i == errorColumnIndex) {
                exportRow.add(StrUtil.blankToDefault(error.getErrorMsg(), ""));
                continue;
            }
            Integer columnIndex = indexByHeader.get(headers.get(i));
            exportRow.add(columnIndex == null ? "" : StrUtil.blankToDefault(rawRow.get(columnIndex), ""));
        }
        return exportRow;
    }

    /**
     * 获取导入任务预创建的主表（禁止异步任务内静默新建，避免绕过提交阶段查重）。
     */
    private LogisticsReconEntity resolveImportMain(LogisticsReconDTO.ImportDTO dto,
                                                   CfgLogisticsCostImportEntity importCfg) {
        String mainId = CollUtil.isNotEmpty(dto.getMainIdMap())
                ? dto.getMainIdMap().get(importCfg.getId()) : null;
        if (StrUtil.isBlank(mainId)) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORT_MAIN_NOT_FOUND);
        }
        LogisticsReconEntity entity = super.getById(mainId);
        if (entity == null) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORT_MAIN_NOT_FOUND);
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
                                                     CfgLogisticsCostImportEntity importCfg,
                                                     String supplierName) {
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DZ);
        LogisticsReconEntity entity = new LogisticsReconEntity()
                .setCode(code)
                .setReconciliationMonth(dto.getReconciliationMonth())
                .setBusinessType(importCfg.getBusinessType())
                .setCfgImportId(importCfg.getId())
                .setCfgType(importCfg.getCfgType())
                .setSupplierId(importCfg.getDictPlatform())
                .setSupplierName(supplierName)
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

    /**
     * 物流商对账单校验状态单条切换（待确认 ↔ 已确认）。
     *
     * @author Will
     * @date 2026/6/12
     * @param id          对账单 id
     * @param checkStatus 目标校验状态
     * @return 切换结果
     */
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
                .update();
        if (!updated) {
            throw new ServiceException(ApiError.BILL_DATA_LOCKED);
        }
        String msg = StrUtil.format("用户【{}】将{}【{}】校验状态切换为【{}】",
                user.getUserName(), DOC_NAME, entity.getCode(),
                LogisticsReconCheckStatusEnum.getName(checkStatus));
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "校验状态切换");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    /**
     * 校验对账单校验状态切换是否合法（待确认 ↔ 已确认）。
     * <p>目标待确认：禁止同状态重复；已确认回退时要求全部费用项未匹配。</p>
     * <p>目标已确认：仅允许当前待确认；禁止导入失败或存在匹配中费用项。</p>
     *
     * @author Will
     * @date 2026/6/12
     * @param entity       当前对账单
     * @param targetStatus 目标校验状态（pending / confirmed）
     */
    private void validateCheckStatusTransition(LogisticsReconEntity entity, String targetStatus) {
        String currentStatus = entity.getCheckStatus();
        // 导入中不允许直接切换校验状态（需等导入完成进入待确认）
        if (LogisticsReconCheckStatusEnum.IMPORTING.getCode().equals(currentStatus)) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORTING_CHECK_STATUS_FORBIDDEN);
        }
        // 仅允许在 待确认 / 已确认 之间流转
        if (!LogisticsReconCheckStatusEnum.PENDING.getCode().equals(targetStatus)
                && !LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(targetStatus)) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_CHECK_STATUS_INVALID);
        }
        if (StrUtil.equals(currentStatus, targetStatus)) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_CHECK_STATUS_NO_CHANGE);
        }
        if (LogisticsReconCheckStatusEnum.PENDING.getCode().equals(targetStatus)) {
            // 已确认 → 待确认：仅当全部费用项匹配状态为未匹配时允许
            long nonUnmatchedCount = logisticsReconDetailSubService.lambdaQuery()
                    .eq(LogisticsReconDetailSubEntity::getMainId, entity.getId())
                    .ne(LogisticsReconDetailSubEntity::getMatchStatus,
                            LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode())
                    .count();
            if (nonUnmatchedCount > 0) {
                throw new ServiceException(ApiError.LOGISTICS_RECON_MATCH_REF_EXISTS_ROLLBACK_FORBIDDEN);
            }
        }
        if (LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(targetStatus)) {
            // 仅待确认 → 已确认
            if (!LogisticsReconCheckStatusEnum.PENDING.getCode().equals(currentStatus)) {
                throw new ServiceException(ApiError.LOGISTICS_RECON_ONLY_PENDING_ALLOW_CHECK_CONFIRM);
            }
            if (StrUtil.isNotBlank(entity.getImportFailReason())) {
                throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORT_FAILED_CHECK_STATUS_FORBIDDEN);
            }
            long matchingCount = logisticsReconDetailSubService.lambdaQuery()
                    .eq(LogisticsReconDetailSubEntity::getMainId, entity.getId())
                    .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                            LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                    .count();
            if (matchingCount > 0) {
                throw new ServiceException(ApiError.LOGISTICS_RECON_MATCHING_CONFIRM_FORBIDDEN);
            }
        }
    }

    // ============================== 合并 & 匹配 ==============================

    /**
     * 批量提交对账单自动匹配：校验主单状态 → 认领费用项（matching）→ 分片提交线程池异步执行。
     */
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
                int submittedChunkCount = 0;
                while (true) {
                    List<String> claimedIds = logisticsReconDetailSubService
                            .claimMainSubsMatchingBatch(mainId, MATCH_CHUNK_SIZE);
                    if (CollUtil.isEmpty(claimedIds)) {
                        break;
                    }
                    List<String> scopeSubIds = new ArrayList<>(claimedIds);
                    try {
                        logisticsReconMatchPool.submit(() -> asyncMatchByMain(mainId, user, scopeSubIds));
                        submittedChunkCount++;
                    } catch (RejectedExecutionException e) {
                        log.warn("[batchMatch] 匹配线程池已满 mainId={}", mainId, e);
                        self.markReconMatchFailed(mainId, scopeSubIds,
                                ApiError.LOGISTICS_RECON_MATCH_POOL_BUSY.getMsg());
                        results.add(BatchResultDTO.fail(entity.getId(), entity.getCode(),
                                submittedChunkCount > 0
                                        ? "部分匹配任务已提交，后续任务提交失败：" + ApiError.LOGISTICS_RECON_MATCH_POOL_BUSY.getMsg()
                                        : ApiError.LOGISTICS_RECON_MATCH_POOL_BUSY.getMsg()));
                        break;
                    }
                }
                if (submittedChunkCount == 0) {
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
                results.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "已提交匹配，请稍后查看明细匹配结果"));
            } catch (Exception e) {
                log.error("[batchMatch] 提交失败 mainId={}", mainId, e);
                results.add(BatchResultDTO.fail(mainId, mainId, e.getMessage()));
            }
        }
        return results;
    }

    /**
     * 异步执行对账单整批匹配：仅处理本次认领的费用项，失败时仅回写该范围。
     */
    private void asyncMatchByMain(String mainId, LoginUser user, List<String> scopeSubIds) {
        LoginUser prev = UserContext.getLoginUser();
        try {
            UserContext.setLoginUser(user);
            self.doMatchByMain(mainId, scopeSubIds);
        } catch (Exception e) {
            log.error("[asyncMatchByMain] 匹配失败 mainId={} scopeSize={}", mainId,
                    scopeSubIds == null ? 0 : scopeSubIds.size(), e);
            try {
                self.markReconMatchFailed(mainId, scopeSubIds, LogisticsReconMatchFailReasonSupport.resolve(e));
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
     * 对账单整批匹配（同步）：按 scopeSubIds 分片调用 {@link #doMatchSubsChunk}，单片失败仅回写该分片。
     */
    @Override
    public void doMatchByMain(String mainId, List<String> scopeSubIds) {
        if (CollUtil.isEmpty(scopeSubIds)) {
            return;
        }
        LogisticsReconEntity entity = super.getByIdOpt(mainId)
                .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));
        if (!LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(entity.getCheckStatus())) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_MATCH);
        }
        List<String> sortedScope = scopeSubIds.stream().filter(StrUtil::isNotBlank).distinct().sorted()
                .collect(Collectors.toList());
        for (int i = 0; i < sortedScope.size(); i += MATCH_CHUNK_SIZE) {
            List<String> chunk = sortedScope.subList(i, Math.min(sortedScope.size(), i + MATCH_CHUNK_SIZE));
            try {
                self.doMatchSubsChunk(mainId, chunk);
            } catch (Exception e) {
                log.error("[doMatchByMain] 分片匹配失败 mainId={} chunkSize={}", mainId, chunk.size(), e);
                self.markReconMatchFailed(mainId, chunk, LogisticsReconMatchFailReasonSupport.resolve(e));
            }
        }
    }

    /**
     * 匹配单个费用项分片：加载 matching 状态的费用项 → 调用 {@link #executeReconMatch} →
     * {@link #commitReconMatchResult} 回写关联与匹配状态。
     */
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
        Set<String> processedSubIds = units.stream().map(unit -> unit.sub.getId()).collect(Collectors.toSet());
        List<String> orphanSubIds = detailSubIds.stream()
                .filter(id -> !processedSubIds.contains(id))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(orphanSubIds)) {
            logisticsReconDetailSubService.batchUpdateMatchStatus(orphanSubIds,
                    LogisticsReconDetailMatchStatusEnum.FAILED.getCode(), "对账明细不存在", MATCHING_FROM_STATUS);
        }
        if (CollUtil.isEmpty(units)) {
            return;
        }
        LogisticsReconMatchExecutionResultDTO executionResult = executeReconMatch(entity, units, false);
        self.commitReconMatchResult(mainId, LogisticsReconRefMatchTypeEnum.AUTO.getCode(),
                executionResult.getRowKeyToDetailId(), executionResult.getRowKeyToSubs(),
                executionResult.getMatchResults());
    }

    /**
     * 刷新匹配中费用项的更新时间（心跳），避免在跑的长任务被认领逻辑误判超时而遭抢占重试。
     */
    private void touchMatchingSubsUpdateTime(List<String> detailSubIds) {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < detailSubIds.size(); i += MATCH_ID_BATCH_SIZE) {
            List<String> batch = detailSubIds.subList(i, Math.min(detailSubIds.size(), i + MATCH_ID_BATCH_SIZE));
            logisticsReconDetailSubService.lambdaUpdate()
                    .in(LogisticsReconDetailSubEntity::getId, batch)
                    .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                            LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                    .set(LogisticsReconDetailSubEntity::getUpdateTime, now)
                    .update();
        }
    }

    /** 认领匹配时可覆盖的前置 match_status（未匹配 / 失败） */
    private static final List<String> MATCH_CLAIM_FROM_STATUSES = Arrays.asList(
            LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode(),
            LogisticsReconDetailMatchStatusEnum.FAILED.getCode());

    /** 回写匹配结果时的前置 match_status（仅处理仍处于匹配中的费用项） */
    private static final List<String> MATCHING_FROM_STATUS = Collections.singletonList(
            LogisticsReconDetailMatchStatusEnum.MATCHING.getCode());

    /** 解绑时可还原为未匹配的前置 match_status */
    private static final List<String> UNBIND_FROM_MATCH_STATUSES = Arrays.asList(
            LogisticsReconDetailMatchStatusEnum.MATCHED.getCode(),
            LogisticsReconDetailMatchStatusEnum.FAILED.getCode());

    /**
     * 三个匹配入口（主表整批 / 手动 / 导入）共用的核心编排：
     * 加载导入模板配置 → 构造匹配上下文 → 调用 reconMatchAndGenerate 生成物流费用。
     *
     * @param entity           对账单主表
     * @param units            待匹配单元（费用项 + 所属明细 + 识别号覆盖）
     * @param matchByProvided  是否按 units 提供的识别字段匹配（手动/导入为 true，自动整批为 false）
     * @return 匹配编排结果（供 commitReconMatchResult 落库）
     */
    private LogisticsReconMatchExecutionResultDTO executeReconMatch(LogisticsReconEntity entity,
                                                   List<ReconMatchUnit> units,
                                                   boolean matchByProvided) {
        if (CollUtil.isEmpty(units)) {
            return new LogisticsReconMatchExecutionResultDTO(Collections.emptyList(),
                    Collections.emptyMap(), Collections.emptyMap());
        }
        CfgLogisticsCostImportEntity costImportEntity = cfgLogisticsCostImportService.getById(entity.getCfgImportId());
        if (ObjectUtil.isEmpty(costImportEntity)) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORT_TEMPLATE_NOT_RECOGNIZED);
        }
        validateReconMatchIdentifyType(costImportEntity);
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
        ctx.setMatchByProvidedIdentifyKeys(matchByProvided);
        ctx.setRows(rows);

        List<LogisticsReconMatchDTO.MatchResultDTO> matchResults = importHistoryRecordService.reconMatchAndGenerate(ctx);
        return new LogisticsReconMatchExecutionResultDTO(matchResults, rowKeyToDetailId, rowKeyToSubs);
    }

    /**
     * 校验费用项导入配置的识别维度枚举是否合法（非法时阻断匹配）。
     */
    private void validateReconMatchIdentifyType(CfgLogisticsCostImportEntity costImportEntity) {
        String identifyType = costImportEntity.getIdentifyType();
        if (StrUtil.isBlank(identifyType)) {
            return;
        }
        if (!CfgLogisticsCostImportIdentifyTypeEnum.isValidCode(identifyType)) {
            throw new ServiceException("费用项配置识别维度不合法");
        }
    }

    /**
     * 提交匹配结果（独立事务）：成功行写 ref 关联并置 matched，失败行置 failed + 原因。
     */
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
        /** 匹配行键（手动/导入一般为 detailSubId） */
        private final String rowKey;
        /** 所属对账明细 */
        private final LogisticsReconDetailEntity detail;
        /** 待匹配费用项 */
        private final LogisticsReconDetailSubEntity sub;
        /** 识别字段覆盖（ERP 单号 → targetField） */
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
                    .update();
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
        Set<String> seenDetailSubIds = new HashSet<>();
        for (LogisticsReconMatchDTO.SubErpInputDTO input : inputs) {
            String detailSubId = input.getDetailSubId();
            if (!seenDetailSubIds.add(detailSubId)) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "费用项重复提交匹配"));
                continue;
            }
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
            List<String> claimIds = claimByMain.getOrDefault(mainId, Collections.emptyList()).stream()
                    .distinct().collect(Collectors.toList());
            LogisticsReconEntity mainEntity = super.getById(mainId);
            if (mainEntity == null) {
                for (LogisticsReconMatchDTO.SubErpInputDTO input : mainInputs) {
                    results.add(BatchResultDTO.fail(input.getDetailSubId(), input.getDetailSubId(), "对账单不存在"));
                }
                continue;
            }
            if (!LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(mainEntity.getCheckStatus())) {
                for (LogisticsReconMatchDTO.SubErpInputDTO input : mainInputs) {
                    results.add(BatchResultDTO.fail(input.getDetailSubId(), input.getDetailSubId(),
                            ApiError.LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_MATCH.getMsg()));
                }
                continue;
            }
            List<String> claimedIds = logisticsReconDetailSubService.batchClaimMatchStatus(claimIds,
                    LogisticsReconDetailMatchStatusEnum.MATCHING.getCode(), null, MATCH_CLAIM_FROM_STATUSES);
            Set<String> claimedSet = new HashSet<>(claimedIds);
            Map<String, LogisticsReconMatchDTO.SubErpInputDTO> claimedInputMap = new LinkedHashMap<>();
            for (LogisticsReconMatchDTO.SubErpInputDTO input : mainInputs) {
                if (claimedSet.contains(input.getDetailSubId())) {
                    claimedInputMap.putIfAbsent(input.getDetailSubId(), input);
                }
            }
            List<LogisticsReconMatchDTO.SubErpInputDTO> claimedInputs =
                    new ArrayList<>(claimedInputMap.values());
            for (LogisticsReconMatchDTO.SubErpInputDTO input : mainInputs) {
                if (!claimedSet.contains(input.getDetailSubId())) {
                    results.add(BatchResultDTO.fail(input.getDetailSubId(), input.getDetailSubId(),
                            "费用项状态已变更或匹配进行中"));
                }
            }
            if (CollUtil.isEmpty(claimedInputs)) {
                continue;
            }
            try {
                logisticsReconMatchPool.submit(() -> asyncMatchDetailSubsByErp(mainId, claimedInputs, matchType,
                        claimedIds, user));
            } catch (RejectedExecutionException e) {
                log.warn("[submitManualMatch] 匹配线程池已满 mainId={}", mainId, e);
                self.markReconMatchFailed(mainId, claimedIds, ApiError.LOGISTICS_RECON_MATCH_POOL_BUSY.getMsg());
                for (LogisticsReconMatchDTO.SubErpInputDTO input : claimedInputs) {
                    results.add(BatchResultDTO.fail(input.getDetailSubId(), input.getDetailSubId(),
                            ApiError.LOGISTICS_RECON_MATCH_POOL_BUSY.getMsg()));
                }
                continue;
            }
            for (LogisticsReconMatchDTO.SubErpInputDTO input : claimedInputs) {
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
            touchMatchingSubsUpdateTime(claimIds);
            self.matchDetailSubsByErp(mainId, inputs, matchType);
        } catch (Exception e) {
            log.error("[asyncMatchDetailSubsByErp] 匹配失败 mainId={}", mainId, e);
            try {
                self.markReconMatchFailed(mainId, claimIds, LogisticsReconMatchFailReasonSupport.resolve(e));
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

    @Override
    public List<BatchResultDTO> matchDetailSubsByErp(String mainId, List<LogisticsReconMatchDTO.SubErpInputDTO> inputs, String matchType) {
        List<BatchResultDTO> results = new ArrayList<>(inputs.size());
        LogisticsReconEntity entity = super.getByIdOpt(mainId)
                .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));
        if (!LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(entity.getCheckStatus())) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_MATCH);
        }

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

        if (CollUtil.isEmpty(units)) {
            return results;
        }
        List<String> matchingSubIds = units.stream().map(unit -> unit.sub.getId()).collect(Collectors.toList());
        touchMatchingSubsUpdateTime(matchingSubIds);

        // 复用统一核心编排（与导入一致：同识别号合并费用、多物流单重量分摊）
        LogisticsReconMatchExecutionResultDTO executionResult = executeReconMatch(entity, units, true);
        touchMatchingSubsUpdateTime(matchingSubIds);
        self.commitReconMatchResult(mainId, matchType, executionResult.getRowKeyToDetailId(),
                executionResult.getRowKeyToSubs(), executionResult.getMatchResults());
        try {
            writeErpSnapshot(inputs, executionResult.getMatchResults());
        } catch (Exception e) {
            log.warn("[matchDetailSubsByErp] ERP 单号快照回写失败 mainId={}", mainId, e);
        }
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
                String value = LogisticsReconMatchGroupHelper.detailIdentifyValue(detail, field);
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
            item.setCostName(sub.getCostName());
            item.setActualAmount(sub.getActualAmount());
            item.setEstimatedAmount(sub.getEstimatedAmount());
            return item;
        }).collect(Collectors.toList());
        row.setCostItems(costItems);
        return row;
    }

    /**
     * ERP 单号 → 识别字段（targetField）覆盖映射，仅保留模板唯一键覆盖到的字段。
     * @author Will
     * @date 2026/6/11
     */
    private Map<String, String> buildErpIdentifyOverride(LogisticsReconMatchDTO.SubErpInputDTO input) {
        Map<String, String> override = new HashMap<>();
        if (StrUtil.isNotBlank(input.getErpSoCode())) {
            override.put(LogisticsCostImportTargetFieldConstant.SOURCE_CODE, input.getErpSoCode().trim());
        }
        if (StrUtil.isNotBlank(input.getErpPlatformOrderNo())) {
            override.put(LogisticsCostImportTargetFieldConstant.PLATFORM_CODE, input.getErpPlatformOrderNo().trim());
        }
        if (StrUtil.isNotBlank(input.getErpTrackNo())) {
            override.put(LogisticsCostImportTargetFieldConstant.TRACK_NO, input.getErpTrackNo().trim());
        }
        if (StrUtil.isNotBlank(input.getErpSoDeliveryCode())) {
            override.put(LogisticsCostImportTargetFieldConstant.SO_DELIVERY_CODE, input.getErpSoDeliveryCode().trim());
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
                    .update();
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
        List<String> successWithoutRefSubIds = new ArrayList<>();
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
                } else {
                    successWithoutRefSubIds.add(sub.getId());
                }
            }
        }
        if (CollUtil.isNotEmpty(successWithoutRefSubIds)) {
            logisticsReconDetailSubService.batchUpdateMatchStatus(successWithoutRefSubIds,
                    LogisticsReconDetailMatchStatusEnum.FAILED.getCode(), "匹配成功但未生成关联关系",
                    MATCHING_FROM_STATUS);
        }
        if (byDetail) {
            logisticsReconRefLogisticsBillService.saveBatchByDetail(refList);
        } else {
            logisticsReconRefLogisticsBillService.saveBatchByDetailSub(refList);
        }
        updateMatchedSubResolvedCfgCost(matchResults, matchedSubIds);
        logisticsReconDetailSubService.batchUpdateMatchStatus(matchedSubIds,
                LogisticsReconDetailMatchStatusEnum.MATCHED.getCode(), null, MATCHING_FROM_STATUS);
    }

    /**
     * 匹配成功且已生成关联关系的费用项，回写 ERP 费用配置到 detail_sub。
     */
    private void updateMatchedSubResolvedCfgCost(List<LogisticsReconMatchDTO.MatchResultDTO> matchResults,
                                                 List<String> matchedSubIds) {
        if (CollUtil.isEmpty(matchedSubIds) || CollUtil.isEmpty(matchResults)) {
            return;
        }
        Map<String, LogisticsReconMatchDTO.ResolvedCfgCostDTO> resolvedBySubId = matchResults.stream()
                .filter(LogisticsReconMatchDTO.MatchResultDTO::isSuccess)
                .map(LogisticsReconMatchDTO.MatchResultDTO::getResolvedCfgCostBySubId)
                .filter(CollUtil::isNotEmpty)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));
        if (CollUtil.isEmpty(resolvedBySubId)) {
            return;
        }
        logisticsReconDetailSubService.batchUpdateResolvedCfgCost(resolvedBySubId, matchedSubIds);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void confirmBillRefCostBatch(String mainId, List<String> batchCostIds, String reconciliationStatus,
                                        LocalDateTime confirmTime) {
        if (CollUtil.isEmpty(batchCostIds)) {
            return;
        }
        List<String> distinctCostIds = batchCostIds.stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        int costUpdated = logisticsBillCostService.batchUpdateReconciliationStatus(distinctCostIds, reconciliationStatus,
                confirmTime);
        if (costUpdated != distinctCostIds.size()) {
            log.warn("[confirmBillRefCostBatch] cost update mismatch mainId={} expected={} actual={} reconciliationStatus={}",
                    mainId, distinctCostIds.size(), costUpdated, reconciliationStatus);
            throw new ServiceException(ApiError.LOGISTICS_RECON_CONFIRM_COST_UPDATE_MISMATCH);
        }
        int refUpdated = 0;
        for (int i = 0; i < distinctCostIds.size(); i += MATCH_ID_BATCH_SIZE) {
            List<String> costIdBatch = distinctCostIds.subList(i,
                    Math.min(distinctCostIds.size(), i + MATCH_ID_BATCH_SIZE));
            long expectedRefCount = buildConfirmRefCountQuery(mainId, costIdBatch, reconciliationStatus).count();
            LambdaUpdateChainWrapper<LogisticsReconRefLogisticsBillEntity> refUpdateChain =
                    logisticsReconRefLogisticsBillService.lambdaUpdate()
                    .eq(LogisticsReconRefLogisticsBillEntity::getMainId, mainId)
                    .in(LogisticsReconRefLogisticsBillEntity::getLogisticsBillCostId, costIdBatch)
                    .set(LogisticsReconRefLogisticsBillEntity::getReconciliationStatus, reconciliationStatus);
            if (ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus)) {
                refUpdateChain.eq(LogisticsReconRefLogisticsBillEntity::getReconciliationStatus,
                        ReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
                applyMatchedSubExists(refUpdateChain, mainId);
            } else if (ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(reconciliationStatus)) {
                refUpdateChain.eq(LogisticsReconRefLogisticsBillEntity::getReconciliationStatus,
                        ReconciliationStatusEnum.CONFIRMED.getCode());
            }
            int batchRefUpdated = logisticsReconRefLogisticsBillService.getBaseMapper()
                    .update(null, refUpdateChain.getWrapper());
            if (batchRefUpdated != expectedRefCount) {
                throw new ServiceException(ApiError.LOGISTICS_RECON_CONFIRM_COST_UPDATE_MISMATCH);
            }
            refUpdated += batchRefUpdated;
        }
        log.info("[confirmBillRefCostBatch] mainId={} costCount={} refUpdated={}", mainId, distinctCostIds.size(), refUpdated);
    }

    /**
     * 账单确认（→账单确认）时仅处理已匹配费用项下的关联 ref。
     */
    private void applyMatchedSubExists(LambdaQueryChainWrapper<LogisticsReconRefLogisticsBillEntity> query,
                                       String mainId) {
        query.apply("EXISTS (SELECT 1 FROM logistics_recon_detail_sub sub "
                        + "WHERE sub.id = logistics_recon_ref_logistics_bill.detail_sub_id "
                        + "AND sub.main_id = {0} AND sub.match_status = {1} AND sub.is_deleted = false)",
                mainId, LogisticsReconDetailMatchStatusEnum.MATCHED.getCode());
    }

    private void applyMatchedSubExists(LambdaUpdateChainWrapper<LogisticsReconRefLogisticsBillEntity> updateChain,
                                       String mainId) {
        updateChain.apply("EXISTS (SELECT 1 FROM logistics_recon_detail_sub sub "
                        + "WHERE sub.id = logistics_recon_ref_logistics_bill.detail_sub_id "
                        + "AND sub.main_id = {0} AND sub.match_status = {1} AND sub.is_deleted = false)",
                mainId, LogisticsReconDetailMatchStatusEnum.MATCHED.getCode());
    }

    /**
     * 扫描对账单关联 ref，统计可执行账单确认/回退的物流费用单。
     */
    private ConfirmBillScanResult scanConfirmBillEligibility(String mainId, String targetReconciliationStatus) {
        ConfirmBillScanResult result = new ConfirmBillScanResult();
        String lastRefId = null;
        while (true) {
            LambdaQueryChainWrapper<LogisticsReconRefLogisticsBillEntity> refQuery =
                    logisticsReconRefLogisticsBillService.lambdaQuery()
                            .eq(LogisticsReconRefLogisticsBillEntity::getMainId, mainId)
                            .eq(LogisticsReconRefLogisticsBillEntity::getIsDeleted, false)
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
            result.hasRef = true;

            List<String> subIds = refBatch.stream()
                    .map(LogisticsReconRefLogisticsBillEntity::getDetailSubId)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            List<String> costIds = refBatch.stream()
                    .map(LogisticsReconRefLogisticsBillEntity::getLogisticsBillCostId)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            Map<String, LogisticsReconDetailSubEntity> subMap = CollUtil.isEmpty(subIds)
                    ? Collections.emptyMap()
                    : logisticsReconDetailSubService.listByIds(subIds).stream()
                    .collect(Collectors.toMap(LogisticsReconDetailSubEntity::getId, s -> s, (a, b) -> a));
            Map<String, LogisticsBillCostEntity> costMap = CollUtil.isEmpty(costIds)
                    ? Collections.emptyMap()
                    : logisticsBillCostService.listByIds(costIds).stream()
                    .collect(Collectors.toMap(LogisticsBillCostEntity::getId, c -> c, (a, b) -> a));

            for (LogisticsReconRefLogisticsBillEntity ref : refBatch) {
                if (ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(ref.getReconciliationStatus())) {
                    result.refToBeConfirmCount++;
                } else if (ReconciliationStatusEnum.CONFIRMED.getCode().equals(ref.getReconciliationStatus())) {
                    result.refConfirmedCount++;
                } else {
                    result.refOtherCount++;
                }
                if (isEligibleConfirmBillRef(ref, subMap, costMap, targetReconciliationStatus)) {
                    result.eligibleCostIds.add(ref.getLogisticsBillCostId());
                } else if (ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(targetReconciliationStatus)
                        && ReconciliationStatusEnum.CONFIRMED.getCode().equals(ref.getReconciliationStatus())) {
                    LogisticsBillCostEntity cost = costMap.get(ref.getLogisticsBillCostId());
                    if (cost != null
                            && ReconciliationStatusEnum.CONFIRMED.getCode().equals(cost.getReconciliationStatus())
                            && !isRevertibleBillCost(cost)) {
                        result.hasNonRevertibleConfirmedRef = true;
                    }
                }
            }
        }
        return result;
    }

    private boolean isEligibleConfirmBillRef(LogisticsReconRefLogisticsBillEntity ref,
                                              Map<String, LogisticsReconDetailSubEntity> subMap,
                                              Map<String, LogisticsBillCostEntity> costMap,
                                              String targetReconciliationStatus) {
        if (ref == null || StrUtil.isBlank(ref.getLogisticsBillCostId())) {
            return false;
        }
        LogisticsBillCostEntity cost = costMap.get(ref.getLogisticsBillCostId());
        if (cost == null) {
            return false;
        }
        if (ReconciliationStatusEnum.CONFIRMED.getCode().equals(targetReconciliationStatus)) {
            if (!ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(ref.getReconciliationStatus())) {
                return false;
            }
            LogisticsReconDetailSubEntity sub = subMap.get(ref.getDetailSubId());
            if (sub == null || !LogisticsReconDetailMatchStatusEnum.MATCHED.getCode().equals(sub.getMatchStatus())) {
                return false;
            }
            return ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(cost.getReconciliationStatus());
        }
        if (ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(targetReconciliationStatus)) {
            if (!ReconciliationStatusEnum.CONFIRMED.getCode().equals(ref.getReconciliationStatus())) {
                return false;
            }
            if (!ReconciliationStatusEnum.CONFIRMED.getCode().equals(cost.getReconciliationStatus())) {
                return false;
            }
            return isRevertibleBillCost(cost);
        }
        return false;
    }

    private boolean isRevertibleBillCost(LogisticsBillCostEntity cost) {
        return cost != null
                && LogisticsBillCostCheckStatusEnum.CHECKING.getCode().equals(cost.getCheckStatus())
                && LogisticsBillCostPayStateEnum.PAYMENT.getCode().equals(cost.getPayStatus());
    }

    private void assertConfirmBillEligibility(ConfirmBillScanResult scanResult, String targetReconciliationStatus) {
        if (!scanResult.hasRef) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_MATCHED_BILL_COST_NOT_FOUND);
        }
        if (CollUtil.isNotEmpty(scanResult.eligibleCostIds)) {
            return;
        }
        if (ReconciliationStatusEnum.CONFIRMED.getCode().equals(targetReconciliationStatus)) {
            if (scanResult.refToBeConfirmCount <= 0 && scanResult.refOtherCount <= 0) {
                throw new ServiceException(ApiError.LOGISTICS_RECON_BILL_CONFIRM_ALREADY_CONFIRMED);
            }
            throw new ServiceException(ApiError.LOGISTICS_RECON_BILL_CONFIRM_NO_ELIGIBLE);
        }
        if (scanResult.refConfirmedCount <= 0 && scanResult.refOtherCount <= 0) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_BILL_CONFIRM_ALREADY_TO_BE_CONFIRM);
        }
        if (scanResult.hasNonRevertibleConfirmedRef) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_BILL_REVERT_COST_STATUS_FORBIDDEN);
        }
        throw new ServiceException(ApiError.LOGISTICS_RECON_BILL_REVERT_NO_ELIGIBLE);
    }

    private static class ConfirmBillScanResult {
        private boolean hasRef;
        private long refToBeConfirmCount;
        private long refConfirmedCount;
        private long refOtherCount;
        private boolean hasNonRevertibleConfirmedRef;
        private final Set<String> eligibleCostIds = new LinkedHashSet<>();
    }

    /**
     * 构建账单确认时需更新的 ref 计数查询（带当前对账状态前置条件，用于乐观校验）。
     *
     * @author Will
     * @date 2026/6/12
     * @param mainId                对账单 id
     * @param costIdBatch           本批物流费用单 id
     * @param reconciliationStatus  目标对账状态
     * @return 带状态前置条件的 ref 计数查询
     */
    private LambdaQueryChainWrapper<LogisticsReconRefLogisticsBillEntity> buildConfirmRefCountQuery(
            String mainId, List<String> costIdBatch, String reconciliationStatus) {
        LambdaQueryChainWrapper<LogisticsReconRefLogisticsBillEntity> query =
                logisticsReconRefLogisticsBillService.lambdaQuery()
                        .eq(LogisticsReconRefLogisticsBillEntity::getMainId, mainId)
                        .eq(LogisticsReconRefLogisticsBillEntity::getIsDeleted, false)
                        .in(LogisticsReconRefLogisticsBillEntity::getLogisticsBillCostId, costIdBatch);
        if (ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus)) {
            query.eq(LogisticsReconRefLogisticsBillEntity::getReconciliationStatus,
                    ReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
            applyMatchedSubExists(query, mainId);
        } else if (ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(reconciliationStatus)) {
            query.eq(LogisticsReconRefLogisticsBillEntity::getReconciliationStatus,
                    ReconciliationStatusEnum.CONFIRMED.getCode());
        }
        return query;
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
        long matchingCount = logisticsReconDetailSubService.lambdaQuery()
                .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                        LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                .count();
        if (matchingCount > 0) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_MATCHING_CONFIRM_FORBIDDEN);
        }
        ConfirmBillScanResult scanResult = scanConfirmBillEligibility(mainId, reconciliationStatus);
        assertConfirmBillEligibility(scanResult, reconciliationStatus);
        List<String> eligibleCostIds = new ArrayList<>(scanResult.eligibleCostIds);
        int batchNo = 0;
        for (int i = 0; i < eligibleCostIds.size(); i += MATCH_ID_BATCH_SIZE) {
            List<String> batchCostIds = eligibleCostIds.subList(i,
                    Math.min(eligibleCostIds.size(), i + MATCH_ID_BATCH_SIZE));
            batchNo++;
            try {
                self.confirmBillRefCostBatch(mainId, batchCostIds, reconciliationStatus, effectiveConfirmTime);
            } catch (Exception e) {
                log.error("[confirmBill] 分片确认失败 mainId={} batchNo={}", mainId, batchNo, e);
                if (e instanceof ServiceException) {
                    throw e;
                }
                throw new ServiceException(ApiError.LOGISTICS_RECON_CONFIRM_PARTIAL_FAILURE, batchNo);
            }
        }
        self.refreshDetailSubReconciliationStatusInTx(mainId);
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
        List<String> requestSubIds = dto.getDetailSubIds().stream()
                .filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(requestSubIds)) {
            return Collections.emptyList();
        }
        Map<String, LogisticsReconDetailSubEntity> subMap = new HashMap<>();
        for (int i = 0; i < requestSubIds.size(); i += MATCH_ID_BATCH_SIZE) {
            List<String> batch = requestSubIds.subList(i, Math.min(requestSubIds.size(), i + MATCH_ID_BATCH_SIZE));
            logisticsReconDetailSubService.lambdaQuery()
                    .eq(LogisticsReconDetailSubEntity::getMainId, dto.getMainId())
                    .in(LogisticsReconDetailSubEntity::getId, batch)
                    .list()
                    .forEach(sub -> subMap.put(sub.getId(), sub));
        }
        long matchingCount = subMap.values().stream()
                .filter(sub -> LogisticsReconDetailMatchStatusEnum.MATCHING.getCode().equals(sub.getMatchStatus()))
                .count();
        if (matchingCount > 0) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_MATCHING_CONFIRM_FORBIDDEN);
        }
        List<String> eligibleSubIds = new ArrayList<>();
        for (String detailSubId : requestSubIds) {
            LogisticsReconDetailSubEntity sub = subMap.get(detailSubId);
            if (sub == null) {
                continue;
            }
            if (UNBIND_FROM_MATCH_STATUSES.contains(sub.getMatchStatus())) {
                eligibleSubIds.add(detailSubId);
            }
        }
        if (CollUtil.isNotEmpty(eligibleSubIds)) {
            logisticsReconRefLogisticsBillService.removeByDetailSubIds(eligibleSubIds);
            logisticsReconDetailSubService.batchUpdateMatchStatus(eligibleSubIds,
                    LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode(), null, UNBIND_FROM_MATCH_STATUSES);
            for (int i = 0; i < eligibleSubIds.size(); i += MATCH_ID_BATCH_SIZE) {
                List<String> batch = eligibleSubIds.subList(i,
                        Math.min(eligibleSubIds.size(), i + MATCH_ID_BATCH_SIZE));
                logisticsReconDetailSubService.lambdaUpdate()
                        .in(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getId, batch)
                        .set(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getReconciliationStatus,
                                LogisticsReconReconciliationStatusEnum.TO_BE_CONFIRM.getCode())
                        .update();
            }
            self.refreshDetailSubReconciliationStatusInTx(dto.getMainId());
        }
        Set<String> eligibleSet = new HashSet<>(eligibleSubIds);
        List<BatchResultDTO> results = new ArrayList<>(requestSubIds.size());
        for (String detailSubId : requestSubIds) {
            LogisticsReconDetailSubEntity sub = subMap.get(detailSubId);
            if (sub == null) {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "对账费用项不存在或不属于当前对账单"));
            } else if (eligibleSet.contains(detailSubId)) {
                results.add(BatchResultDTO.success(detailSubId, detailSubId, OperationTypeEnum.UPDATE));
            } else {
                results.add(BatchResultDTO.fail(detailSubId, detailSubId, "费用项状态不允许解绑"));
            }
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
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshDetailSubReconciliationStatusInTx(String mainId) {
        refreshDetailSubReconciliationStatus(mainId);
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
                    .update();
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
