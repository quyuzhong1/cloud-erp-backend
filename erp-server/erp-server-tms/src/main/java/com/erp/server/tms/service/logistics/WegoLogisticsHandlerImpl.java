package com.erp.server.tms.service.logistics;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.model.wms.dto.WegoTransportQueryDTO;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.sdk.wms.wego.service.WegoOpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * WEGO 物流接口处理器
 * <p>
 * WEGO 授权属于特殊场景，无需调用 WEGO 第三方授权接口，
 * 仅需将入参中的 appSecret、appToken 落库到物流授权字段表。
 * 字段落库由 {@code LogisticsAuthServiceImpl#add} 中的
 * {@code logisticsAuthFieldService.saveOrUpdateAuthField} 统一完成，
 * 这里只需校验必填字段后返回授权成功，避免控制器走回滚分支。
 * <p>
 * 物流渠道同步走 {@code LogisticsBaseServiceImpl#syncSingleChannel} 通用流程，
 * 由本 handler 的 {@link #getChannel(ChanelQueryVO)} 调用 WEGO {@code transport.get}
 * 接口拉取派送渠道并转换为 {@link LogisticsSaleChannelEntity}。
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.WEGO)
public class WegoLogisticsHandlerImpl extends AbstractLogisticsHandler {

    private static final String APP_SECRET = "appSecret";
    private static final String APP_TOKEN = "appToken";

    /**
     * tms 侧 ERP 服务平台标识，统一与其它 tms 物流商保持一致
     */
    private static final String SERVICE_PLATFORM_TMS = "tms";

    /**
     * WEGO transport.get 渠道类型 0 表示 2C
     */
    private static final Integer TRANSPORTATION_TYPE_2C = 0;

    @Resource
    private WegoOpenApiService wegoOpenApiService;

    @Override
    public ApiResult<Object> authorization(Map<String, String> authMap) {
        if (authMap == null) {
            throw new ServiceException("授权信息不能为空");
        }
        String appSecret = authMap.get(APP_SECRET);
        String appToken = authMap.get(APP_TOKEN);
        if (CharSequenceUtil.isBlank(appSecret)) {
            throw new ServiceException("appSecret不能为空");
        }
        if (CharSequenceUtil.isBlank(appToken)) {
            throw new ServiceException("appToken不能为空");
        }
        return success("授权成功");
    }

    /**
     * 调用 WEGO {@code transport.get} 接口拉取派送渠道。
     * <p>
     * 由 {@code LogisticsBaseServiceImpl#syncSingleChannel} 在轮询每一份
     * {@code logistics_auth} 授权配置时回调，返回的列表会被通用流程统一
     * 落库到 {@code logistics_sale_channel}（含状态置 0 等动作）。
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        Map<String, String> authMap = chanelQueryVO == null ? null : chanelQueryVO.getAuthMap();
        if (authMap == null || authMap.isEmpty()) {
            return ApiResult.error(-1, "WEGO授权信息为空");
        }
        String appToken = authMap.get(APP_TOKEN);
        String appSecret = authMap.get(APP_SECRET);
        if (CharSequenceUtil.hasBlank(appToken, appSecret)) {
            return ApiResult.error(-1, "WEGO授权信息缺失appToken/appSecret");
        }

        JSONObject response;
        try {
            WegoTransportQueryDTO.QueryReqDTO reqDTO = new WegoTransportQueryDTO.QueryReqDTO();
            reqDTO.setAccessToken(appToken);
            reqDTO.setSecret(appSecret);
            response = wegoOpenApiService.queryTransport(reqDTO);
        } catch (Exception e) {
            log.error("[WEGO渠道同步] 调用 transport.get 异常, authId={}", authMap.get("id"), e);
            return ApiResult.error(-1, "WEGO查询派送渠道异常: " + e.getMessage());
        }
        if (Objects.isNull(response)) {
            return ApiResult.error(-1, "WEGO查询派送渠道接口返回为空");
        }
        if (!Boolean.TRUE.equals(response.getBoolean("success"))) {
            String errorMsg = response.getString("errorMsg");
            Integer errorCode = response.getInteger("errorCode");
            log.error("[WEGO渠道同步] 接口返回失败, authId={}, errorCode={}, errorMsg={}",
                    authMap.get("id"), errorCode, errorMsg);
            return ApiResult.error(errorCode == null ? -1 : errorCode,
                    CharSequenceUtil.isBlank(errorMsg) ? "WEGO查询派送渠道失败" : errorMsg);
        }
        JSONArray result = response.getJSONArray("result");
        if (result == null || result.isEmpty()) {
            return success(new ArrayList<>());
        }
        List<LogisticsSaleChannelEntity> channelList = new ArrayList<>(result.size());
        String platform = LogisticsPlatformEnum.WEGO.getCode();
        for (int i = 0; i < result.size(); i++) {
            JSONObject item = result.getJSONObject(i);
            if (item == null) {
                continue;
            }
            LogisticsSaleChannelEntity entity = buildSaleChannel(platform, item);
            if (entity != null) {
                channelList.add(entity);
            }
        }
        return success(channelList);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.WEGO;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 将 WEGO transport.get 单条结果映射为 {@link LogisticsSaleChannelEntity}。
     * <p>
     * 落库 upsert 唯一键（受 {@code saveOrUpdateSaleChannel} 约束）：
     * {@code logisticsPlatform + code + platformWarehouseCode}。
     */
    private LogisticsSaleChannelEntity buildSaleChannel(String platform, JSONObject item) {
        String warehouseCode = item.getString("warehouseCode");
        String transportationBusiness = item.getString("transportationBusiness");
        String transportationService = item.getString("transportationService");
        Integer transportationType = item.getInteger("transportationType");
        if (CharSequenceUtil.isAllBlank(transportationBusiness, transportationService)) {
            log.warn("[WEGO渠道同步] 派送渠道关键字段为空, 跳过: {}", item.toJSONString());
            return null;
        }
        String channelCode = buildChannelCode(transportationBusiness, transportationService, transportationType);

        LogisticsSaleChannelEntity entity = new LogisticsSaleChannelEntity();
        entity.setLogisticsPlatform(platform);
        entity.setServicePlatform(SERVICE_PLATFORM_TMS);
        entity.setPlatformChannelId(channelCode);
        entity.setCode(channelCode);
        entity.setCnName(transportationService);
        entity.setEnName(transportationService);
        entity.setSupplierName(transportationBusiness);
        entity.setSupplierCode(transportationBusiness);
        entity.setPlatformWarehouseCode(warehouseCode);
        entity.setCarrierType(transportationBusiness);
        entity.setChannelType(resolveChannelType(transportationType));
        entity.setChannelStatus(MathUtil.ZERO);
        entity.setSourceData(item.toJSONString());
        return entity;
    }

    /**
     * 构造 WEGO 渠道 code。
     * <p>
     * {@code platformWarehouseCode} 已在 entity 中单独保留，code 不再叠加，
     * 仅由 transportationBusiness / transportationService / transportationType
     * 组合保证同一仓库下唯一。
     */
    private String buildChannelCode(String transportationBusiness, String transportationService, Integer transportationType) {
        return CharSequenceUtil.nullToEmpty(transportationBusiness) + "|"
                + CharSequenceUtil.nullToEmpty(transportationService) + "|"
                + (transportationType == null ? "" : transportationType);
    }

    private String resolveChannelType(Integer transportationType) {
        if (transportationType == null) {
            return null;
        }
        return TRANSPORTATION_TYPE_2C.equals(transportationType) ? "2C" : "2B";
    }
}
