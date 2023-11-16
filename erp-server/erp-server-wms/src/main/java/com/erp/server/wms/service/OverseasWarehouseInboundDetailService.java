package com.erp.server.wms.service;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;

/**
 * <p>
 * 海外仓入库单详情 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
public interface OverseasWarehouseInboundDetailService extends SuperService<OverseasWarehouseInboundDetailEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasWarehouseInboundDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasWarehouseInboundDetailDTO.UpdateDTO dto);


}
