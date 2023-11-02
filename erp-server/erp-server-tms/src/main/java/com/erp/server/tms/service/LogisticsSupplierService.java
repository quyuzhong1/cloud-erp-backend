package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsSupplierDTO;

/**
 * <p>
 * 物理商表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsSupplierService extends SuperService<LogisticsSupplierEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsSupplierDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(LogisticsSupplierDTO.UpdateDTO dto);


}
