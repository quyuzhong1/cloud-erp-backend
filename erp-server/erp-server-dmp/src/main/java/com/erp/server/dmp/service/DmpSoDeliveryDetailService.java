package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoDeliveryDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoDeliveryDetailDTO;

/**
 * <p>
 * 中台配货单明细表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-04-17
 */
public interface DmpSoDeliveryDetailService extends SuperService<DmpSoDeliveryDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-04-17
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoDeliveryDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-04-17
    * @param dto
    * @return
    */
    Boolean update(DmpSoDeliveryDetailDTO.UpdateDTO dto);


}
