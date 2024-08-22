package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.GetShipmentItemsResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItem;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItemList;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFbaShipmentApiInitHandler extends DmpInputInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> findMongoData = null;
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isNotBlank(parentStorageName)) {
            List<ParamData> paramDataList = new ArrayList<>();
            paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
            findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
        }
        if (CollUtil.isEmpty(findMongoData)) {
            return new ArrayList<>();
        }
        String nextLevelId = dmpCfgInputDetailEntity.getNextLevelId();
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(nextLevelId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + nextLevelId);
        }

        List<JSONObject> allItemList = new LinkedList<>();
        for (Map<String, Object> findMongo : findMongoData) {
            String fbaShipmentId = findMongo.get("fbaShipmentId").toString();
            // 检查来源
            if (StringUtils.isBlank(fbaShipmentId)) {
                String msg = StrUtil.format("货件ID为空:{}", JSONUtil.toJsonStr(findMongo));
                throw new ServiceException(msg);
            }
            // 缓存获取结果
            String amazonOrderIdResultKey = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.ORDER_ITEMS.getBusinessTypeName(), fbaShipmentId);
            Object resultObj = redisUtil.get(amazonOrderIdResultKey);
            if (null != resultObj) {
                List<JSONObject> curItemList = JSONUtil.toList(resultObj.toString(), JSONObject.class);
                allItemList.addAll(curItemList);
                continue;
            }

            AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.FBA_SHIPMENT_DETAIL;
            // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
            String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), shopInfoDTO.getPlatformShopCode(), requestTypeRateLimiterEnum.getBusinessTypeName());
            // 默认请求速率配置
            // 获取动态速率
            Object limitObj = redisUtil.get(limitKey);
            if (null != limitObj) {
                String msg = StrUtil.format("【FBA明细拉取】 fbaShipmentId={}, platformShopCode={},存在429等待恢复:放弃当前请求任务", fbaShipmentId, shopInfoDTO.getPlatformShopCode());
                throw new ServiceException(msg);
            }
            String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();

            AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
            try {
                // 查询FBA货件item
                FbaInboundApi api = FbaInboundApi.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false);
                GetShipmentItemsResponse response = api.getShipmentItemsByShipmentId(fbaShipmentId, marketPlaceEnum.getMarketplaceId());
                InboundShipmentItemList curItemData = response.getPayload().getItemData();
                if (CollectionUtils.isEmpty(curItemData)) {
                    continue;
                }
                List<JSONObject> curJsonList = curItemData.stream().map(e -> setFbaShipmentIdAndToJsonObject(e, fbaShipmentId)).collect(Collectors.toList());
                allItemList.addAll(curJsonList);
            } catch (ApiException e) {
                if (429 == e.getCode()) {
                    // 设置动态速率，失效时间=1/limit
                    BigDecimal timeOut = BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN);
                    redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                }
                throw new ServiceException("查询亚马逊订单详情失败：API异常：" + JSONUtil.toJsonStr(e));
            } catch (Exception e) {
                throw new ServiceException("查询亚马逊订单详情失败：" + JSONUtil.toJsonStr(e));
            }
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(allItemList)));
    }

    /**
     * 设置亚马逊货件ID和转换JSON
     */
    private JSONObject setFbaShipmentIdAndToJsonObject(InboundShipmentItem item, String fbaShipmentId) {
        JSONObject json = (JSONObject) JSON.toJSON(item);
        json.put("fbaShipmentId", fbaShipmentId);
        return json;
    }
}
