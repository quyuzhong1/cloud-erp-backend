package com.erp.server.dmp.push.service.sdy;

import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.core.controller.vo.ApiResult;

import java.util.List;

public interface SdyPushCommonService {
    ApiResult executeConsumer(ShudiyunB2cOrderDTO shudiyunB2cOrderDTOList);
}
