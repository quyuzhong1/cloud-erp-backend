package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.api.ListingsApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonIdentifiersTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.listingsitems.Item;
import com.erp.sdk.oms.amz.spapi.model.listingsitems.ItemSearchResults;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzListingInitHandler extends DmpInputAmzCommonInitHandler {
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CfgAppClientService cfgAppClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 获取店铺信息
        String shopId = nextLevelId;
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }

        // 是否创建报告
        String taskExtendJson = dmpInputTaskEntity.getExtendJson();
        JSONArray platformSkuNoList = new JSONArray();
        if (StringUtils.isNotBlank(taskExtendJson)) {
            JSONObject taskExtendObj = JSONObject.parseObject(taskExtendJson);
            platformSkuNoList = taskExtendObj.getJSONArray("platformSkuNoList");
        }

        // 校验当前是否429限流
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), shopInfoDTO.getDictCountryCode(), BusinessTypeEnum.PRODUCT_LISTING.getCode());
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            log.warn("【订单拉取】 PlatformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.PRODUCT_LISTING;
        String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
        ListingsApi api = AmazonSpApiInitUtils.create(ListingsApi.class, shopInfoDTO, false);
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        List<Item> curItems = null;
        try {
            String sellerId = shopInfoDTO.getPlatformShopCode();
            List<String> marketplaceIds = Collections.singletonList(marketPlaceEnum.getMarketplaceId());
            List<String> identifiers = platformSkuNoList.stream().map(Object::toString).collect(Collectors.toList());
            String identifiersType = (AmazonIdentifiersTypeEnum.SKU.getCode());
            // 查询订单详情
            ItemSearchResults response = api.searchListingsItems(sellerId, marketplaceIds, null, null,
                    identifiers, identifiersType, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null);
            curItems = response.getItems();
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                log.warn("【DmpInputAmzListingInitHandler】查询亚马逊订单详情本次首次429限流:{}", shopInfoDTO.getPlatformShopCode());
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextStatus(false);
                return Collections.emptyList();
            }
            throw new ServiceException("查询亚马逊订单详情失败：API异常：" + JSONUtil.toJsonStr(e));
        } catch (Exception e) {
            throw new ServiceException("查询亚马逊订单详情失败：" + JSONUtil.toJsonStr(e));
        }
        if (CollUtil.isEmpty(curItems)) {
            return dmpInputTaskInitDTOList;
        }
        List<JSONObject> curJsonList = curItems.stream().map(e -> setAmazonOrderIdAndToJsonObject(e,shopInfoDTO.getPlatformShopCode(), shopId)).collect(Collectors.toList());
        dmpInputTaskInitDTOList.add(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(curJsonList)));
        return dmpInputTaskInitDTOList;
    }

    /**
     * 设置亚马逊订单ID和转换JSON
     */
    private JSONObject setAmazonOrderIdAndToJsonObject(Item item,  String platformShopCode, String currentShopId) {
        JSONObject json = (JSONObject) JSON.toJSON(item);
        json.put("platformShopCode", platformShopCode);
        json.put("shopId", currentShopId);
        return json;
    }

}
