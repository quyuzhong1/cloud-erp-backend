package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.dto.WdtKolB2cApplicationDTO;
import com.common.business.enums.*;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.entity.KolSubB2cApplicationDetailEntity;
import com.erp.model.oms.entity.KolSubB2cApplicationEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
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
 * 下载平台商品消费服务
 * @author Jim
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_WDT_KOL_B2C_APPLICATION_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_WDT_KOL_B2C_APPLICATION_TO_OMS_TAG,
        consumerGroup ="${spring.cloud.nacos.discovery.namespace}-erp_dmp_group",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformWdtB2cOrderConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private KolSubB2cApplicationService kolSubB2cApplicationService;
    @Resource
    private KolSubB2cApplicationDetailService KolSubB2cApplicationDetailService;

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
        DmpPullTaskEntity dmpPullTaskEntity = dmpTaskFeign.getPullTaskById(syncTaskId);
        WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(dmpPullTaskEntity, msg);
        mqProducerService.sendWarnMsg(msgInfoDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        if(Objects.isNull(ext)){
            return ApiResult.error("[KolSubB2c] 消费:来源数据异常 ext为null");
        }
        log.info("[KolSubB2c] 消费: dto={}", JSONUtil.toJsonStr(ext));
        WdtKolB2cApplicationDTO dto = JSONUtil.toBean(ext.toString(), WdtKolB2cApplicationDTO.class);
            // ALiExpress,Shopify来源卖家sku可能为空
            if (StringUtils.isBlank(dto.getCode())) {
                log.warn("[KolSubB2c] 消费:来源数据异常code为空, msg={}", JSONUtil.toJsonStr(dto));
                return ApiResult.error("[KolSubB2c] 消费:来源数据异常code为空");
            }

        KolSubB2cApplicationEntity oldEntity = kolSubB2cApplicationService.lambdaQuery().eq(KolSubB2cApplicationEntity::getCode, dto.getCode()).one();
        KolSubB2cApplicationEntity entity = new KolSubB2cApplicationEntity();
        BeanMapper.copy(oldEntity, entity);

        if(Objects.isNull(entity)){
            log.warn("[KolSubB2c] 消费:KolSubB2cApplicationEntity不存在");
            return ApiResult.error("[KolSubB2c] 消费:KolSubB2cApplicationEntity不存在");
        }

        List<WdtKolB2cApplicationDTO.WdtKolB2cApplicationDetailDTO> details = dto.getDetails();
        Map<String, WdtKolB2cApplicationDTO.WdtKolB2cApplicationDetailDTO> map = details.stream().collect(Collectors.toMap(WdtKolB2cApplicationDTO.WdtKolB2cApplicationDetailDTO::getSkuNo, Function.identity()));
        List<KolSubB2cApplicationDetailEntity> detailList = KolSubB2cApplicationDetailService.lambdaQuery().eq(KolSubB2cApplicationDetailEntity::getMainId, entity.getId()).list();
        for (KolSubB2cApplicationDetailEntity detailEntity : detailList) {
            WdtKolB2cApplicationDTO.WdtKolB2cApplicationDetailDTO detailDTO = map.get(detailEntity.getSkuNo());
            if(Objects.nonNull(detailDTO)){
                detailEntity.setPlatformDetailId(detailDTO.getPlatformDetailId());
                detailEntity.setThirdDetailId(detailDTO.getThirdDetailId());
            }
        }

        entity.setOrderStatus(dto.getOrderStatus());
        entity.setDeliveryStatus(dto.getDeliveryStatus());
        entity.setTrackNo(dto.getTrackNo());
        entity.setPlatformSoCode(dto.getPlatformSoCode());
        entity.setPlatformOrderCode(dto.getPlatformOrderCode());

        kolSubB2cApplicationService.updateById(entity);
        KolSubB2cApplicationDetailService.updateBatchById(detailList);
        //记录更新日志
        String msg =  CharSequenceUtil.format("拉取旺店通B2C销售订单【{}】状态更新", entity.getCode());
        operateLogService.addModuleOperateLogByObj(oldEntity, entity, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getSourceId(), msg);
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
     * @param platform
     * @return
     */
    private String getTableName(String platform){
        return  CharSequenceUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
                platform, BusinessTypeEnum.PRODUCT.getCode());
    }
    private WarnMsgInfoDTO buildWarnMsgInfoDTO(DmpPullTaskEntity dmpPullTaskEntity, String msg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
        warnMsgInfo.setTitle( CharSequenceUtil.format("KOL消费失败，来源平台:{},目标平台:{}", dmpPullTaskEntity.getSourcePlatformName(), dmpPullTaskEntity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.KOL_SUB_B2C_APPLICATION.getTableName());
        warnMsgInfo.setTableId(dmpPullTaskEntity.getId());
        warnMsgInfo.setKeyInfo(StringUtils.isBlank(msg) ? "" : msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }
}
