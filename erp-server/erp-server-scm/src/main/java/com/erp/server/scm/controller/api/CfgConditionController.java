package com.erp.server.scm.controller.api;


import com.erp.model.scm.dto.CfgConditionDTO;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.scm.service.CfgConditionService;
import com.common.core.controller.vo.ApiResult;

import java.util.List;

/**
 * 
 *
 * @author jack
 * @since 2025-06-16
 */
@Slf4j
@RestController
@RequestMapping("/cfgCondition")
public class CfgConditionController extends BaseController {


    @Resource
    private CfgConditionService cfConditionService;

    /**
     * 根据类型获取所有条件
     * @param type 类型
     */
    @GetMapping("/listByType")
    public ApiResult<List<CfgConditionDTO.CommonDTO>> listByType(@RequestParam String type) {
        List<CfgConditionDTO.CommonDTO> resultList = cfConditionService.listByType(type);
        return success(resultList);
    }

    /**
     * 根据类型获取树结构
     * @param type 类型
     */
    @GetMapping("/tree")
    public ApiResult<List<CfgConditionDTO.TreeDTO>> tree(@RequestParam String type) {
        List<CfgConditionDTO.TreeDTO> result = cfConditionService.tree(type);
        return success(result);
    }

}
