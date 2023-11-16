package com.erp.server.wms.service;
import com.erp.model.wms.entity.OverseasWarehouseInboundReceivedEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasWarehouseInboundReceivedDTO;

/**
 * <p>
 * 海外仓签收记录 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
public interface OverseasWarehouseInboundReceivedService extends SuperService<OverseasWarehouseInboundReceivedEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasWarehouseInboundReceivedDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasWarehouseInboundReceivedDTO.UpdateDTO dto);


}
