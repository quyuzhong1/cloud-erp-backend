package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.scm.dto.excel.PurchaseOrderImportExcelDTO;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/27 16:00
 */
public class PurchaseOrderExcelListener extends AnalysisEventListener<PurchaseOrderImportExcelDTO> {



    public PurchaseOrderExcelListener() {

    }

    @Override
    public void invoke(PurchaseOrderImportExcelDTO purchaseOrderImportExcelDTO, AnalysisContext analysisContext) {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
