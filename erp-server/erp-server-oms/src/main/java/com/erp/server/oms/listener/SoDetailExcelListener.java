package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.excel.SoDetailImportExcelDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lambda
 * @Classname SoDetailExcelListener
 * @Description TODO
 * @Date 2023-05-17 19:47
 * @Created by yl
 */
public class SoDetailExcelListener  extends AnalysisEventListener<SoDetailImportExcelDTO> {

    /**
     * 成功的数据
     */
    private List<SoDetailDTO.ExcelDTO> successList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    private List<SoDetailImportExcelDTO> errorList = new ArrayList<>();


    /**
     * 没解析一行执行一次
     *
     * @param soDetailImportExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-04-21 19:40
     */
    @Override
    public void invoke(SoDetailImportExcelDTO soDetailImportExcelDTO, AnalysisContext analysisContext) {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }


    public List<SoDetailImportExcelDTO> getErrorList() {
        return errorList;
    }


    public List<SoDetailDTO.ExcelDTO> getSuccessList() {
        return successList;
    }
}
