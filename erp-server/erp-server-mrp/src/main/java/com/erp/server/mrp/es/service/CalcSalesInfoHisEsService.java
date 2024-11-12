package com.erp.server.mrp.es.service;

import com.erp.server.mrp.es.entity.CalcSalesInfoHisEsEntity;

import java.util.List;

public interface CalcSalesInfoHisEsService {

    void batchSave(List<CalcSalesInfoHisEsEntity> list);
}
