package com.erp.server.fms.controller.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.fms.dto.AssetAcceptDTO;
import com.erp.model.scm.dto.AssetPurchaseOrderDTO;
import com.erp.server.fms.service.AssetAcceptDetailService;
import com.erp.server.fms.service.AssetAcceptService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author: wtr
 * @Date: 2025/10/23 10:31
 * @Param:
 * @Return:
 * @Description:
 **/
@RestController
@RequestMapping("feign/assetAccept")
public class AssetAceptFeignController {

    @Resource
    private AssetAcceptDetailService assetAcceptDetailService;

    @Resource
    private AssetAcceptService assetAcceptService;

    /**
     * 资产采购单明细id
     * @param
     * @return
     */
    @PostMapping("/getAcceptQty")
    public Integer getAcceptQtyByDetailId(@RequestBody String detailId) {
        return assetAcceptDetailService.getAcceptQtyByDetailId(detailId);
    }

    @PostMapping("/getAcceptByDetailId")
    public ApiResult<List<AssetAcceptDTO.AssetPurchaseOrderRefListDTO>> getAcceptByDetailId(@RequestBody String detailId) {
        return assetAcceptService.getAcceptByDetailId(detailId);
    }

    /**
     * 下推资产验收单
     * @param dtoList
     * @return
     */
    @PostMapping("/generateAssetAccept")
    public Boolean generateAssetAccept(@RequestBody List<AssetPurchaseOrderDTO.GenerateAssetAcceptDTO> dtoList){
        return assetAcceptService.generateAssetAccept(dtoList);
    }

    /**
     * 获取可验收数量
     * @param
     * @return
     */
    @PostMapping("/getAcceptableQtyByDetailId")
    public Map<String, BigDecimal> getAcceptableQtyByDetailId(@RequestBody List<String> detailIdList){
        return assetAcceptDetailService.getAcceptableQtyByDetailId(detailIdList);
    }

    @PostMapping("/getAcceptByPurchaseOrderId")
    public ApiResult<List<AssetAcceptDTO.AssetPurchaseOrderRefListDTO>> getAcceptByPurchaseOrderId(@RequestBody String id){
        return ApiResult.success(assetAcceptService.getAcceptByPurchaseOrderId(id));
    }
}
