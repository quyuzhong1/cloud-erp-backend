package com.erp.server.wms.service;
import com.erp.model.wms.entity.AliexpressDeliveryDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.AliexpressDeliveryDetailDTO;

/**
 * <p>
 * 速卖通发货单详情 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-05-06
 */
public interface AliexpressDeliveryDetailService extends SuperService<AliexpressDeliveryDetailEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-05-06
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AliexpressDeliveryDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-05-06
    * @param dto
    * @return
    */
    Boolean update(AliexpressDeliveryDetailDTO.UpdateDTO dto);


}
