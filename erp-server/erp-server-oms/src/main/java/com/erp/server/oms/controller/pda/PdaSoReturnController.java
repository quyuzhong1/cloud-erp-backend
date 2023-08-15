package com.erp.server.oms.controller.pda;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.server.oms.service.SoReturnService;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * PDA:销售退货单
 * @Author Luo_WG
 * @Date 2023/8/15 14:31
 **/
@RestController
@RequestMapping("/pdaSoReturn")
public class PdaSoReturnController extends BaseController {
    @Resource
    private SoReturnService soReturnService;

    /**
     * 根据sku查询销售退货单
     * @Author Luo_WG
     * @Date 2023/8/15 16:48
     * @param skuNo
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.oms.dto.SoReturnDTO.PdaSoReturn>>
     **/
    @GetMapping(value = "/listBySkuNo")
    public ApiResult<List<SoReturnDTO.PdaSoReturn>> listBySkuNo(@RequestParam("skuNo") String skuNo) {
        List<SoReturnDTO.PdaSoReturn> entityList = soReturnService.listBySkuNo(skuNo);
        return success(entityList);
    }

    /**
     * 查询采购单详情
     * @Author Luo_WG
     * @Date 2023/8/11 14:39
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.scm.dto.PurchaseOrderDTO.ViewDTO>
     **/
    @GetMapping("/view")
    public ApiResult<PurchaseOrderDTO.ViewDTO> view(@Param("id") String id) {
       /* PurchaseOrderDTO.ViewDTO dto = purchaseOrderService.pdaView(id);
        return success(dto);*/
        return null;
    }
}
