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
import java.util.*;
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
        //无表头数据报错
        if (ObjectUtil.isEmpty(headMap)) {
            throw new ServiceException(ApiError.COMMON_FILE_HEAD_READ_HEAD_FAIL);
        }
        //忽略公式错误值(#REF!/#VALUE! 等)，统一按空处理，避免脏单元格被录入或参与匹配
        sanitizeErrorCellValues(map);
        //整行无任何有效数据则跳过（含 EasyExcel 解析 .xls 公式错误单元格时多产生的幽灵空行），不计入进度也不写入清洗结果
        if (isBlankDataRow(map)) {
            return;
        }
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
     * Excel 公式错误值集合（POI 读取公式错误单元格时返回的文本），这些值按空处理。
     */
    private static final Set<String> EXCEL_ERROR_VALUES = new HashSet<>(Arrays.asList(
            "#REF!", "#VALUE!", "#DIV/0!", "#NAME?", "#N/A", "#NUM!", "#NULL!",
            "#GETTING_DATA", "#SPILL!", "#CALC!"));

    /**
     * 将单元格中的 Excel 公式错误值清成空字符串。
     * 典型场景：源文件「核对/差异」等列是跨表公式，引用的表缺失后整列变成 #REF!，
     * EasyExcel 解析 .xls 时会因这些错误单元格额外吐出只含该列、其余全空的幽灵行。
     */
    private void sanitizeErrorCellValues(Map<Integer,String> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        for (Map.Entry<Integer,String> entry : map.entrySet()) {
            String value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (EXCEL_ERROR_VALUES.contains(value.trim().toUpperCase(Locale.ROOT))) {
                entry.setValue("");
            }
        }
    }

    /**
     * 判断当前行是否没有任何有效业务数据（所有单元格清洗后均为空）。
     */
    private boolean isBlankDataRow(Map<Integer,String> map) {
        if (map == null || map.isEmpty()) {
            return true;
        }
        for (String value : map.values()) {
            if (CharSequenceUtil.isNotBlank(value)) {
                return false;
            }
        }
        return true;
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
            // 批量处理，由 Service 内部负责事务控制
            List<ImportHistoryRecordDTO.ImportConfirmDTO> importConfirmDTOS = importHistoryRecordService.handleImportSuccessList(importDTO, costImportEntity, cfgImportDetailList, successList, errorList2, headList, headMap);
            writeMatchResult(errorList2);
            confirmPairList.addAll(importConfirmDTOS);
        } catch (Exception e) {
            failedBatchCount++;
            log.error("批量导入处理异常批次 taskId={} fileName={} 当前批次条数={} 已失败批次数={}/{}",
                    taskId, importDTO.getFileName(), successList.size(), failedBatchCount, totalBatchCount, e);
            // 整个批次失败的处理逻辑
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
                // 将错误信息写入匹配结果
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
            //对所有确认数据进行批量确认
            importHistoryRecordService.confirmImportData(importDTO,confirmPairList);
            //添加匹配结果
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

        //添加导入历史记录表数据
        ImportHistoryRecordDTO.AddOrUpdateDTO addOrUpdateDTO = new ImportHistoryRecordDTO.AddOrUpdateDTO();
        addOrUpdateDTO.setReconciliationMonth(importDTO.getReconciliationMonth());
        addOrUpdateDTO.setBusinessType(costImportEntity.getBusinessType());
        addOrUpdateDTO.setFileUrl(importDTO.getFileUrl());
        addOrUpdateDTO.setFileName(importDTO.getFileName());
        //清洗结果
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
        initMatchExcelWriter();
        List<List<String>> rows = new ArrayList<>(batchMatchList.size());
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
