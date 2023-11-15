package com.erp.server.tms.service;

import com.common.core.controller.vo.ApiResult;

/**
 * @author zdy
 * @ClassName LogisticsBaseService
 * @description: TODO
 * @date 2023年11月15日
 * @version: 1.0
 */
public interface LogisticsBaseService {
    /**
     * 同步渠道
     *
     * @param platform
     * @return
     */
    ApiResult syncLogisticsChannel(String platform);

    /**
     * 同步所有渠道
     * @return
     */
    ApiResult syncAllLogisticsChannel();
}
