package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoDeliveryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoDeliveryDTO;

/**
 * <p>
 * 中台配货单主表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-04-17
 */
public interface DmpSoDeliveryService extends SuperService<DmpSoDeliveryEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-04-17
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoDeliveryDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-04-17
    * @param dto
    * @return
    */
    Boolean update(DmpSoDeliveryDTO.UpdateDTO dto);


}
