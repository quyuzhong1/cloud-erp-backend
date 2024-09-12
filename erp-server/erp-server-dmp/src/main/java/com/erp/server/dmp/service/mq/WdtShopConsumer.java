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
import com.erp.model.dmp.dto.ThirdShopDTO;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.dmp.service.ThirdShopService;
import com.sdk.wangdian.dto.ErpShopDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 旺店通仓库数据消费
 * @date 2024-05-23
 * @author tanmujin
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_wdt_shop_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_wdt_shop_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class WdtShopConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ThirdShopService thirdShopService;

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
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
        ErpShopDto entity = JSONUtil.toBean(ext.toString(), ErpShopDto.class);
        //入库
        ThirdShopDTO.AddDTO addDTO = new ThirdShopDTO.AddDTO();
        BeanUtils.copyProperties(entity,addDTO);
        thirdShopService.add(addDTO);
        return ApiResult.success();
    }
}
