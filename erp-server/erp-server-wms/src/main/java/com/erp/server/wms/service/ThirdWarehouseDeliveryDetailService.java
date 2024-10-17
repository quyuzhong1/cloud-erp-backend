package com.erp.server.wms.service;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDetailDTO;

/**
 * <p>
 * 三方仓发货单明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
public interface ThirdWarehouseDeliveryDetailService extends SuperService<ThirdWarehouseDeliveryDetailEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-10-17
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ThirdWarehouseDeliveryDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-10-17
    * @param dto
    * @return
    */
    Boolean update(ThirdWarehouseDeliveryDetailDTO.UpdateDTO dto);


}
