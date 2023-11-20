package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
public class ImlHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_IML;
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

}
