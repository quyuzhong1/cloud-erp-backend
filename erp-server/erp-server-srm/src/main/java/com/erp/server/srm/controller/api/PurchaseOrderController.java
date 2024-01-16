package com.erp.server.srm.controller.api;

import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.PurchaseOrderSrmDTO;
import com.erp.rpc.wms.feign.PurchaseOrderFeign;
import com.erp.server.srm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购订单管理
 *
 * @author zdy
 * @ClassName PurchaseOrderController
 * @description: 采购订单管理
 * @date 2024年01月15日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("采购订单管理")
@RequestMapping("/purchaseOrder")
public class PurchaseOrderController extends BaseController {

    @Resource
    private UserService userService;
    @Resource
    private PurchaseOrderFeign purchaseOrderFeign;
    /**
     * srm订单确认列表统计
     * @author zdy
     * @date: 2024/1/16 17:34
     * @return ApiResult
     */
    @PostMapping("/srmOrderConfirmCount")
    public List<ListStatusCountDTO.PurchaseOrderConfirmCountDTO> srmOrderConfirmCount(@RequestBody PurchaseOrderSrmDTO.SearchParamDTO dto) {
        dto.setSupplierId(userService.getSupplierId());
        return purchaseOrderFeign.srmOrderConfirmCount(dto);
    }
}
