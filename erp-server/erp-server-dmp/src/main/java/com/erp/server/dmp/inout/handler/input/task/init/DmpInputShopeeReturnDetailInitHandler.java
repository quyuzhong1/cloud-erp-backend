package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.CfgAppClientService;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.order.request.OrderRequest;
import com.sdk.oms.shopee.service.ShopeeReturnService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Shopee 售后退货明细 init：读取父任务 mongo 中的 return_sn，调用 v2.returns.get_return_detail。
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputShopeeReturnDetailInitHandler extends DmpInputInitHandler {

    private static final int RETURN_SOLUTION_RETURN_AND_REFUND = 0;
    private static final int MAX_RETRY = 10;
    private static final String SHOPEE_RETURN_LIST_DATA = "Shopee_returnList_data";

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private ShopeeReturnService shopeeReturnService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> parentMongoData = loadParentMongoData();
        if (CollUtil.isEmpty(parentMongoData)) {
            return new ArrayList<>();
        }

        List<String> returnSnList = parentMongoData.stream()
                .map(item -> Objects.toString(item.get("return_sn"), ""))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(returnSnList)) {
            return new ArrayList<>();
        }

        String shopId = Objects.toString(parentMongoData.get(0).get("nextLevelId"), nextLevelId);
        CfgAppClientEntity cfgAppClientEntity = loadShopeeAppClient();
        ShopAuthEntity shopAuthEntity = loadShopAuth(shopId);

        OrderRequest orderRequest = OrderRequest.builder()
                .host(cfgAppClientEntity.getUrl())
                .offset(0)
                .token(shopAuthEntity.getAccessToken())
                .shopId(Long.parseLong(shopAuthEntity.getShopeeId()))
                .partnerId(Long.parseLong(cfgAppClientEntity.getClientId()))
                .tmpPartnerKey(cfgAppClientEntity.getClientSecret())
                .build();

        JSONArray detailList = new JSONArray();
        for (String returnSn : returnSnList) {
            orderRequest.setOrderSns(returnSn);
            ShopeeResponse response = executeWithRetry(orderRequest, "退货明细");
            JSONObject detail = response.getResponse();
            if (detail == null || !isReturnAndRefund(detail)) {
                continue;
            }
            detailList.add(detail);
        }

        DmpInputTaskInitDTO initDTO = new DmpInputTaskInitDTO();
        initDTO.setMsg(JSONUtil.toJsonStr(detailList));
        List<DmpInputTaskInitDTO> result = new ArrayList<>();
        result.add(initDTO);
        return result;
    }

    private List<Map<String, Object>> loadParentMongoData() {
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isBlank(parentStorageName)) {
            parentStorageName = SHOPEE_RETURN_LIST_DATA;
        }
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID,
                DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        return mongoService.findMongoData(paramDataList, parentStorageName);
    }

    private boolean isReturnAndRefund(JSONObject returnDetail) {
        Object returnSolution = returnDetail.get("return_solution");
        if (returnSolution == null) {
            return false;
        }
        return RETURN_SOLUTION_RETURN_AND_REFUND == Integer.parseInt(String.valueOf(returnSolution));
    }

    private ShopeeResponse executeWithRetry(OrderRequest orderRequest, String apiName) {
        ShopeeResponse response = null;
        long sleepTime = 1000;
        int count = 0;
        while (response == null) {
            response = execute(orderRequest);
            if (response != null) {
                break;
            }
            if (count >= MAX_RETRY) {
                throw new ServiceException("调用shopee" + apiName + "接口重试" + count + "失败");
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException("调用shopee" + apiName + "接口被中断");
            }
            sleepTime += 1000;
            count++;
        }
        return response;
    }

    private ShopeeResponse execute(OrderRequest orderRequest) {
        try {
            ShopeeResponse response = shopeeReturnService.getReturnDetail(orderRequest);
            if (response != null && StringUtils.isNotBlank(response.getError())) {
                throw new ServiceException("调用shopee退货明细接口报错，错误原因：" + response.getMessage());
            }
            return response;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof SSLHandshakeException || cause instanceof SocketTimeoutException) {
                return null;
            }
            throw new ServiceException("调用shopee退货明细接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
        }
    }

    private CfgAppClientEntity loadShopeeAppClient() {
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        List<CfgAppClientEntity> cfgAppClientEntityList = cfgAppClientService.lambdaQuery()
                .eq(CfgAppClientEntity::getBusinessType, appClientEnum.getBusinessType())
                .eq(CfgAppClientEntity::getDictPlatform, appClientEnum.getPlatform())
                .eq(CfgAppClientEntity::getPlatformType, appClientEnum.getPlatformType())
                .list();
        if (CollUtil.isEmpty(cfgAppClientEntityList)) {
            throw new ServiceException("shopee应用未配置");
        }
        return cfgAppClientEntityList.get(0);
    }

    private ShopAuthEntity loadShopAuth(String shopId) {
        List<ShopAuthEntity> shopAuthEntityList = FeignQuery.create(ShopAuthEntity.class)
                .eq(ShopAuthEntity::getShopId, shopId)
                .list();
        if (CollUtil.isEmpty(shopAuthEntityList)) {
            throw new ServiceException("shopee授权未配置");
        }
        return shopAuthEntityList.get(0);
    }
}
