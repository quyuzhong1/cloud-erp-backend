package com.erp.server.srm.service;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;

import java.util.List;

/**
 * <p>
 * 送货单明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
public interface DeliveryOrderDetailService extends SuperService<DeliveryOrderDetailEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-01-12
    * @param dto
    * @return
    */
    void add(List<DeliveryOrderDetailDTO.AddDTO> dto, String mainId);

    List<DeliveryOrderDetailEntity> listByMainId(String mainId);

    /**
    * 修改
    * @author lrp
    * @date: 2024-01-12
    * @param dto
    * @return
    */
    Boolean update(List<DeliveryOrderDetailDTO.UpdateDTO> dto,String mainId);


}
