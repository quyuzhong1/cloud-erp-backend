package com.erp.server.wms.controller.api;


import com.erp.model.wms.dto.AqlSamplingRequest;
import com.erp.model.wms.dto.AqlSamplingResponse;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.QcSamplingAqlRuleService;
import com.common.core.controller.vo.ApiResult;

import javax.validation.Valid;

/**
 * GB/T2828.1-2012 AQL判定数主表
 *
 * @author zdy
 * @since 2026-03-19
 */
@Slf4j
@RestController
@LogSystemModule("GB/T2828.1-2012 AQL判定数主表")
@RequestMapping("/aqlJudgeMapping")
public class QcSamplingAqlRuleController extends BaseController {

    @Resource
    private QcSamplingAqlRuleService qcSamplingAqlRuleService;
    /**
     * 获取抽样方案
     * POST /api/aql/sampling/calculate
     */
    @PostMapping("/calculate")
    public ApiResult<AqlSamplingResponse> calculate(@Valid @RequestBody AqlSamplingRequest request) {
        return ApiResult.success(qcSamplingAqlRuleService.calculateSamplingPlan(request));
    }


}
