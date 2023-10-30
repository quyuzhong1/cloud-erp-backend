package com.erp.server.wms.service;
import com.erp.model.wms.entity.FbaDeliveryLogisticsEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaDeliveryLogisticsDTO;

/**
 * <p>
 * FBI发货单物流信息表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
public interface FbaDeliveryLogisticsService extends SuperService<FbaDeliveryLogisticsEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    String add(FbaDeliveryLogisticsDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    Boolean update(FbaDeliveryLogisticsDTO.UpdateDTO dto);


}
