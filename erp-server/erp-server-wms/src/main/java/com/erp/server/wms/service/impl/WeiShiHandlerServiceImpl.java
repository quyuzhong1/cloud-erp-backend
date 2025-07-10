package com.erp.server.wms.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.jifeng.dto.request.JiFengAuthRequest;
import com.sdk.wms.jifeng.dto.request.JiFengCreateInboundRequest;
import com.sdk.wms.jifeng.dto.request.JiFengCreateOutboundRequest;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengCreateInboundResp;
import com.sdk.wms.jifeng.dto.response.JiFengTokenResp;
import com.sdk.wms.jifeng.service.JiFengService;
import com.sdk.wms.weishi.dto.request.WeiShiBaseRequest;
import com.sdk.wms.weishi.dto.response.WeiShiBaseResp;
import com.sdk.wms.weishi.dto.response.WeiShiReturnOrderResp;
import com.sdk.wms.weishi.dto.response.WeiShiTokenResp;
import com.sdk.wms.weishi.service.WeiShiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
public class WeiShiHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private WeiShiService weiShiService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.WEI_SHI;
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
        throw new ServiceException("该仓库入库单不允许修改，请取消入库单后重新创建");
    }

    @Override
    protected ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq) {

        return success();
    }

    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return null;
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return success(new ThirdWarehouseUploadFileResponse());
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
        WeiShiBaseResp<WeiShiTokenResp> authResp = weiShiService.accessToken(authJson);
        if(!isSuccess(authResp)){
            throw new ServiceException("授权失败,"+authResp.getMsg());
        }
        WeiShiTokenResp weiShiTokenResp = authResp.getData();
        authJson.put("accessToken",weiShiTokenResp.getAccessToken());
        Long expireInSecond = weiShiTokenResp.getExpiresIn();
        LocalDateTime expireIn = LocalDateTime.now().plusSeconds(expireInSecond);
        authJson.put("expireIn",expireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dto.setAuthJson(authJson);
        return true;
    }
    @Override
    protected  ApiResult<String> refreshToken(Map<String,Object> map){
        WeiShiBaseResp<WeiShiTokenResp> authResp = weiShiService.accessToken(map);
        if(!isSuccess(authResp)){
            throw new ServiceException("授权失败,"+authResp.getMsg());
        }
        WeiShiTokenResp weiShiTokenResp = authResp.getData();
        map.put("accessToken",weiShiTokenResp.getAccessToken());
        Long expireInSecond = weiShiTokenResp.getExpiresIn();
        LocalDateTime expireIn = LocalDateTime.now().plusSeconds(expireInSecond);
        map.put("expireIn",expireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return success();
    }

    public <T> boolean isSuccess(WeiShiBaseResp<T> resp){
        return resp.getCode()==200;
    }

}
