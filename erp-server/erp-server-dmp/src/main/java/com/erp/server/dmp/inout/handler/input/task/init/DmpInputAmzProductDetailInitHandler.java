package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.dto.ReportListingMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.handler.AmazonListingHandler;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzProductDetailInitHandler extends DmpInputInitHandler {

    @Resource
    private AmazonListingHandler amazonListingHandler;
    @Resource
    private CfgAppClientService cfgAppClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 当前任务
        String shopId = dmpInputTaskEntity.getNextLevelId();
        String extendJson = dmpInputTaskEntity.getExtendJson();
        String lasMongoId = "";
        int size = 20;
        if (StringUtils.isNotBlank(extendJson)) {
            JSONObject jsonObject = JSON.parseObject(extendJson);
            lasMongoId = jsonObject.getString("lastMongoId");
            size = jsonObject.getInteger("size");
        }

        List<ReportListingMongoDTO> findMongoData = null;
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isNotBlank(parentStorageName)) {
            List<ParamData> paramDataList = new ArrayList<>();
            // 跳过不支持类型
            paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
//            findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
        }
        if (CollUtil.isEmpty(findMongoData)) {
            log.warn("[拉取亚马逊商品详情任务] 无需要执行的详情,platform={}", JSONUtil.toJsonStr(shopId));
            return new ArrayList<>();
        }

        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.PRODUCT_ITEMS;
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        // 动态请求配置
        // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
        String redissonKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), shopInfoDTO.getPlatformShopCode(), requestTypeRateLimiterEnum.getBusinessTypeName());
        JSONObject extentJsonObj = requestTypeRateLimiterEnum.getExtentJsonObj();
        extentJsonObj.put(AmazonRequestTypeRateLimiterEnum.limitKey, redissonKey);

        // 根据IdentifiersType分组查询
        List<JSONObject> itemsJsonList = amazonListingHandler.newDownloadDetailListByIdentifiersType(findMongoData, extentJsonObj, shopInfoDTO, marketPlaceEnum, size);

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(itemsJsonList)));
    }


}
