package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.CfgRuleWarehouseDTO;
import com.erp.model.mrp.dto.CfgRuleWarehouseDetailDTO;
import com.erp.server.mrp.service.CfgRuleWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
     * 校验店铺
     * @author will
     * @date 2024/9/13 15:44
     * @param dto
     * @return ApiResult<WarehouseShopDTO>
     */
    @PostMapping("/checkShop")
    public ApiResult<CfgRuleWarehouseDTO.WarehouseShopDTO> checkShop(@RequestBody @Validated CfgRuleWarehouseDTO.UpdateDTO dto) {
        CfgRuleWarehouseDTO.WarehouseShopDTO shopDTO = cfgRuleWarehouseService.checkShop(dto);
        return success(shopDTO);
    }
    /**
    * 修改
    * @author will
    * @date:  2024-08-24
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "仓库规则修改")
    public ApiResult<String> update(@RequestBody @Validated CfgRuleWarehouseDTO.UpdateDTO dto) {
        cfgRuleWarehouseService.update(dto);
        return success();
    }

    /**
     * 查看详情
     * @author will
     * @date 2024/8/24 15:47
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<CfgRuleWarehouseDTO.ViewDTO> view() {
        return success(cfgRuleWarehouseService.view());
    }

    /**
     * 更新虚拟仓数据
     * @author will
     * @date 2024/9/3 17:26
     * @return ApiResult<ViewDTO>
     */
    @PostMapping("/refreshVirtual")
    @LogAction(value = LogActionEnum.UPDATE_WITHOUT_PARAMS, desc = "更新虚拟仓数据")
    public ApiResult<String> refreshVirtual() {
        cfgRuleWarehouseService.refreshVirtual();
        return success();
    }

    /**
     * 查询是否启用海外仓
     * @author will
     * @date 2024/9/4 11:11
     * @param platformType
     * @return ApiResult<?>
     */
    @GetMapping("/getIsEnableOverseas")
    public ApiResult<Boolean> getIsEnableOverseas(@RequestParam("platformType") String platformType) {
        Boolean isEnableOverseas = cfgRuleWarehouseService.getIsEnableOverseas(platformType);
        return success(isEnableOverseas);
    }

    /**
     * 查询是否启用虚拟仓
     */
    @GetMapping("/getIsEnableVirtual")
    public ApiResult<Boolean> getIsEnableVirtual() {
        Boolean isEnableOverseas = cfgRuleWarehouseService.getIsEnableVirtual();
        return success(isEnableOverseas);
    }

    /**
     * 查询海外仓配置
     * @author will
     * @date 2024/10/29 11:26
     * @return ApiResult<OverseasWarehouseDTO>
     */
    @GetMapping("/listOverseasWarehouse")
    public ApiResult<List<CfgRuleWarehouseDetailDTO.OverseasWarehouseDTO>> listOverseasWarehouse() {
        return success(cfgRuleWarehouseService.listOverseasWarehouse());
    }
}
