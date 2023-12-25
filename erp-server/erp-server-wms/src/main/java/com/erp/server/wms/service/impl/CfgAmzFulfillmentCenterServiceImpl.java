package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.server.wms.mapper.CfgAmzFulfillmentCenterMapper;
import com.erp.server.wms.service.CfgAmzFulfillmentCenterService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.CfgAmzFulfillmentCenterDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
