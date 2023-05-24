package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.model.wms.entity.InstockForcastDetailEntity;

import java.util.List;

/**
 * <p>
 * 入库预报明细表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-09
 */
public interface InstockForcastDetailService extends SuperService<InstockForcastDetailEntity> {

    /**
     * 新增入库预报详情
     * @param dto
     * @param id
     * @return
     */
    List<InstockForcastDetailEntity> add(InstockForcastDTO.AddDTO dto, String id);

    /**
     * 根据入库预报主单id和采购订单明细id获取入库预报明细信息
     * @param mainId
     * @param purchaseOrderDetailId
     * @return
     */
    InstockForcastDetailEntity find(String mainId, String purchaseOrderDetailId);


}
