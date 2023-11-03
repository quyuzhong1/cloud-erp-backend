package com.erp.server.tms.service.logistics;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.server.tms.service.LogisticsService;

/**
 * @author zdy
 * @ClassName AbstractLogisticsService
 * @description: 抽象类 封装公共方法
 * @date 2023年11月03日
 * @version: 1.0
 */
public abstract class AbstractLogisticsService implements LogisticsService {
    //对于一些公共方法可以进行封装
    public ApiResult<String> createOrder(String soId, String authId){
        //1.获取配置信息
        LogisticsAuthEntity logisticsAuthConfig = getLogisticsAuthConfig(authId);
        //获取订单
        SoInfoEntity soInfo = getSoInfo(soId);
        //必填字段校验

        //创建订单
        return createOrder(soInfo, null, null, logisticsAuthConfig);
    }
}
