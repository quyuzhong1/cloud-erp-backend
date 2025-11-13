package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.ChannelTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.wms.iml.dto.ImlBaseResp;
import com.sdk.wms.iml.dto.response.ImlInventoryLogisticsProductsResp;
import com.sdk.wms.iml.dto.response.ImlLogisticChannelResp;
import com.sdk.wms.iml.dto.response.ImlResponse;
import com.sdk.wms.iml.service.ImlService;
import io.jsonwebtoken.lang.Collections;
import jnr.ffi.annotations.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

            ImlBaseResp<List<ImlLogisticChannelResp>> imlResponse = imlService.getShippingMethod();
            if (isFailure(imlResponse.getCode())) {
                logAndReturnFailure(chanelQueryVO, RequestStatusEnums.FAILED, imlResponse);
            }

            //封装仓库id
            List<String> warehouseCodeList =     // 使用Stream流提取所有非空的warehouseCode
                    imlResponse.getData().stream()
                    // 过滤掉null的ImlLogisticChannelResp对象
                    .filter(Objects::nonNull)
                    // 提取每个对象中的storeWarehouse列表
                    .map(ImlLogisticChannelResp::getStoreWarehouse)
                    // 过滤掉null的storeWarehouse列表
                    .filter(Objects::nonNull)
                    // 将List<StoreWarehouseDTO>扁平化为StoreWarehouseDTO流
                    .flatMap(List::stream)
                    // 过滤掉null的StoreWarehouseDTO对象
                    .filter(Objects::nonNull)
                    // 提取warehouseCode字段
                    .map(ImlLogisticChannelResp.StoreWarehouseDTO::getWarehouseCode)
                    // 过滤掉null的warehouseCode
                    .filter(Objects::nonNull)
                    // 收集结果为List<String>
                    .collect(Collectors.toList());

            List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntityList =
                    overseasWarehouseFeign.getOverseasWarehouseListByPlatformCodes(warehouseCodeList,getPlatForm().getCode());

            //实体转换
            List<LogisticsSaleChannelEntity> response = new ArrayList<>();
            List<ImlLogisticChannelResp> imlLogisticChannelResps = imlResponse.getData();
            for (ImlLogisticChannelResp imlLogisticChannelResp : imlLogisticChannelResps) {
                List<ImlLogisticChannelResp.StoreWarehouseDTO> storeWarehouse = imlLogisticChannelResp.getStoreWarehouse();
                if(Collections.isEmpty(storeWarehouse)){
                    LogisticsSaleChannelEntity logisticsSaleChannelEntity = new LogisticsSaleChannelEntity();
                    logisticsSaleChannelEntity.setPlatformChannelId(imlLogisticChannelResp.getProductCode());
                    logisticsSaleChannelEntity.setCode(imlLogisticChannelResp.getProductCode());
                    logisticsSaleChannelEntity.setCnName(imlLogisticChannelResp.getProductName());
                    logisticsSaleChannelEntity.setLogisticsPlatform(OmsPlatformEnum.OMS_IML.getCode());
                    logisticsSaleChannelEntity.setAging(Objects.nonNull(imlLogisticChannelResp.getPrescription())?String.valueOf(imlLogisticChannelResp.getPrescription()/24):"");
                    logisticsSaleChannelEntity.setChannelType("FIRST_TRANSPORT".equals(imlLogisticChannelResp.getProductType()) ? ChannelTypeEnum.FIRST_MILE.getCode() : ChannelTypeEnum.LAST_MILE.getCode());
                    response.add(logisticsSaleChannelEntity);
                    continue;
                }
                for (ImlLogisticChannelResp.StoreWarehouseDTO storeWarehouseDTO : storeWarehouse) {
                    LogisticsSaleChannelEntity logisticsSaleChannelEntity = new LogisticsSaleChannelEntity();
                    logisticsSaleChannelEntity.setPlatformChannelId(imlLogisticChannelResp.getProductCode());
                    logisticsSaleChannelEntity.setCode(imlLogisticChannelResp.getProductCode());
                    logisticsSaleChannelEntity.setCnName(imlLogisticChannelResp.getProductName());
                    logisticsSaleChannelEntity.setLogisticsPlatform(OmsPlatformEnum.OMS_IML.getCode());
                    logisticsSaleChannelEntity.setAging(Objects.nonNull(imlLogisticChannelResp.getPrescription())?String.valueOf(imlLogisticChannelResp.getPrescription()/24):"");
                    logisticsSaleChannelEntity.setPlatformWarehouseCode(storeWarehouseDTO.getWarehouseCode());
                    logisticsSaleChannelEntity.setChannelType("FIRST_TRANSPORT".equals(imlLogisticChannelResp.getProductType()) ? ChannelTypeEnum.FIRST_MILE.getCode() : ChannelTypeEnum.LAST_MILE.getCode());
                    OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseEntityList.stream().filter(v->v.getPlatformWarehouseCode().equals(storeWarehouseDTO.getWarehouseCode()) && StringUtils.isNotBlank(v.getWarehouseId())).findFirst().orElse(null);
                    if(Objects.nonNull(overseasProviderWarehouseEntity)){
                        logisticsSaleChannelEntity.setOverseasWarehouseId(overseasProviderWarehouseEntity.getId());
                    }
                    response.add(logisticsSaleChannelEntity);
                }
            }

            logAndReturnSuccess(chanelQueryVO, RequestStatusEnums.SUCCESS, imlResponse);

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
        try {
            Map<String, Object> authObjMap = authMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            ThirdWarehouseContext.setAuthMap(authObjMap);

            ImlBaseResp<List<ImlLogisticChannelResp>> imlResponse = imlService.getShippingMethod();
            if (isFailure(imlResponse.getCode())) {
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
                                     ImlBaseResp<?> imlResponse) {
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

    private boolean isFailure(Integer code) {
        return !code.equals(0);
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
