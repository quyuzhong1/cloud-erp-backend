package com.erp.server.dmp.inout.handler.input.task.init.api.aiya;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 爱亚海外仓仓库列表拉取 InitHandler，对齐 {@code WegoWarehouseInitHandler}。
 * <p>
 * 调用爱亚 {@code GLINK_QUERY_WAREHOUSE_NOTIFY} 一次性获取仓库列表，写入 DMP Init 阶段，
 * 后续由 mongo/dmp handler 落 {@code dmp_third_warehouse_info}。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaWarehouseInitHandler extends AbstractAiyaInitHandler {

    private static final String ACTION = "查询仓库";

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        AiyaAuth auth = resolveAuth();
        JSONObject resp = aiyaOpenApiService.queryWarehouse(auth.getCustomerCode(), auth.getPartnerKey(), null);
        if (resp == null) {
            log.warn("[爱亚仓库] 服务商[id={}] 接口无响应", auth.getAuthId());
            return Collections.emptyList();
        }
        if (!Boolean.TRUE.equals(resp.getBoolean("success"))) {
            throw new ServiceException(ApiError.WH_AIYA_RESPONSE_FAILED, ACTION,
                    resp.getString("errorCode"), resp.getString("errorMsg"));
        }
        JSONArray warehouseArray = resp.getJSONArray("result");
        if (CollUtil.isEmpty(warehouseArray)) {
            log.info("[爱亚仓库] 服务商[id={}] 未拉到任何仓库", auth.getAuthId());
            return Collections.emptyList();
        }
        return Collections.singletonList(buildInitDTO(warehouseArray, auth.getAuthId()));
    }
}
