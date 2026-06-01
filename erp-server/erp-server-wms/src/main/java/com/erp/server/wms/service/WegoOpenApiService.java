package com.erp.server.wms.service;

import com.alibaba.fastjson.JSONObject;
import com.erp.model.wms.dto.WegoWarehouseQueryDTO;

/**
 * WEGO 开放接口服务
 */
public interface WegoOpenApiService {

    /**
     * 查询 WEGO 仓库
     *
     * @param dto 查询参数
     * @return WEGO 返回结果（附带请求报文信息）
     */
    JSONObject queryWarehouse(WegoWarehouseQueryDTO.QueryReqDTO dto);
}
