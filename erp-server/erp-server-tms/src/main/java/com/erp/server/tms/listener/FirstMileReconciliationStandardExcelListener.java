package com.erp.server.tms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.excel.FirstMileReconciliationStandardExcelDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 * 头程对账单标准模板监听
 *
 * @author Jim
 * {@code @date:} 2024/03/22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FirstMileReconciliationStandardExcelListener extends AnalysisEventListener<FirstMileReconciliationStandardExcelDTO> {

    /**
     * 错误信息
     */
    private List<FirstMileReconciliationStandardExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<FirstMileReconciliationStandardExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    private List<FirstMileReconciliationStandardExcelDTO> successList = new ArrayList<>();

    public FirstMileReconciliationStandardExcelListener() {

    }

    /**
     * 每解析一行数据回调一遍
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(FirstMileReconciliationStandardExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if ((CharSequenceUtil.isBlank(excelDTO.getCostName()) || CharSequenceUtil.isBlank(excelDTO.getCostValue()))
                && CharSequenceUtil.isBlank(excelDTO.getActualWeight())
                && CharSequenceUtil.isBlank(excelDTO.getVolumeWeight())
        ) {
            errorMsgList.add("实际实重、实际计费重、（费用项、费用金额）至少填一个");
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

    public List<FirstMileReconciliationStandardExcelDTO> getExcelDateList() {
        return dataList;
    }

    /**
     * 据全部解析完后
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
