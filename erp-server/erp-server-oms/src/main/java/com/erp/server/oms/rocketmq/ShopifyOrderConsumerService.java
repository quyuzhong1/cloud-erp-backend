package com.erp.server.oms.rocketmq;

import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.SoB2cDetailService;
import com.erp.server.oms.service.SoB2cService;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 下载平台shopify订单消费服务
 * @author Jim
 */
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "shopify_orders_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_shopify_orders_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class ShopifyOrderConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(T ext) {
        PlatformOrderDTO dto = (PlatformOrderDTO) ext;
        // 组合信息
        SoB2cEntity entity = new SoB2cEntity();
        BeanUtils.copyProperties(dto, entity);
        if (!soB2cService.save(entity)){
            throw new ServiceException("Shopify订单保存失败");
        }
        // TODO 信息校验


        // 订单明细
        List<SoB2cDetailEntity> detailEntityList = dto.getDetails().stream().map(d -> {
            SoB2cDetailEntity detailEntity = new SoB2cDetailEntity();
            BeanUtils.copyProperties(d, detailEntity);
            return detailEntity;
        }).collect(Collectors.toList());

        if (!soB2cDetailService.saveBatch(detailEntityList)){
            throw new ServiceException("Shopify 订单明细批量保存失败");
        }
        return ApiResult.success();
    }
}
