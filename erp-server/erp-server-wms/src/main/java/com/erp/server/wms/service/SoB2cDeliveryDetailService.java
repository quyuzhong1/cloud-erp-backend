package com.erp.server.wms.service;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoB2cDeliveryDetailDTO;

/**
 * <p>
 * b2c发货单详情 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
public interface SoB2cDeliveryDetailService extends SuperService<SoB2cDeliveryDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-12-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoB2cDeliveryDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-12-13
    * @param dto
    * @return
    */
    Boolean update(SoB2cDeliveryDetailDTO.UpdateDTO dto);


}
