package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.dmp.entity.DmpFbaDeliveryEntity;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.mapper.DmpFbaDeliveryMapper;
import com.erp.server.dmp.service.DmpFbaDeliveryDetailService;
import com.erp.server.dmp.service.DmpFbaDeliveryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.dmp.service.DmpPullTaskService;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * <p>
 * FBA发货单 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@Slf4j
@Service
public class DmpFbaDeliveryServiceImpl extends SuperServiceImpl<DmpFbaDeliveryMapper, DmpFbaDeliveryEntity> implements DmpFbaDeliveryService {

    @Autowired
    private DmpFbaDeliveryDetailService dmpFbaDeliveryDetailService;

    @Autowired
    private DmpPullTaskService dmpPullTaskService;

    @Autowired
    private MQProducerService mqProducerService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void checkDelivery(DmpFbaDeliveryEntity fbaDeliveryEntity) {
        DmpFbaDeliveryEntity dmpFbaDeliveryEntity =  lambdaQuery().eq(DmpFbaDeliveryEntity::getPlatformSign, fbaDeliveryEntity.getPlatformSign()).eq(DmpFbaDeliveryEntity::getDeliveryNo, fbaDeliveryEntity.getDeliveryNo())
                .one();

        if(null != dmpFbaDeliveryEntity && dmpFbaDeliveryEntity.getIsDeleted()){
            log.warn("FBA发货单【{}】已经被删除，不处理", dmpFbaDeliveryEntity.getDeliveryNo());
            return;
        }

        if(Objects.isNull(dmpFbaDeliveryEntity)) {
            // 新增（含明细）
            this.add(fbaDeliveryEntity);
        } else {
            // 如果数据有变动需要更新数据库订单信息
            if (!fbaDeliveryEntity.toString().equals(dmpFbaDeliveryEntity.toString())) {
                fbaDeliveryEntity.setId(dmpFbaDeliveryEntity.getId());
                updateById(fbaDeliveryEntity);
            }
            // 判断明细是否发生变化
            dmpFbaDeliveryDetailService.update(fbaDeliveryEntity.getItemList(), dmpFbaDeliveryEntity.getId());
        }

        //新增发送任务
        DmpPullTaskEntity dmpPullTaskEntity = new DmpPullTaskEntity();
        dmpPullTaskEntity.setSourcePlatformName(PlatformEnum.MABANG.getDesc());
        dmpPullTaskEntity.setSourceType(SourceTypeEnum.MABANG_FBA_DELIVERY.getCode());
        dmpPullTaskEntity.setSourceId(fbaDeliveryEntity.getDeliveryId());
        dmpPullTaskEntity.setSourceCode(fbaDeliveryEntity.getDeliveryNo());
        dmpPullTaskEntity.setTargetPlatformName(PlatformEnum.ERP.getDesc());
        dmpPullTaskEntity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
        dmpPullTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
        dmpPullTaskEntity.setMqTag(RocketMqTagEnum.SYNC_MABANG_FBA_DELIVERY_TO_WMS_TAG.getName());
        String mqData = JSONObject.toJSONString(fbaDeliveryEntity);
        dmpPullTaskEntity.setMqData(mqData);
        dmpPullTaskService.save(dmpPullTaskEntity);

        if(PlatformEnum.MABANG.getDesc().equals(fbaDeliveryEntity.getPlatformSign())) {
            // 发送到ERP WMS系统，生成加工单
            DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpPullTaskEntity.getId(), mqData);
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.SYNC_MABANG_FBA_DELIVERY_TO_WMS_TAG.getName(),
                    dmpSyncMqDTO, StrUtil.uuid().toLowerCase());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(DmpFbaDeliveryEntity entity) {
        //新增主表数据
        boolean save = this.save(entity);
        if (!save) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //新增明细
        Boolean addDetail = dmpFbaDeliveryDetailService.add(entity.getItemList(), entity.getId());
        if (!addDetail) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return Boolean.TRUE;
    }

}
