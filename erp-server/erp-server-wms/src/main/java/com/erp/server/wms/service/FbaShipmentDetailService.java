package com.erp.server.wms.service;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaShipmentDetailDTO;

/**
 * <p>
 * FBI拣货明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
public interface FbaShipmentDetailService extends SuperService<FbaShipmentDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    String add(FbaShipmentDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    Boolean update(FbaShipmentDetailDTO.UpdateDTO dto);


}
