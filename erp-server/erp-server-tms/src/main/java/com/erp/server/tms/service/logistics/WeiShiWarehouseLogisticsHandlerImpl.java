package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthFieldService;
import com.sdk.wms.weishi.dto.request.WeiShiLogisticProductRequest;
import com.sdk.wms.weishi.dto.response.WeiShiBaseResp;
import com.sdk.wms.weishi.dto.response.WeiShiChannelResp;
import com.sdk.wms.weishi.dto.response.WeiShiTokenResp;
import com.sdk.wms.weishi.service.WeiShiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 纬狮海外仓物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.WEI_SHI_WAREHOUSE)
public class WeiShiWarehouseLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private WeiShiService weiShiService;

    @Resource
    private LogisticsAuthFieldService logisticsAuthFieldService;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        Map<String, Object> authMap = chanelQueryVO.getAuthMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<LogisticsSaleChannelEntity> response = new ArrayList<>();

        //通过海外仓仓库查询
        List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntityList = overseasWarehouseFeign.getOverseasWarehouseListByPlatformCodes(new ArrayList<>(),getPlatForm().getCode());
        List<String> platformWarehouseCodeList = overseasProviderWarehouseEntityList.stream().map(OverseasProviderWarehouseEntity::getPlatformWarehouseCode).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        for (String platformWarehouseCode : platformWarehouseCodeList) {
            WeiShiLogisticProductRequest weiShiCancelInboundRequest = new WeiShiLogisticProductRequest();
            weiShiCancelInboundRequest.setWarehouseCode(platformWarehouseCode);
            weiShiCancelInboundRequest.setIsMultiPackage(0);
            WeiShiBaseResp<List<WeiShiChannelResp>> resp = weiShiService.getLogisticProductList(weiShiCancelInboundRequest,authMap);
            if(!isSuccess(resp)){
                log.error("纬狮查询渠道失败,请求参数：{}，仓库,:{},返回结果：{}", JSONUtil.toJsonStr(authMap),platformWarehouseCode, JSONUtil.toJsonStr(resp));
                return ApiResult.error(-1, "纬狮查询渠道失败,"+resp.getMsg());
            }
            List<WeiShiChannelResp> rowsDTOList = resp.getData();
            weiShiCancelInboundRequest.setIsMultiPackage(1);
            WeiShiBaseResp<List<WeiShiChannelResp>> resp2 = weiShiService.getLogisticProductList(weiShiCancelInboundRequest,authMap);
            if(!isSuccess(resp2)){
                log.error("纬狮查询渠道失败,请求参数：{}，仓库,:{},返回结果：{}", JSONUtil.toJsonStr(authMap),platformWarehouseCode, JSONUtil.toJsonStr(resp2));
                return ApiResult.error(-1, "纬狮查询渠道失败,"+resp2.getMsg());
            }
            rowsDTOList.addAll(resp2.getData());
            for (WeiShiChannelResp rowsDTO : rowsDTOList) {
                LogisticsSaleChannelEntity logisticsSaleChannelEntity = new LogisticsSaleChannelEntity();
                logisticsSaleChannelEntity.setPlatformChannelId(rowsDTO.getProductCode());
                logisticsSaleChannelEntity.setCode(rowsDTO.getProductCode());
                logisticsSaleChannelEntity.setCnName(rowsDTO.getCnName());
                logisticsSaleChannelEntity.setEnName(rowsDTO.getEnName());
                logisticsSaleChannelEntity.setLogisticsPlatform(OmsPlatformEnum.WEI_SHI.getCode());
                logisticsSaleChannelEntity.setPlatformWarehouseCode(platformWarehouseCode);
                OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseEntityList.stream().filter(v->v.getPlatformWarehouseCode().equals(platformWarehouseCode) && StringUtils.isNotBlank(v.getWarehouseId())).findFirst().orElse(null);
                if(Objects.nonNull(overseasProviderWarehouseEntity)){
                    logisticsSaleChannelEntity.setOverseasWarehouseId(overseasProviderWarehouseEntity.getId());
                }
                response.add(logisticsSaleChannelEntity);
            }
        }
        return success(response);
    }

    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getCode, LogisticsPlatformEnum.WEI_SHI_WAREHOUSE)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list();
        //三方仓如果已授权，直接取三方仓授权信息
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream().filter(v->{
            Map<String,Object> warehouseAuthMap = v.getAuthJson();
            return warehouseAuthMap.containsKey("appKey") && warehouseAuthMap.get("appKey").toString().equals(authMap.get("appKey"));
        }).findFirst().orElse(null);
        if(Objects.nonNull(overseasProviderEntity)){
            Map<String,Object> warehouseAuthMap = overseasProviderEntity.getAuthJson();
            authMap.put("accessToken",warehouseAuthMap.get("accessToken").toString());
            logisticsAuthFieldService.saveOrUpdateAuthField(authMap.get("id"),authMap);
        }else{
            Map<String,Object> map = new HashMap<>();
            map.put("appKey",authMap.get("authMap"));
            WeiShiBaseResp<WeiShiTokenResp> authResp = weiShiService.accessToken(map);
            if(!isSuccess(authResp)){
                throw new ServiceException("授权失败,"+authResp.getMsg());
            }
            WeiShiTokenResp weiShiTokenResp = authResp.getData();
            authMap.put("accessToken",weiShiTokenResp.getAccessToken());
            logisticsAuthFieldService.saveOrUpdateAuthField(authMap.get("id"),authMap);
        }
        return success();
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.WEI_SHI_WAREHOUSE;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }

    public <T> boolean isSuccess(WeiShiBaseResp<T> resp){
        return resp.getCode()==200;
    }
}
