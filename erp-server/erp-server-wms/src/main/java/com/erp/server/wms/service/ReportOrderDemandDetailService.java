package com.erp.server.wms.service;
import com.erp.model.wms.entity.ReportOrderDemandDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.ReportOrderDemandDetailDTO;

/**
 * <p>
 * 订单需求明细报表 服务类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
public interface ReportOrderDemandDetailService extends SuperService<ReportOrderDemandDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-09-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ReportOrderDemandDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-09-23
    * @param dto
    * @return
    */
    Boolean update(ReportOrderDemandDetailDTO.UpdateDTO dto);


}
