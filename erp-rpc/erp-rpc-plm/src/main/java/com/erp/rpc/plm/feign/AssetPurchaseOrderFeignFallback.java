package com.erp.rpc.plm.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.AssetPurchaseOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 资产采购订单 Feign 客户端 Fallback
 *
 * @author wuht
 * @since 2025-01-20
 */
@Slf4j
@Component
public class AssetPurchaseOrderFeignFallback implements AssetPurchaseOrderFeign {

    @Override
    public ApiResult<List<AssetPurchaseOrderDTO.DetailForAcceptDTO>> queryDetailsForAccept(String assetPurchaseOrderId) {
        log.error("AssetPurchaseOrderFeign.queryDetailsForAccept fallback, assetPurchaseOrderId: {}", assetPurchaseOrderId);
        return ApiResult.success(new ArrayList<>());
    }

    @Override
    public ApiResult<List<AssetPurchaseOrderDTO.SelectDTO>> selectList(AssetPurchaseOrderDTO.SelectParamDTO paramDTO) {
        log.error("AssetPurchaseOrderFeign.selectList fallback, paramDTO: {}", paramDTO);
        return ApiResult.success(new ArrayList<>());
    }
}
