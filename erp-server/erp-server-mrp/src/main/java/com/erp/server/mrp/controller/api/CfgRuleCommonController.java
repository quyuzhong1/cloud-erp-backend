package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.server.mrp.service.CfgRuleCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 公共配置（规则设置），库存，建议
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@RestController
@LogSystemModule("公共配置（规则设置）")
@RequestMapping("/cfgRuleCommon")
public class CfgRuleCommonController extends BaseController {

    @Resource
    private CfgRuleCommonService cfgRuleCommonService;


    /**
    * 修改
    * @author will
    * @date:  2024-08-23
    * @param updateList
    * @return ApiResult
    */
    @PostMapping("/update")
    public ApiResult<?> update(@RequestBody @Validated List<CfgRuleCommonDTO.UpdateDTO> updateList) {
        cfgRuleCommonService.update(updateList);
        return success();
    }

    /**
     * 查询详情
     * @author will
     * @date 2024/8/26 9:19
     * @param platformType
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<List<CfgRuleCommonDTO.ViewDTO>> view(@RequestParam("platformType") String platformType,@RequestParam("type") String type) {
        return success(cfgRuleCommonService.view(platformType,type));
    }


    /**
     * 获取备注描述
     */
    @GetMapping("/description")
    public ApiResult<List<CfgRuleCommonDTO.DescriptionDTO>> description(@RequestParam("platformType") String platformType) {
        List<CfgRuleCommonDTO.DescriptionDTO> descriptionDTOS = cfgRuleCommonService.description(platformType);
        return success(descriptionDTOS);
    }
}
