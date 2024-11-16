package com.erp.server.oms.controller.pda;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.server.oms.service.SoReturnService;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

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
     * 根据条件查询销售退货单
     * @Author Luo_WG
     * @Date 2023/8/18 9:44
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.oms.dto.SoReturnDTO.PdaSoReturn>>
     **/
    @PostMapping(value = "/pdaList")
    public ApiResult<List<SoReturnDTO.PdaSoReturn>> pdaList(@RequestBody SoReturnDTO.PdaSoReturnParam dto) {
        List<SoReturnDTO.PdaSoReturn> entityList = soReturnService.pdaList(dto);
        return success(entityList);
    }

    /**
     * 查询退货单详情
     * @Author Luo_WG
     * @Date 2023/8/11 14:39
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.scm.dto.PurchaseOrderDTO.ViewDTO>
     **/
    @GetMapping("/view")
    public ApiResult<SoReturnDTO.View> view(@Param("id") String id) {
        SoReturnDTO.View dto = soReturnService.pdaView(id);
        return success(dto);
    }
}
