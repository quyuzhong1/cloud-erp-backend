package com.erp.server.tms.service.transfer;

import com.common.business.annotation.TransferLogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.server.tms.handler.AbstractTransferLogisticsHandler;
import com.sdk.tms.baohong.api.order.GetShippingMethodListResponse;
import com.sdk.tms.baohong.service.BaoHongService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 保宏中转报关服务商
 * @Author Luo_WG
 * @Date 2024/1/20 11:01
 **/
@Slf4j
@Component
@TransferLogisticsPlatformType(LogisticsPlatformEnum.BAO_HONG)
public class baoHongTransferHandlerImpl extends AbstractTransferLogisticsHandler {
    @Resource
    private BaoHongService baoHongService;

    @Override
    public Map<String, String> getLogisticsAuthConfig(String authId) {
        return super.getLogisticsAuthConfig(authId);
    }

    @Override
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform) {
        return super.getLogisticsAuthConfigByPlatform(platform);
    }

    @Override
    public ApiResult<List<TransferLogisticsChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        return super.getChannel(chanelQueryVO);
    }

    @Override
    public ApiResult authorization(Map<String, String> authMap) {
        try {
            Map<String, Object> authObjMap = authMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            ThirdWarehouseContext.setAuthMap(authObjMap);

            GetShippingMethodListResponse response = baoHongService.authorization("");

            return "1".equals(response.getAsk()) ? success("授权成功") : failure("授权失败:" + response.getMessage());
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return super.getPlatForm();
    }


}
