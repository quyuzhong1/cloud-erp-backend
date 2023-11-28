package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformWarehouseDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.OverseasWarehouseConverter;
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
 * 下载平台仓库消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_warehouse_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_warehouse_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformWarehouseConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

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
        PlatformWarehouseDTO dto = JSONUtil.toBean(ext.toString(), PlatformWarehouseDTO.class);
        //海外仓
        if(WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode().equals(dto.getWarehousePlatformType())){
            handleOverseasWarehouse(dto);
        }
        return ApiResult.success();
    }

    private void handleOverseasWarehouse(PlatformWarehouseDTO dto) {
        //查询数据库存在的数据
        OverseasProviderWarehouseEntity dbEntity = overseasProviderWarehouseService.getByPlatform(dto.getProviderErpId(),dto.getWarehouseCode());
        OverseasProviderWarehouseEntity mqEntity = OverseasWarehouseConverter.INSTANCE.warehouseDb(dto);
        //设置国家名称
        setCountryName(mqEntity);
        if(Objects.isNull(dbEntity)){
            //不存在，插入,默认禁用
            mqEntity.setDisabled(Boolean.TRUE);
            overseasProviderWarehouseService.save(mqEntity);
        }else{
            updateOverseasWarehouse(dbEntity, mqEntity);
        }
    }

    private void updateOverseasWarehouse(OverseasProviderWarehouseEntity dbEntity, OverseasProviderWarehouseEntity mqEntity) {
        dbEntity.setPlatformWarehouseName(mqEntity.getPlatformWarehouseName());
        dbEntity.setCountry(mqEntity.getCountry());
        dbEntity.setCountryName(mqEntity.getCountryName());
        overseasProviderWarehouseService.updateById(dbEntity);
    }

    private void setCountryName(OverseasProviderWarehouseEntity mqEntity){
        if(StrUtils.isNotEmpty(mqEntity.getCountryName())){
            return;
        }
        Optional.ofNullable(mqEntity.getCountry())
                .map(sysUserFeign::getCountryById)
                .ifPresent(dictCountryEntity -> mqEntity.setCountryName(dictCountryEntity.getNameCn()));
    }
}
