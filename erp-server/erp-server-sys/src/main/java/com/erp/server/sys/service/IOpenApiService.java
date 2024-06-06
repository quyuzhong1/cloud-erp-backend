package com.erp.server.sys.service;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.OpenApiInputDTO;

public interface IOpenApiService {

	ApiResult<?> unitPlatformService(OpenApiInputDTO input);
}
