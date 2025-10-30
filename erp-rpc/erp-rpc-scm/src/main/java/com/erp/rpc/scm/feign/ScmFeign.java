package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.vo.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * SCM 服务 Feign 接口
 * @author wuhaotian
 * @since 2025-09-24
 */
@FeignClient(name = "erp-scm", contextId = "scmFeign", configuration = {FeignErrorDecoder.class})
public interface ScmFeign {

    /**
     * 审批状态下拉列表
     */
    @GetMapping("/feign/dropDown/approveStatus/list")
    ApiResult<List<BaseDropDownDTO.CommonDTO>> approveStatusList();
}
