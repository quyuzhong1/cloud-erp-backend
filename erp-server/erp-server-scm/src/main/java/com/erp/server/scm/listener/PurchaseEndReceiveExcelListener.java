package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.dto.excel.PurchaseEndReceiveImportExcelDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 采购结束交货
 * @author Will
 * @date: 2024/3/5 11:25
 */
public class PurchaseEndReceiveExcelListener extends AnalysisEventListener<PurchaseEndReceiveImportExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<PurchaseEndReceiveImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<PurchaseEndReceiveImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<PurchaseEndReceiveImportExcelDTO> successList = new ArrayList<>();

    public PurchaseEndReceiveExcelListener() {

    }

    @Override
    public void invoke(PurchaseEndReceiveImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
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

    public List<PurchaseEndReceiveImportExcelDTO> getAllList(){
        return allList;
    }

    public List<PurchaseEndReceiveImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<PurchaseEndReceiveImportExcelDTO> getSuccessList(){
        return successList;
    }
}
