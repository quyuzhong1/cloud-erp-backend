package com.erp.server.oms.rocketmq;

import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 下载Shopify商品消费服务
 * @author Jim
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = "topic",
        selectorExpression = "third_system_product_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_products_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformListingConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
//    @Resource
//    private ListingInfoService listingInfoService;

    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
//        PlatformProductDTO dto = JSONUtil.toBean(ext.toString(), PlatformProductDTO.class);
//        // 组合信息
//        ListingInfoEntity entity = new ListingInfoEntity();
//        BeanUtils.copyProperties(dto, entity);
//        entity.setSkuNo("");
//        entity.setProductName("");
//        entity.setMatchResult(false);
//        if (!listingInfoService.save(entity)){
//            throw new ServiceException("Listing 产品保存失败");
//        }
        return ApiResult.success();
    }
}
