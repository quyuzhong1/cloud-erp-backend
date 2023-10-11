package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.server.dmp.mapper.DmpPushTaskMapper;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 中台同步任务表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-09-06
 */
@Slf4j
@Service
public class DmpPushTaskServiceImpl extends SuperServiceImpl<DmpPushTaskMapper, DmpPushTaskEntity> implements DmpPushTaskService {

    @Resource
    private MQProducerService mqProducerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMqAndSaveTask(DmpPushTaskFeignDTO dto) {
        // 保存任务表
        DmpPushTaskEntity entity = new DmpPushTaskEntity(dto);
        String entityId = saveOrUpdateDmpSyncTask(entity);
        // 发送MQ消息
        DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(entityId, dto.getMqData());
        SendResult result = mqProducerService.syncClassMsg(dto.getMqTopic(), dto.getMqTag(), dmpSyncMqDTO, entity.getSourceId());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String id, String status, String msg) {
        LambdaUpdateWrapper<DmpPushTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DmpPushTaskEntity::getId, id);
        updateWrapper.set(DmpPushTaskEntity::getLastSyncTime, LocalDateTime.now());
        updateWrapper.set(DmpPushTaskEntity::getStatus, status);
        updateWrapper.set(StrUtil.isNotBlank(msg), DmpPushTaskEntity::getReturnMsg, msg);
        updateWrapper.set(DmpPushTaskEntity::getUpdateTime, LocalDateTime.now());
        this.update(updateWrapper);

    }

    private String saveOrUpdateDmpSyncTask(DmpPushTaskEntity entity) {
        DmpPushTaskEntity found = lambdaQuery()
                .eq(DmpPushTaskEntity::getSourceType, entity.getSourceType())
                .eq(DmpPushTaskEntity::getSourceId, entity.getSourceId())
                .eq(DmpPushTaskEntity::getSourcePlatformName, entity.getSourcePlatformName())
                .eq(DmpPushTaskEntity::getTargetPlatformName, entity.getTargetPlatformName())
                .eq(DmpPushTaskEntity::getMqTopic, entity.getMqTopic())
                .eq(DmpPushTaskEntity::getMqTag, entity.getMqTag())
                .last("LIMIT 1")
                .one();
        //存在则修改
        if (ObjectUtil.isNotEmpty(found)) {
            entity.setId(found.getId());
        }
        this.saveOrUpdate(entity);
        return entity.getId();
    }
}
