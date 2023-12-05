package com.erp.server.wms.service;
import com.erp.model.wms.entity.OverseasTransferWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasTransferWarehouseDTO;

import java.util.List;

/**
 * <p>
 * 海外仓签收记录 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
public interface OverseasTransferWarehouseService extends SuperService<OverseasTransferWarehouseEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasTransferWarehouseDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasTransferWarehouseDTO.UpdateDTO dto);


    Boolean saveOrUpdateByPlatform(OverseasTransferWarehouseEntity mqEntity);

    /**
     * 中转仓列表基础信息
     * @author Jim
     * @date: 2023-11-16
     */
    List<BaseSelectDTO> baseSelectlist(String dictPlatform);

    /**
     * 中转仓列对应物流名称
     * @author Jim
     * @date: 2023-11-16
     */
    List<BaseSelectDTO> LogisticsProductList(String code);
}
