package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsWarehouseDTO;

/**
 * <p>
 * 海外仓物流商 仓库表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsWarehouseService extends SuperService<LogisticsWarehouseEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsWarehouseDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(LogisticsWarehouseDTO.UpdateDTO dto);


}
