package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoOutstockPositionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoOutstockPositionDTO;

/**
 * <p>
 * 中台销售订单出库详情 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-07-12
 */
public interface DmpSoOutstockPositionService extends SuperService<DmpSoOutstockPositionEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-07-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoOutstockPositionDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-07-12
    * @param dto
    * @return
    */
    Boolean update(DmpSoOutstockPositionDTO.UpdateDTO dto);


}
