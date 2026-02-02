package com.erp.server.tms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
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
import org.springframework.transaction.annotation.Transactional;

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


    private static final int BATCH_COUNT = 1000;
    private final String taskId;
    private final Integer importCount;
    private final ImportHistoryRecordDTO.ImportSyncDTO importDTO;
    private final CfgLogisticsCostImportEntity costImportEntity;
    private final List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList;
    @Getter
    private Integer count = 0;
    /**
     * 错误信息
     */
    @Getter
    private List<JSONObject> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private final List<JSONObject> dataList = new ArrayList<>();

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
                importHistoryRecordService.handleImportSuccessList(importDTO,costImportEntity,cfgImportDetailList,successList, errorList2, headList, headMap);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(jsonObject -> {
                    jsonObject.set(ObjectUtil.isNull(jsonObject) ? "" : String.valueOf(jsonObject.size() - 1) ,e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage());
                });
                errorList.addAll(successList);
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
                importHistoryRecordService.handleImportSuccessList(importDTO,costImportEntity,cfgImportDetailList,successList, errorList2, headList, headMap);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(jsonObject -> {
                    jsonObject.set(ObjectUtil.isNull(jsonObject) ? "" : String.valueOf(jsonObject.size() - 1) ,e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage());
                });
                errorList.addAll(successList);
            }
        }
        //添加导入历史记录表数据
        ImportHistoryRecordDTO.AddDTO addDTO = new ImportHistoryRecordDTO.AddDTO();
        addDTO.setReconciliationMonth(importDTO.getReconciliationMonth());
        addDTO.setBusinessType(costImportEntity.getBusinessType());
        addDTO.setFileUrl(importDTO.getFileUrl());
        addDTO.setFileName(importDTO.getFileName());
        if (CharSequenceUtil.equals(importDTO.getProcessingType(), ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode())) {
            addDTO.setStatus( ImportHistoryRecordStatusEnum.WAIT_HANDLE.getStatus());
        } else {
            addDTO.setStatus( ImportHistoryRecordStatusEnum.HANDLE.getStatus());
        }
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        addDTO.setType(importDTO.getType());
        addDTO.setOperationUserId(userInfo.getUid());
        addDTO.setImportCount(count);
        addDTO.setMatchCount(count - errorList.size());
        importHistoryRecordService.add(addDTO);
    }

    @Override
    public void invokeHeadMap(Map<Integer,String> map, AnalysisContext analysisContext) {
        List<String> headList = map.values().stream().map(String::toString).collect(Collectors.toList());
        headList.add("匹配结果");
        headList.add("错误信息");
        map.put(map.size(),"匹配结果");
        map.put(map.size() + 1,"错误信息");
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
}
