package com.erp.server.wms.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.wms.service.PoInstockService;
import com.erp.server.wms.service.PurchaseReturnOrderService;
import com.erp.server.wms.service.WarehouseReceiveService;
import com.erp.server.wms.service.WorkOptionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 采购订单feign
 *
 * @Author Luo_WG
 * @Date 2023/4/13 11:11
 **/
@RestController
@RequestMapping("feign/wmsWorkOption")
public class WmsWorkOptionFeignController {
    @Resource
    private WorkOptionService workOptionService;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    @Resource
    private PoInstockService poInstockService;

    @Resource
    private PurchaseReturnOrderService purchaseReturnOrderService;

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("/getTableNum")
    public Integer getTableNum(@RequestBody WorkOptionDTO.TableNumDTO tableNumDTO) {
        return workOptionService.getTableNum(tableNumDTO);
    }

    /**
     * 采购收货审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/warehouseReceiveApprove")
    public Boolean warehouseReceiveApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = warehouseReceiveService.approve(baseApproveParamDTO);
        return flag;
    }

    /**
     * 采购入库审核
     * @author Will
     * @date: 2023/4/11 20:11
     * @param baseApproveParamDTO
     * @return ApiResult
     */
    @PostMapping("/poInstockApprove")
    public void poInstockApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        poInstockService.approve(baseApproveParamDTO);
    }
    /**
     * 采购退货审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/purchaseReturnOrderApprove")
    public Boolean purchaseReturnOrderApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = purchaseReturnOrderService.approve(baseApproveParamDTO);
        return flag;
    }
}
