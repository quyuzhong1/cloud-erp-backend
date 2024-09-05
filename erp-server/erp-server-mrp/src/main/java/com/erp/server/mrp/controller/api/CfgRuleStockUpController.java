package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.server.mrp.service.CfgRuleStockUpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 备货（规则设置）
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@RestController
@LogSystemModule("备货（规则设置）")
@RequestMapping("/cfgRuleStockUp")
public class CfgRuleStockUpController extends BaseController {

    @Resource
    private CfgRuleStockUpService cfgRuleStockUpService;


    /**
    * 修改
    * @author will
    * @date:  2024-08-23
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleStockUpDTO.UpdateDTO dto) {
        cfgRuleStockUpService.update(dto);
        return success();
    }

    /**
     * 查看详情
     * @author will
     * @date 2024/8/23 17:06
     * @param platformType
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<CfgRuleStockUpDTO.ViewDTO> view(@RequestParam("platformType") String platformType) {
        return success(cfgRuleStockUpService.view(platformType,""));
    }

    /**
     * 根据关联id和平台类型查询
     * @author will
     * @date 2024/9/5 17:58
     * @param platformType
     * @param refId
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/viewByRefId")
    @LogViewService
    public ApiResult<CfgRuleStockUpDTO.ViewDTO> view(@RequestParam("platformType") String platformType,@RequestParam("refId") String refId) {
        return success(cfgRuleStockUpService.view(platformType,refId));
    }
}
