package com.erp.server.dmp.inout.handler.input.task.init.api.damai;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.enums.B2BThirdDeliveryCancelResultEnum;
import com.erp.rpc.wms.feign.ThirdWarehouseFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.api.b2b.B2bThirdOutboundInitHandler;
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
 * 大卖B2B三方仓出库状态 init handler。
 * 大卖不支持按时间范围查询，这里按ERP参考号分批通过WMS Feign补查。
 */
@Service
@Scope("prototype")
public class DaMaiB2bThirdOutboundInitHandler extends B2bThirdOutboundInitHandler {

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
                    "查询大卖B2B三方仓出库单状态失败");
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

    private boolean isActionStatus(ThirdWarehouseQueryFbaOutboundResponse response) {
        if (Objects.isNull(response) || StringUtils.isBlank(response.getStatus())) {
            return false;
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
        result.put("abnormalProblemReason", response.getErrorType());
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
