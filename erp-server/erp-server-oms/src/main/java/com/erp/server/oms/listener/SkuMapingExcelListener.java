package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.oms.dto.excel.SkuMapingImportExcelDTO;

/**
 * @author Lambda
 * @Classname SkuMapingExcelListener
 * @Description TODO
 * @Date 2023-06-28 18:07
 * @Created by yl
 */
public class SkuMapingExcelListener extends AnalysisEventListener<SkuMapingImportExcelDTO> {
    
    /**
     * 每解析一行执行一次
     * @author yl
     * @date 2023-06-28 18:12
     * @param skuMapingImportExcelDTO
     * @param analysisContext
     * @return void
     */
    @Override
    public void invoke(SkuMapingImportExcelDTO skuMapingImportExcelDTO, AnalysisContext analysisContext) {
        
    }


    //所有执行玩后 在执行
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
