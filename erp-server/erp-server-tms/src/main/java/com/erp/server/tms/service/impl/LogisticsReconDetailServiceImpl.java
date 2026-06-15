package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.vo.LoginUser;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import com.erp.model.tms.entity.LogisticsReconDetailEntity;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;
import com.erp.model.tms.entity.LogisticsReconEntity;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconReconciliationStatusEnum;
import com.erp.model.tms.enums.LogisticsReconRefMatchTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.mapper.LogisticsReconDetailMapper;
import com.erp.server.tms.service.LogisticsReconDetailService;
import com.erp.server.tms.service.LogisticsReconDetailSubService;
import com.erp.server.tms.service.LogisticsReconService;
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

    private static final int IMPORT_MATCH_READ_BATCH = 3000;

    private static final int IMPORT_MATCH_TRACK_NO_BATCH = 1000;

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
            byte[] fileBytes = fileFeign.downloadFile(dto.getFileUrl());
            Map<String, LogisticsReconMatchImportExcelDTO> rowByTrackNo = new LinkedHashMap<>();
            Map<String, List<String>> trackNoErrorMap = new LinkedHashMap<>();
            Set<String> matchedTrackNoSet = new HashSet<>();
            int[] totalCountHolder = {0};
            List<LogisticsReconMatchImportExcelDTO> buffer = new ArrayList<>();
            EasyExcel.read(new ByteArrayInputStream(fileBytes), LogisticsReconMatchImportExcelDTO.class,
                    new AnalysisEventListener<LogisticsReconMatchImportExcelDTO>() {
                        @Override
                        public void invoke(LogisticsReconMatchImportExcelDTO row, AnalysisContext context) {
                            if (StrUtil.isBlank(row.getTrackNo())) {
                                return;
                            }
                            totalCountHolder[0]++;
                            buffer.add(row);
                            rowByTrackNo.putIfAbsent(StrUtil.trim(row.getTrackNo()), row);
                            if (buffer.size() >= IMPORT_MATCH_READ_BATCH) {
                                processImportMatchBatch(dto.getMainId(), buffer, trackNoErrorMap, matchedTrackNoSet);
                                buffer.clear();
                            }
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            if (!buffer.isEmpty()) {
                                processImportMatchBatch(dto.getMainId(), buffer, trackNoErrorMap, matchedTrackNoSet);
                                buffer.clear();
                            }
                        }
                    }).sheet().doRead();
            if (totalCountHolder[0] == 0) {
                throw new ServiceException(ApiError.FILE_IMPORT_DATA_NOT_NULL, "对账导入匹配");
            }

            List<LogisticsReconMatchImportExcelDTO> errorRows = new ArrayList<>();
            for (Map.Entry<String, LogisticsReconMatchImportExcelDTO> entry : rowByTrackNo.entrySet()) {
                String trackNo = entry.getKey();
                LogisticsReconMatchImportExcelDTO row = entry.getValue();
                String failReason = null;
                if (!matchedTrackNoSet.contains(trackNo)) {
                    failReason = "未匹配到对账明细";
                } else if (trackNoErrorMap.containsKey(trackNo)) {
                    failReason = String.join("；", trackNoErrorMap.get(trackNo));
                }
                if (StrUtil.isNotBlank(failReason)) {
                    row.setMatchResult("失败");
                    row.setErrorMsg(failReason);
                    errorRows.add(row);
                }
            }

            resultDTO.setCount(totalCountHolder[0]);
            resultDTO.setErrorUrl(uploadMatchErrorFile(errorRows));
            resultDTO.setFinishTime(LocalDateTime.now());
            resultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
            resultDTO.setRemark("处理完成，失败" + errorRows.size() + "条");
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
     * 导入匹配分批处理：按 trackNo 定位明细，仅未匹配/失败且未确认的费用项认领后同步匹配。
     */
    private void processImportMatchBatch(String mainId, List<LogisticsReconMatchImportExcelDTO> excelBatch,
                                         Map<String, List<String>> trackNoErrorMap,
                                         Set<String> matchedTrackNoSet) {
        if (CollUtil.isEmpty(excelBatch)) {
            return;
        }
        Map<String, LogisticsReconMatchImportExcelDTO> batchRowByTrackNo = new LinkedHashMap<>();
        for (LogisticsReconMatchImportExcelDTO row : excelBatch) {
            batchRowByTrackNo.putIfAbsent(StrUtil.trim(row.getTrackNo()), row);
        }
        List<String> trackNos = new ArrayList<>(batchRowByTrackNo.keySet());
        List<LogisticsReconDetailEntity> detailList = new ArrayList<>();
        for (int i = 0; i < trackNos.size(); i += IMPORT_MATCH_TRACK_NO_BATCH) {
            List<String> trackNoBatch = trackNos.subList(i,
                    Math.min(trackNos.size(), i + IMPORT_MATCH_TRACK_NO_BATCH));
            detailList.addAll(lambdaQuery()
                    .eq(LogisticsReconDetailEntity::getMainId, mainId)
                    .in(LogisticsReconDetailEntity::getTrackNo, trackNoBatch)
                    .list());
        }
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        Map<String, List<LogisticsReconDetailEntity>> detailGroupByTrackNo = detailList.stream()
                .filter(detail -> StrUtil.isNotBlank(detail.getTrackNo()))
                .collect(Collectors.groupingBy(detail -> StrUtil.trim(detail.getTrackNo())));
        Set<String> duplicateTrackNoSet = detailGroupByTrackNo.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        for (String duplicateTrackNo : duplicateTrackNoSet) {
            trackNoErrorMap.computeIfAbsent(duplicateTrackNo, key -> new ArrayList<>())
                    .add("对账物流跟踪号对应多条明细");
        }
        matchedTrackNoSet.addAll(detailGroupByTrackNo.keySet());

        List<String> detailIds = detailList.stream()
                .map(LogisticsReconDetailEntity::getId)
                .collect(Collectors.toList());
        Map<String, List<LogisticsReconDetailSubEntity>> subByDetail = new HashMap<>();
        for (int i = 0; i < detailIds.size(); i += IMPORT_MATCH_TRACK_NO_BATCH) {
            List<String> detailIdBatch = detailIds.subList(i,
                    Math.min(detailIds.size(), i + IMPORT_MATCH_TRACK_NO_BATCH));
            logisticsReconDetailSubService.lambdaQuery()
                    .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                    .in(LogisticsReconDetailSubEntity::getDetailId, detailIdBatch)
                    .list()
                    .forEach(sub -> subByDetail.computeIfAbsent(sub.getDetailId(), key -> new ArrayList<>()).add(sub));
        }

        List<LogisticsReconMatchDTO.SubErpInputDTO> inputs = new ArrayList<>();
        Map<String, String> subIdToTrackNo = new HashMap<>();
        Set<String> detailWithEligibleSub = new HashSet<>();
        for (LogisticsReconDetailEntity detail : detailList) {
            String trackNo = StrUtil.trim(detail.getTrackNo());
            if (duplicateTrackNoSet.contains(trackNo)) {
                continue;
            }
            LogisticsReconMatchImportExcelDTO row = batchRowByTrackNo.get(trackNo);
            if (row == null) {
                continue;
            }
            List<LogisticsReconDetailSubEntity> subs = subByDetail.get(detail.getId());
            if (CollUtil.isEmpty(subs)) {
                trackNoErrorMap.computeIfAbsent(trackNo, key -> new ArrayList<>()).add("对账明细下无费用项");
                continue;
            }
            boolean hasEligible = false;
            for (LogisticsReconDetailSubEntity sub : subs) {
                if (!canImportMatchSub(sub)) {
                    continue;
                }
                hasEligible = true;
                LogisticsReconMatchDTO.SubErpInputDTO input = new LogisticsReconMatchDTO.SubErpInputDTO();
                input.setDetailSubId(sub.getId());
                input.setErpSoCode(row.getErpSoCode());
                input.setErpPlatformOrderNo(row.getErpPlatformOrderNo());
                input.setErpTrackNo(row.getErpTrackNo());
                input.setErpSoDeliveryCode(row.getErpSoDeliveryCode());
                inputs.add(input);
                subIdToTrackNo.put(sub.getId(), trackNo);
            }
            if (hasEligible) {
                detailWithEligibleSub.add(trackNo);
            }
        }
        for (String trackNo : batchRowByTrackNo.keySet()) {
            if (matchedTrackNoSet.contains(trackNo)
                    && !detailWithEligibleSub.contains(trackNo)
                    && !trackNoErrorMap.containsKey(trackNo)) {
                trackNoErrorMap.computeIfAbsent(trackNo, key -> new ArrayList<>())
                        .add("费用项已匹配或已确认，无法重新匹配");
            }
        }
        if (CollUtil.isEmpty(inputs)) {
            return;
        }
        List<String> claimIds = inputs.stream()
                .map(LogisticsReconMatchDTO.SubErpInputDTO::getDetailSubId)
                .distinct()
                .collect(Collectors.toList());
        logisticsReconDetailSubService.batchUpdateMatchStatus(claimIds,
                LogisticsReconDetailMatchStatusEnum.MATCHING.getCode(), null,
                Arrays.asList(LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode(),
                        LogisticsReconDetailMatchStatusEnum.FAILED.getCode()));
        try {
            List<BatchResultDTO> matchResults = logisticsReconService.matchDetailSubsByErp(
                    mainId, inputs, LogisticsReconRefMatchTypeEnum.MANUAL.getCode());
            for (BatchResultDTO matchResult : matchResults) {
                if (Boolean.TRUE.equals(matchResult.getSuccess())) {
                    continue;
                }
                String trackNo = subIdToTrackNo.get(matchResult.getCode());
                if (StrUtil.isBlank(trackNo)) {
                    continue;
                }
                trackNoErrorMap.computeIfAbsent(trackNo, key -> new ArrayList<>()).add(matchResult.getMsg());
            }
        } catch (Exception e) {
            logisticsReconService.markReconMatchFailed(mainId, claimIds,
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * 导入匹配仅处理未匹配/失败且未确认、非匹配中的费用项。
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
