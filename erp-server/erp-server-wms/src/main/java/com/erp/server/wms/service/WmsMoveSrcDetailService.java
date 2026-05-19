package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.model.wms.entity.WmsMoveSrcDetailEntity;

import java.util.List;

/**
 * 仓位移动来源明细表 服务接口
 *
 * @author liuchao
 * @since 2026-05-18
 */
public interface WmsMoveSrcDetailService extends SuperService<WmsMoveSrcDetailEntity> {

    /**
     * 根据仓位移动主单 ID 查询全部来源明细（用于移箱明细弹窗）
     *
     * @param mainId warehouse_location_move.id
     * @return 该移仓单对应的所有来源明细行 VO
     */
    List<AfterSalesWarehouseLocationSuggestDto.BoxMoveDetailListDto> listBoxMoveDetail(String mainId);
}
