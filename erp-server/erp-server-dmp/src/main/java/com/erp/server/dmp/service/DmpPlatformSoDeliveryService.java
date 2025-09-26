package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpPlatformSoDeliveryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpPlatformSoDeliveryDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zdy
 * @since 2025-08-29
 */
public interface DmpPlatformSoDeliveryService extends SuperService<DmpPlatformSoDeliveryEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-08-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpPlatformSoDeliveryDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-08-29
    * @param dto
    * @return
    */
    Boolean update(DmpPlatformSoDeliveryDTO.UpdateDTO dto);


}
