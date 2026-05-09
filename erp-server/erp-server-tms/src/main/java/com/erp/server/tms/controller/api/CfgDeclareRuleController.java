package com.erp.server.tms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
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
import javax.servlet.http.HttpServletResponse;
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

    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "报关规则主表修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgDeclareRule:update",
            serviceClass = CfgDeclareRuleService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgDeclareRuleDTO.UpdateDTO dto) {
        cfgDeclareRuleService.update(dto);
        return success();
    }

    @PostMapping("/paging")
    public ApiResult<CfgDeclareRuleDTO.SaveListDTO> paging(@RequestBody @Validated CfgDeclareRuleDTO.ListParamDTO dto) {
        return success(cfgDeclareRuleService.paging(dto));
    }

    @GetMapping("/dropDownList")
    public ApiResult<List<BaseDropDownDTO.Tree>> dropDownList(@RequestParam("type") String type,
                                                              @RequestParam(value = "name", required = false) String name) {
        return success(cfgDeclareRuleService.dropDownList(type, name));
    }

    /**
     * 引用取值：根据规则类型和条件参数匹配报关规则。
     * 参数是Map<String, String> paramMap
     * 需要传参：都是去重逗号拼接
     * countryCode
     * fromWarehouseId
     * salesOrgId
     * transferWarehouseId
     * destWarehouseId
     */
    @PostMapping("/listMatchedRule")
    public ApiResult<List<CfgDeclareRuleEntity>> listMatchedRule(@RequestBody Map<String, String> paramMap) {
        return success(cfgDeclareRuleService.listMatchedRule(paramMap));
    }

}
