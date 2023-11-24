package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformInventoryDTO;
import com.common.business.dto.PlatformWarehouseDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.OverseasWarehouseConverter;
import com.erp.server.wms.service.OverseasInventoryService;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

/**
 * 下载平台库存消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_inventory_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_inventory_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformInventoryConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private OverseasInventoryService overseasInventoryService;

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
        PlatformInventoryDTO dto = JSONUtil.toBean(ext.toString(), PlatformInventoryDTO.class);
        //海外仓
        if(WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode().equals(dto.getWarehousePlatformType())){
            //转换成数据库实体对象
            OverseasInventoryEntity entity = OverseasWarehouseConverter.INSTANCE.inventoryDtoToDb(dto);
            overseasInventoryService.saveOrUpdateByPlatform(entity);
        }
        return ApiResult.success();
    }

}
