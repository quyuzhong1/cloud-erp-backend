package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.excel.QcNoticeDetailImportExcelDTO;

import java.util.ArrayList;
import java.util.List;

public class QcNoticeDetailExcelListener extends AnalysisEventListener<QcNoticeDetailImportExcelDTO> {

    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<QcNoticeDetailImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<QcNoticeDetailImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<QcNoticeDetailImportExcelDTO> successList = new ArrayList<>();

    public QcNoticeDetailExcelListener() {
    }

    @Override
    public void invoke(QcNoticeDetailImportExcelDTO xcelDTO, AnalysisContext analysisContext) {
        QcNoticeDetailImportExcelDTO viewDTO = new QcNoticeDetailImportExcelDTO();
        //添加数据用于判断是否为空
        allList.add(xcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(xcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            xcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(xcelDTO);
            return;
        }
        successList.add(xcelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {


    }

    public List<QcNoticeDetailImportExcelDTO> getAllList(){
        return allList;
    }

    public List<QcNoticeDetailImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<QcNoticeDetailImportExcelDTO> getSuccessList(){
        return successList;
    }
}
