package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformTransferWarehouseDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.wms.entity.OverseasTransferWarehouseEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.convert.OverseasWarehouseConverter;
import com.erp.server.wms.service.OverseasTransferWarehouseService;
import com.erp.server.wms.service.OverseasWarehouseInboundDetailService;
import com.erp.server.wms.service.OverseasWarehouseInboundReceivedService;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 下载平台入库数据消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_inbound_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_inbound_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformInboundConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;

    @Resource
    private OverseasWarehouseInboundDetailService overseasWarehouseInboundDetailService;

    @Resource
    private OverseasWarehouseInboundReceivedService overseasWarehouseInboundReceivedService;

    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    public void sendWarnMsg(String syncTaskId) {
        dmpTaskFeign.sendWarnMsg(syncTaskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        PlatformInboundDTO dto = JSONUtil.toBean(ext.toString(), PlatformInboundDTO.class);
        //海外仓
        if(WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode().equals(dto.getWarehousePlatformType())){
            //通过单号查询主表记录
            OverseasWarehouseInboundEntity mainEntity = overseasWarehouseInboundService.getByCode(dto.getReceivingCode());
            if(Objects.isNull(mainEntity)){
                return ApiResult.success();
            }
            //查询明细数据
            List<OverseasWarehouseInboundDetailEntity> detailList = overseasWarehouseInboundDetailService.getByMainId(mainEntity.getId());


        }
        return ApiResult.success();
    }
}
