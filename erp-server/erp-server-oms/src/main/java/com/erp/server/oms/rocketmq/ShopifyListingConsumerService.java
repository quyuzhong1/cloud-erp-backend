package com.erp.server.oms.rocketmq;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.ListingInfoService;
import com.sdk.oms.shopify.api.rest.model.ShopifyProduct;
import com.sdk.oms.shopify.dto.PlatformShopifyListingDTO;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 下载Shopify商品消费服务
 * @author Jim
 */
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "shopify_products_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_shopify_products_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class ShopifyListingConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private ListingInfoService listingInfoService;

    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(T ext) {
        PlatformProductDTO dto = (PlatformProductDTO) ext;
        // 组合信息
        ListingInfoEntity entity = new ListingInfoEntity();
        BeanUtils.copyProperties(dto, entity);
        entity.setSkuNo("");
        entity.setProductName("");
        entity.setMatchResult(false);
        if (!listingInfoService.save(entity)){
            throw new ServiceException("Shopify Listing 产品保存失败");
        }
        return ApiResult.success();
    }
}
