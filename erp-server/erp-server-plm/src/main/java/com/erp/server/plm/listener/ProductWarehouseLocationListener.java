package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.plm.dto.excel.ProductWarehouseLocationExcelDTO;

public class ProductWarehouseLocationListener extends AnalysisEventListener<ProductWarehouseLocationExcelDTO> {

    @Override
    public void invoke(ProductWarehouseLocationExcelDTO productWarehouseLocationExcelDTO, AnalysisContext analysisContext) {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
