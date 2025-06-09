package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.CfgProductOwnerRuleDTO;
import com.erp.server.plm.service.CfgProductOwnerRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 * 产品归属规则配置表
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
@RestController
@RequestMapping("/cfgProductOwnerRule")
public class CfgProductOwnerRuleController extends BaseController {

    @Autowired
    private CfgProductOwnerRuleService cfgProductOwnerRuleService;




    @PostMapping("/add")
    public ApiResult<Object> add(@RequestBody @Validated CfgProductOwnerRuleDTO.AddDTO dto) {
        Boolean flag = this.cfgProductOwnerRuleService.add(dto);
        return flag  ? success() : failure();
    }



}
