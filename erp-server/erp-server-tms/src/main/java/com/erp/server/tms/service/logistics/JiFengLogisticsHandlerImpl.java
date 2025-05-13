package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthFieldService;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.wms.goodcang.dto.response.GoodCangLogisticsProductsResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.service.GoodCangService;
import com.sdk.wms.jifeng.dto.request.JiFengAuthRequest;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengTokenResp;
import com.sdk.wms.jifeng.service.JiFengService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 极风物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.JIFENG)
public class JiFengLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private JiFengService jiFengService;

    @Resource
    private LogisticsAuthFieldService logisticsAuthFieldService;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        return success();
    }

    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getCode, LogisticsPlatformEnum.JIFENG)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list();
        //三方仓如果已授权，直接取三方仓授权信息
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream().filter(v->{
            Map<String,Object> warehouseAuthMap = v.getAuthJson();
            return warehouseAuthMap.containsKey("appKey") && warehouseAuthMap.get("appKey").toString().equals(authMap.get("appKey"));
        }).findFirst().orElse(null);
        if(Objects.nonNull(overseasProviderEntity)){
            Map<String,Object> warehouseAuthMap = overseasProviderEntity.getAuthJson();
            authMap.put("refreshToken",warehouseAuthMap.get("refreshToken").toString());
            authMap.put("accessToken",warehouseAuthMap.get("accessToken").toString());
            authMap.put("token",warehouseAuthMap.get("token").toString());
            authMap.put("userId",warehouseAuthMap.get("userId").toString());
            logisticsAuthFieldService.saveOrUpdateAuthField(authMap.get("id"),authMap);
        }else{
            JiFengAuthRequest jiFengAuthRequest = JiFengAuthRequest.builder()
                    .email(authMap.get("email"))
                    .token(authMap.get("token"))
                    .domain(authMap.get("domain"))
                    .clientId(authMap.get("appKey"))
                    .clientSecret(authMap.get("appToken"))
                    .build();
            JiFengBaseResp<String> authResp = jiFengService.authorize(jiFengAuthRequest);
            if(!isSuccess(authResp)){
                throw new ServiceException("授权失败,"+authResp.getMessage());
            }
            jiFengAuthRequest.setKey(authResp.getData());
            JiFengBaseResp<JiFengTokenResp> tokenResp = jiFengService.accessToken(jiFengAuthRequest);
            if(!isSuccess(tokenResp)){
                throw new ServiceException("授权失败,"+tokenResp.getMessage());
            }
            JiFengTokenResp jiFengTokenResp = tokenResp.getData();
            authMap.put("accessToken",jiFengTokenResp.getAccessToken());
            authMap.put("refreshToken",jiFengTokenResp.getRefreshToken());
            authMap.put("userId",jiFengTokenResp.getUserId().toString());
            logisticsAuthFieldService.saveOrUpdateAuthField(authMap.get("id"),authMap);
        }
        return success();
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.JIFENG;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }

    public <T> boolean isSuccess(JiFengBaseResp<T> resp){
        return resp.getCode()==0;
    }
}
