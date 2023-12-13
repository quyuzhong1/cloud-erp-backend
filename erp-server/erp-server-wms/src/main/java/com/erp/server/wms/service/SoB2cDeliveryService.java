package com.erp.server.wms.service;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;

/**
 * <p>
 * b2c发货单 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
public interface SoB2cDeliveryService extends SuperService<SoB2cDeliveryEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-12-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoB2cDeliveryDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-12-13
    * @param dto
    * @return
    */
    Boolean update(SoB2cDeliveryDTO.UpdateDTO dto);


}
