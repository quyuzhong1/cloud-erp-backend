package com.erp.server.dmp.pull.service;


import com.erp.model.dmp.dto.RequestDTO;

public interface IReportHistoryService<T> {

    void pullDataSave(RequestDTO dto) throws Exception;

    void pullHistoryOrderInfo(RequestDTO requestDTO) throws Exception;

    void analysisOrder(T entity) throws Exception;

}
