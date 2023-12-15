package com.erp.server.tms.service.impl;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.AsyncService;
import com.erp.server.tms.service.LogisticsSaleChannelService;
import com.erp.server.tms.service.LogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName AsyncServiceImpl
 * @description: TODO
 * @date 2023年12月14日
 * @version: 1.0
 */
@Slf4j
@Service
public class AsyncServiceImpl implements AsyncService {
    @Resource
    private LogisticsRegistry logisticsRegistry;
    @Lazy
    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;

    @Async("tmsExecutor")
    @Override
    public void asyncUpdateSaleChannel(Map<String, String> authMap) {
        if (Objects.isNull(authMap)) return;
        if(StringUtils.isBlank(authMap.get("logisticsPlatform"))) return;
        LogisticsService service = logisticsRegistry.getHandler(authMap.get("logisticsPlatform"));
        ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
        chanelQueryVO.setAuthMap(authMap);
        ApiResult<List<LogisticsSaleChannelEntity>> channels = service.getChannel(chanelQueryVO);
        if (channels.isSuccess()) {
            channels.getData().forEach(logisticsSaleChannelEntity -> {
                logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
            });
        } else {
            log.error("同步渠道异常：{}",channels.getMsg());
        }
    }
}
