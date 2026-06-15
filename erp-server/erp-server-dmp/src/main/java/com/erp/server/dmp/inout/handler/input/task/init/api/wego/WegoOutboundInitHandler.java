package com.erp.server.dmp.inout.handler.input.task.init.api.wego;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.WegoOutboundQueryPageDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.wms.feign.ThirdWarehouseDeliveryFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.wms.wego.dto.response.WegoOutboundResp;
import com.sdk.wms.wego.service.WegoOpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DMP 输入 init 任务处理器：WEGO 2C 出库单状态轮询。
 * 按 so_b2c.shipping_order_no（WEGO 出库单号）调用 2c.order.queryPage noList 模式拉取最新状态。
 */
@Slf4j
@Service
@Scope("prototype")
public class WegoOutboundInitHandler extends DmpInputInitHandler {

    private static final String AUTH_KEY_APP_TOKEN = "appToken";
    private static final String AUTH_KEY_APP_SECRET = "appSecret";
    private static final int BATCH_SIZE = WegoOutboundQueryPageDTO.MAX_PAGE_SIZE;
    private static final int MAX_PAGE_LIMIT = 100;

    @Resource
    private WegoOpenApiService wegoOpenApiService;
    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private ThirdWarehouseDeliveryFeign thirdWarehouseDeliveryFeign;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<OverseasProviderEntity> providerList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.WEGO.getCode())
                .list();
        if (CollUtil.isEmpty(providerList)) {
            throw new ServiceException("WEGO授权信息不存在");
        }
        OverseasProviderEntity provider = providerList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst().orElse(null);
        if (provider == null) {
            throw new ServiceException("WEGO对应授权ID信息不存在, nextId:" + dmpInputTaskEntity.getNextLevelId());
        }
        Map<String, Object> authJson = provider.getAuthJson();
        if (authJson == null || authJson.isEmpty()) {
            throw new ServiceException("WEGO出库：服务商[" + provider.getId() + "]auth_json为空");
        }
        String appToken = toStr(authJson.get(AUTH_KEY_APP_TOKEN));
        String appSecret = toStr(authJson.get(AUTH_KEY_APP_SECRET));
        if (StringUtils.isAnyBlank(appToken, appSecret)) {
            throw new ServiceException("WEGO出库：服务商[" + provider.getId() + "]appToken/appSecret缺失");
        }
        String authId = provider.getId();

        List<OverseasProviderWarehouseEntity> warehouseList = FeignQuery.create(OverseasProviderWarehouseEntity.class)
                .eq(OverseasProviderWarehouseEntity::getMainId, provider.getId()).list();
        warehouseList = warehouseList.stream()
                .filter(v -> !v.getDisabled() && StringUtils.isNotBlank(v.getWarehouseId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(warehouseList)) {
            log.warn("[WEGO出库] 服务商[id={}] 未配置启用的海外仓库", authId);
            return Collections.emptyList();
        }

        List<String> warehouseIds = warehouseList.stream()
                .map(OverseasProviderWarehouseEntity::getWarehouseId).collect(Collectors.toList());
        List<ThirdWarehouseDeliveryEntity> deliveryList = thirdWarehouseDeliveryFeign.listWaitShipByWarehouseIds(warehouseIds);
        if (CollectionUtils.isEmpty(deliveryList)) {
            return Collections.emptyList();
        }

        List<String> soIds = deliveryList.stream().map(ThirdWarehouseDeliveryEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soList = soB2cFeign.listByIds(soIds);
        if (CollectionUtils.isEmpty(soList)) {
            return Collections.emptyList();
        }
        Map<String, String> soIdToWegoNo = soList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getShippingOrderNo()))
                .collect(Collectors.toMap(SoB2cEntity::getId, SoB2cEntity::getShippingOrderNo, (a, b) -> a));
        if (soIdToWegoNo.isEmpty()) {
            log.info("[WEGO出库] 服务商[id={}] 待发货单均无 shippingOrderNo", authId);
            return Collections.emptyList();
        }

        List<String> wegoOrderNos = deliveryList.stream()
                .map(v -> soIdToWegoNo.get(v.getSoId()))
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());

        List<WegoOutboundResp.OutboundOrderDTO> allResult = new ArrayList<>();
        for (List<String> batch : ListUtils.partition(wegoOrderNos, BATCH_SIZE)) {
            allResult.addAll(fetchOrderPages(appToken, appSecret, batch, authId));
        }
        if (allResult.isEmpty()) {
            return Collections.emptyList();
        }

        JSONArray result = JSON.parseArray(JSONObject.toJSONString(allResult));
        result.forEach(item -> {
            JSONObject obj = (JSONObject) item;
            obj.put("authId", authId);
            obj.put("sourcePlatform", DmpBasicSystemCodeEnum.WEGO.getCode());
        });
        log.info("[WEGO出库] 服务商[id={}] 共拉取={}条", authId, result.size());
        DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
        dto.setMsg(result.toJSONString());
        return Collections.singletonList(dto);
    }

    private List<WegoOutboundResp.OutboundOrderDTO> fetchOrderPages(String appToken, String appSecret,
                                                                      List<String> noList, String authId) {
        List<WegoOutboundResp.OutboundOrderDTO> orderList = new ArrayList<>();
        int pageNum = 1;
        Integer totalPages = null;
        while (pageNum <= MAX_PAGE_LIMIT) {
            WegoOutboundQueryPageDTO.QueryReqDTO req = new WegoOutboundQueryPageDTO.QueryReqDTO();
            req.setAccessToken(appToken);
            req.setSecret(appSecret);
            req.setNoList(noList);
            req.setPageNum(pageNum);
            req.setPageSize(BATCH_SIZE);
            WegoOutboundResp resp;
            try {
                resp = wegoOpenApiService.query2cOrderPage(req);
            } catch (Exception e) {
                log.error("[WEGO出库] 服务商[id={}] query2cOrderPage 异常, pageNum={}", authId, pageNum, e);
                break;
            }
            if (resp == null || resp.getResult() == null) { break; }
            WegoOutboundResp.PageResultDTO pageResult = resp.getResult();
            List<WegoOutboundResp.OutboundOrderDTO> list = pageResult.getList();
            if (CollUtil.isNotEmpty(list)) { orderList.addAll(list); }
            if (totalPages == null) { totalPages = pageResult.getPages(); }
            if (Boolean.TRUE.equals(pageResult.getEmptyFlag()) || CollUtil.isEmpty(list)
                    || (totalPages != null && pageNum >= totalPages)) { break; }
            pageNum++;
        }
        log.info("[WEGO出库] 服务商[id={}] noList={}条 拉取={}条 翻页={}", authId, noList.size(), orderList.size(), pageNum);
        return orderList;
    }

    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }
}