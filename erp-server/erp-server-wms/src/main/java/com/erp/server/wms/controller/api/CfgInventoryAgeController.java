package com.erp.server.wms.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.CfgInventoryAgeDTO;
import com.erp.server.wms.service.CfgInventoryAgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 库龄配置表
 *
 * @author will
 * @since 2025-08-20
 */
@Slf4j
@RestController
@LogSystemModule("库龄配置表")
@RequestMapping("/cfgInventoryAge")
public class CfgInventoryAgeController extends BaseController {

    @Resource
    private CfgInventoryAgeService cfgInventoryAgeService;

    /**
    * 新增
    * @author will
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "库龄配置表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgInventoryAgeDTO.AddDTO dto) {
        return success(cfgInventoryAgeService.add(dto));
    }

    /**
     * 虚拟仓设置查看
     * @author will
     * @date 2024/9/23 15:59
     * @return ApiResult<AddDTO>
     */
    @GetMapping("/view")
    public ApiResult<CfgInventoryAgeDTO.ViewDTO> view() {
        CfgInventoryAgeDTO.ViewDTO view = cfgInventoryAgeService.viewVirtual();
        return success(view);
    }
}
