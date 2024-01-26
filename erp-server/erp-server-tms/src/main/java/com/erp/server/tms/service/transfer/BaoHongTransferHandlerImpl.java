package com.erp.server.tms.service.transfer;

import com.common.business.annotation.TransferLogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateInboundReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsOrderDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsProductDTO;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.server.tms.handler.AbstractTransferLogisticsHandler;
import com.sdk.tms.baohong.api.order.SmRow;
import com.sdk.tms.baohong.dto.response.BaoHongResponse;
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
public class BaoHongTransferHandlerImpl extends AbstractTransferLogisticsHandler {

    @Resource
    private BaoHongService baoHongService;


    @Override
    public ApiResult authorization(Map<String, String> authMap) {
        try {
            Map<String, Object> authObjMap = authMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            ThirdWarehouseContext.setAuthMap(authObjMap);

            BaoHongResponse<List<SmRow>> shippingMethodList = baoHongService.getShippingMethodList();
            if ("0".equals(shippingMethodList.getAsk())) {
                return failure("授权失败:" + shippingMethodList.getMessage());
            } else {
                return success("授权成功");
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.BAO_HONG;
    }

    @Override
    protected ApiResult<List<TransferLogisticsChannelEntity>> getShippingMethodList() {
        return null;
    }

    @Override
    protected ApiResult<String> createOrder(TransferLogisticsCreateOrderReq createOrderReq) {
        return null;
    }

    @Override
    protected ApiResult<TransferLogisticsOrderDTO> getOrderByCode(String orderCode) {
        return null;
    }

    @Override
    protected ApiResult<List<TransferLogisticsProductDTO>> getAllProductInfo() {
        return null;
    }

    @Override
    protected ApiResult<String> createInbound(TransferLogisticsCreateInboundReq createInboundReq) {
        return null;
    }

    @Override
    protected ApiResult<String> printLabel(String orderCode) {
        return null;
    }
}
