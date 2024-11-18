package com.erp.server.tms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.excel.DeclareReconciliationStandardExcelDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
/**
 * @description: 报关对账单标准模板监听
 * @author Will
 * @date: 2024/3/27 12:01
 */
public class DeclareReconciliationStandardExcelListener extends AnalysisEventListener<DeclareReconciliationStandardExcelDTO> {

    /**
     * 错误信息
     */
    private List<DeclareReconciliationStandardExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<DeclareReconciliationStandardExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    private List<DeclareReconciliationStandardExcelDTO> successList = new ArrayList<>();


   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2023/3/7 11:22
    * @param excelDTO 导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(DeclareReconciliationStandardExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if ((CharSequenceUtil.isBlank(excelDTO.getCostName()) || CharSequenceUtil.isBlank(excelDTO.getCostValue()))
                && CharSequenceUtil.isBlank(excelDTO.getActualWeight())
                && CharSequenceUtil.isBlank(excelDTO.getActualBillingWeight())
                && CharSequenceUtil.isBlank(excelDTO.getActualWeightUnit())) {
            errorMsgList.add("实际实重、实际计费重、（费用项、费用金额）至少填一个");
        }

        //添加数据用于判断是否为空
        dataList.add(excelDTO);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);

    }

    public List<DeclareReconciliationStandardExcelDTO> getErrorList(){
        return errorList;
    }

    public List<DeclareReconciliationStandardExcelDTO> getSuccessList(){
        return successList;
    }

    public List<DeclareReconciliationStandardExcelDTO> getExcelDateList(){
        return dataList;
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @author Will
     * @date: 2023/3/7 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(CollectionUtils.isEmpty(successList)){
            return;
        }
    }
}
