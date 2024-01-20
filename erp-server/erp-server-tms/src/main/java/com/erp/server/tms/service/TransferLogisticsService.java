package com.erp.server.tms.service;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;

import java.util.List;
import java.util.Map;

public interface TransferLogisticsService {
    /**
     * 获取授权信息
     *
     * @param authId
     * @return
     */
    Map<String, String> getLogisticsAuthConfig(String authId);

    /**
     * 根据平台获取授权列表
     * @param platform
     * @return
     */
    List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform);


    /**
     * 渠道查询
     *
     * @return
     */
    ApiResult<List<TransferLogisticsChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO);


    /**
     * 渠道查询
     *
     * @return
     */
    ApiResult authorization(Map<String, String> authMap);
    /**
     * 获取平台标识
     *
     * @return
     */
    LogisticsPlatformEnum getPlatForm();
}
