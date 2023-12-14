package com.erp.server.wms.service;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;

/**
 * <p>
 * b2c发货拦截单 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
public interface SoB2cDeliveryInterceptService extends SuperService<SoB2cDeliveryInterceptEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-12-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoB2cDeliveryInterceptDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-12-13
    * @param dto
    * @return
    */
    Boolean update(SoB2cDeliveryInterceptDTO.UpdateDTO dto);


}
