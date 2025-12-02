package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.CfgQueryConditionDTO;
import com.erp.server.sys.service.CfgQueryConditionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 查询条件配置 Feign 控制器
 * @author jack
 * @since 2025-01-18
 */
@RestController
@RequestMapping("/feign/cfgQueryCondition")
public class CfgQueryConditionFeignController extends BaseController {

    @Resource
    private CfgQueryConditionService cfgQueryConditionService;

    /**
     * 获取查询条件配置
     * @param code 页面code
     * @return 查询条件配置列表
     */
    @GetMapping("/getQueryCondition")
    public ApiResult<List<CfgQueryConditionDTO.ViewDTO>> getQueryCondition(@RequestParam("code") String code) {
        return success(cfgQueryConditionService.getQueryCondition(code));
    }
}
