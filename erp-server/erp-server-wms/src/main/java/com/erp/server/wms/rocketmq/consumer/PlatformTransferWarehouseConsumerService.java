package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformTransferWarehouseDTO;
import com.common.business.dto.PlatformWarehouseDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.entity.OverseasTransferWarehouseEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.OverseasWarehouseConverter;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import com.erp.server.wms.service.OverseasTransferWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

/**
 * 下载平台中转仓仓库消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_transfer_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_transfer_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformTransferWarehouseConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private OverseasTransferWarehouseService overseasTransferWarehouseService;

    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    public void sendWarnMsg(String syncTaskId) {
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        PlatformTransferWarehouseDTO dto = JSONUtil.toBean(ext.toString(), PlatformTransferWarehouseDTO.class);
        //海外仓
        if(WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode().equals(dto.getWarehousePlatformType())){
            //查询数据库存在的数据
            OverseasTransferWarehouseEntity mqEntity = OverseasWarehouseConverter.INSTANCE.transferDtoConvert(dto);
            overseasTransferWarehouseService.saveOrUpdateByPlatform(mqEntity);
        }
        return ApiResult.success();
    }
}
