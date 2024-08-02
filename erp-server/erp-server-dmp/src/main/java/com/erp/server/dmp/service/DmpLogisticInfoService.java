package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpLogisticInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpLogisticInfoDTO;

/**
 * <p>
 * 中台销售订单出库库位详情 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-07-30
 */
public interface DmpLogisticInfoService extends SuperService<DmpLogisticInfoEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-07-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpLogisticInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-07-30
    * @param dto
    * @return
    */
    Boolean update(DmpLogisticInfoDTO.UpdateDTO dto);


}
