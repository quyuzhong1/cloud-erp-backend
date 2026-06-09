package com.erp.server.dmp.inout.handler.input.task.init.api.wego;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.WegoWarehouseQueryDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.wms.wego.service.WegoOpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * dmp输入init任务基础处理器下的WEGO海外仓api获取数据方式
 */
@Slf4j
@Service
@Scope("prototype")
public class WegoWarehouseInitHandler extends DmpInputInitHandler {

    /**
     * WEGO 授权 JSON 中的 accessToken 字段，与 erp-server-wms 中 WEGO 授权约定保持一致。
     */
    private static final String AUTH_KEY_APP_TOKEN = "appToken";

    /**
     * WEGO 授权 JSON 中的 secret 字段，仅用于本地签名。
     */
    private static final String AUTH_KEY_APP_SECRET = "appSecret";

    @Resource
    private WegoOpenApiService wegoOpenApiService;

    @Resource
    private OverseasProviderFeign overseasProviderFeign;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<DmpInputTaskInitDTO> resultList = new ArrayList<>();

        List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, OmsPlatformEnum.WE_GO.getCode())
                .list();
        if (CollUtil.isEmpty(overseasProviderEntityList)) {
            throw new ServiceException("WEGO授权信息不存在");
        }
        // 取对应授权ID授权
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if (null == overseasProviderEntity) {
            throw new ServiceException(OmsPlatformEnum.WE_GO.getCode() + "对应授权ID信息不存在,nextId:" + dmpInputTaskEntity.getNextLevelId());
        }

        JSONObject resp = callWarehouseList(overseasProviderEntity);
        if (resp == null) {
            return Collections.emptyList();
        }
        if (!Boolean.TRUE.equals(resp.getBoolean("success"))) {
            // token 失效场景：刷新 token 后重试一次（与 jifeng 保持一致的容错策略）
            if (isTokenInvalid(resp)) {
                overseasProviderEntity = overseasProviderFeign.refreshToken(overseasProviderEntity);
                resp = callWarehouseList(overseasProviderEntity);
                if (resp == null) {
                    return Collections.emptyList();
                }
                if (!Boolean.TRUE.equals(resp.getBoolean("success"))) {
                    throw new ServiceException("WEGO获取仓库列表失败,errorCode:" + resp.getString("errorCode")
                            + ",errorMsg:" + resp.getString("errorMsg"));
                }
            } else {
                throw new ServiceException("WEGO获取仓库列表失败,errorCode:" + resp.getString("errorCode")
                        + ",errorMsg:" + resp.getString("errorMsg"));
            }
        }

        JSONArray warehouseArray = resp.getJSONArray("result");
        if (CollUtil.isEmpty(warehouseArray)) {
            return Collections.emptyList();
        }
        String id = overseasProviderEntity.getId();
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        warehouseArray.forEach(p -> {
            JSONObject j = (JSONObject) p;
            j.put("authId", id);
        });
        dmpInputTaskInitDTO.setMsg(warehouseArray.toJSONString());
        resultList.add(dmpInputTaskInitDTO);
        return resultList;
    }

    /**
     * 调用 WEGO warehouse.get 接口；将 OverseasProviderEntity 中的 authJson 转换为 SDK 入参。
     *
     * @param overseasProviderEntity 当前授权的海外仓配置
     * @return WEGO 接口原始响应解析后的 JSONObject，authJson 缺失关键字段时抛出 ServiceException
     */
    private JSONObject callWarehouseList(OverseasProviderEntity overseasProviderEntity) {
        Map<String, Object> authJson = overseasProviderEntity.getAuthJson();
        if (authJson == null || authJson.isEmpty()) {
            throw new ServiceException("WEGO授权信息auth_json为空,authId:" + overseasProviderEntity.getId());
        }
        String appToken = toStr(authJson.get(AUTH_KEY_APP_TOKEN));
        String appSecret = toStr(authJson.get(AUTH_KEY_APP_SECRET));
        if (CharSequenceUtil.hasBlank(appToken, appSecret)) {
            throw new ServiceException("WEGO授权信息appToken/appSecret缺失,authId:" + overseasProviderEntity.getId());
        }

        WegoWarehouseQueryDTO.QueryReqDTO reqDTO = new WegoWarehouseQueryDTO.QueryReqDTO();
        reqDTO.setAccessToken(appToken);
        reqDTO.setSecret(appSecret);
        return wegoOpenApiService.queryWarehouse(reqDTO);
    }

    /**
     * 识别 WEGO 接口返回中的 token 失效场景，errorCode/errorMsg 中含 token 关键字时认为是 token 过期，
     * 触发 {@link OverseasProviderFeign#refreshToken(OverseasProviderEntity)} 刷新后重试。
     */
    private boolean isTokenInvalid(JSONObject resp) {
        String errorCode = resp.getString("errorCode");
        String errorMsg = resp.getString("errorMsg");
        return CharSequenceUtil.containsIgnoreCase(errorCode, "token")
                || CharSequenceUtil.containsIgnoreCase(errorMsg, "token");
    }

    private String toStr(Object value) {
        return value == null ? null : value.toString();
    }
}
