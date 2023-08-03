package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.excel.ImportPurchasePriceExcelDTO;

import java.util.List;

/**
 * @CreateTime: 2023-08-03  18:07
 * @Author: zhangchunlin
 */
public class PurchasePriceExcelListener extends AnalysisEventListener<ImportPurchasePriceExcelDTO> {

    private List<FindUserDTO> userList;

    private List<SkuVO> skuList;

    @Override
    public void invoke(ImportPurchasePriceExcelDTO importPurchasePriceExcelDTO, AnalysisContext analysisContext) {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

}