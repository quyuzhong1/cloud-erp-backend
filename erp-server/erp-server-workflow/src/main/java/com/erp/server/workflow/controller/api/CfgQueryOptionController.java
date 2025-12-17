package com.erp.server.workflow.controller.api;

import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.server.workflow.service.CfgQueryOptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 查询option配置表(数大臣单据字段)
 *
 * @author hcg
 * @since 2025-05-15
 */
@Slf4j
@RestController
@LogSystemModule("查询option配置表(数大臣单据字段)")
@RequestMapping("/cfgQueryOption")
public class CfgQueryOptionController extends BaseController {

    @Resource
    private CfgQueryOptionService cfgQueryOptionService;

    /**
     * 查询option配置表下拉,useType,cfgProcess流程配置,allData全量数据
     * @return
     */
    @GetMapping("/drop/down")
    public ApiResult<List<CfgQueryOptionDTO.ListDTO>> proDropDown(@RequestParam(value = "bussinessKey") String bussinessKey,@RequestParam(value = "useType") String useType) {
        return success(cfgQueryOptionService.proDropDown(bussinessKey,useType));
    }

    /**
     * 查询option配置表下拉（值包含主表和拓展字段）
     * @return
     */
    @GetMapping("/drop/downByMain")
    public ApiResult<List<CfgQueryOptionDTO.ListDTO>> proDropDownByMain(@RequestParam(value = "bussinessKey") String bussinessKey,@RequestParam(value = "useType") String useType) {
        return success(cfgQueryOptionService.proDropDownByMain(bussinessKey,useType));
    }

    /**
     * 字段配置系统字段下拉
     * @return
     */
    @GetMapping("/drop/down/sysField")
    public ApiResult<List<CfgQueryOptionDTO.ViewDTO>> getSystenfield(@RequestParam(value = "bussinessKey") String bussinessKey,@RequestParam(value = "useType") String useType) {
        return success(cfgQueryOptionService.getSystemfield(bussinessKey,useType));
    }

    /**
     *
     * @return
     */
    @GetMapping("/cfgApproveSync/drop/cfgApproveSyncDropDown")
    public ApiResult<List<CfgQueryOptionDTO.cfgApproveSyncDropDownDTO>> cfgApproveSyncDropDown(@RequestParam(value = "bussinessKey") String bussinessKey,@RequestParam(value = "useType") String useType,@RequestParam(value = "fieldBelongsType") String fieldBelongsType) {
        return success(cfgQueryOptionService.cfgApproveSyncDropDown(bussinessKey,useType,fieldBelongsType));
    }

    /**
     * 条件 树结构
     * @author yl
     * @date 2023-10-08 15:08
     * @param
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.oms.dto.CfConditionDTO.TreeDTO>>
     */
    @GetMapping("/tree")
    public ApiResult<List<CfgQueryOptionDTO.TreeDTO>> tree(@RequestParam(value = "bussinessKey") String bussinessKey,@RequestParam(value = "useType") String useType) {
        List<CfgQueryOptionDTO.TreeDTO> result = cfgQueryOptionService.tree(bussinessKey,useType);
        return success(result);
    }
}
