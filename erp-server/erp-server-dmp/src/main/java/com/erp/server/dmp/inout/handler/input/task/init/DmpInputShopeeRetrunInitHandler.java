package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Shopee 售后列表 init：按更新时间拉取 v2.returns.get_return_list，保留 return_solution=0/1。
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputShopeeRetrunInitHandler extends DmpInputInitHandler {

    private static final int RETURN_SOLUTION_RETURN_AND_REFUND = 0;
    private static final int RETURN_SOLUTION_REFUND_ONLY = 1;
    private static final int MAX_RETRY = 10;

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private ShopeeReturnService shopeeReturnService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        CfgAppClientEntity cfgAppClientEntity = loadShopeeAppClient();
        ShopAuthEntity shopAuthEntity = loadShopAuth(nextLevelId);

        long timeFrom = Timestamp.valueOf(dmpInputTaskEntity.getStartTime()).getTime() / 1000;
        long timeTo = Timestamp.valueOf(dmpInputTaskEntity.getEndTime()).getTime() / 1000;
        OrderRequest orderRequest = OrderRequest.builder()
                .host(cfgAppClientEntity.getUrl())
                .offset(0)
                .token(shopAuthEntity.getAccessToken())
                .shopId(Long.parseLong(shopAuthEntity.getShopeeId()))
                .partnerId(Long.parseLong(cfgAppClientEntity.getClientId()))
                .tmpPartnerKey(cfgAppClientEntity.getClientSecret())
                .timeFrom(timeFrom)
                .timeTo(timeTo)
                .cursor("")
                .build();

        JSONArray returnItems = new JSONArray();
        boolean hasNextPage = true;
        while (hasNextPage) {
            ShopeeResponse response = executeWithRetry(orderRequest, "退货列表");
            JSONObject result = response.getResponse();
            hasNextPage = Boolean.TRUE.equals(result.getBool("more"));
            if (hasNextPage) {
                orderRequest.setCursor(result.getStr("next_cursor"));
            }
            JSONArray pageReturns = result.getJSONArray("return");
            if (CollUtil.isEmpty(pageReturns)) {
                continue;
            }
            for (int i = 0; i < pageReturns.size(); i++) {
                JSONObject returnItem = pageReturns.getJSONObject(i);
                if (isSupportedReturnSolution(returnItem)) {
                    returnItems.add(returnItem);
                }
            }
        }

        DmpInputTaskInitDTO initDTO = new DmpInputTaskInitDTO();
        initDTO.setMsg(returnItems.toString());
        List<DmpInputTaskInitDTO> result = new ArrayList<>();
        result.add(initDTO);
        return result;
    }

    private boolean isSupportedReturnSolution(JSONObject returnItem) {
        if (returnItem == null) {
            return false;
        }
        Object returnSolution = returnItem.get("return_solution");
        if (returnSolution == null) {
            return false;
        }
        int solution = Integer.parseInt(String.valueOf(returnSolution));
        return RETURN_SOLUTION_RETURN_AND_REFUND == solution || RETURN_SOLUTION_REFUND_ONLY == solution;
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
            ShopeeResponse response = shopeeReturnService.getReturnList(orderRequest);
            if (response != null && StringUtils.isNotBlank(response.getError())) {
                throw new ServiceException("调用shopee退货列表接口报错，错误原因：" + response.getMessage());
            }
            return response;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof SSLHandshakeException || cause instanceof SocketTimeoutException) {
                return null;
            }
            throw new ServiceException("调用shopee退货列表接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
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
