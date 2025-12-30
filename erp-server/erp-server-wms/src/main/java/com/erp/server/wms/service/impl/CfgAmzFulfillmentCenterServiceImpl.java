package com.erp.server.wms.service.impl;


import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.server.wms.mapper.CfgAmzFulfillmentCenterMapper;
import com.erp.server.wms.service.CfgAmzFulfillmentCenterService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 亚马逊仓储中心配置 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-12-25
 */
@Slf4j
@Service
public class CfgAmzFulfillmentCenterServiceImpl extends SuperServiceImpl<CfgAmzFulfillmentCenterMapper, CfgAmzFulfillmentCenterEntity> implements CfgAmzFulfillmentCenterService {

    @Override
    public String findCountryByCode(String fulfillmentCenter) {
        CfgAmzFulfillmentCenterEntity entity = lambdaQuery()
                .eq(CfgAmzFulfillmentCenterEntity::getCode, fulfillmentCenter)
                .eq(CfgAmzFulfillmentCenterEntity::getDisabled, false)
                .last("LIMIT 1")
                .one();
        if (null == entity){
            return "";
        }
        return entity.getCountry();
    }
}
