package com.erp.server.mrp.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.mrp.dto.excel.PurchaseSuggestMergeImportExcelDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 采购建议（合并）导入
 * @author will
 * @date 2024/8/30 16:59
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseSuggestMergeImportExcelListener extends AnalysisEventListener<PurchaseSuggestMergeImportExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private final List<PurchaseSuggestMergeImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private final List<PurchaseSuggestMergeImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private final List<PurchaseSuggestMergeImportExcelDTO> successList = new ArrayList<>();

    @Override
    public void invoke(PurchaseSuggestMergeImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        //添加数据用于判断是否为空
        allList.add(importExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }
        successList.add(importExcelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
