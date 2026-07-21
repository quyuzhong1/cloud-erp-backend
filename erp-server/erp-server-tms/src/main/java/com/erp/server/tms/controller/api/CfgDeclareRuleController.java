package com.erp.server.tms.controller.api;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.CfgDeclareRuleDTO;
import com.erp.model.tms.entity.CfgDeclareRuleEntity;
import com.erp.server.tms.service.CfgDeclareRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 报关规则主表
 */
@Slf4j
@RestController
@LogSystemModule("报关规则主表")
@RequestMapping("/cfgDeclareRule")
public class CfgDeclareRuleController extends BaseController {

    @Resource
    private CfgDeclareRuleService cfgDeclareRuleService;

    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "报关规则主表批量保存")
    public ApiResult<?> add(@RequestBody @Validated CfgDeclareRuleDTO.SaveListDTO dto) {
        cfgDeclareRuleService.add(dto);
        return success();
    }

    @PostMapping("/paging")
    public ApiResult<CfgDeclareRuleDTO.SaveListDTO> paging(@RequestBody @Validated CfgDeclareRuleDTO.ListParamDTO dto) {
        return success(cfgDeclareRuleService.paging(dto));
    }

    @GetMapping("/dropDownList")
    public ApiResult<List<BaseDropDownDTO.Tree>> dropDownList(@RequestParam("type") String type,
                                                              @RequestParam(value = "isShowCustomerId", required = false) Boolean isShowCustomerId,
                                                              @RequestParam(value = "name", required = false) String name) {
        return success(cfgDeclareRuleService.dropDownList(type, isShowCustomerId,name));
    }

    /**
     * 引用取值：根据规则类型和业务条件匹配一条报关规则。
     *
     * <p>入参为 Map<String, String>，必须包含 ruleType，其它字段由报关规则条件配置决定。
     * 同一字段支持用英文逗号拼接多个去重值，例如多个发货仓、目的仓或销售组织。</p>
     *
     * <p>出参只返回一条规则：未匹配时 data 为 null；匹配多条时，返回
     * {@link CfgDeclareRuleEntity#getIndex()} 最小的规则。</p>
     *
     * <p>常用条件字段：</p>
     * countryCode
     * fromWarehouseId
     * salesOrgId
     * transferWarehouseId
     * destWarehouseId
     */
    @PostMapping("/listMatchedRule")
    public ApiResult<CfgDeclareRuleEntity> listMatchedRule(@RequestBody Map<String, String> paramMap) {
        return success(cfgDeclareRuleService.listMatchedRule(paramMap));
    }

}
