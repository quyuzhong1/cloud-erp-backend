package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.WdtKolB2cApplicationDTO;
import com.common.core.utils.BeanMapper;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.oms.entity.KolSubB2cApplicationDetailEntity;
import com.erp.model.oms.entity.KolSubB2cApplicationEntity;
import com.erp.model.oms.enums.KolSubB2cApplicationDeliveryStatusEnum;
import com.erp.model.oms.enums.KolSubB2cApplicationOrderStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * KOL-B2C寄样申请单更新消费者
 * @author jack
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_WDT_KOL_B2C_APPLICATION_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_WDT_KOL_B2C_APPLICATION_TO_OMS_TAG,
        consumerGroup = RocketMqConsumerGroup.DMP_WDT_KOL_B2C_APPLICATION_CONSUMER)
public class PlatformWdtB2cOrderConsumerService extends AbstractNewPlatformConsumerHandler {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private KolSubB2cApplicationService kolSubB2cApplicationService;
    @Resource
    private KolSubB2cApplicationDetailService kolSubB2cApplicationDetailService;
    @Resource
    private KolB2cApplicationService kolB2cApplicationService;

    @Override
    public String getBizName() {
        return "KOL-B2C寄样申请单更新";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(String ext) {
        log.info("[KolSubB2c] 消费: dto={}", JSONUtil.toJsonStr(ext));
        WdtKolB2cApplicationDTO dto = JSONUtil.toBean(ext, WdtKolB2cApplicationDTO.class);
        if (dto == null) {
            log.warn("[KolSubB2c] 消费:JSON解析失败, ext={}", ext);
            return;
        }
        // ALiExpress,Shopify来源卖家sku可能为空
        if (StringUtils.isBlank(dto.getCode())) {
            log.warn("[KolSubB2c] 消费:来源数据异常code为空, msg={}", JSONUtil.toJsonStr(dto));
            return;
        }

        KolSubB2cApplicationEntity oldEntity = kolSubB2cApplicationService.lambdaQuery().eq(KolSubB2cApplicationEntity::getCode, dto.getCode()).one();

        if(Objects.isNull(oldEntity)){
            log.warn("[KolSubB2c] 消费:KolSubB2cApplicationEntity不存在,code:{}",dto.getCode());
            return;
        }
        KolSubB2cApplicationEntity entity = new KolSubB2cApplicationEntity();
        BeanMapper.copy(oldEntity, entity);


        List<WdtKolB2cApplicationDTO.WdtKolB2cApplicationDetailDTO> details = dto.getDetails();
        List<KolSubB2cApplicationDetailEntity> detailList = kolSubB2cApplicationDetailService.lambdaQuery()
                .eq(KolSubB2cApplicationDetailEntity::getMainId, entity.getId()).list();
        if (details != null && !details.isEmpty()) {
            // platformDetailId就是KolSubB2cApplicationDetailEntity的id，直接通过id匹配
            Map<String, WdtKolB2cApplicationDTO.WdtKolB2cApplicationDetailDTO> map = details.stream()
                    .filter(detail -> StringUtils.isNotBlank(detail.getPlatformDetailId()))
                    .collect(Collectors.toMap(
                            WdtKolB2cApplicationDTO.WdtKolB2cApplicationDetailDTO::getPlatformDetailId,
                            Function.identity(),
                            (existing, replacement) -> replacement));
            for (KolSubB2cApplicationDetailEntity detailEntity : detailList) {
                WdtKolB2cApplicationDTO.WdtKolB2cApplicationDetailDTO detailDTO = map.get(detailEntity.getId());
                if (Objects.nonNull(detailDTO)) {
                    detailEntity.setPlatformDetailId(detailDTO.getPlatformDetailId());
                    detailEntity.setThirdDetailId(detailDTO.getThirdDetailId());
                }
            }
        }
        entity.setDeliveryStatus(dto.getDeliveryStatus());
        entity.setOrderStatus(dto.getOrderStatus());
        if(Objects.equals(KolSubB2cApplicationDeliveryStatusEnum.SHIPPED.getCode(),dto.getDeliveryStatus())){
            entity.setOrderStatus(KolSubB2cApplicationOrderStatusEnum.APPROVE.getCode());
        }
        entity.setTrackNo(dto.getTrackNo());
        entity.setPlatformSoCode(dto.getPlatformSoCode());
        entity.setPlatformOrderCode(dto.getPlatformOrderCode());

        kolSubB2cApplicationService.updateById(entity);
        if (detailList != null && !detailList.isEmpty()) {
            kolSubB2cApplicationDetailService.updateBatchById(detailList);
        }
        kolB2cApplicationService.refreshCancelStatusBySubOrder(entity.getSourceId(), buildCancelFailReason(entity, dto));
        //记录更新日志
        String msg =  CharSequenceUtil.format("拉取旺店通B2C销售订单【{}】状态更新", entity.getCode());
        operateLogService.addModuleOperateLogByObj(oldEntity, entity, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getSourceId(), msg);
    }

    private String buildCancelFailReason(KolSubB2cApplicationEntity entity, WdtKolB2cApplicationDTO dto) {
        if (Objects.equals(KolSubB2cApplicationOrderStatusEnum.NOT.getCode(), dto.getOrderStatus())) {
            return "";
        }
        String orderStatusName = StringUtils.defaultIfBlank(KolSubB2cApplicationOrderStatusEnum.getName(dto.getOrderStatus()), dto.getOrderStatus());
        String deliveryStatusName = StringUtils.defaultIfBlank(KolSubB2cApplicationDeliveryStatusEnum.getName(dto.getDeliveryStatus()), dto.getDeliveryStatus());
        return StringUtils.substring(CharSequenceUtil.format("旺店通回传拆分单【{}】取消未成功，当前订单状态【{}】，发货状态【{}】",
                entity.getCode(), orderStatusName, deliveryStatusName), 0, 500);
    }
}
