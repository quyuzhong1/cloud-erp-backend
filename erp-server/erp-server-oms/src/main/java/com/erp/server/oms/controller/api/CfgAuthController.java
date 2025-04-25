package com.erp.server.oms.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.CfgAuthDTO;
import com.erp.server.oms.service.CfgAuthCountryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 授权国家配置
 *
 * @author Jim
 * @since 2025-04-21
 */
@Slf4j
@RestController
@LogSystemModule("授权配置")
@RequestMapping("/cfgAuth")
public class CfgAuthController extends BaseController {

    @Resource
    private CfgAuthCountryService cfgAuthCountryService;

    /**
    * 授权区域/国家配置
    * @author Jim
    * @date:  2025-04-21
    * @param dto CfgAuthDTO.ListDTO
    * @return ApiResult<CfgAuthDTO.ViewDTO>
    */
    @PostMapping("/list")
    @LogAction(value = LogActionEnum.INSERT, desc = "授权区域/国家配置")
    public ApiResult<List<CfgAuthDTO.ViewDTO>> regionList(@RequestBody @Validated CfgAuthDTO.ListDTO dto) {
        return success(cfgAuthCountryService.listByDictPlatform(dto.getDictPlatform()));
    }




}
