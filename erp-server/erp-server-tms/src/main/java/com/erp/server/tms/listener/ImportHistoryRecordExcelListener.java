package com.erp.server.tms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.enums.CfgLogisticsCostImportBusinessTypeEnum;
import com.erp.model.tms.enums.ImportHistoryRecordProcessingTypeEnum;
import com.erp.model.tms.enums.ImportHistoryRecordStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.service.ImportHistoryRecordService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 自定义导入
 * @author will
 * @date 2026/1/21 09:34
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class ImportHistoryRecordExcelListener extends AnalysisEventListener<Map<Integer,String>> {

    public static final String MATCH_FIELD  = "匹配结果";
    public static final String MATCH_SUCCESS  = "匹配成功";
    public static final String MATCH_FAIL  = "匹配失败";
    public static final String ERROR_MSG  = "错误信息";


    private static final int BATCH_COUNT = 3000;
    private final String taskId;
    private final Integer importCount;
    private final ImportHistoryRecordDTO.ImportSyncDTO importDTO;
    private final CfgLogisticsCostImportEntity costImportEntity;
    private final List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList;
    @Getter
    private Integer count = 0;
    /**
     * 匹配数据信息
     */
    @Getter
    private List<JSONObject> matchList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<JSONObject> successList = new ArrayList<>();

    /**
     * 确认状态更新数据
     */
    private List<ImportHistoryRecordDTO.ImportConfirmDTO> confirmPairList = new ArrayList<>();

    private Map<Integer,String> headMap;

    @Getter
    private List<String> headList;
    private ExcelWriter matchExcelWriter;
    private WriteSheet matchWriteSheet;
    private File matchResultFile;
    private boolean hasMatchResult;
    private int matchSuccessCount;
    private int matchFailCount;
    private String matchResultUrl = "";
    /**
     * processBatch 中整批次失败的次数（PG 死锁 / 服务异常等导致整个 3000 行批次写入失败）。
     * 之前 catch 把异常吞掉只打了一条 error 日志，从外部看任务还是"正常完成"，
     * 这里加一个累计计数 + 全量汇总日志，便于运维一眼看出"已完成"其实并不干净。
     */
    private int failedBatchCount;
    private int totalBatchCount;

    private final ImportHistoryRecordService importHistoryRecordService = SpringUtil.getBean(ImportHistoryRecordService.class);
    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    public ImportHistoryRecordExcelListener(CfgLogisticsCostImportEntity costImportEntity, List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList, ImportHistoryRecordDTO.ImportSyncDTO importDTO) {
        this.costImportEntity = costImportEntity;
        this.cfgImportDetailList = cfgImportDetailList;
        this.importDTO = importDTO;
        this.taskId = importDTO.getTaskId();
        this.importCount = importDTO.getImportCount();
    }

    /**
     * 每解析一行数据回调一遍
     * @author will
     * @date 2026/1/21 15:42
     * @param map
     * @param analysisContext
     * @return void
     */
    @Override
    public void invoke(Map<Integer,String>  map, AnalysisContext analysisContext) {
        count += 1;
        //已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount){
            return;
        }
        //无表头数据报错
        if (ObjectUtil.isEmpty(headMap)) {
            throw new ServiceException(ApiError.COMMON_FILE_HEAD_READ_HEAD_FAIL);
        }

        //当导入的最后一列数据都是空时map无值导致表头size和map.size不一致，所以需要添加表头一致的数据
        for (Map.Entry<Integer,String> entry : headMap.entrySet()) {
            String value = map.get(entry.getKey());
            if (ObjectUtil.isEmpty(value)) {
                map.put(entry.getKey(),"");
            }
        }
        JSONObject excelDTO = new JSONObject(map);
        successList.add(excelDTO);
        if (successList.size() >= BATCH_COUNT){
            processBatch();
            successList.clear();
            updateTask(count);
        }
    }

    /**
     * @description: 批量处理
     * @author Will
     * @date: 2026/1/21 15:31
     */
    private void processBatch() {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        totalBatchCount++;
        try {
            List<JSONObject> errorList2 = new ArrayList<>();
            // 解析结果按批交给 Service：预处理只生成匹配/清洗结果，正式导入和导入确认才会进入费用新增更新链路。
            List<ImportHistoryRecordDTO.ImportConfirmDTO> importConfirmDTOS = importHistoryRecordService.handleImportSuccessList(importDTO, costImportEntity, cfgImportDetailList, successList, errorList2, headList, headMap);
            writeMatchResult(errorList2);
            // 导入确认需要在全部批次完成后统一确认，避免单批确认成功后后续批次失败造成同文件状态不一致。
            confirmPairList.addAll(importConfirmDTOS);
        } catch (Exception e) {
            failedBatchCount++;
            log.error("批量导入处理异常批次 taskId={} fileName={} 当前批次条数={} 已失败批次数={}/{}",
                    taskId, importDTO.getFileName(), successList.size(), failedBatchCount, totalBatchCount, e);
            // 整个批次失败的处理逻辑
            log.error("批量导入处理异常批次，条数：{}", successList.size(), e);
            // 批次级异常说明本批无法准确定位到单行，统一写入匹配失败结果，便于用户在清洗文件中重新处理。
            String msg = e.getMessage();
            if (CharSequenceUtil.isNotBlank(msg) && msg.length() > 100) {
                msg = msg.substring(0, 100);
            }
            final String finalMsg = msg != null ? msg : "未知异常";
            Integer errorIdx = getMapKey(headMap, ERROR_MSG);
            Integer matchIdx = getMapKey(headMap, MATCH_FIELD);
            String errorIdxStr = errorIdx != null ? errorIdx.toString() : null;
            String matchIdxStr = matchIdx != null ? matchIdx.toString() : null;

            successList.forEach(jsonObject -> {
                if (matchIdxStr != null) jsonObject.set(matchIdxStr, MATCH_FAIL);
                if (errorIdxStr != null) jsonObject.set(errorIdxStr, finalMsg);
            });
            writeMatchResult(successList);
        }
    }

    /**
     * @description: 数据全部解析完后
     * @author Will
     * @date: 2026/1/21 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        try {
            if (!successList.isEmpty()) {
                processBatch();
                successList.clear();
            }
            finishMatchExcelWriter();
            // confirmImport 模式才会执行确认；preprocessing/import 模式在 Service 内部直接跳过，保持三个入口动作共用同一解析链路。
            importHistoryRecordService.confirmImportData(importDTO,confirmPairList);
            // 无论本次是预处理还是正式导入，都需要记录导入历史，前端后续才能查看清洗文件或继续确认。
            addMatchExcelResult();
            // 汇总日志：让运维一眼看到"已完成"任务里有没有败批
            if (failedBatchCount > 0) {
                log.error("[导入存在败批] taskId={} fileName={} 总批次={} 失败批次={} 行级成功={} 行级失败={} 总行数={}",
                        taskId, importDTO.getFileName(), totalBatchCount, failedBatchCount,
                        matchSuccessCount, matchFailCount, count);
            } else {
                log.info("[导入完成] taskId={} fileName={} 总批次={} 行级成功={} 行级失败={} 总行数={}",
                        taskId, importDTO.getFileName(), totalBatchCount,
                        matchSuccessCount, matchFailCount, count);
            }
        } finally {
            finishMatchExcelWriter();
        }
    }
    /**
     * 添加匹配结果
     * @author will
     * @date 2026/2/4 16:28
     * @return void
     */
    private void addMatchExcelResult() {

        // 导入历史记录保存原文件、清洗文件和匹配数量，是预处理后继续导入/确认的业务入口。
        ImportHistoryRecordDTO.AddOrUpdateDTO addOrUpdateDTO = new ImportHistoryRecordDTO.AddOrUpdateDTO();
        addOrUpdateDTO.setReconciliationMonth(importDTO.getReconciliationMonth());
        addOrUpdateDTO.setBusinessType(CfgLogisticsCostImportBusinessTypeEnum.LAST_MILE_DELIVERY.getCode());
        addOrUpdateDTO.setFileUrl(importDTO.getFileUrl());
        addOrUpdateDTO.setFileName(importDTO.getFileName());
        // 清洗文件在原 Excel 后追加“匹配结果”和“错误信息”，用户可据此修正失败数据。
        String url = "";
        String fileName = importDTO.getFileName();
        if (hasMatchResult && matchResultFile != null) {
            if (!matchResultFile.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(matchResultFile, fileName);
            }
        }
        matchResultUrl = url;
        addOrUpdateDTO.setSheetName(costImportEntity.getSheetName());
        addOrUpdateDTO.setCleanFileUrl(url);
        addOrUpdateDTO.setCleanFileName(fileName);
        // 预处理只完成清洗和匹配，因此状态为待处理；正式导入/导入确认已经进入费用处理链路，状态为已处理。
        if (CharSequenceUtil.equals(importDTO.getProcessingType(), ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode())) {
            addOrUpdateDTO.setStatus( ImportHistoryRecordStatusEnum.WAIT_HANDLE.getStatus());
        } else {
            addOrUpdateDTO.setStatus( ImportHistoryRecordStatusEnum.HANDLE.getStatus());
        }
        addOrUpdateDTO.setType(importDTO.getType());
        addOrUpdateDTO.setOperationUserId(importDTO.getUserId());
        addOrUpdateDTO.setImportCount(count);

        addOrUpdateDTO.setMatchCount(matchSuccessCount);
        importHistoryRecordService.addOrUpdate(addOrUpdateDTO);
    }

    @Override
    public void invokeHeadMap(Map<Integer,String> map, AnalysisContext analysisContext) {
        // 清洗结果需要在原始 Excel 后追加匹配结果和错误信息，用户下载后可直接定位失败行。
        List<String> headList = map.values().stream().map(obj -> CharSequenceUtil.isBlank(obj) ? "" : obj).collect(Collectors.toList());
        headList.add(MATCH_FIELD);
        headList.add(ERROR_MSG);
        int size = map.size();
        map.put(size,MATCH_FIELD);
        map.put(size + 1,ERROR_MSG);
        this.headMap = map;
        this.headList = headList;
    }

    private void writeMatchResult(List<JSONObject> batchMatchList) {
        if (CollectionUtils.isEmpty(batchMatchList) || headList == null) {
            return;
        }
        // 首次写入时初始化临时 Excel，后续批次复用同一个 writer 追加清洗结果。
        initMatchExcelWriter();
        List<List<String>> rows = new ArrayList<>(batchMatchList.size());
        // 匹配结果列用于统计成功和失败数量，最终写入导入历史记录。
        Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
        String matchIndexStr = matchIndex == null ? null : matchIndex.toString();
        for (JSONObject map : batchMatchList) {
            List<String> row = new ArrayList<>(headList.size());
            for (int j = 0; j < headList.size(); j++) {
                Object value = map.get(String.valueOf(j));
                row.add(ObjectUtil.isEmpty(value) ? "" : value.toString());
            }
            rows.add(row);
            if (matchIndexStr != null && CharSequenceUtil.equals(MATCH_SUCCESS, (CharSequence) map.get(matchIndexStr))) {
                matchSuccessCount++;
            } else if (matchIndexStr != null && CharSequenceUtil.equals(MATCH_FAIL, (CharSequence) map.get(matchIndexStr))) {
                matchFailCount++;
            }
        }
        // 分批写入清洗结果，避免大文件一次性持有所有导出行占用过多内存。
        matchExcelWriter.write(rows, matchWriteSheet);
        hasMatchResult = true;
    }

    private void initMatchExcelWriter() {
        if (matchExcelWriter != null) {
            return;
        }
        try {
            matchResultFile = File.createTempFile("import-history-record-", ".xlsx", FileUtils.getTempDirectory());
        } catch (IOException e) {
            throw new ServiceException("创建导入结果临时文件失败");
        }
        // EasyExcel 表头必须与追加后的 headList 一致，否则匹配结果和错误信息列会错位。
        List<List<String>> heads = headList.stream().map(Arrays::asList).collect(Collectors.toList());
        matchExcelWriter = EasyExcel.write(matchResultFile)
                .head(heads)
                .inMemory(false)
                .build();
        matchWriteSheet = EasyExcel.writerSheet(costImportEntity.getSheetName()).build();
    }

    private void finishMatchExcelWriter() {
        if (matchExcelWriter == null) {
            return;
        }
        matchExcelWriter.finish();
        matchExcelWriter = null;
    }

    private void updateTask(Integer count){
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setRemark("处理中");
        importResultDTO.setCount(count);
        downloadTaskFeign.updateTask(importResultDTO);
    }

    /**
     * 匹配表头数据
     */
    private Integer getMapKey (Map<Integer,String> headMap,String targetValue) {
        Integer resultKey = null;
        for (Integer key : headMap.keySet()) {
            // 获取对应的value
            String value = headMap.get(key);
            if (CharSequenceUtil.isBlank(value)) {
                continue;
            }
            // 如果value等于目标值，输出对应的key
            if (value.contains(targetValue)) {
                resultKey = key;
                // 如果只需要找到一个匹配的key，可以break
                break;
            }
        }
        return  resultKey;
    }
}
