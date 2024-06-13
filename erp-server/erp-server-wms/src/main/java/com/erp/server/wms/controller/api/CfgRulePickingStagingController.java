package com.erp.server.wms.controller.api;


import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.entity.CfgRulePickingStagingEntity;
import com.erp.server.wms.service.CfgRulePickingStagingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 拣货暂存规则 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
@RestController
@RequestMapping("/cfg-rule-picking-staging")
public class CfgRulePickingStagingController extends BaseController {

    @Resource
    private CfgRulePickingStagingService cfgRulePickingStagingService;

    /**
     * 保存默认暂存库位
     */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody List<CfgRulePickingStagingEntity> entity){
        cfgRulePickingStagingService.saveOrUpdateBatch(entity);
        return success();
    }

    /**
     * 保存默认暂存库位
     */
    @PostMapping("/delete")
    public ApiResult<String> delete(@RequestBody List<String> ids){
        cfgRulePickingStagingService.removeByIds(ids);
        return success();
    }

}
