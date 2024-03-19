package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsFirstMileLogisticEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;

/**
 * <p>
 * 头程物流单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
 */
public interface TmsFirstMileLogisticService extends SuperService<TmsFirstMileLogisticEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsFirstMileLogisticDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    Boolean update(TmsFirstMileLogisticDTO.UpdateDTO dto);


}
