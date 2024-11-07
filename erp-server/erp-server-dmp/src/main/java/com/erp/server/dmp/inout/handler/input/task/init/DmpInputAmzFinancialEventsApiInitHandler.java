package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.api.FinancesApi;
import com.erp.sdk.oms.amz.spapi.api.OrdersV0Api;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.finances.FinancialEvents;
import com.erp.sdk.oms.amz.spapi.model.finances.ListFinancialEventsResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFinancialEventsApiInitHandler extends DmpInputInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 获取店铺信息
        String shopId = nextLevelId;
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }

        // 校验当前是否429限流
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), shopInfoDTO.getPlatformShopCode(), "REFUND");
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            log.warn("【财务事件拉取】 PlatformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextChain(false);
            return Collections.emptyList();
        }

        // 开始时间
        LocalDateTime startTime = dmpInputTaskEntity.getStartTime();
        // 结束时间
        LocalDateTime endTime = dmpInputTaskEntity.getEndTime();

        String rateLimitStr = AmazonRequestTypeRateLimiterEnum.ORDER_LIST.getRateLimit();
        // 根据亚马逊的响应时间记录下次执行开始时间
        LocalDateTime nextStartTime;
        // 亚马逊订单下载
        OrdersV0Api api = AmazonSpApiInitUtils.create(OrdersV0Api.class, shopInfoDTO, false);
        // 正式环境请求
        String postedAfter = DateUtil.plus8SameUtcOffset(startTime).toString();
        String postedBefore = DateUtil.plus8SameUtcOffset(endTime).toString();

        Integer maxResultsPerPage = 100;
        try {
            // 请求全站点
            List<String> marketplaceIds = new ArrayList<>(shopInfoDTO.getMarketplaceShopIdMap().keySet());
            // 发起请求
            FinancesApi financesApi = AmazonSpApiInitUtils.create(FinancesApi.class, shopInfoDTO, false);
            ApiResponse<ListFinancialEventsResponse> respWithHttpInfo = financesApi.listFinancialEventsWithHttpInfo(maxResultsPerPage, postedAfter, postedBefore, null);

            List<String> limitArray = respWithHttpInfo.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
            rateLimitStr = limitArray.get(0);
            ListFinancialEventsResponse data = respWithHttpInfo.getData();

            List<FinancialEvents> financialEventList = new LinkedList<>();
            financialEventList.add(data.getPayload().getFinancialEvents());
            String currentNextToken = data.getPayload().getNextToken();
            while (StringUtils.isNotBlank(currentNextToken) ) {
                ApiResponse<ListFinancialEventsResponse> currentResp = financesApi.listFinancialEventsWithHttpInfo(null, null, null, null);
                ListFinancialEventsResponse currentData = currentResp.getData();
                currentNextToken = currentData.getPayload().getNextToken();
                financialEventList.add(currentData.getPayload().getFinancialEvents());
                List<String> currentLimitArray = respWithHttpInfo.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
                rateLimitStr = currentLimitArray.get(0);
            }
            List<JSONObject> curJsonList = financialEventList.stream().map(e -> fillDataAndToJsonObject(e, shopInfoDTO)).collect(Collectors.toList());

            // 返回下载源数据
            return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSONArray.toJSONString(curJsonList)));
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextChain(false);
                return Collections.emptyList();
            }
            throw new ServiceException("请求亚马逊SP-APi api 【财务事件拉取】异常失败,body=" + JSONUtil.toJsonStr(e));
        } catch (Exception e) {
            throw new ServiceException("请求亚马逊SP-APi 【财务事件拉取】失败,body=" + JSONUtil.toJsonStr(e));
        }
    }

    /**
     * 设置亚马逊账号和转换JSON
     */
    private JSONObject fillDataAndToJsonObject(FinancialEvents entity, AmazonShopInfoDTO shopInfoDTO) {
        JSONObject json = (JSONObject) JSON.toJSON(entity);
        json.put("platformShopCode", shopInfoDTO.getPlatformShopCode());
//        // 根据站点判断店铺ID
//        json.put("shopId", shopInfoDTO.getId());
//        json.put("shopName", shopInfoDTO.getName());
        return json;
    }

}
