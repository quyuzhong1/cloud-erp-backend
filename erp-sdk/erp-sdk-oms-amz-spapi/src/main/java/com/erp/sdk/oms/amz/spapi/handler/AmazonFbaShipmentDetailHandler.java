package com.erp.sdk.oms.amz.spapi.handler;

import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractFbaShipmentDetailHandler;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfiguration;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfigurationOnRequests;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.convert.SdkFbaShipmentConverter;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFbaShipmentDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaQueryTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.GetShipmentItemsResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItem;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiRateLimitUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 亚马逊根据FBA货件明细更新时间处理器
 *
 * @author Jim
 * @since 2024-02-22
 **/
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.AMAZON)
@BusinessType(BusinessTypeEnum.FBA_SHIPMENT_DETAIL)
public class AmazonFbaShipmentDetailHandler extends AbstractFbaShipmentDetailHandler<PlatformAmazonFbaShipmentDTO, PlatformFbaShipmentDTO> {

    @Resource
    private DmpAmazonFeign dmpAmazonFeign;
    @Resource
    private AmazonSpApiRateLimitUtils amazonSpApiRateLimitUtils;
    @Resource
    private RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PlatformAmazonFbaShipmentDTO> download(JobTaskDTO data) {
        // 获取店铺信息
        String shopId = data.getShopId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.FBA_SHIPMENT_DETAIL;
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX, data.getGroupId());
        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);

        String rateLimitStr;
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        try {
            String queryType = AmazonFbaQueryTypeEnum.DATE_RANGE.getCode();
            String marketplaceId = marketPlaceEnum.getMarketplaceId();
            String lastUpdatedAfter = DateUtil.plus8SameUtcOffset(data.getLastTime()).toString();
            String lastUpdatedBefore = DateUtil.plus8SameUtcOffset(data.getNextTime()).toString();
            FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
            ApiResponse<GetShipmentItemsResponse> respWithHttpInfo = api.getShipmentItemsWithHttpInfo(queryType, marketplaceId, lastUpdatedAfter, lastUpdatedBefore, null);
            List<String> limitArray = respWithHttpInfo.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
            rateLimitStr = limitArray.get(0);
            GetShipmentItemsResponse itemList = respWithHttpInfo.getData();

            List<InboundShipmentItem> resultList = new LinkedList<>(itemList.getPayload().getItemData());
            String currentNextToken = itemList.getPayload().getNextToken();
            int currentSize = itemList.getPayload().getItemData().size();
            while (StringUtils.isNotBlank(currentNextToken) && currentSize == 100) {
                // 上一次请求的响应频率设置
                if (StringUtils.isNotBlank(rateLimitStr)){
                    RateLimitConfigurationOnRequests rateLimitConfigurationRequests = (RateLimitConfigurationOnRequests) rateLimitConfig;
                    rateLimitConfigurationRequests.setRateLimitPermit(Double.parseDouble(rateLimitStr));
                    api.getApiClient().setRateLimiter(rateLimitConfigurationRequests);
                }
                ApiResponse<GetShipmentItemsResponse> currentResp = api.getShipmentItemsWithHttpInfo(AmazonFbaQueryTypeEnum.DATE_RANGE.getCode(), marketplaceId, null, null, currentNextToken);
                resultList.addAll(currentResp.getData().getPayload().getItemData());
                // 亚马逊接口响应时间UTC转换8区
                currentNextToken = currentResp.getData().getPayload().getNextToken();
                currentSize = currentResp.getData().getPayload().getItemData().size();
                List<String> currentLimitArray = currentResp.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
                rateLimitStr = currentLimitArray.get(0);
            }
            if (StringUtils.isNotBlank(rateLimitStr)){
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
            // 按货件单号分组
            List<String> shipmentIdList = resultList.stream().map(InboundShipmentItem::getShipmentId).distinct().collect(Collectors.toList());

            // 返回下载源数据
            return shipmentIdList.stream()
                    .map(e -> new PlatformAmazonFbaShipmentDTO(e, shopInfoDTO.getId(), shopInfoDTO.getName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServiceException("[Amazon SP-APi] 下载FBA货件失败" + e);
        }
    }


    @Override
    public List<PlatformFbaShipmentDTO> convert(List<PlatformAmazonFbaShipmentDTO> sourceDataList) {
        // 亚马逊FBA货件转换为发送mq数据
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return SdkFbaShipmentConverter.INSTANCE.downloadDtoToSaveDtoList(sourceDataList);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }

    /**
     * 是否发送MQ
     * true=发送
     * false=不发送（有其他详情需要额外拉取）
     */
    public Boolean getIsSendMq() {
        return Boolean.FALSE;
    }
}
