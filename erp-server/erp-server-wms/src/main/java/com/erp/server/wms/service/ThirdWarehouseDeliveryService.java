package com.erp.server.wms.service;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDTO;

/**
 * <p>
 * 三方仓发货单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
public interface ThirdWarehouseDeliveryService extends SuperService<ThirdWarehouseDeliveryEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-10-17
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ThirdWarehouseDeliveryDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-10-17
    * @param dto
    * @return
    */
    Boolean update(ThirdWarehouseDeliveryDTO.UpdateDTO dto);


}
