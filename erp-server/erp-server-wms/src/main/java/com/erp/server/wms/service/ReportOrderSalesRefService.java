package com.erp.server.wms.service;
import com.erp.model.wms.entity.ReportOrderSalesRefEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.ReportOrderSalesRefDTO;

/**
 * <p>
 * 订单销量关联表 服务类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
public interface ReportOrderSalesRefService extends SuperService<ReportOrderSalesRefEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-09-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ReportOrderSalesRefDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-09-23
    * @param dto
    * @return
    */
    Boolean update(ReportOrderSalesRefDTO.UpdateDTO dto);


}
