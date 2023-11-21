package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.dto.response.GoodCangWarehouseResp;
import com.sdk.wms.goodcang.service.GoodCangService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
public class GoodCangHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private GoodCangService goodCangService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_GOOD_CANG;
    }

    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        return null;
    }

    @Override
    public ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq) {
        return null;
    }

    @Override
    public ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        return null;
    }

    @Override
    public ApiResult<String> cancelOutboundBill(ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        return null;
    }


    @Override
    protected Boolean hasWarehouse() {
        GoodCangResponse<List<GoodCangWarehouseResp>> response = goodCangService.getWarehouse();
        if(!isSuccess(response.getAsk())){
            throw new ServiceException("授权失败,"+response.getMessage());
        }
        return isSuccess(response.getAsk());
    }

    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
