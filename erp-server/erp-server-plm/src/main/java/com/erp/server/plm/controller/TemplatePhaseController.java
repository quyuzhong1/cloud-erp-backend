package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.BasicTemplateIdDTO;
import com.erp.model.plm.dto.BatchTemplatePhaseDTO;
import com.erp.model.plm.dto.TemplatePhaseDTO;
import com.erp.model.plm.dto.TemplatePhaseDeleteDTO;
import com.erp.server.plm.service.TemplatePhaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板管理
 *
 * @author Will
 * @version 1.0
 * @date 2022/11/17 9:58
 */
@RestController
@RequestMapping("plm/templatePhase")
public class TemplatePhaseController extends BaseController {

    @Autowired
    private TemplatePhaseService templatePhaseService;


    /**
     * 模板详情-模板阶段-下拉框数据
     *
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public ApiResult<List<TemplatePhaseDTO>> list(@RequestBody @Validated BasicTemplateIdDTO dto) {
        List<TemplatePhaseDTO> resultList = templatePhaseService.findList(dto);
        return success(resultList);
    }

    /**
     * 模板详情-模板阶段-新增或修改
     *
     * @author Will
     * @date: 2022/11/17 10:17
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/batchSaveOrUpdate")
    public ApiResult batchSaveOrUpdate(@RequestBody @Validated BatchTemplatePhaseDTO dto) {
        templatePhaseService.batchSaveOrUpdate(dto);
        return success();
    }

    /**
     * 模板详情-模板阶段-删除
     *
     * @author Will
     * @date: 2022/11/17 10:17
     * @param dto
     * @return ApiResult
     */
    @DeleteMapping("/remove")
    public ApiResult remove(@RequestBody @Validated TemplatePhaseDeleteDTO dto) {
        Boolean flag = templatePhaseService.removeTemplatePhase(dto.getId(),dto.getTemplateId());
        return flag == true ? success() : failure();
    }
}
