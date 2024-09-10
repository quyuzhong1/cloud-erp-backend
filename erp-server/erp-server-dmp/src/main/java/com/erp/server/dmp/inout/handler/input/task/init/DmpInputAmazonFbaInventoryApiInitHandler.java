package com.erp.server.dmp.inout.handler.input.task.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.api.FbaInventoryApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.fbainventory.InventorySummary;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 * 亚马逊FBA库存
 *
 * @author Jim
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmazonFbaInventoryApiInitHandler extends DmpInputInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String shopId = dmpCfgInputDetailEntity.getNextLevelId();
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        // 根据明细类型扩展
        String extendJson = dmpCfgInputDetailEntity.getExtendJson();
        // 查询所有
        boolean hasAll = false;
        if(StringUtils.isNotBlank(extendJson)) {
            JSONObject parseObject = JSON.parseObject(extendJson);
            if(parseObject != null) {
                hasAll = parseObject.getBooleanValue("hasAll");
            }
        }

        // 目前仅支持市场类型
        String granularityType = "Marketplace";
        // 目前仅支持单个市场ID
        String granularityId = marketplaceEnum.getMarketplaceId();
        List<String> marketplaceIds = Collections.singletonList(marketplaceEnum.getMarketplaceId());
        // 是否包含明细
        Boolean details = true;
        // 数据开始时间(空=全量)
        OffsetDateTime startDateTime = null == dmpInputTaskEntity.getStartTime() || hasAll ? null : DateUtil.plus8SameUtcOffset(dmpInputTaskEntity.getStartTime());
        // Sku列表
        List<String> sellerSkus = null;
        FbaInventoryApi api = AmazonSpApiInitUtils.create(FbaInventoryApi.class, shopInfoDTO, false);

        try {
            List<InventorySummary> allList = api.getAllInventorySummaries(granularityType, granularityId, marketplaceIds, details, startDateTime, sellerSkus);
            // 拼接来源信息
            List<JSONObject> resultList = allList.stream().map(e -> setAmazonOrderIdAndToJsonObject(e, shopInfoDTO, marketplaceEnum)).collect(Collectors.toList());
            // 组合响应
            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(JSON.toJSONString(resultList));
            return Collections.singletonList(dmpInputTaskInitDTO);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 设置亚马逊店铺信息和转换JSON
     */
    private JSONObject setAmazonOrderIdAndToJsonObject(InventorySummary sourceEntity, AmazonShopInfoDTO shopInfoDTO, AmazonMarketplaceEnum marketplaceEnum) {
        JSONObject json = (JSONObject) JSON.toJSON(sourceEntity);
        json.put("marketplaceId", marketplaceEnum.getMarketplaceId());
        json.put("platformShopCode", shopInfoDTO.getPlatformShopCode());
        if (null != sourceEntity.getLastUpdatedTime()) {
            LocalDateTime updateTime = DateUtil.utcSamePlus8(sourceEntity.getLastUpdatedTime().toLocalDateTime());
            json.put("lastPlatformUpdateTime", updateTime);
        }
        return json;
    }


}
