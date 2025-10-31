package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.CfgQueryConditionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 查询条件配置 Feign 接口
 * @author jack
 * @since 2025-01-18
 */
@FeignClient(name = "erp-sys", contextId = "cfgQueryConditionFeign", configuration = {FeignErrorDecoder.class})
public interface CfgQueryConditionFeign {

    /**
     * 获取查询条件配置
     * @param code 页面code
     * @return 查询条件配置列表
     */
    @GetMapping("/feign/cfgQueryCondition/getQueryCondition")
    ApiResult<List<CfgQueryConditionDTO.ViewDTO>> getQueryCondition(@RequestParam("code") String code);
}
