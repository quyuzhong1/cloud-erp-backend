package com.erp.server.wms.service;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDetailDTO;

/**
 * <p>
 * b2c发货拦截单详情 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
public interface SoB2cDeliveryInterceptDetailService extends SuperService<SoB2cDeliveryInterceptDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-12-13
    * @param addDTO
    * @param mainId
    * @return
    */
    BaseResultDTO.AddDTO add(SoB2cDeliveryInterceptDTO.AddDTO addDTO, String mainId);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-12-13
    * @param dto
    * @return
    */
    Boolean update(SoB2cDeliveryInterceptDetailDTO.UpdateDTO dto);


}
