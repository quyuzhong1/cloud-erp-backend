package com.erp.server.tms.service.logistics;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.ChannelTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.wms.tongyou.dto.response.TongYouBaseResp;
import com.sdk.wms.tongyou.dto.response.TongYouLogisticChannelResp;
import com.sdk.wms.tongyou.service.TongYouService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 通邮海外仓物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.TONG_YOU_WAREHOUSE)
public class TongYouWarehouseLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsOperateService logisticsOperateService;

    @Resource
    private TongYouService tongYouService;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            Map<String, Object> authMap = chanelQueryVO.getAuthMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            ThirdWarehouseContext.setAuthMap(authMap);
            authMap.put("token",authMap.get("appToken"));
            TongYouBaseResp<List<TongYouLogisticChannelResp>> tongYouResponse = tongYouService.getLogisticsChannel(authMap);
            if (!isSuccess(tongYouResponse)) {
                logAndReturnFailure(chanelQueryVO, RequestStatusEnums.FAILED, tongYouResponse);
            }
            //封装仓库id
            List<String> warehouseCodeList =     // 使用Stream流提取所有非空的warehouseCode
                    tongYouResponse.getData().stream()
                            // 提取warehouseCode字段
                            .map(TongYouLogisticChannelResp::getWarehouseCode)
                            // 过滤掉null的warehouseCode
                            .filter(Objects::nonNull)
                            // 收集结果为List<String>
                            .collect(Collectors.toList());

            List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntityList =
                    overseasWarehouseFeign.getOverseasWarehouseListByPlatformCodes(warehouseCodeList,getPlatForm().getCode());

            //实体转换
            List<LogisticsSaleChannelEntity> response = new ArrayList<>();
            List<TongYouLogisticChannelResp> tongYouLogisticChannelResps = tongYouResponse.getData();
            for (TongYouLogisticChannelResp imlLogisticChannelResp : tongYouLogisticChannelResps) {
                LogisticsSaleChannelEntity logisticsSaleChannelEntity = new LogisticsSaleChannelEntity();
                logisticsSaleChannelEntity.setPlatformChannelId(imlLogisticChannelResp.getLogisticsChannelCode());
                logisticsSaleChannelEntity.setCode(imlLogisticChannelResp.getLogisticsChannelCode());
                logisticsSaleChannelEntity.setCnName(imlLogisticChannelResp.getLogisticsChannelName());
                logisticsSaleChannelEntity.setLogisticsPlatform(OmsPlatformEnum.TONG_YOU.getCode());
                logisticsSaleChannelEntity.setPlatformWarehouseCode(imlLogisticChannelResp.getWarehouseCode());
                logisticsSaleChannelEntity.setChannelType(ChannelTypeEnum.LAST_MILE.getCode());
                OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseEntityList.stream().filter(v->v.getPlatformWarehouseCode().equals(imlLogisticChannelResp.getWarehouseCode()) && StringUtils.isNotBlank(v.getWarehouseId())).findFirst().orElse(null);
                if(Objects.nonNull(overseasProviderWarehouseEntity)){
                    logisticsSaleChannelEntity.setOverseasWarehouseId(overseasProviderWarehouseEntity.getId());
                }
                response.add(logisticsSaleChannelEntity);
            }

            logAndReturnSuccess(chanelQueryVO, RequestStatusEnums.SUCCESS, tongYouResponse);

            return success(response);
        } catch (Exception e) {
            log.error("艾姆勒渠道接口异常：", e);
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
        Map<String,Object> map = new HashMap<>();
        map.put("token",authMap.get("appToken"));
        TongYouBaseResp<List<TongYouLogisticChannelResp>> authResp = tongYouService.getLogisticsChannel(map);
        if(!isSuccess(authResp)){
            throw new ServiceException(getPlatForm().getName() +"授权失败,"+authResp.getContent());
        }
        return success();
    }


    private void logAndReturnSuccess(ChanelQueryVO chanelQueryVO, RequestStatusEnums status,
                                     TongYouBaseResp<?> imlResponse) {
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

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.TONG_YOU_WAREHOUSE;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }

    public <T> boolean isSuccess(TongYouBaseResp<T> resp){
        return CharSequenceUtil.equals(resp.getError(),"T");
    }
}
