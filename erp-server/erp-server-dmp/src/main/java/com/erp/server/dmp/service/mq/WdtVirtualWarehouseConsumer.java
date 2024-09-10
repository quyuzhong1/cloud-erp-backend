package com.erp.server.dmp.service.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.dmp.service.ThirdWarehouseService;
import com.sdk.wangdian.dto.ErpWarehouseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 旺店通虚拟仓数据消费
 * @date 2024-06-13
 * @author hyj
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_wdt_virtual_warehouse_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_wdt_virtual_warehouse_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class WdtVirtualWarehouseConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ThirdWarehouseService thirdWarehouseService;

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        log.info("更新旺店通仓库数据拉取任务状态：{} {} {}", paramDTO.getDmpSyncTaskId(), paramDTO.getSyncStatus(), paramDTO.getResponseMsg());
        try {
            dmpTaskFeign.updateSyncInfo(paramDTO);
        }catch (Exception e){
            throw new ServiceException("erp-dmp更新dmp_pull_task异常："+ e.getMessage());
        }
    }

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {
        if (StringUtils.isEmpty(uniqueId) || StringUtils.isEmpty(platform) || Objects.isNull(isClean)){
            return;
        }
        String mongoTableName = StrUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(), platform, BusinessTypeEnum.WDT_WAREHOUSE.getCode());
        MongoDBUpdateDTO dto = MongoDBUpdateDTO.builder()
                .tableName(mongoTableName)
                .uniqueId(uniqueId)
                .isClean(isClean)
                .build();
        dmpMongoDbFeign.updateMongoDbData(dto);
    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpTaskFeign.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        log.info("旺店通仓库数据处理：{}", JSONUtil.parse(ext).toString());
        ErpWarehouseDto entity = JSONUtil.toBean(ext.toString(), ErpWarehouseDto.class);
        //入库
        ThirdWarehouseDTO.AddDTO addDTO = new ThirdWarehouseDTO.AddDTO();
        BeanUtils.copyProperties(entity,addDTO);
        addDTO.setCategory(ThirdSysTypeEnum.VIRTUAL_WAREHOUSE.getCode());
        thirdWarehouseService.add(addDTO);
        return ApiResult.success();
    }
}
