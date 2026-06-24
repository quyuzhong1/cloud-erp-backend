package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.SysApiTokenDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * API Token 网关校验 Feign 契约
 */
@FeignClient(name = "erp-sys", contextId = "sysApiTokenFeign", configuration = {FeignErrorDecoder.class})
public interface SysApiTokenFeign {

    @PostMapping("/feign/apiToken/validate")
    ApiResult<SysApiTokenDTO.ValidateRespDTO> validate(@RequestBody @Valid SysApiTokenDTO.ValidateReqDTO dto);
}
