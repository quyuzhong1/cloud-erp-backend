package com.erp.server.tms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.excel.LogisticsCarrierExcelDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zdy
 * @version 1.0
 * @description: 物流船司信息
 * @date 2024/5/8 14:17
 */
public class LogisticsCarrierExcelListener extends AnalysisEventListener<LogisticsCarrierExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<LogisticsCarrierExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<LogisticsCarrierExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<LogisticsCarrierExcelDTO> successList = new ArrayList<>();

    public LogisticsCarrierExcelListener() {

    }

    @Override
    public void invoke(LogisticsCarrierExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        //添加数据用于判断是否为空
        allList.add(importExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }
        successList.add(importExcelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<LogisticsCarrierExcelDTO> getAllList(){
        return allList;
    }

    public List<LogisticsCarrierExcelDTO> getErrorList(){
        return errorList;
    }

    public List<LogisticsCarrierExcelDTO> getSuccessList(){
        return successList;
    }
}
