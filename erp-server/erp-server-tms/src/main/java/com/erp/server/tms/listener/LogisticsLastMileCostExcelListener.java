package com.erp.server.tms.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.service.LogisticsLastMileCostService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: 尾程费用导入
 * @author Will
 * @date: 2024/5/10 18:34
 */
public class LogisticsLastMileCostExcelListener extends AnalysisEventListener<Map<Integer,String>> {
    private static final int BATCH_COUNT = 1000;
    private final String taskId;
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

    private final LogisticsLastMileCostService logisticsLastMileCostService = SpringUtil.getBean(LogisticsLastMileCostService.class);
    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);
    public LogisticsLastMileCostExcelListener(String taskId) {
        this.taskId = taskId;
    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2023/3/7 11:22
    * @param map 导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(Map<Integer,String>  map, AnalysisContext analysisContext) {
        count += 1;
//        List<String> errorMsgList = new ArrayList<>();
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
        //存在错误数据则直接返回
//        if (errorMsgList.size() > 0) {
//            String errorMsg = FieldValidUtil.getMsgSort(errorMsgList);
//            excelDTO.set("错误信息",errorMsg);
//            errorList.add(excelDTO);
//            return;
//        }

        successList.add(excelDTO);
        if (successList.size() >= BATCH_COUNT){
            try {
                List<JSONObject> errorList2 = new ArrayList<>();
                logisticsLastMileCostService.handleImportSuccessList(successList, errorList2, headList, headMap);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(jsonObject -> {
                    jsonObject.set("错误信息",e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage());
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
     * @date: 2023/3/7 15:32
     * @param analysisContext
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()) {
            try {
                List<JSONObject> errorList2 = new ArrayList<>();
                logisticsLastMileCostService.handleImportSuccessList(successList, errorList2, headList, headMap);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(jsonObject -> {
                    jsonObject.set("错误信息",e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage());
                });
                errorList.addAll(successList);
            }
        }
    }

    @Override
    public void invokeHeadMap(Map<Integer,String> map, AnalysisContext analysisContext) {
        List<String> headList = map.values().stream().map(String::toString).collect(Collectors.toList());
        headList.add("错误信息");
        map.put(map.size(),"错误信息");
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
