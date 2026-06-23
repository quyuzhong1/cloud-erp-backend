package com.erp.server.wms.service.imports;

import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.dto.CfgQcUserDTO;
import com.erp.model.wms.entity.CfgQcUserEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * 质检员配置导入落库中间对象（校验通过后批量 insert/update 使用）
 */
@Getter
@RequiredArgsConstructor
public class CfgQcUserImportPersistItem {

    private final CfgQcUserDTO.ImportExcelDTO dto;
    private final SupplierEntity supplier;
    private final String warehouseId;
    private final CfgQcUserEntity exists;
    private final CfgQcUserDTO.CommonDTO commonDTO;
    private final Map<String, String> qcUserIdToNameMap;
    private final String pairKey;

    @Setter
    private CfgQcUserEntity entity;

    @Setter
    private CfgQcUserEntity savedEntity;
}
