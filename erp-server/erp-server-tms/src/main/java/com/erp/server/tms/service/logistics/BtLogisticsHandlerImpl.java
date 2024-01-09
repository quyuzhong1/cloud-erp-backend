package com.erp.server.tms.service.logistics;

import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
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
@LogisticsPlatformType(LogisticsPlatformEnum.TONG_YOU)
public class BtLogisticsHandlerImpl extends AbstractLogisticsHandler {
}
