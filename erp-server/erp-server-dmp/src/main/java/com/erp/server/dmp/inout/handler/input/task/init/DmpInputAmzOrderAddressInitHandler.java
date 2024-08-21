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
import com.erp.sdk.oms.amz.spapi.api.OrdersV0Api;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.handler.AmazonOrderHandler;
import com.erp.sdk.oms.amz.spapi.model.orders.Address;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzOrderAddressInitHandler extends DmpInputInitHandler {
    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private AmazonOrderHandler amazonOrderHandler;

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
        // 店铺ID
        String nextLevelId = dmpCfgInputDetailEntity.getNextLevelId();
        // 需要查询的店铺
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(nextLevelId);
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        List<ParamData> paramDataList = new ArrayList<>();
//		List<String> orderIdList = findMongoData.stream().map(f -> f.get("order_id").toString()).collect(Collectors.toList());
//		paramDataList.add(new ParamData("order_id", "order_id", PannoEnum.IN, orderIdList));
//		findMongoData = mongoService.findMongoData(paramDataList, "aliexpress_orderDetail_data");
        if (CollUtil.isEmpty(findMongoData)) {
            return new ArrayList<>();
        }

        List<JSONObject> addressJsonList = new LinkedList<>();
        for (Map<String, Object> mongoData : findMongoData) {
            String amazonOrderId = mongoData.get("amazonOrderId").toString();
            // 检查来源
            if (StringUtils.isBlank(amazonOrderId)) {
                String msg = StrUtil.format("订单来源ID:{}", JSONUtil.toJsonStr(mongoData));
                throw new ServiceException(msg);
            }
            String platformShopCode = shopInfoDTO.getPlatformShopCode();

            OrdersV0Api ordersVoApi = OrdersV0Api.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);

            // 生成RDT权限获取地址信息
            // amazon-rdt-token:店铺ID:订单ID
            String rdtToken = amazonOrderHandler.queryAndGetRDT(amazonOrderId, shopInfoDTO);

            // 修改x-amz-access-token的token
            ordersVoApi.getApiClient().addDefaultHeader(ApiClient.SIGNED_ACCESS_TOKEN_HEADER_NAME, rdtToken);

            // 设置地址
            Address shippingAddress = amazonOrderHandler.downloadAddress(platformShopCode, amazonOrderId, ordersVoApi);
            addressJsonList.add(setAmazonOrderIdAndToJsonObject(shippingAddress, amazonOrderId));
            // 缓存移除结果
//            String addressKey = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS.getBusinessTypeName(), uniqueId);
//            if (redisUtil.hasKey(addressKey)) {
//                redisUtil.del(addressKey);
//            }
        }

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(addressJsonList)));
    }

    /**
     * 设置亚马逊订单ID和转换JSON
     */
    private JSONObject setAmazonOrderIdAndToJsonObject(Address orderItem, String amazonOrderId) {
        JSONObject json = (JSONObject) JSON.toJSON(orderItem);
        json.put("amazonOrderId", amazonOrderId);
        return json;
    }

}
