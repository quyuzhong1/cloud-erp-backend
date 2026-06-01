package com.erp.server.wms.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WegoWarehouseQueryDTO;
import com.erp.server.wms.service.WegoOpenApiService;
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
public class WegoOpenApiController extends BaseController {

    @Resource
    private WegoOpenApiService wegoOpenApiService;

    /**
     * 查询 WEGO 仓库
     */
    @PostMapping("/warehouse/query")
    public ApiResult<JSONObject> queryWarehouse(@RequestBody @Validated WegoWarehouseQueryDTO.QueryReqDTO dto) {
        return success(wegoOpenApiService.queryWarehouse(dto));
    }
}
