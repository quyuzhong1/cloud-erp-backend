package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.wms.dto.third.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author lrp
 */
@FeignClient(name = "erp-wms",contextId = "thirdWarehouse",configuration = {FeignErrorDecoder.class})
public interface ThirdWarehouseFeign {

    /**
     * 创建海外仓出库单
     * @return 出库单号
     */
    @PostMapping("feign/thirdWarehouse/createOutboundOrder")
    ApiResult<String> createOutboundOrder(@RequestBody ThirdWarehouseCreateOutboundReq createOutboundReq);

    /**
     * 取消海外仓出库单
     *  拦截中，拦截成功，拦截失败都是返回success data是 {@link com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum} 的code
     *  如果其他情况返回failure,data 是失败message
     */
    @PostMapping("feign/thirdWarehouse/cancelOutboundOrder")
    ApiResult<String> cancelOutboundOrder(@RequestBody ThirdWarehouseCancelOutboundReq cancelOutboundReq);

    /**
     * 运费试算
     * @param params
     * @return
     */
    @PostMapping("feign/thirdWarehouse/getCalculateFeeBatch")
    List<ShippingCalculationDTO.ListDTO> getCalculateFeeBatch(@RequestBody ShippingCalculationDTO.PagingParamDTO params);

    /**
     * 上传附件
     * @param uploadFileReq
     * @return
     */
    @PostMapping("feign/thirdWarehouse/uploadFile")
    ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(@RequestBody @Validated ThirdWarehouseUploadFileReq uploadFileReq);

    /**
     * 上传面单
     * @param uploadOrderLabelReq
     * @return
     */
    @PostMapping("feign/thirdWarehouse/uploadOrderLabel")
    ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(@RequestBody ThirdWarehouseUploadOrderLabelReq uploadOrderLabelReq);

    /**
     * 上传交接文件
     * @return
     */
    @PostMapping("feign/thirdWarehouse/uploadHandoverFile")
    ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(@RequestBody ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) ;
}
