package com.erp.server.tms.service.logistics;

import cn.hutool.core.collection.CollUtil;
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
import com.sdk.wms.goodcang.dto.response.GoodCangLogisticsProductsResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.service.GoodCangService;
import com.sdk.wms.zhongbao.dto.request.ChannelRequest;
import com.sdk.wms.zhongbao.dto.request.CommonRequest;
import com.sdk.wms.zhongbao.dto.request.PageRequest;
import com.sdk.wms.zhongbao.dto.response.BaseResponse;
import com.sdk.wms.zhongbao.dto.response.ChannelResponse;
import com.sdk.wms.zhongbao.service.ZhongbaoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 谷仓物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.ZHONG_BAO)
public class ZhongBaoWarehouseLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsOperateService logisticsOperateService;

    @Resource
    private GoodCangService goodCangService;
    @Resource
    private ZhongbaoService zhongbaoService;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        Map<String, Object> authMap = chanelQueryVO.getAuthMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<LogisticsSaleChannelEntity> response = new ArrayList<>();
        //通过海外仓仓库查询
        List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntityList = overseasWarehouseFeign.getOverseasWarehouseListByPlatformCodes(new ArrayList<>(),getPlatForm().getCode());
        if (CollUtil.isEmpty(overseasProviderWarehouseEntityList)){
            return ApiResult.success(response);
        }
        List<String> platformWarehouseCodeList = overseasProviderWarehouseEntityList.stream().map(OverseasProviderWarehouseEntity::getPlatformWarehouseCode).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        Map<String, String> warehouseMap = overseasProviderWarehouseEntityList.stream()
                .collect(Collectors.toMap(OverseasProviderWarehouseEntity::getPlatformWarehouseCode,
                        OverseasProviderWarehouseEntity::getId,(v1,v2)->v1));
        for (String platformWarehouseCode : platformWarehouseCodeList) {
            ChannelRequest channelRequest = ChannelRequest.builder().warehouseCode(platformWarehouseCode).commonParam(CommonRequest.builder().pageParam(PageRequest.builder().pageNum("0").pageSize("10").build()).build()).build();
            List<ChannelResponse.Channel> channels = zhongbaoService.chanelList(authMap,channelRequest);
            if (CollUtil.isEmpty(channels)){
                continue;
            }

            //封装仓库id
            channels.forEach(channel -> channel.setErpWarehouseId(warehouseMap.get(channel.getOpenWarehouse().getWarehouseCode())));
            //记录日志
            logAndReturnSuccess(chanelQueryVO, RequestStatusEnums.SUCCESS, channels);
            //实体转换
            response.addAll(LogisticsChannelConverter.INSTANCE.channelConvertByZhongBao(channels));
        }
        return ApiResult.success(response);
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
            String appKey = authMap.get("appKey");
            String appSecret = authMap.get("appSecret");
            BaseResponse open = zhongbaoService.open(zhongbaoService.getToken(appKey, appSecret));
            if (open.getSuccess()){
                return success("授权成功");
            }else {
                return failure("授权失败:" + open.getMessage());
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }finally {
            ThirdWarehouseContext.remove();
        }
    }

    private void logAndReturnSuccess(ChanelQueryVO chanelQueryVO, RequestStatusEnums status,
                                     List<ChannelResponse.Channel> channels) {
        logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(),
                getPlatForm().getCode(), status.getCode(), JSONUtil.toJsonStr(chanelQueryVO),
                JSONUtil.toJsonStr(channels));
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
        return LogisticsPlatformEnum.ZHONG_BAO;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
