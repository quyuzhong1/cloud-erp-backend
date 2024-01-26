package com.erp.server.tms.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateInboundReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsOrderDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsProductDTO;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.server.tms.service.TransferLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public abstract class AbstractTransferLogisticsHandler extends BaseController implements TransferLogisticsService {

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return null;
    }

    @Override
    public Boolean authorize(Map<String, String> authConfig, String authId) {
        return null;
    }

    @Override
    public ApiResult<List<TransferLogisticsChannelEntity>> getShippingMethodList(String authId) {
        return null;
    }

    @Override
    public ApiResult<String> createOrder(TransferLogisticsCreateOrderReq createOrderReq, String authId) {
        return handleAndRemoveContext(() -> createOrder(createOrderReq), authId, SourceTypeEnum.THIRD_WAREHOUSE_CREATE_INBOUND_BILL,createOrderReq.getReferenceNo());
    }

    @Override
    public ApiResult<TransferLogisticsOrderDTO> getOrderByCode(String orderCode, String authId) {
        return handleAndRemoveContext(() -> getOrderByCode(orderCode), authId, SourceTypeEnum.THIRD_WAREHOUSE_CREATE_INBOUND_BILL,orderCode);
    }

    @Override
    public ApiResult<List<TransferLogisticsProductDTO>> getAllProductInfo(String authId) {
        return null;
    }

    @Override
    public ApiResult<String> createInbound(TransferLogisticsCreateInboundReq createInboundReq, String authId) {
        return null;
    }

    @Override
    public ApiResult<String> printLabel(String orderCode, String authId) {
        return null;
    }

    protected abstract ApiResult<List<TransferLogisticsChannelEntity>> getShippingMethodList();

    protected abstract ApiResult<String> createOrder(TransferLogisticsCreateOrderReq createOrderReq);

    protected abstract ApiResult<TransferLogisticsOrderDTO> getOrderByCode(String orderCode);

    protected abstract ApiResult<List<TransferLogisticsProductDTO>> getAllProductInfo();

    protected abstract ApiResult<String> createInbound(TransferLogisticsCreateInboundReq createInboundReq);

    protected abstract ApiResult<String> printLabel(String orderCode);

    private <T> ApiResult<T> handleAndRemoveContext(Handler<T> handler, String authId,SourceTypeEnum businessType,String erpBusinessCode) {
        try {
            //设置授权信息
//            handleAuthInfo(authId);
            //执行逻辑
            ApiResult<T> result = handler.handle();
            ThirdWarehouseContext.setMsg(result.getMsg());
            //记录日志
//            pushOperateLog(businessType,result.getCode(),erpBusinessCode);
            return result;
        } catch (Exception e){
            log.error(ApiError.THIRD_WAREHOUSE_INTERFACE_EXCEPTION.msg,e);
            ThirdWarehouseContext.setMsg(ExceptionUtil.stacktraceToString(e,2000));
//            pushOperateLog(businessType,2000,erpBusinessCode);
            return ApiResult.error(ApiError.THIRD_WAREHOUSE_INTERFACE_EXCEPTION.code,e.getMessage());
        } finally {
            // remove thread-local
            ThirdWarehouseContext.remove();
        }
    }

    @FunctionalInterface
    private interface Handler<T> {
        ApiResult<T> handle();
    }
}
