package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.pickingstrategy.WarehouseAreaDTO;
import com.erp.model.wms.entity.WarehouseAreaInfoEntity;

import java.util.List;

/**
 * <p>
 * 库区管理 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-05-29
 */
public interface WarehouseAreaInfoService extends SuperService<WarehouseAreaInfoEntity>, ApproveHandler {

    void updateStatus(UpdateStateDTO.BatchUpdateDTO dto);

    void delete(List<String> ids);

    WarehouseAreaDTO.View view(String id);

    void update(WarehouseAreaDTO.Add dto, String id);

    void add(WarehouseAreaDTO.Add dto);

    PagingVO<WarehouseAreaDTO.PagingView> paging(PagingDTO<WarehouseAreaDTO.PagingParam> dto);


}
