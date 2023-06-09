package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.erp.server.plm.service.CfgProductOwnerRuleService;
import org.springframework.beans.factory.annotation.Autowired;
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



}
