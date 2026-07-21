package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.SysApiTokenDTO;
import com.erp.server.sys.service.SysApiTokenService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * API Token 网关校验 Feign 接口
 */
@RestController
@RequestMapping("feign/apiToken")
public class SysApiTokenFeignController extends BaseController {

    @Resource
    private SysApiTokenService sysApiTokenService;

    @PostMapping("/validate")
    public ApiResult<SysApiTokenDTO.ValidateRespDTO> validate(@RequestBody @Validated SysApiTokenDTO.ValidateReqDTO dto) {
        return success(sysApiTokenService.validate(dto));
    }
}
