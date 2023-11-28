package com.erp.server.wms.handler;

import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.ThirdWarehouseTaskDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.ThirdWarehouseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public abstract class AbstractThirdWarehouseHandler extends BaseController implements ThirdWarehouseService {

    @Resource
    private OverseasProviderService overseasProviderService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    public void handleAuthInfo(String id) {
        OverseasProviderEntity authEntity = getAuthEntity(id);
        ThirdWarehouseContext.setAuthMap(authEntity.getAuthJson());
    }

    private OverseasProviderEntity getAuthEntity(String id) {
        return overseasProviderService.getById(id);
    }

    @Override
    public Boolean authorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        try {
            ThirdWarehouseContext.setAuthMap(dto.getAuthJson());
            boolean result = hasWarehouse();
            if (result) {
                dmpTaskFeign.createThirdWarehouseTask(new ThirdWarehouseTaskDTO.AddDTO(dto.getId(), dto.getAuthJson(), getPlatForm().getCode()));
            }
            return result;
        } finally {
            ThirdWarehouseContext.remove();
        }
    }

    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq, String authId) {
        return handleAndRemoveContext(() -> createInboundBill(createInboundReq), authId);
    }

    @Override
    public ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq, String authId) {
        return handleAndRemoveContext(() -> cancelInboundBill(cancelInboundReq), authId);
    }

    @Override
    public ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq, String authId) {
        return handleAndRemoveContext(() -> createOutboundBill(createOutboundReq), authId);
    }

    @Override
    public ApiResult<String> cancelOutboundBill(ThirdWarehouseCancelOutboundReq cancelOutboundReq, String authId) {
        return handleAndRemoveContext(() -> cancelOutboundBill(cancelOutboundReq), authId);
    }

    protected abstract ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq);

    protected abstract ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq);

    protected abstract ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq);

    protected abstract ApiResult<String> cancelOutboundBill(ThirdWarehouseCancelOutboundReq cancelOutboundReq);

    protected abstract Boolean hasWarehouse();

    private ApiResult<String> handleAndRemoveContext(Handler handler, String authId) {
        try {
            handleAuthInfo(authId);
            return handler.handle();
        } finally {
            ThirdWarehouseContext.remove();
        }
    }

    @FunctionalInterface
    private interface Handler {
        ApiResult<String> handle();
    }
}