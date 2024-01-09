package com.erp.server.tms.service.logistics;

import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Lambda
 * @Classname BtLogisticsHandlerImpl
 * @Description 巴通物流商对接
 * @Date 2024-01-09 15:07
 * @Created by yl
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.BT)
public class BtLogisticsHandlerImpl extends AbstractLogisticsHandler {

    /**
     * 下物流单
     * @param logisticsOrderVO
     * @return
     */
    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();

        return null;

    }

}
