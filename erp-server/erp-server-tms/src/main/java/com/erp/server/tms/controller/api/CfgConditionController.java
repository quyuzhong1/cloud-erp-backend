package com.erp.server.tms.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.CfgConditionDTO;
import com.erp.server.tms.service.CfgConditionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 条件配置表 前端控制器
 * </p>
 *
 * @author jack
 * @since 2026-04-22
 */
@RestController
@LogSystemModule("规则条件配置")
@RequestMapping("/cfg-condition")
public class CfgConditionController extends BaseController {

    @Resource
    private CfgConditionService cfConditionService;

    /**
     * 新增规则条件
     * @param dto 规则条件
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "规则条件配置新增")
    public ApiResult<String> add(@RequestBody @Validated CfgConditionDTO.AddDTO dto) {
        cfConditionService.add(dto);
        return success();
    }
    /**
     * 编辑规则条件
     * @param dto 规则条件
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "规则条件配置编辑")
    public ApiResult<String> update(@RequestBody @Validated CfgConditionDTO.UpdateDTO dto) {
        cfConditionService.update(dto);
        return success();
    }

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
