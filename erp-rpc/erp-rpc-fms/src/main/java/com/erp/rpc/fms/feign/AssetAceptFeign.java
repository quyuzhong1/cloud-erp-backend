package com.erp.rpc.fms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.fms.dto.AssetAcceptDTO;
import com.erp.model.plm.dto.AssetPurchaseOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author: wtr
 * @Date: 2025/10/23 10:30
 * @Param:
 * @Return:
 * @Description:
 **/

@FeignClient(name = "erp-fms",contextId = "assetAceptFeign",configuration = {FeignErrorDecoder.class})
public interface AssetAceptFeign {


    @PostMapping("feign/assetAcept/getAcceptQty")
    Integer getAcceptQtyByDetailId(@RequestBody String detailId);

    @PostMapping("feign/assetAcept/getAssetAccept")
    ApiResult<List<AssetAcceptDTO.AssetPurchaseOrderRefListDTO>> getAcceptByDetailId(@RequestBody String detailId);

    @PostMapping("feign/assetAcept/generateAssetAccept")
    Boolean generateAssetAccept(@RequestBody List<AssetPurchaseOrderDTO.GenerateAssetAcceptDTO> dtoList);

    @PostMapping("feign/assetAcept/getAcceptableQty")
    Map<String, BigDecimal> getAcceptableQtyByDetailId(@RequestBody List<String> detailIdList);
}
