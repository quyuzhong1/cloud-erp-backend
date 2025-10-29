package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.AssetPurchaseOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * 资产采购订单 Feign 客户端
 *
 * @author wuht
 * @since 2025-01-20
 */
@FeignClient(name = "erp-scm",  configuration = FeignErrorDecoder.class)
public interface AssetPurchaseOrderFeign {

    /**
     * 查询资产采购订单明细（用于资产验收单添加明细）
     *
     * @param assetPurchaseOrderId 资产采购订单ID
     * @return 资产采购订单明细列表
     */
    @PostMapping("/feign/assetPurchaseOrder/queryDetailsForAccept")
    ApiResult<List<AssetPurchaseOrderDTO.DetailForAcceptDTO>> queryDetailsForAccept(@RequestBody String assetPurchaseOrderId);

    /**
     * 查询资产采购订单下拉选择列表
     *
     * @param paramDTO 查询参数
     * @return 下拉选择列表
     */
    @PostMapping("/feign/assetPurchaseOrder/selectList")
    ApiResult<List<AssetPurchaseOrderDTO.SelectDTO>> selectList(@RequestBody AssetPurchaseOrderDTO.SelectParamDTO paramDTO);

    /**
     * 根据订单编号查询资产采购订单（用于导入）
     *
     * @param code 订单编号
     * @return 资产采购订单信息（包含明细）
     */
    @PostMapping("/feign/assetPurchaseOrder/getByCode")
    ApiResult<AssetPurchaseOrderDTO.DetailWithSkuDTO> getByCode(@RequestBody String code);
}
