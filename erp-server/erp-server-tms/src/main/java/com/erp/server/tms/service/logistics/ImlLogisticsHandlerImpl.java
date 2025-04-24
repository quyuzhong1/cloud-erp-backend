package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.wms.iml.dto.response.ImlInventoryLogisticsProductsResp;
import com.sdk.wms.iml.dto.response.ImlResponse;
import com.sdk.wms.iml.service.ImlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 艾姆勒物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.IML)
public class ImlLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsOperateService logisticsOperateService;

    @Resource
    private ImlService imlService;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            Map<String, Object> authMap = chanelQueryVO.getAuthMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            ThirdWarehouseContext.setAuthMap(authMap);

            ImlResponse<List<ImlInventoryLogisticsProductsResp>> imlResponse = imlService.getShippingMethod("");
            if (isFailure(imlResponse.getAsk())) {
                logAndReturnFailure(chanelQueryVO, RequestStatusEnums.FAILED, imlResponse);
            }

            //封装仓库id
            List<String> warehouseCodeList = imlResponse.getData().stream()
                    .map(ImlInventoryLogisticsProductsResp::getWarehouseCode).distinct().collect(Collectors.toList());

            List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntityList =
                    overseasWarehouseFeign.getOverseasWarehouseListByPlatformCodes(warehouseCodeList,getPlatForm().getCode());

            Map<String, String> warehouseMap = overseasProviderWarehouseEntityList.stream()
                    .collect(Collectors.toMap(OverseasProviderWarehouseEntity::getPlatformWarehouseCode,
                            OverseasProviderWarehouseEntity::getId));

            imlResponse.getData().forEach(data -> data.setErpWarehouseId(warehouseMap.get(data.getWarehouseCode())));

            //实体转换
            List<LogisticsSaleChannelEntity> response =
                    LogisticsChannelConverter.INSTANCE.channelConvertByIml(imlResponse.getData());

            logAndReturnSuccess(chanelQueryVO, RequestStatusEnums.SUCCESS, imlResponse);

            return success(response);
        } catch (Exception e) {
            log.error("艾姆勒渠道接口异常：{}", e.getMessage());
            logAndReturnFailure(chanelQueryVO, RequestStatusEnums.FAILED, e);
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        } finally {
            ThirdWarehouseContext.remove();
        }
    }

    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        try {
            Map<String, Object> authObjMap = authMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            ThirdWarehouseContext.setAuthMap(authObjMap);

            ImlResponse<List<ImlInventoryLogisticsProductsResp>> imlResponse =
                    imlService.getShippingMethod("");
            if (isFailure(imlResponse.getAsk())) {
                return failure("授权失败:" + imlResponse.getMessage());
            } else {
                return success("授权成功");
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        } finally {
            ThirdWarehouseContext.remove();
        }
    }

    private void logAndReturnSuccess(ChanelQueryVO chanelQueryVO, RequestStatusEnums status,
                                     ImlResponse<?> imlResponse) {
        logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(),
                getPlatForm().getCode(), status.getCode(), JSONUtil.toJsonStr(chanelQueryVO),
                JSONUtil.toJsonStr(imlResponse));
    }

    private void logAndReturnFailure(ChanelQueryVO chanelQueryVO, RequestStatusEnums status,
                                     Object failureDetails) {
        logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(),
                getPlatForm().getCode(), status.getCode(), JSONUtil.toJsonStr(chanelQueryVO),
                JSONUtil.toJsonStr(failureDetails));
    }

    private boolean isFailure(String ask) {
        return !"Success".equals(ask);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.IML;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
