package com.erp.server.wms.service;
import com.erp.model.wms.entity.ReportOrderSalesEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.ReportOrderSalesDTO;

/**
 * <p>
 * 订单销量表 服务类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
public interface ReportOrderSalesService extends SuperService<ReportOrderSalesEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-09-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ReportOrderSalesDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-09-23
    * @param dto
    * @return
    */
    Boolean update(ReportOrderSalesDTO.UpdateDTO dto);


}
