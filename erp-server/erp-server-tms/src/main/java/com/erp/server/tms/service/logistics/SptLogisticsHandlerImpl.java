package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OmsPlatformEnum;
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
import com.sdk.wms.antu.dto.response.AntuLogisticsProductsResp;
import com.sdk.wms.antu.dto.response.AntuResponse;
import com.sdk.wms.antu.service.AntuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 速派通物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.SPT)
public class SptLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsOperateService logisticsOperateService;

    @Resource
    private AntuService antuService;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            Map<String, Object> authMap = chanelQueryVO.getAuthMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            ThirdWarehouseContext.setAuthMap(authMap);

            AntuResponse<List<AntuLogisticsProductsResp>> antuResponse = antuService.getShippingMethod("",OmsPlatformEnum.OMS_SPT);
            if (isFailure(antuResponse.getAsk())) {
                logAndReturnFailure(chanelQueryVO, RequestStatusEnums.FAILED, antuResponse);
            }

            //封装仓库id
            List<String> warehouseCodeList = antuResponse.getData().stream()
                    .map(AntuLogisticsProductsResp::getWarehouseCode).distinct().collect(Collectors.toList());

            List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntityList =
                    overseasWarehouseFeign.getOverseasWarehouseListByPlatformCodes(warehouseCodeList,getPlatForm().getCode());

            Map<String, String> warehouseMap = overseasProviderWarehouseEntityList.stream()
                    .collect(Collectors.toMap(OverseasProviderWarehouseEntity::getPlatformWarehouseCode,
                            OverseasProviderWarehouseEntity::getId));

            antuResponse.getData().forEach(data -> data.setErpWarehouseId(warehouseMap.get(data.getWarehouseCode())));

            //实体转换
            List<LogisticsSaleChannelEntity> response =
                    LogisticsChannelConverter.INSTANCE.channelConvertBySpt(antuResponse.getData());

            logAndReturnSuccess(chanelQueryVO, RequestStatusEnums.SUCCESS, antuResponse);

            return success(response);
        } catch (Exception e) {
            log.error("速派通渠道接口异常：{}", e.getMessage());
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
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> (Object) e.getValue()));
            ThirdWarehouseContext.setAuthMap(authObjMap);

            AntuResponse<List<AntuLogisticsProductsResp>> antuResponse =
                    antuService.getShippingMethod("",OmsPlatformEnum.OMS_SPT);
            if (isFailure(antuResponse.getAsk())) {
                return failure("授权失败:" + antuResponse.getMessage());
            } else {
                return success("授权成功");
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }finally {
            ThirdWarehouseContext.remove();
        }
    }

    private void logAndReturnSuccess(ChanelQueryVO chanelQueryVO, RequestStatusEnums status,
                                     AntuResponse<?> antuResponse) {
        logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(),
                getPlatForm().getCode(), status.getCode(), JSONUtil.toJsonStr(chanelQueryVO),
                JSONUtil.toJsonStr(antuResponse));
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
        return LogisticsPlatformEnum.SPT;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
