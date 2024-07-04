package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoOutstockEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoOutstockDTO;

/**
 * <p>
 * 中台销售订单出库详情 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-26
 */
public interface DmpSoOutstockService extends SuperService<DmpSoOutstockEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoOutstockDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-26
    * @param dto
    * @return
    */
    Boolean update(DmpSoOutstockDTO.UpdateDTO dto);


}
