package com.erp.server.dmp.pull.service;


import com.erp.model.dmp.dto.RequestDTO;

public interface IReportSaveService<T> {

    void pullDataSave(RequestDTO dto) throws Exception;

}
