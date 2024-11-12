package com.erp.server.mrp.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.mrp.dto.CfgRuleCalcDTO;
import com.erp.server.mrp.es.entity.CalcSalesInfoHisEsEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistorySalesQtyExcelListener extends AnalysisEventListener<CfgRuleCalcDTO.HistorySaleImportDTO> {


    private List<CalcSalesInfoHisEsEntity> dataList = new ArrayList<>();

    @Override
    public void invoke(CfgRuleCalcDTO.HistorySaleImportDTO data, AnalysisContext context) {




    }

    public List<CalcSalesInfoHisEsEntity> getDateList(){
        return dataList;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        dataList = new ArrayList<>(dataList.stream()
                .collect(Collectors.toMap(
                        v -> new CfgRuleCalcDTO.GroupDTO(v.getSkuId(), v.getShopId(), v.getDate()),
                        v -> v,
                        (v1, v2) -> {
                            v1.setQty(v1.getQty() + v2.getQty());
                            return v1;
                        }
                ))
                .values());
    }
}
