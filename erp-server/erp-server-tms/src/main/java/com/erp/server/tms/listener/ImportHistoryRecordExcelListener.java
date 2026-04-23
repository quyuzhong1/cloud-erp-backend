package com.erp.server.tms.listener;

import cn.hutool.core.lang.Pair;
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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
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
public class ImportHistoryRecordExcelListener extends AnalysisEventListener<Map<Integer,String>> {

    public static final String MATCH_FIELD  = "匹配结果";
    public static final String MATCH_SUCCESS  = "匹配成功";
    public static final String MATCH_FAIL  = "匹配失败";
    public static final String ERROR_MSG  = "错误信息";


    private static final int BATCH_COUNT = 1000;
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
     * 全部数据（用于判断导入是否为空）
     */
    private final List<JSONObject> dataList = new ArrayList<>();
    /**
     * 确认状态更新数据
     */
    private List<Pair<String, LocalDateTime>> confirmPairList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<JSONObject> successList = new ArrayList<>();

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
    @Transactional(rollbackFor = Exception.class)
    public void invoke(Map<Integer,String>  map, AnalysisContext analysisContext) {
        count += 1;
        //已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount){
            return;
        }
        //当导入的最后一列数据都是空时map无值导致表头size和map.size不一致，所以需要添加表头一致的数据
        for (Map.Entry<Integer,String> entry : headMap.entrySet()) {
            String value = map.get(entry.getKey());
            if (ObjectUtil.isEmpty(value)) {
                map.put(entry.getKey(),"");
            }
        }
        JSONObject excelDTO = new JSONObject(map);
        //添加数据用于判断是否为空
        dataList.add(excelDTO);
        successList.add(excelDTO);
        if (successList.size() >= BATCH_COUNT){
            try {
                List<JSONObject> errorList2 = new ArrayList<>();
                List<Pair<String, LocalDateTime>> pairs = importHistoryRecordService.handleImportSuccessList(importDTO, costImportEntity, cfgImportDetailList, successList, errorList2, headList, headMap);
                confirmPairList.addAll(pairs);
                matchList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(jsonObject -> {
                    jsonObject.set(ObjectUtil.isNull(jsonObject) ? "" : String.valueOf(jsonObject.size() - 1) ,e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage());
                });
                matchList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    public List<JSONObject> getExcelDateList(){
        return dataList;
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @author Will
     * @date: 2026/1/21 15:32
     * @param analysisContext
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()) {
            try {
                List<JSONObject> errorList2 = new ArrayList<>();
                List<Pair<String, LocalDateTime>> pairs = importHistoryRecordService.handleImportSuccessList(importDTO, costImportEntity, cfgImportDetailList, successList, errorList2, headList, headMap);
                confirmPairList.addAll(pairs);
                matchList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(jsonObject -> {
                    jsonObject.set(ObjectUtil.isNull(jsonObject) ? "" : String.valueOf(jsonObject.size() - 1) ,e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage());
                });
                matchList.addAll(successList);
            }
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
        ImportHistoryRecordDTO.AddDTO addDTO = new ImportHistoryRecordDTO.AddDTO();
        addDTO.setReconciliationMonth(importDTO.getReconciliationMonth());
        addDTO.setBusinessType(costImportEntity.getBusinessType());
        addDTO.setFileUrl(importDTO.getFileUrl());
        addDTO.setFileName(importDTO.getFileName());
        //清洗结果
        String url = "";
        String fileName = "物流商费用导入结果.xlsx";
        if (CollectionUtils.isNotEmpty(matchList)) {
            File file = ExcelUtil.customExportUtil(fileName, matchList, headList);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        addDTO.setCleanFileUrl(url);
        addDTO.setCleanFileName(fileName);
        if (CharSequenceUtil.equals(importDTO.getProcessingType(), ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode())) {
            addDTO.setStatus( ImportHistoryRecordStatusEnum.WAIT_HANDLE.getStatus());
        } else {
            addDTO.setStatus( ImportHistoryRecordStatusEnum.HANDLE.getStatus());
        }
        addDTO.setType(importDTO.getType());
        addDTO.setOperationUserId(importDTO.getUserId());
        addDTO.setImportCount(count);

        //匹配结果序号
        Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
        long errorCount = matchList.stream().filter(obj -> CharSequenceUtil.equals(MATCH_SUCCESS, (CharSequence) obj.get(matchIndex.toString()))).count();
        addDTO.setMatchCount((int)errorCount);
        importHistoryRecordService.add(addDTO);
    }

    @Override
    public void invokeHeadMap(Map<Integer,String> map, AnalysisContext analysisContext) {
        List<String> headList = map.values().stream().map(obj -> CharSequenceUtil.isBlank(obj) ? "" : obj).collect(Collectors.toList());

        long blankCount = map.values().stream().filter(CharSequenceUtil::isBlank).count();
        if (blankCount > 1) {
            throw new ServiceException(ApiError.COMMON_FILE_HEAD_NOT_EMPTY);
        }
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
