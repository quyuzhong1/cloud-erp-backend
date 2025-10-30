package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.core.controller.vo.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * OMS 服务 Feign 接口
 * @author wuhaotian
 * @since 2025-09-24
 */
@FeignClient(name = "erp-oms", contextId = "omsFeign", configuration = {FeignErrorDecoder.class})
public interface OmsFeign {

    /**
     * 启用客户列表
     */
    @PostMapping("/feign/customer/listEnable")
    ApiResult<List<BaseDropDownDTO.CommonDTO>> customerListEnable(@RequestBody PermissionsDTO dto);
}
