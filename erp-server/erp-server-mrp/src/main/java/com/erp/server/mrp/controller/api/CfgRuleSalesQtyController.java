package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.server.mrp.service.CfgRuleSalesQtyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 销量（规则设置）
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@RestController
@LogSystemModule("销量（规则设置）")
@RequestMapping("/cfgRuleSalesQty")
public class CfgRuleSalesQtyController extends BaseController {

    @Resource
    private CfgRuleSalesQtyService cfgRuleSalesQtyService;

    /**
    * 修改
    * @author will
    * @date:  2024-08-23
    * @param updateDTO
    * @return ApiResult
    */
    @PostMapping("/batchUpdate")
    public ApiResult<?> batchUpdate(@RequestBody @Validated CfgRuleSalesQtyDTO.UpdateDTO updateDTO) {
        cfgRuleSalesQtyService.batchUpdate(updateDTO);
        return success();
    }

    /**
     * 查看详情
     * @author will
     * @date 2024/8/24 9:19
     * @param platformType
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<CfgRuleSalesQtyDTO.ViewDTO> view(@RequestParam("platformType") String platformType) {
        return success(cfgRuleSalesQtyService.view(platformType));
    }

    /**
     * 补货建议查询销量详情
     * @author will
     * @date 2024/9/6 12:21
     * @param platformType
     * @param refId
     * @return ApiResult<ViewDetailDTO>
     */
    @GetMapping("/viewDetail")
    @LogViewService
    public ApiResult<CfgRuleSalesQtyDTO.ViewDetailDTO> view(@RequestParam("platformType") String platformType,@RequestParam("refId") String refId) {
        return success(cfgRuleSalesQtyService.viewDetail(platformType,refId));
    }
}
