package com.erp.server.mrp.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.mrp.dto.CfgRuleSalesEstimateFileDTO;

public class SalesEstimateExcelFileListener extends AnalysisEventListener<CfgRuleSalesEstimateFileDTO.ExcelDTO> {



    @Override
    public void invoke(CfgRuleSalesEstimateFileDTO.ExcelDTO data, AnalysisContext context) {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }
}
