package com.erp.server.dmp.inout.handler.input.task.init.api.zhongbao;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundPageReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundPageResponse;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.wms.feign.ThirdWarehouseFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 众包B2B三方仓出库状态 init handler。
 * 众包支持按时间范围分页查询，DMP通过WMS Feign转发拉取。
 */
@Service
@Scope("prototype")
public class ZhongBaoB2bThirdOutboundInitHandler extends DmpInputInitHandler {

    private static final DateTimeFormatter QUERY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int PAGE_SIZE = 100;

    @Resource
    private DmpHandlerCache dmpHandlerCache;

    @Resource
    private ThirdWarehouseFeign thirdWarehouseFeign;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        OverseasProviderEntity providerEntity = getProviderEntity();
        List<JSONObject> resultList = new ArrayList<>();
        int pageNum = 1;
        int totalPage = 1;
        do {
            ThirdWarehouseQueryFbaOutboundPageReq req = buildQueryReq(providerEntity.getId(), pageNum);
            ThirdWarehouseQueryFbaOutboundPageResponse pageResponse = OptionalResult.unwrap(
                    thirdWarehouseFeign.queryFbaOutboundBillPage(req),
                    "查询众包B2B出库单状态失败");
            if (Objects.isNull(pageResponse) || CollUtil.isEmpty(pageResponse.getList())) {
                break;
            }
            pageResponse.getList().stream()
                    .filter(this::isActionStatus)
                    .map(this::toResult)
                    .forEach(resultList::add);
            totalPage = parseTotalPage(pageResponse.getTotalPage());
            pageNum++;
        } while (pageNum <= totalPage);
        if (CollUtil.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
        dto.setMsg(JSON.toJSONString(resultList));
        return Collections.singletonList(dto);
    }

    private OverseasProviderEntity getProviderEntity() {
        List<OverseasProviderEntity> providerEntityList = dmpHandlerCache.getOverseasProviderEntityList(item ->
                StringUtils.equalsIgnoreCase(item.getCode(), DmpBasicSystemCodeEnum.ZHONG_BAO.getCode()));
        if (CollUtil.isEmpty(providerEntityList)) {
            throw new ServiceException("众包授权信息不存在");
        }
        OverseasProviderEntity providerEntity = providerEntityList.stream()
                .filter(item -> StringUtils.equalsIgnoreCase(item.getId(), dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if (Objects.isNull(providerEntity)) {
            throw new ServiceException("众包对应授权ID信息不存在,nextId:" + dmpInputTaskEntity.getNextLevelId());
        }
        return providerEntity;
    }

    private ThirdWarehouseQueryFbaOutboundPageReq buildQueryReq(String authId, int pageNum) {
        ThirdWarehouseQueryFbaOutboundPageReq req = new ThirdWarehouseQueryFbaOutboundPageReq();
        req.setAuthId(authId);
        req.setThirdWarehouseProvideCode(DmpBasicSystemCodeEnum.ZHONG_BAO.getCode());
        req.setStartUpdateTime(formatQueryTime(dmpInputTaskEntity.getStartTime()));
        req.setEndUpdateTime(formatQueryTime(dmpInputTaskEntity.getEndTime()));
        req.setPageNum(pageNum);
        req.setPageSize(PAGE_SIZE);
        return req;
    }

    private String formatQueryTime(LocalDateTime time) {
        if (Objects.isNull(time)) {
            return null;
        }
        return QUERY_TIME_FORMATTER.format(time);
    }

    private int parseTotalPage(String totalPage) {
        if (StringUtils.isBlank(totalPage)) {
            return 1;
        }
        return Integer.parseInt(totalPage);
    }

    private boolean isActionStatus(ThirdWarehouseQueryFbaOutboundResponse response) {
        return Objects.nonNull(response)
                && StringUtils.isNotBlank(response.getStatus())
                && ("-1".equals(response.getStatus()) || "-2".equals(response.getStatus()) || "5".equals(response.getStatus()));
    }

    private JSONObject toResult(ThirdWarehouseQueryFbaOutboundResponse response) {
        JSONObject result = new JSONObject();
        result.put("sourcePlatform", dmpBasicSystemEntity.getCode());
        result.put("orderCode", response.getPlatformOrderCode());
        result.put("referenceNo", response.getCode());
        result.put("orderStatus", response.getStatus());
        result.put("trackingNo", response.getTrackNo());
        result.put("abnormalProblemReason", response.getErrorReason());
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
