package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.DmpPullOtherOutStockDTO;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.SoMultiChannelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 下载平台多渠道订单消费服务
 * @author Jim
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_so_multi_channel_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_so_multi_channel_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformSoMultiChannelConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Resource
    private SoMultiChannelService soMultiChannelService;


    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        try {
            dmpTaskFeign.updateSyncInfo(paramDTO);
        }catch (Exception e){
            throw new ServiceException("erp-dmp更新dmp_pull_task异常："+ e.getMessage());
        }
    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpTaskFeign.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        log.info("[多渠道订单消费] 消费:dto={}", JSONUtil.toJsonStr(ext));
        PlatformOrderDTO dto = JSONUtil.toBean(ext.toString(), PlatformOrderDTO.class);
        SoMultiChannelEntity mainEntity = soMultiChannelService.handleSave(dto);

        if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(mainEntity.getDictPlatform())){
            try {
                // 多渠道订单生成其他出库单
                Boolean result = dmpMongoDbFeign.checkOtherOutStock(new DmpPullOtherOutStockDTO(
                        mainEntity.getShopId(),
                        mainEntity.getPlatformCode(),
                        mainEntity.getId(),
                        mainEntity.getDictPlatform()
                ));
                if (!result){
                    log.warn("处理检查渠道订单生成其他出库单失败:platformOrderId={}", dto.getPlatformCode());
                }
            } catch (Exception e) {
                log.error("检查渠道订单生成其他出库单失败:platformOrderId={}", dto.getPlatformCode());
            }
        }
        return ApiResult.success();
    }

    @Override
    public void updateMongodbData(String platform,String uniqueId, Integer isClean){
        if (StringUtils.isEmpty(uniqueId) || StringUtils.isEmpty(platform) || Objects.isNull(isClean)){
            return;
        }
        MongoDBUpdateDTO dto = MongoDBUpdateDTO.builder()
                .tableName(getTableName(platform))
                .uniqueId(uniqueId)
                .isClean(isClean)
                .build();
        dmpMongoDbFeign.updateMongoDbData(dto);
    }

    /**
     * 根据平台组装表名
     */
    public String getTableName(String platform){
        // 亚马逊多渠道订单和B2C订单来源一致
        if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(platform)){
            return  CharSequenceUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
                    platform, BusinessTypeEnum.ORDER.getCode());
        } else {
            return  CharSequenceUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
                    platform, BusinessTypeEnum.SO_MULTI_CHANNEL.getCode());
        }

    }
}
