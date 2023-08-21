package com.erp.server.scm.controller.pda;


import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.*;
import com.erp.server.scm.service.PurchaseOrderService;
import org.apache.ibatis.annotations.Param;
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

    /**
     * 查询采购单详情
     * @Author Luo_WG
     * @Date 2023/8/11 14:39
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.scm.dto.PurchaseOrderDTO.ViewDTO>
     **/
    @GetMapping("/view")
    public ApiResult<PurchaseOrderDTO.PdaViewDTO> view(@Param("id") String id) {
        PurchaseOrderDTO.PdaViewDTO dto = purchaseOrderService.pdaView(id);
        return success(dto);
    }
}
