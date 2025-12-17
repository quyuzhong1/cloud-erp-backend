package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.dto.excel.PurchaseOrderMainExcelDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 采购订单主表导入
 * @author will
 * @date 2025/7/30 18:12
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderMainExcelListener extends AnalysisEventListener<PurchaseOrderMainExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private final List<PurchaseOrderMainExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private final List<PurchaseOrderMainExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private final List<PurchaseOrderMainExcelDTO> successList = new ArrayList<>();

    @Override
    public void invoke(PurchaseOrderMainExcelDTO importExcelDTO, AnalysisContext analysisContext) {
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
