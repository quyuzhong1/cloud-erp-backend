package com.erp.rpc.fms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.fms.dto.AssetAcceptDTO;
import com.erp.model.scm.dto.AssetPurchaseOrderDTO;
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

    /**
     * 获取验收数量
     * @param detailId
     * @return
     */
    @PostMapping("feign/assetAcept/getAcceptQty")
    Integer getAcceptQtyByDetailId(@RequestBody String detailId);

    /**
     * 通过资产采购单明细获取资产验收单明细
     * @param detailId
     * @return
     */
    @PostMapping("feign/assetAcept/getAssetAccept")
    ApiResult<List<AssetAcceptDTO.AssetPurchaseOrderRefListDTO>> getAcceptByDetailId(@RequestBody String detailId);

    /**
     * 下推资产验收单
     * @param dtoList
     * @return
     */
    @PostMapping("feign/assetAcept/generateAssetAccept")
    Boolean generateAssetAccept(@RequestBody List<AssetPurchaseOrderDTO.GenerateAssetAcceptDTO> dtoList);

    /**
     * 获取可验收数量
     * @param detailIdList
     * @return
     */
    @PostMapping("feign/assetAcept/getAcceptableQty")
    Map<String, BigDecimal> getAcceptableQtyByDetailId(@RequestBody List<String> detailIdList);
}
