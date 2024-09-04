package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.CfgRuleWarehouseDTO;
import com.erp.server.mrp.service.CfgRuleWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 仓库（规则设置）
 *
 * @author will
 * @since 2024-08-24
 */
@Slf4j
@RestController
@LogSystemModule("仓库（规则设置）")
@RequestMapping("/cfgRuleWarehouse")
public class CfgRuleWarehouseController extends BaseController {

    @Resource
    private CfgRuleWarehouseService cfgRuleWarehouseService;


    /**
    * 修改
    * @author will
    * @date:  2024-08-24
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleWarehouseDTO.UpdateDTO dto) {
        cfgRuleWarehouseService.update(dto);
        return success();
    }

    /**
     * 查看详情
     * @author will
     * @date 2024/8/24 15:47
     * @param platformType
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<CfgRuleWarehouseDTO.ViewDTO> view(@RequestParam("platformType") String platformType) {
        return success(cfgRuleWarehouseService.view(platformType));
    }

    /**
     * 更新虚拟仓数据
     * @author will
     * @date 2024/9/3 17:26
     * @param dto
     * @return ApiResult<ViewDTO>
     */
    @PostMapping("/refreshVirtual")
    public ApiResult<?> refreshVirtual(@RequestBody @Validated CfgRuleWarehouseDTO.ParamDTO dto) {
        cfgRuleWarehouseService.refreshVirtual(dto.getPlatformType());
        return success();
    }
}
