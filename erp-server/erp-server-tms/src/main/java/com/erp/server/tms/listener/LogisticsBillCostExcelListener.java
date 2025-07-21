package com.erp.server.tms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.excel.LogisticsBillCostExcelDTO;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.service.LogisticsBillCostService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

public class LogisticsBillCostExcelListener extends AnalysisEventListener<LogisticsBillCostExcelDTO> {
    private static final int BATCH_COUNT = 1000;

    private String taskId;
    @Getter
    private Integer count = 0;
    /**
     * 错误信息
     */
    @Getter
    private List<LogisticsBillCostExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<LogisticsBillCostExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<LogisticsBillCostExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    private LogisticsBillCostService logisticsBillCostService = SpringUtil.getBean(LogisticsBillCostService.class);
    private DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    public LogisticsBillCostExcelListener(String taskId) {
        this.taskId = taskId;
    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2023/3/7 11:22
    * @param excelDTO 导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(LogisticsBillCostExcelDTO excelDTO, AnalysisContext analysisContext) {
        count += 1;
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        excelDTO.setPayType(excelDTO.getPayTypeName().equals("付款") ? "pay" : "refund");
        successList.add(excelDTO);
        if (successList.size() >= BATCH_COUNT){
            logisticsBillCostService.handleImportSuccessList(successList, errorList, DictCostAttributionEnum.SELF_DELIVER.getCode());
            successList.clear();
            updateTask(count);
        }
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @author Will
     * @date: 2023/3/7 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()) {
            logisticsBillCostService.handleImportSuccessList(successList, errorList, DictCostAttributionEnum.SELF_DELIVER.getCode());
        }
    }

    private void updateTask(Integer count){
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setMsg("处理中");
        importResultDTO.setCount(count);
        downloadTaskFeign.updateTask(importResultDTO);
    }

}
