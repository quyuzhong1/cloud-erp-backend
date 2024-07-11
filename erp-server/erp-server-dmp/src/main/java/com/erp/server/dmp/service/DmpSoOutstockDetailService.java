package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoOutstockDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoOutstockDetailDTO;

/**
 * <p>
 * 中台销售订单出库详情明细表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-26
 */
public interface DmpSoOutstockDetailService extends SuperService<DmpSoOutstockDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoOutstockDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-26
    * @param dto
    * @return
    */
    Boolean update(DmpSoOutstockDetailDTO.UpdateDTO dto);


}
