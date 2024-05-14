package com.erp.server.sys.controller.openapi;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.openapi.CollectorPacksDTO;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.sys.openapi.ReturnTrackingDTO;
import com.erp.server.sys.config.OpenApi;

import javax.validation.Valid;

@OpenApi
public class WarehouseOpenApi {

    @OpenApi("dimensionalWeightPipeline")
    public ApiResult<String> dimensionalWeightPipeline(@Valid DimensionalWeightDTO dto) {
        return ApiResult.success("1");
    }

    @OpenApi("dimensionalWeightTob")
    public ApiResult<String> dimensionalWeightTob(@Valid DimensionalWeightDTO dto) {

        return ApiResult.success("");
    }

    @OpenApi("dimensionalWeightFba")
    public ApiResult<String> dimensionalWeightFba(@Valid DimensionalWeightDTO dto) {

        return ApiResult.success("");
    }

    @OpenApi("dimensionalWeightMeasure")
    public ApiResult<String> dimensionalWeightMeasure(@Valid DimensionalWeightDTO dto) {

        return ApiResult.success("");
    }

    @OpenApi("dimensionalWeightPackage")
    public ApiResult<String> dimensionalWeightPackage(@Valid CollectorPacksDTO dto) {

        return ApiResult.success("");
    }


    @OpenApi("dimensionalWeightReturn")
    public ApiResult<String> dimensionalWeightReturn(@Valid ReturnTrackingDTO dto) {

        return ApiResult.success("");
    }
}
