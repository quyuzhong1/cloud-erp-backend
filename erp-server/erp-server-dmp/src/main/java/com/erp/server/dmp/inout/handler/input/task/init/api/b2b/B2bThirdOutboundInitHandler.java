package com.erp.server.dmp.inout.handler.input.task.init.api.b2b;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.enums.B2BThirdDeliveryCancelResultEnum;
import com.erp.model.wms.enums.ThirdDeliveryStatusEnum;
import com.erp.rpc.wms.feign.ThirdWarehouseFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * B2B三方仓出库状态 DMP init 拉取处理器
 */
@Service
@Scope("prototype")
public class B2bThirdOutboundInitHandler extends DmpInputInitHandler {

    @Resource
    private ThirdWarehouseFeign thirdWarehouseFeign;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<OverseasProviderWarehouseEntity> providerWarehouseList = queryProviderWarehouseList();
        if (CollUtil.isEmpty(providerWarehouseList)) {
            return Collections.emptyList();
        }
        List<B2bThirdDeliveryEntity> deliveryList = queryB2bThirdDeliveryList(providerWarehouseList);
        if (CollUtil.isEmpty(deliveryList)) {
            return Collections.emptyList();
        }
        List<JSONObject> resultList = new ArrayList<>();
        List<String> codeList = deliveryList.stream()
                .map(B2bThirdDeliveryEntity::getCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        for (List<String> subCodeList : ListUtils.partition(codeList, 100)) {
            ThirdWarehouseQueryFbaOutboundReq req = new ThirdWarehouseQueryFbaOutboundReq();
            req.setAuthId(dmpInputTaskEntity.getNextLevelId());
            req.setThirdWarehouseProvideCode(dmpBasicSystemEntity.getCode());
            req.setErpOrderCodeList(subCodeList);
            List<ThirdWarehouseQueryFbaOutboundResponse> responseList = OptionalResult.unwrap(
                    thirdWarehouseFeign.queryFbaOutboundBill(req),
                    "查询B2B三方仓出库单状态失败");
            if (CollUtil.isEmpty(responseList)) {
                continue;
            }
            responseList.stream()
                    .filter(this::isActionStatus)
                    .map(this::toResult)
                    .forEach(resultList::add);
        }
        if (CollUtil.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
        dto.setMsg(JSON.toJSONString(resultList));
        return Collections.singletonList(dto);
    }

    protected List<OverseasProviderWarehouseEntity> queryProviderWarehouseList() {
        List<OverseasProviderWarehouseEntity> providerWarehouseList = com.common.business.wrapper.FeignQuery.create(OverseasProviderWarehouseEntity.class)
                .eq(OverseasProviderWarehouseEntity::getMainId, dmpInputTaskEntity.getNextLevelId())
                .eq(OverseasProviderWarehouseEntity::getDisabled, Boolean.FALSE)
                .list();
        return providerWarehouseList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getWarehouseId()))
                .collect(Collectors.toList());
    }

    protected List<B2bThirdDeliveryEntity> queryB2bThirdDeliveryList(List<OverseasProviderWarehouseEntity> providerWarehouseList) {
        List<String> warehouseIds = providerWarehouseList.stream()
                .map(OverseasProviderWarehouseEntity::getWarehouseId)
                .distinct()
                .collect(Collectors.toList());
        List<String> statusList = Arrays.asList(
                ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode(),
                ThirdDeliveryStatusEnum.INTERCEPTING.getCode(),
                ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode()
        );
        return com.common.business.wrapper.FeignQuery.create(B2bThirdDeliveryEntity.class)
                .eq(B2bThirdDeliveryEntity::getIsApiDelivery, Boolean.TRUE)
                .in(B2bThirdDeliveryEntity::getDeliveryWarehouseId, warehouseIds)
                .in(B2bThirdDeliveryEntity::getStatus, statusList)
                .list();
    }

    private boolean isActionStatus(ThirdWarehouseQueryFbaOutboundResponse response) {
        if (Objects.isNull(response) || StringUtils.isBlank(response.getStatus())) {
            return false;
        }
        if (isZhongBaoProvider()) {
            return Arrays.asList("-1", "-2", "5").contains(response.getStatus());
        }
        B2BThirdDeliveryCancelResultEnum statusEnum = B2BThirdDeliveryCancelResultEnum.getByCode(response.getStatus());
        return Objects.nonNull(statusEnum)
                && !Arrays.asList(
                B2BThirdDeliveryCancelResultEnum.NEW,
                B2BThirdDeliveryCancelResultEnum.SUBMIT,
                B2BThirdDeliveryCancelResultEnum.PROCESSED,
                B2BThirdDeliveryCancelResultEnum.WAIT_UPLOAD,
                B2BThirdDeliveryCancelResultEnum.UPLOADED
        ).contains(statusEnum);
    }

    private JSONObject toResult(ThirdWarehouseQueryFbaOutboundResponse response) {
        JSONObject result = new JSONObject();
        result.put("sourcePlatform", dmpBasicSystemEntity.getCode());
        result.put("orderCode", response.getPlatformOrderCode());
        result.put("referenceNo", response.getCode());
        result.put("orderStatus", response.getStatus());
        result.put("trackingNo", response.getTrackNo());
        result.put("abnormalProblemReason", isZhongBaoProvider() ? response.getErrorReason() : response.getErrorType());
        result.put("dateShippingStr", response.getDeliveryTimeStr());
        result.put("platformCreateTimeStr", response.getPlatformCreateTimeStr());
        result.put("platformUpdateTimeStr", response.getPlatformUpdateTimeStr());
        result.put("swOrderNumber", response.getSwOrderNumber());
        result.put("warehouseCode", response.getWarehouseCode());
        result.put("shippingMethod", response.getShippingMethod());
        result.put("carrierName", response.getCarrierName());
        result.put("orderType", StringUtils.defaultIfBlank(response.getOrderType(), "B2B"));
        return result;
    }

    private boolean isZhongBaoProvider() {
        return Objects.nonNull(dmpBasicSystemEntity)
                && StringUtils.equalsIgnoreCase("zhongbao", dmpBasicSystemEntity.getCode());
    }

    /**
     * 局部封装，避免在handler里散落重复校验
     */
    private static final class OptionalResult {
        private OptionalResult() {
        }

        private static <T> T unwrap(com.common.core.controller.vo.ApiResult<T> result, String errorMsg) {
            if (Objects.isNull(result)) {
                throw new ServiceException(errorMsg + "，响应为空");
            }
            if (!result.isSuccess()) {
                throw new ServiceException(errorMsg + "，" + result.getMsg());
            }
            return result.getData();
        }
    }
}
