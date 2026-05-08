package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WarehouseLocationMappingDTO;
import com.erp.model.wms.entity.WarehouseLocationMappingEntity;

import javax.servlet.http.HttpServletResponse;

/**
 *
 * @date 2024-08-14
 * @author tanmujin
 */
public interface WarehouseLocationMappingService extends SuperService<WarehouseLocationMappingEntity> {

    PagingVO<WarehouseLocationMappingDTO.ViewDTO> paging(PagingDTO<WarehouseLocationMappingDTO.SearchDTO> dto);

    WarehouseLocationMappingDTO.ViewDTO view(String id);

    void add(WarehouseLocationMappingDTO.AddDTO dto);

    void update(WarehouseLocationMappingDTO.UpdateDTO dto);

    void delete(WarehouseLocationMappingDTO.IdsDTO dto);

    Boolean importFile(BaseDTO.ImportDTO dto);

    void importWarehouseLocationMapping(BaseDTO.ImportDTO dto);

    void downloadTemplate(HttpServletResponse response);

    WarehouseLocationMappingDTO.BindWarehouseDTO getBindWarehouse(String sysWarehouseId, String dictPlatform);
}
