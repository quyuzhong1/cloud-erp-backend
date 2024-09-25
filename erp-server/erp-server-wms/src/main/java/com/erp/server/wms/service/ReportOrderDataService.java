package com.erp.server.wms.service;
import com.erp.model.wms.entity.ReportOrderDataEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.ReportOrderDataDTO;

/**
 * <p>
 * 订单报表信息 服务类
 * </p>
 *
 * @author will
 * @since 2024-09-24
 */
public interface ReportOrderDataService extends SuperService<ReportOrderDataEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-09-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ReportOrderDataDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-09-24
    * @param dto
    * @return
    */
    Boolean update(ReportOrderDataDTO.UpdateDTO dto);


}
