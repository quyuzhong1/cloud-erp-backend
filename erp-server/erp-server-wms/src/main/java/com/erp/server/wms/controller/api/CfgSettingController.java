package com.erp.server.wms.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.CfgSettingDTO;
import com.erp.model.wms.dto.CfgSettingValueDTO;
import com.erp.server.wms.service.CfgSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 系统配置管理
 *
 * @author will
 * @since 2024-01-08
 */
@Slf4j
@RestController
@LogSystemModule("系统配置管理")
@RequestMapping("/cfgSetting")
public class CfgSettingController extends BaseController {

    @Resource
    private CfgSettingService cfgSettingService;

    /**
    * 新增
    * @author will
    * @date:  2024-01-08
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "系统配置管理新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgSettingDTO.AddDTO dto) {
        return success(cfgSettingService.add(dto));
    }

    /**
     * 查询配置
     * @author will
     * @date:  2024-01-08
     * @return ApiResult
     */
    @GetMapping("/view")
    @LogAction(value = LogActionEnum.UPDATE, desc = "系统配置管理修改")
    public ApiResult<CfgSettingDTO.ViewDTO> view() {
        CfgSettingDTO.ViewDTO view = cfgSettingService.view();
        return success(view);
    }

    /**
     * 查询采购退货配置
     * @author Will
     * @date: 2024/2/2 15:56
     * @return ApiResult<PoReturnSettingDTO>
     */
    @GetMapping("/getPoReturnSetting")
    public ApiResult<CfgSettingValueDTO.PoReturnSettingDTO> getPoReturnSetting() {
        return success(cfgSettingService.getPoReturnSetting());
    }

    /**
     * 获取委外入库自动入库配置
     * @return
     */
    @GetMapping("/getSubcontractInStockSetting")
    public ApiResult<String> getSubcontractInStockSetting() {
        return success(cfgSettingService.getSubcontractInStockSetting());
    }
}
