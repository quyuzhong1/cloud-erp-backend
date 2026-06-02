package com.erp.server.wms.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WegoSkuQueryDTO;
import com.erp.server.wms.schedule.WegoWarehouseBaseDataJob;
import com.sdk.wms.wego.service.WegoOpenApiService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * WEGO 开放接口
 */
@RestController
@RequestMapping("/wego")
@LogSystemModule("WEGO开放接口")
public class WegoOpenApiController extends BaseController {

    @Resource
    private WegoOpenApiService wegoOpenApiService;

    @Resource
    private WegoWarehouseBaseDataJob wegowarehouseBaseDataJob;

    /**
     * 查询 WEGO 仓库
     */
    @PostMapping("/warehouse/query")
    public ApiResult<JSONObject> queryWarehouse() {
        wegowarehouseBaseDataJob.wegoWarehouseBaseDataJob();
        return success();
    }

    /**
     * 查询 WEGO SKU
     *
     * @param dto 请求参数（accessToken、secret、pageSize、pageNum）
     * @return WEGO 原始响应
     */
    @PostMapping("/sku/query")
    public ApiResult<JSONObject> querySku(@RequestBody @Validated WegoSkuQueryDTO.QueryReqDTO dto) {
        return success(wegoOpenApiService.querySku(dto));
    }
}
