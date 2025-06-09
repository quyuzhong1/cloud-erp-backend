package com.erp.server.plm.listener;/**
 * @author Lambda
 * @Classname LogisticsProductExcelListener
 * @Description TODO
 * @Date 2023-11-08 11:59
 * @Created by yl
 */

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.LogisticsProductExcelDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 物流产品导入
 * @Author yl
 * @Date 2023-11-08 11:59
 */
public class LogisticsProductExcelListener extends AnalysisEventListener<LogisticsProductExcelDTO>{

    /**
     * 错误信息
     */
    private List<LogisticsProductExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<LogisticsProductExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    private List<LogisticsProductExcelDTO> successList = new ArrayList<>();


    /**
     * 解析一行执行一行
     * @param logisticsProductExcelDTO
     * @param analysisContext
     */
    @Override
    public void invoke(LogisticsProductExcelDTO logisticsProductExcelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(logisticsProductExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        dataList.add(logisticsProductExcelDTO);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            logisticsProductExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(logisticsProductExcelDTO);
            return;
        }
        successList.add(logisticsProductExcelDTO);
    }

    public List<LogisticsProductExcelDTO> getErrorList(){
        return errorList;
    }

    public List<LogisticsProductExcelDTO> getSuccessList(){
        return successList;
    }

    public List<LogisticsProductExcelDTO> getExcelDateList(){
        return dataList;
    }
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        return;
    }
}
