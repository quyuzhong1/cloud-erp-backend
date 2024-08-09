package com.erp.server.sys.service;

import com.erp.model.sys.dto.CfgExportFieldDTO;

import java.util.List;

public interface CfgExportService {


    List<CfgExportFieldDTO> getExportField(String exportName);
}
