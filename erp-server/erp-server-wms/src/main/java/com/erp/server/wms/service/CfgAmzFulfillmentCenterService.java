package com.erp.server.wms.service;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgAmzFulfillmentCenterDTO;

/**
 * <p>
 * 亚马逊仓储中心配置 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-12-25
 */
public interface CfgAmzFulfillmentCenterService extends SuperService<CfgAmzFulfillmentCenterEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2023-12-25
    * @param fulfillmentCenter fulfillmentCenter
    * @return
    */
    String findCountryByCode(String fulfillmentCenter);
}
