package com.erp.server.tms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
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

import java.io.File;
import java.util.ArrayList;
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
        try {
            List<JSONObject> errorList2 = new ArrayList<>();
            // 批量处理，由 Service 内部负责事务控制
            List<ImportHistoryRecordDTO.ImportConfirmDTO> importConfirmDTOS = importHistoryRecordService.handleImportSuccessList(importDTO, costImportEntity, cfgImportDetailList, successList, errorList2, headList, headMap);
            matchList.addAll(errorList2);
            confirmPairList.addAll(importConfirmDTOS);
        } catch (Exception e) {
            log.error("批量导入处理异常批次，条数：{}", successList.size(), e);
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
            matchList.addAll(new ArrayList<>(successList));
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
        if (!successList.isEmpty()) {
            processBatch();
            successList.clear();
        }
        //添加匹配结果
        addMatchExcelResult();
        //对所有确认数据进行批量确认
        importHistoryRecordService.confirmImportData(importDTO,confirmPairList);
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
        String fileName = "物流商费用导入结果.xlsx";
        if (CollectionUtils.isNotEmpty(matchList) && headList != null) {
            //matchList = matchList.stream().filter(Objects::nonNull).collect(Collectors.toList());
            File file = ExcelUtil.customExportUtil(fileName, matchList, headList);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
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

        //匹配结果序号
        Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
        long errorCount = matchList.stream().filter(obj -> CharSequenceUtil.equals(MATCH_SUCCESS, (CharSequence) obj.get(matchIndex.toString()))).count();
        addOrUpdateDTO.setMatchCount((int)errorCount);
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
