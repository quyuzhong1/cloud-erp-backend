package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.ThirdWarehouseService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lrp
 */
@RestController
@RequestMapping("feign/thirdWarehouse")
public class ThirdWarehouseFeignController extends BaseController {

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;
    @Resource
    private OverseasProviderService overseasProviderService;
    @PostMapping("/createOutboundOrder")
    public ApiResult<String> createOutboundOrder(@RequestBody ThirdWarehouseCreateOutboundReq createOutboundReq) {
        try {
            ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(createOutboundReq.getThirdWarehouseProvideCode());
            return service.createOutboundBill(createOutboundReq, createOutboundReq.getAuthId());
        } catch (ServiceException serviceException) {
            return failure(serviceException.getMsg());
        }
    }


    @PostMapping("/cancelOutboundOrder")
    public ApiResult<String> cancelOutboundOrder(@RequestBody ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        try {
            ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(cancelOutboundReq.getThirdWarehouseProvideCode());
            return service.cancelOutboundBill(cancelOutboundReq, cancelOutboundReq.getAuthId());
        } catch (ServiceException serviceException) {
            return failure(serviceException.getMsg());
        }
    }

    @PostMapping("/queryOutboundOrder")
    public ApiResult<String> queryOutboundOrder(@RequestBody ThirdWarehouseQueryOutboundReq queryOutboundReq) {
        try {
            ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(queryOutboundReq.getThirdWarehouseProvideCode());
            return service.queryOutboundBill(queryOutboundReq, queryOutboundReq.getAuthId());
        } catch (ServiceException serviceException) {
            return failure(serviceException.getMsg());
        }
    }

    /**
     * 运费试算
     * @param params
     * @return
     */
    @PostMapping("/getCalculateFeeBatch")
    public List<ShippingCalculationDTO.ListDTO> getCalculateFeeBatch(@RequestBody ShippingCalculationDTO.PagingParamDTO params) {
        return overseasProviderService.getCalculateFeeBatch(params);
    }

    /**
     * 上传附件
     * @param uploadFileReq
     * @return
     */
    @PostMapping("/uploadFile")
    public ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(@RequestBody @Validated ThirdWarehouseUploadFileReq uploadFileReq) {
        try {
            ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(uploadFileReq.getThirdWarehouseProvideCode());
            return service.uploadFile(uploadFileReq, uploadFileReq.getAuthId());
        } catch (ServiceException serviceException) {
            return failure(serviceException.getMsg());
        }
    }
    /**
     * 上传面单
     * @param uploadOrderLabelReq
     * @return
     */
    @PostMapping("/uploadOrderLabel")
    public ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(@RequestBody ThirdWarehouseUploadOrderLabelReq uploadOrderLabelReq) {
        try {
            ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(uploadOrderLabelReq.getThirdWarehouseProvideCode());
            return service.uploadOrderLabel(uploadOrderLabelReq, uploadOrderLabelReq.getAuthId());
        } catch (ServiceException serviceException) {
            return failure(serviceException.getMsg());
        }
    }

    /**
     * 上传面单
     * @return
     */
    @PostMapping("/uploadHandoverFile")
    public ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(@RequestBody ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        try {
            ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(uploadHandoverFileReq.getThirdWarehouseProvideCode());
            return service.uploadHandoverFile(uploadHandoverFileReq, uploadHandoverFileReq.getAuthId());
        } catch (ServiceException serviceException) {
            return failure(serviceException.getMsg());
        }
    }
}
