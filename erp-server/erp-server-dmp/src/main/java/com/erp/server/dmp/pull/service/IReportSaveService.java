package com.erp.server.dmp.pull.service;

import com.erp.server.dmp.entity.dto.RequestDTO;

public interface IReportSaveService {

    void pullDataSave(RequestDTO dto) throws Exception;
}
