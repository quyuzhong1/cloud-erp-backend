package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsFirstMileLogisticFeeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsFirstMileLogisticFeeDTO;

/**
 * <p>
 * 头程物流单费用 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
 */
public interface TmsFirstMileLogisticFeeService extends SuperService<TmsFirstMileLogisticFeeEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsFirstMileLogisticFeeDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    Boolean update(TmsFirstMileLogisticFeeDTO.UpdateDTO dto);


}
