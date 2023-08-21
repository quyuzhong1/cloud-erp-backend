package com.erp.server.scm.controller.pda;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.*;
import com.erp.server.scm.service.PurchaseOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * PDA:采购订单管理
 * @Author Luo_WG
 * @Date 2023/8/11 14:18
 **/
@RestController
@RequestMapping("/pdaPurchaseOrder")
public class PdaPurchaseOrderController extends BaseController {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    /**
     * 根据sku编号查询采购单信息
     * @Author Luo_WG
     * @Date 2023/8/11 14:18
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.scm.dto.PurchaseOrderDTO.PdaPurchaseOrder>>
     **/
    @PostMapping("/pdaList")
    public ApiResult<List<PurchaseOrderDTO.PdaPurchaseOrder>> pdaList(@RequestBody PurchaseOrderDTO.PdaPurchaseOrderParam dto) {
        List<PurchaseOrderDTO.PdaPurchaseOrder> list = purchaseOrderService.pdaList(dto);
        return success(list);
    }

}
