package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.jifeng.dto.request.JiFengAuthRequest;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengTokenResp;
import com.sdk.wms.jifeng.service.JiFengService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
public class JiFengHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private JiFengService jiFengService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.JIFENG;
    }


    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return null;
    }

    @Override
    protected ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        return null;
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        return null;
    }

    @Override
    protected ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq) {
        return null;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return null;
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return null;
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        return null;
    }

    @Override
    protected ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        return null;
    }

    @Override
    protected ApiResult<String> cancelOutboundBill(ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        return null;
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        Map<String, Object> authJson = dto.getAuthJson();
        JiFengAuthRequest jiFengAuthRequest = JiFengAuthRequest.builder()
                .email(authJson.get("email").toString())
                .token(authJson.get("token").toString())
                .domain(authJson.get("domain").toString())
                .clientId(authJson.get("appKey").toString())
                .clientSecret(authJson.get("appToken").toString())
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
        authJson.put("accessToken",jiFengTokenResp.getAccessToken());
        authJson.put("refreshToken",jiFengTokenResp.getRefreshToken());
        authJson.put("userId",jiFengTokenResp.getUserId());
        LocalDateTime expireIn = Instant.ofEpochMilli(jiFengTokenResp.getExpireIn())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        authJson.put("expireIn",expireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        LocalDateTime refreshExpireIn = Instant.ofEpochMilli(jiFengTokenResp.getRefreshExpireIn())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        authJson.put("refreshExpireIn",refreshExpireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dto.setAuthJson(authJson);
        return true;
    }

    @Override
    protected  ApiResult<String> refreshToken(Map<String,Object> map){
        JiFengAuthRequest jiFengAuthRequest = JiFengAuthRequest.builder()
                .domain(map.get("domain").toString())
                .clientId(map.get("appKey").toString())
                .clientSecret(map.get("appToken").toString())
                .refreshToken(map.get("refreshToken").toString())
                .userId(Integer.valueOf(map.get("userId").toString()))
                .build();
        JiFengBaseResp<JiFengTokenResp> resp = jiFengService.refreshToken(jiFengAuthRequest);
        if(!isSuccess(resp)){
            return failure(resp.getMessage());
        }
        JiFengTokenResp jiFengTokenResp = resp.getData();
        map.put("accessToken",jiFengTokenResp.getAccessToken());
        LocalDateTime expireIn = Instant.ofEpochMilli(jiFengTokenResp.getExpireIn())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        map.put("expireIn",expireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return success();
    };
    public <T> boolean isSuccess(JiFengBaseResp<T> resp){
        return resp.getCode()==0;
    }

    public static void main(String[] args) {
        LocalDateTime localDateTime = Instant.ofEpochMilli(1778310710517l)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        System.out.println(localDateTime);
    }
}
