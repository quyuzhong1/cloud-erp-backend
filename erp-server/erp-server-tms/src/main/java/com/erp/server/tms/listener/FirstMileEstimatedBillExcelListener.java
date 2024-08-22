package com.erp.server.tms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.excel.FirstMileEstimatedBillExcelDTO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 头程暂估账单
 * @date 2024-08-21
 * @author tanmujin
 */
@Getter
public class FirstMileEstimatedBillExcelListener extends AnalysisEventListener<FirstMileEstimatedBillExcelDTO> {

    /**
     * 错误信息
     */
    private List<FirstMileEstimatedBillExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<FirstMileEstimatedBillExcelDTO> dataList = new ArrayList<>();
    /**
     * 成功信息
     */
    private List<FirstMileEstimatedBillExcelDTO> successList = new ArrayList<>();

    @Override
    public void invoke(FirstMileEstimatedBillExcelDTO excelDTO, AnalysisContext context) {
        List<String> errorMsgList = new ArrayList<>();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //添加数据用于判断是否为空
        dataList.add(excelDTO);
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }
}
