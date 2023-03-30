package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.scm.dto.excel.SupplierImportExcelDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lambda
 * @Classname SupplierExcelListener
 * @Description TODO
 * @Date 2023-03-30 9:46
 * @Created by yl
 */
public class SupplierExcelListener extends AnalysisEventListener<SupplierImportExcelDTO> {


    /**
     * 错误信息
     */
    private List<SupplierImportExcelDTO> errorList = new ArrayList<>();

    public SupplierExcelListener(){

    }
   
   
    /**
     * 每解析一行数据回调一遍
     * @author yl
     * @date 2023-03-30 9:50
     * @param excelDTO
     * @param analysisContext
     * @return void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SupplierImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        
    }

    
    /**
     * 数据全部解析完成后执行
     * @author yl
     * @date 2023-03-30 9:51
     * @param analysisContext
     * @return void
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
