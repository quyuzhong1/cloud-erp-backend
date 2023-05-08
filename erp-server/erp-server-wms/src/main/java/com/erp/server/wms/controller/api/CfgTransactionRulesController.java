package com.erp.server.wms.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.wms.service.CfgTransactionRulesService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Classname: CfgTransactionRulesController
 * @Description: TODO
 * @CreateTime: 2023-05-05  19:29
 * @Author: zhangchunlin
 */
@AllArgsConstructor
@RestController
@RequestMapping("/cfgTransactionRules")
public class CfgTransactionRulesController extends BaseController {

    private final CfgTransactionRulesService cfgTransactionRulesService;

    /**
     * 初始化库存交易配置
     *
     * @return
     */
    @PostMapping("/init")
    public ApiResult<Void> init() {
        cfgTransactionRulesService.initRules();
        return success();
    }

}