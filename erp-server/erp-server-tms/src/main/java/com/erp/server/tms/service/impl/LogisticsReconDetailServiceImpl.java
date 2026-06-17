package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.common.business.dto.FindUserDTO;
import com.common.business.vo.LoginUser;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.LogisticsReconDetailDTO;
import com.erp.model.tms.dto.LogisticsReconMatchDTO;
import com.erp.model.tms.dto.excel.LogisticsReconMatchImportExcelDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.entity.LogisticsReconDetailEntity;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;
import com.erp.model.tms.entity.LogisticsReconEntity;
import com.erp.model.tms.enums.LogisticsReconCheckStatusEnum;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconReconciliationStatusEnum;
import com.erp.model.tms.enums.LogisticsReconRefMatchTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.constant.LogisticsReconImportMatchConstant;
import com.erp.server.tms.listener.LogisticsReconMatchImportExcelListener;
import com.erp.server.tms.mapper.LogisticsReconDetailMapper;
import com.erp.server.tms.service.CfgLogisticsCostImportDetailService;
import com.erp.server.tms.service.CfgLogisticsCostImportService;
import com.erp.server.tms.service.LogisticsReconDetailService;
import com.erp.server.tms.service.LogisticsReconDetailSubService;
import com.erp.server.tms.service.LogisticsReconService;
import com.erp.server.tms.service.support.LogisticsReconImportMatchContext;
import com.erp.server.tms.util.LogisticsReconMatchGroupHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_RECON_DETAIL;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_TMS_LOGISTICS_RECON_MATCH;

/**
 * <p>
 * 物流商对账明细（行级） 服务实现类
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Slf4j
@Service
public class LogisticsReconDetailServiceImpl
        extends SuperServiceImpl<LogisticsReconDetailMapper, LogisticsReconDetailEntity>
        implements LogisticsReconDetailService {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private LogisticsReconDetailSubService logisticsReconDetailSubService;

    @Lazy
    @Resource
    private LogisticsReconService logisticsReconService;

    @Resource
    private FileFeign fileFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CfgLogisticsCostImportDetailService cfgLogisticsCostImportDetailService;

    @Resource
    private CfgLogisticsCostImportService cfgLogisticsCostImportService;

    /** 识别未命中对账明细时的统一提示 */
    private static final String IMPORT_MATCH_IDENTIFY_NOT_FOUND = "未匹配到对账明细";

    @Override
    public PagingVO<LogisticsReconDetailDTO.ListDTO> paging(PagingDTO<LogisticsReconDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<LogisticsReconDetailDTO.ListDTO> query =
                new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<LogisticsReconDetailDTO.ListDTO> pageData = baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public List<LogisticsReconDetailDTO.TabListDTO> tabList(LogisticsReconDetailDTO.TabListParamDTO param) {
        List<LogisticsReconDetailDTO.TabListDTO> list = baseMapper.tabList(param);
        if (list == null) {
            list = new ArrayList<>();
        }
        List<String> existStatus = list.stream()
                .map(LogisticsReconDetailDTO.TabListDTO::getTabFlag)
                .collect(Collectors.toList());
        for (String status : LogisticsReconDetailMatchStatusEnum.getStatusList()) {
            if (!existStatus.contains(status)) {
                list.add(new LogisticsReconDetailDTO.TabListDTO(status, 0));
            }
        }
        list.add(new LogisticsReconDetailDTO.TabListDTO("all",
                list.stream().mapToInt(LogisticsReconDetailDTO.TabListDTO::getCount).sum()));
        return list;
    }

    @Override
    public void exportList(LogisticsReconDetailDTO.PagingParamDTO param) {
        downloadTaskFeign.saveDownloadTask("物流商对账明细", EXPORT_TMS_LOGISTICS_RECON_DETAIL.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByMainIds(Collection<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return;
        }
        lambdaUpdate()
                .in(LogisticsReconDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<LogisticsReconDetailEntity> listByMainIds(Collection<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(LogisticsReconDetailEntity::getMainId, mainIds)
                .list();
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/logisticsReconDetailTemplate.xlsx";
        String excelName = "物流商对账明细导入模板.xlsx";
        ExcelUtil.downloadTemplate(path, excelName, response);
    }

    @Override
    public List<BatchResultDTO> importMatch(LogisticsReconDetailDTO.ImportMatchDTO dto) {
        // 标准异步导入：控制层循环逐文件提交导入任务，单文件失败不影响其它文件
        String userId = UserContext.getDefaultLoginUser().getUid();
        List<BatchResultDTO> results = new ArrayList<>(dto.getList().size());
        for (BaseDTO.ImportDTO file : dto.getList()) {
            try {
                LogisticsReconDetailDTO.ImportMatchSyncDTO syncDTO =
                        new LogisticsReconDetailDTO.ImportMatchSyncDTO(dto.getMainId(), file);
                syncDTO.setUserId(userId);
                String taskId = downloadTaskFeign.saveImportTask("物流商对账导入匹配",
                        IMPORT_TMS_LOGISTICS_RECON_MATCH.getCode(), syncDTO);
                results.add(BatchResultDTO.success(taskId, file.getFileName()));
            } catch (Exception e) {
                log.error("[importMatch] 提交导入匹配任务失败 fileName={}", file.getFileName(), e);
                results.add(BatchResultDTO.fail(file.getTaskId(), file.getFileName(), e.getMessage()));
            }
        }
        return results;
    }

    @Override
    public void executeImportMatchTask(LogisticsReconDetailDTO.ImportMatchSyncDTO dto) {
        BaseDTO.ImportResultDTO resultDTO = new BaseDTO.ImportResultDTO();
        resultDTO.setTaskId(dto.getTaskId());
        LoginUser prev = UserContext.getLoginUser();
        try {
            setupImportMatchUserContext(dto.getUserId());
            LogisticsReconEntity main = logisticsReconService.getById(dto.getMainId());
            if (main == null) {
                throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "物流商对账单");
            }
            if (!LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(main.getCheckStatus())) {
                throw new ServiceException(ApiError.LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_MATCH);
            }
            LogisticsReconImportMatchContext matchContext = buildImportMatchContext(main);
            byte[] fileBytes = fileFeign.downloadFile(dto.getFileUrl());
            LogisticsReconMatchImportExcelListener listener =
                    new LogisticsReconMatchImportExcelListener(matchContext);
            EasyExcel.read(new ByteArrayInputStream(fileBytes), LogisticsReconMatchImportExcelDTO.class, listener)
                    .sheet().doRead();
            if (listener.getTotalCount() == 0) {
                throw new ServiceException(ApiError.FILE_IMPORT_DATA_NOT_NULL, "对账导入匹配");
            }

            List<LogisticsReconMatchImportExcelDTO> errorRows = new ArrayList<>();
            for (Map.Entry<String, LogisticsReconMatchImportExcelDTO> entry : listener.getRowByGroupKey().entrySet()) {
                String groupKey = entry.getKey();
                LogisticsReconMatchImportExcelDTO row = entry.getValue();
                String failReason = null;
                if (!listener.getMatchedGroupKeySet().contains(groupKey)) {
                    failReason = IMPORT_MATCH_IDENTIFY_NOT_FOUND;
                } else if (listener.getGroupErrorMap().containsKey(groupKey)) {
                    failReason = String.join("；", listener.getGroupErrorMap().get(groupKey));
                }
                if (StrUtil.isNotBlank(failReason)) {
                    row.setMatchResult("失败");
                    row.setErrorMsg(failReason);
                    errorRows.add(row);
                }
            }

            resultDTO.setCount(listener.getTotalCount());
            resultDTO.setErrorUrl(uploadMatchErrorFile(errorRows));
            resultDTO.setFinishTime(LocalDateTime.now());
            resultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
            resultDTO.setRemark("处理完成，失败" + errorRows.size() + "条；匹配任务已异步提交，请稍后查看明细匹配结果");
            downloadTaskFeign.updateTask(resultDTO);
        } catch (Exception e) {
            log.error("[executeImportMatchTask] 导入匹配失败 mainId={} fileName={}", dto.getMainId(), dto.getFileName(), e);
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            resultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            resultDTO.setRemark(msg.length() > 490 ? msg.substring(0, 490) : msg);
            downloadTaskFeign.updateTask(resultDTO);
        } finally {
            if (prev != null) {
                UserContext.setLoginUser(prev);
            } else {
                UserContext.clear();
            }
        }
    }

    /**
     * 导入匹配异步回调：按操作人 id 恢复登录上下文。
     *
     * @author Will
     * @date 2026/6/12
     * @param userId 操作人 id
     */
    private void setupImportMatchUserContext(String userId) {
        if (StrUtil.isBlank(userId)) {
            return;
        }
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Collections.singletonList(userId));
        FindUserDTO findUserDTO = userList.stream()
                .filter(user -> Objects.equals(user.getUserId(), userId))
                .findFirst()
                .orElse(null);
        if (findUserDTO != null) {
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }
    }

    /**
     * 导入匹配分批处理：按模板识别号分组合并费用项，认领后异步提交匹配。
     *
     * @author Will
     * @date 2026/6/12
     * @param context            导入匹配上下文（识别号分组后的账单费用项）
     * @param excelBatch         本批 Excel 行
     * @param groupErrorMap      识别号分组键 → 错误文案（可累积）
     * @param matchedGroupKeySet 本文件已命中账单识别组的键（可累积，允许 null）
     * @param handledGroupKeySet 本文件已处理过的识别组（跨分批去重，可累积，允许 null）
     */
    @Override
    public void processImportMatchBatch(LogisticsReconImportMatchContext context,
                                        List<LogisticsReconMatchImportExcelDTO> excelBatch,
                                        Map<String, List<String>> groupErrorMap,
                                        Set<String> matchedGroupKeySet,
                                        Set<String> handledGroupKeySet) {
        if (context == null || CollUtil.isEmpty(excelBatch)) {
            return;
        }
        List<CfgLogisticsCostImportDetailEntity> uniqueKeyList = context.getUniqueKeyList();
        Map<String, LogisticsReconMatchImportExcelDTO> batchRowByGroupKey = new LinkedHashMap<>();
        for (LogisticsReconMatchImportExcelDTO row : excelBatch) {
            if (LogisticsReconMatchGroupHelper.isTemplateRowBlank(row)) {
                continue;
            }
            if (!LogisticsReconMatchGroupHelper.isConfiguredIdentifyComplete(row, uniqueKeyList)) {
                continue;
            }
            String groupKey = LogisticsReconMatchGroupHelper.buildImportMatchExcelGroupKey(row, uniqueKeyList);
            batchRowByGroupKey.putIfAbsent(groupKey, row);
        }
        if (batchRowByGroupKey.isEmpty()) {
            return;
        }

        List<LogisticsReconMatchDTO.SubErpInputDTO> inputs = new ArrayList<>();
        Map<String, String> subIdToGroupKey = new HashMap<>();
        for (Map.Entry<String, LogisticsReconMatchImportExcelDTO> entry : batchRowByGroupKey.entrySet()) {
            String groupKey = entry.getKey();
            if (handledGroupKeySet != null && handledGroupKeySet.contains(groupKey)) {
                continue;
            }
            if (handledGroupKeySet != null) {
                handledGroupKeySet.add(groupKey);
            }
            LogisticsReconMatchImportExcelDTO row = entry.getValue();
            List<String> allSubIds = context.getGroupKeyToAllSubIds().get(groupKey);
            if (CollUtil.isEmpty(allSubIds)) {
                continue;
            }
            if (matchedGroupKeySet != null) {
                matchedGroupKeySet.add(groupKey);
            }
            List<String> eligibleSubIds = context.getGroupKeyToEligibleSubIds().get(groupKey);
            if (CollUtil.isEmpty(eligibleSubIds)) {
                groupErrorMap.computeIfAbsent(groupKey, key -> new ArrayList<>())
                        .add("费用项已匹配或已确认，无法重新匹配");
                continue;
            }
            for (String subId : eligibleSubIds) {
                LogisticsReconMatchDTO.SubErpInputDTO input = new LogisticsReconMatchDTO.SubErpInputDTO();
                input.setDetailSubId(subId);
                input.setErpSoCode(row.getErpSoCode());
                input.setErpPlatformOrderNo(row.getErpPlatformOrderNo());
                input.setErpTrackNo(row.getErpTrackNo());
                input.setErpSoDeliveryCode(row.getErpSoDeliveryCode());
                inputs.add(input);
                subIdToGroupKey.put(subId, groupKey);
            }
        }
        if (CollUtil.isEmpty(inputs)) {
            return;
        }
        submitImportMatchInputsByGroupChunks(context.getMainId(), inputs, subIdToGroupKey, groupErrorMap);
    }

    /**
     * 按识别组边界分片提交匹配，单组不拆分（保证组内费用合并匹配）。
     *
     * @author Will
     * @date 2026/6/12
     * @param mainId          对账单 id
     * @param inputs          待提交费用项
     * @param subIdToGroupKey 费用项 id → 识别组键
     * @param groupErrorMap   识别组错误累积
     */
    private void submitImportMatchInputsByGroupChunks(String mainId,
                                                      List<LogisticsReconMatchDTO.SubErpInputDTO> inputs,
                                                      Map<String, String> subIdToGroupKey,
                                                      Map<String, List<String>> groupErrorMap) {
        Map<String, List<LogisticsReconMatchDTO.SubErpInputDTO>> inputsByGroupKey = new LinkedHashMap<>();
        for (LogisticsReconMatchDTO.SubErpInputDTO input : inputs) {
            String groupKey = subIdToGroupKey.get(input.getDetailSubId());
            if (StrUtil.isBlank(groupKey)) {
                continue;
            }
            inputsByGroupKey.computeIfAbsent(groupKey, key -> new ArrayList<>()).add(input);
        }
        List<LogisticsReconMatchDTO.SubErpInputDTO> chunk = new ArrayList<>();
        for (Map.Entry<String, List<LogisticsReconMatchDTO.SubErpInputDTO>> entry : inputsByGroupKey.entrySet()) {
            String groupKey = entry.getKey();
            List<LogisticsReconMatchDTO.SubErpInputDTO> groupInputs = entry.getValue();
            if (groupInputs.size() > LogisticsReconImportMatchConstant.SUBMIT_CHUNK_SIZE) {
                log.warn("[submitImportMatchInputsByGroupChunks] 单识别组费用项超过提交分片阈值，整组提交不拆分 "
                                + "mainId={} groupKey={} subCount={} threshold={}",
                        mainId, groupKey, groupInputs.size(), LogisticsReconImportMatchConstant.SUBMIT_CHUNK_SIZE);
                if (CollUtil.isNotEmpty(chunk)) {
                    mergeSubmitResultsToGroupErrors(
                            logisticsReconService.submitManualMatch(chunk, LogisticsReconRefMatchTypeEnum.MANUAL.getCode()),
                            subIdToGroupKey, groupErrorMap);
                    chunk.clear();
                }
                mergeSubmitResultsToGroupErrors(
                        logisticsReconService.submitManualMatch(groupInputs, LogisticsReconRefMatchTypeEnum.MANUAL.getCode()),
                        subIdToGroupKey, groupErrorMap);
                continue;
            }
            if (chunk.size() + groupInputs.size() > LogisticsReconImportMatchConstant.SUBMIT_CHUNK_SIZE) {
                mergeSubmitResultsToGroupErrors(
                        logisticsReconService.submitManualMatch(chunk, LogisticsReconRefMatchTypeEnum.MANUAL.getCode()),
                        subIdToGroupKey, groupErrorMap);
                chunk.clear();
            }
            chunk.addAll(groupInputs);
        }
        if (CollUtil.isNotEmpty(chunk)) {
            mergeSubmitResultsToGroupErrors(
                    logisticsReconService.submitManualMatch(chunk, LogisticsReconRefMatchTypeEnum.MANUAL.getCode()),
                    subIdToGroupKey, groupErrorMap);
        }
    }

    /**
     * 合并同步提交阶段的失败结果到识别组错误 Map。
     *
     * @author Will
     * @date 2026/6/12
     * @param submitResults   提交结果
     * @param subIdToGroupKey 费用项 id → 识别组键
     * @param groupErrorMap   识别组错误累积
     */
    private void mergeSubmitResultsToGroupErrors(List<BatchResultDTO> submitResults,
                                                 Map<String, String> subIdToGroupKey,
                                                 Map<String, List<String>> groupErrorMap) {
        if (CollUtil.isEmpty(submitResults)) {
            return;
        }
        for (BatchResultDTO submitResult : submitResults) {
            if (Boolean.TRUE.equals(submitResult.getSuccess())) {
                continue;
            }
            String groupKey = subIdToGroupKey.get(submitResult.getCode());
            if (StrUtil.isBlank(groupKey)) {
                continue;
            }
            groupErrorMap.computeIfAbsent(groupKey, key -> new ArrayList<>()).add(submitResult.getMsg());
        }
    }

    /**
     * 预加载对账单费用项：按模板唯一识别字段分组（与导入匹配分组键一致）。
     *
     * @author Will
     * @date 2026/6/12
     * @param main 对账单主表
     * @return 导入匹配上下文
     */
    private LogisticsReconImportMatchContext buildImportMatchContext(LogisticsReconEntity main) {
        if (StrUtil.isBlank(main.getCfgImportId())) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORT_TEMPLATE_NOT_RECOGNIZED);
        }
        List<CfgLogisticsCostImportDetailEntity> cfgDetails = cfgLogisticsCostImportDetailService.lambdaQuery()
                .eq(CfgLogisticsCostImportDetailEntity::getMainId, main.getCfgImportId())
                .list();
        if (CollUtil.isEmpty(cfgDetails)) {
            throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
        }
        String importCfgName = resolveImportCfgName(main.getCfgImportId());
        List<CfgLogisticsCostImportDetailEntity> uniqueKeyList = extractUniqueKeyList(cfgDetails, importCfgName);

        Map<String, String> detailIdToGroupKey = buildDetailIdToGroupKeyMap(main.getId(), uniqueKeyList);
        Map<String, List<String>> groupKeyToAllSubIds = new LinkedHashMap<>();
        Map<String, List<String>> groupKeyToEligibleSubIds = new LinkedHashMap<>();
        aggregateSubIdsByGroupKey(main.getId(), detailIdToGroupKey, groupKeyToAllSubIds, groupKeyToEligibleSubIds);

        return new LogisticsReconImportMatchContext(main.getId(), uniqueKeyList, groupKeyToAllSubIds, groupKeyToEligibleSubIds);
    }

    /**
     * 游标扫描对账明细，构建 detailId → 识别组键映射（避免一次性加载全量明细实体）。
     *
     * @author Will
     * @date 2026/6/12
     * @param mainId         对账单 id
     * @param uniqueKeyList  模板唯一识别字段
     * @return detailId → groupKey
     */
    private Map<String, String> buildDetailIdToGroupKeyMap(String mainId,
                                                           List<CfgLogisticsCostImportDetailEntity> uniqueKeyList) {
        Map<String, String> detailIdToGroupKey = new HashMap<>();
        String lastDetailId = null;
        while (true) {
            LambdaQueryChainWrapper<LogisticsReconDetailEntity> detailQuery = lambdaQuery()
                    .eq(LogisticsReconDetailEntity::getMainId, mainId)
                    .orderByAsc(LogisticsReconDetailEntity::getId)
                    .last("LIMIT " + LogisticsReconImportMatchConstant.CONTEXT_SCAN_BATCH_SIZE);
            if (lastDetailId != null) {
                detailQuery.gt(LogisticsReconDetailEntity::getId, lastDetailId);
            }
            List<LogisticsReconDetailEntity> detailBatch = detailQuery.list();
            if (CollUtil.isEmpty(detailBatch)) {
                break;
            }
            lastDetailId = detailBatch.get(detailBatch.size() - 1).getId();
            for (LogisticsReconDetailEntity detail : detailBatch) {
                String groupKey = LogisticsReconMatchGroupHelper.buildDetailGroupKey(detail, uniqueKeyList);
                if (StrUtil.isNotBlank(groupKey)) {
                    detailIdToGroupKey.put(detail.getId(), groupKey);
                }
            }
        }
        return detailIdToGroupKey;
    }

    /**
     * 游标扫描费用项，按识别组聚合 subId（仅保留 id，降低超大对账单内存占用）。
     *
     * @author Will
     * @date 2026/6/12
     * @param mainId                   对账单 id
     * @param detailIdToGroupKey       明细 id → 识别组键
     * @param groupKeyToAllSubIds      输出：组 → 全部费用项 id
     * @param groupKeyToEligibleSubIds 输出：组 → 可匹配费用项 id
     */
    private void aggregateSubIdsByGroupKey(String mainId,
                                           Map<String, String> detailIdToGroupKey,
                                           Map<String, List<String>> groupKeyToAllSubIds,
                                           Map<String, List<String>> groupKeyToEligibleSubIds) {
        if (CollUtil.isEmpty(detailIdToGroupKey)) {
            return;
        }
        String lastSubId = null;
        while (true) {
            LambdaQueryChainWrapper<LogisticsReconDetailSubEntity> subQuery = logisticsReconDetailSubService.lambdaQuery()
                    .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                    .orderByAsc(LogisticsReconDetailSubEntity::getId)
                    .last("LIMIT " + LogisticsReconImportMatchConstant.CONTEXT_SCAN_BATCH_SIZE);
            if (lastSubId != null) {
                subQuery.gt(LogisticsReconDetailSubEntity::getId, lastSubId);
            }
            List<LogisticsReconDetailSubEntity> subBatch = subQuery.list();
            if (CollUtil.isEmpty(subBatch)) {
                break;
            }
            lastSubId = subBatch.get(subBatch.size() - 1).getId();
            for (LogisticsReconDetailSubEntity sub : subBatch) {
                String groupKey = detailIdToGroupKey.get(sub.getDetailId());
                if (StrUtil.isBlank(groupKey)) {
                    continue;
                }
                groupKeyToAllSubIds.computeIfAbsent(groupKey, key -> new ArrayList<>()).add(sub.getId());
                if (canImportMatchSub(sub)) {
                    groupKeyToEligibleSubIds.computeIfAbsent(groupKey, key -> new ArrayList<>()).add(sub.getId());
                }
            }
        }
    }

    /**
     * 提取模板唯一识别字段配置（与物流费用导入一致）。
     *
     * @author Will
     * @date 2026/6/12
     * @param cfgImportDetailList 物流费用导入模板明细配置
     * @param importCfgName       导入模板名称（用于错误提示）
     * @return 唯一识别字段配置列表
     */
    private List<CfgLogisticsCostImportDetailEntity> extractUniqueKeyList(
            List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList, String importCfgName) {
        List<CfgLogisticsCostImportDetailEntity> uniqueKeyList = cfgImportDetailList.stream()
                .filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(uniqueKeyList)) {
            throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_IS_UNIQUE_KEY_NOT_FOUND, importCfgName);
        }
        List<String> invalidUniqueFields = uniqueKeyList.stream()
                .filter(detail -> StrUtil.isBlank(detail.getSourceField()))
                .map(detail -> StrUtil.blankToDefault(detail.getTargetFieldName(), detail.getTargetField()))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(invalidUniqueFields)) {
            throw new ServiceException(ApiError.LOGISTICS_BILL_COST_IMPORT_RECORD_UNIQUE_KEY_ERROR);
        }
        return uniqueKeyList;
    }

    /**
     * 解析物流费用导入模板名称（用于唯一键相关错误提示）。
     *
     * @author Will
     * @date 2026/6/12
     * @param cfgImportId 导入模板主表 id
     * @return 模板名称，查不到时回退为 id
     */
    private String resolveImportCfgName(String cfgImportId) {
        CfgLogisticsCostImportEntity importCfg = cfgLogisticsCostImportService.getById(cfgImportId);
        if (importCfg != null && StrUtil.isNotBlank(importCfg.getName())) {
            return importCfg.getName();
        }
        return cfgImportId;
    }

    /**
     * 判断费用项是否可参与导入匹配（未匹配/失败且未确认、非匹配中）。
     *
     * @author Will
     * @date 2026/6/12
     * @param sub 对账费用项
     * @return 可匹配时返回 true
     */
    private boolean canImportMatchSub(LogisticsReconDetailSubEntity sub) {
        if (LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode().equals(sub.getReconciliationStatus())) {
            return false;
        }
        if (LogisticsReconDetailMatchStatusEnum.MATCHING.getCode().equals(sub.getMatchStatus())) {
            return false;
        }
        if (LogisticsReconDetailMatchStatusEnum.MATCHED.getCode().equals(sub.getMatchStatus())) {
            return false;
        }
        return LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode().equals(sub.getMatchStatus())
                || LogisticsReconDetailMatchStatusEnum.FAILED.getCode().equals(sub.getMatchStatus());
    }

    /**
     * 导出并上传导入匹配错误结果文件（仅失败行）。
     * @author Will
     * @date: 2026/06/12
     * @param errorRows 失败行
     * @return 文件 url，无失败行返回空串
     */
    private String uploadMatchErrorFile(List<LogisticsReconMatchImportExcelDTO> errorRows) {
        if (CollUtil.isEmpty(errorRows)) {
            return "";
        }
        String fileName = "物流商对账导入匹配错误信息.xlsx";
        File file = ExcelUtil.exportFile(fileName, "error", errorRows, LogisticsReconMatchImportExcelDTO.class);
        return file.isDirectory() ? "" : FastDFSClientUtil.uploadFile(file, fileName);
    }

    @Override
    public List<BatchResultDTO> manualMatch(LogisticsReconDetailDTO.ManualMatchDTO dto) {
        // 异步：组装入参后提交异步匹配（识别单号 + 物流商，match_type=manual），结果异步写回 detail_sub
        List<LogisticsReconMatchDTO.SubErpInputDTO> inputs = dto.getItemList().stream().map(item -> {
            LogisticsReconMatchDTO.SubErpInputDTO input = new LogisticsReconMatchDTO.SubErpInputDTO();
            input.setDetailSubId(item.getDetailSubId());
            input.setErpSoCode(item.getErpSoCode());
            input.setErpPlatformOrderNo(item.getErpPlatformOrderNo());
            input.setErpTrackNo(item.getErpTrackNo());
            input.setErpSoDeliveryCode(item.getErpSoDeliveryCode());
            return input;
        }).collect(Collectors.toList());
        return logisticsReconService.submitManualMatch(inputs, LogisticsReconRefMatchTypeEnum.MANUAL.getCode());
    }

    /**
     * 费用项列表填充：补 match_status 名称 + 金额 / 尺寸展示字段
     * @author Will
     * @date: 2026/06/02
     * @param list 列表数据
     * @return void
     */
    private void fillList(List<LogisticsReconDetailDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<String, String> currencySymbolMap = FeignQuery.list(DictCurrencyEntity.class).stream()
                .collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol, (first, second) -> first));
        for (LogisticsReconDetailDTO.ListDTO data : list) {
            data.setReconciliationMonth(DateUtil.formatCnYearMonth(data.getReconciliationMonth()));
            data.setMatchStatusName(LogisticsReconDetailMatchStatusEnum.getName(data.getMatchStatus()));
            data.setReconciliationStatusName(
                    LogisticsReconReconciliationStatusEnum.getName(data.getReconciliationStatus()));
            data.setThirdSize(buildThirdSize(data.getThirdLength(), data.getThirdWidth(), data.getThirdHeight()));
            String symbol = currencySymbolMap.getOrDefault(data.getCurrency(), "¥");
            data.setCurrencySymbol(symbol);
            data.setActualAmountStr(formatAmount(data.getActualAmount(), symbol));
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
     * 组装物流商尺寸展示串（长*宽*高，去掉无意义的小数尾零）
     * @author Will
     * @date 2026/6/1 12:20
     * @param length 长
     * @param width 宽
     * @param height 高
     * @return String
     */
    private String buildThirdSize(BigDecimal length, BigDecimal width, BigDecimal height) {
        if (length == null && width == null && height == null) {
            return "";
        }
        return plain(length) + "*" + plain(width) + "*" + plain(height);
    }

    /**
     * BigDecimal 转普通字符串并去除末尾零
     * @author Will
     * @date: 2026/06/02
     * @param value
     * @return String
     */
    private String plain(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }
}
