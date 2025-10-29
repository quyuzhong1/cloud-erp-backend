package com.erp.server.scm.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.AssetPurchaseOrderDTO;
import com.erp.server.scm.service.AssetPurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 资产采购订单 Feign Controller
 *
 * @author wuht
 * @since 2025-01-20
 */
@RestController
@RequestMapping("/feign/assetPurchaseOrder")
public class AssetPurchaseOrderFeignController extends BaseController {

    @Autowired
    private AssetPurchaseOrderService assetPurchaseOrderService;

    /**
     * 查询资产采购订单明细（用于资产验收单添加明细）
     *
     * @param assetPurchaseOrderId 资产采购订单ID
     * @return 明细列表
     */
    @PostMapping("/queryDetailsForAccept")
    public ApiResult<List<AssetPurchaseOrderDTO.DetailForAcceptDTO>> queryDetailsForAccept(@RequestBody String assetPurchaseOrderId) {
        return success(assetPurchaseOrderService.queryDetailsForAccept(assetPurchaseOrderId));
    }

    /**
     * 查询资产采购订单下拉选择列表
     *
     * @param paramDTO 查询参数
     * @return 下拉选择列表
     */
    @PostMapping("/selectList")
    public ApiResult<List<AssetPurchaseOrderDTO.SelectDTO>> selectList(@RequestBody AssetPurchaseOrderDTO.SelectParamDTO paramDTO) {
        return success(assetPurchaseOrderService.selectList(paramDTO));
    }

    /**
     * 根据订单编号查询资产采购订单（用于导入）
     * 查询未删除且审核通过的订单及其明细
     *
     * @param code 订单编号
     * @return 资产采购订单信息（包含明细）
     */
    @PostMapping("/getByCode")
    public ApiResult<AssetPurchaseOrderDTO.DetailWithSkuDTO> getByCode(@RequestBody String code) {
        return success(assetPurchaseOrderService.getByCode(code));
    }
}

