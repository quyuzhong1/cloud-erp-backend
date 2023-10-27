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
 * 下载平台订单消费服务
 * @author Cloud
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = "topic",
        selectorExpression = "third_system_order_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_order_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformOrderConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
//    @Resource
//    private SoB2cService soB2cService;
//    @Resource
//    private SoB2cDetailService soB2cDetailService;

    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
//        PlatformOrderDTO dto = JSONUtil.toBean(ext.toString(), PlatformOrderDTO.class);
//        // 组合信息
//        SoB2cEntity entity = new SoB2cEntity();
//        BeanUtils.copyProperties(dto, entity);
//        if (!soB2cService.save(entity)){
//            throw new ServiceException("soB2c订单保存失败");
//        }
//        // 信息校验?
//
//        // 订单明细
//        List<SoB2cDetailEntity> detailEntityList = dto.getDetails().stream().map(d -> {
//            SoB2cDetailEntity detailEntity = new SoB2cDetailEntity();
//            BeanUtils.copyProperties(d, detailEntity);
//            return detailEntity;
//        }).collect(Collectors.toList());
//
//        if (!soB2cDetailService.saveBatch(detailEntityList)){
//            throw new ServiceException("Shopify 订单明细批量保存失败");
//        }
        return ApiResult.success();
    }
}
