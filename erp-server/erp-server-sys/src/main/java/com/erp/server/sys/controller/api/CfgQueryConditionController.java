package com.erp.server.sys.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.CfgQueryConditionDTO;
import com.erp.server.sys.service.CfgQueryConditionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 查询条件配置表
 * @author lrp
 * @since 2024-01-03
 */
@Slf4j
@RestController
@LogSystemModule("查询条件配置表")
@RequestMapping("/cfgQueryCondition")
public class CfgQueryConditionController extends BaseController {

    @Resource
    private CfgQueryConditionService cfgQueryConditionService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public ApiResult<Boolean> getQueryCondition(@RequestBody CfgQueryConditionDTO.AddDTO addDTO) {
        return success(cfgQueryConditionService.add(addDTO));
    }

    /**
     * 获取查询条件配置
     */
    @GetMapping("/getQueryCondition")
    public ApiResult<List<CfgQueryConditionDTO.ViewDTO>> getQueryCondition(@RequestParam(name = "code") String code) {
        if(StringUtils.isBlank(code)){
            return failure("页面code不能为空");
        }
        return success(cfgQueryConditionService.getQueryCondition(code));
    }
}
