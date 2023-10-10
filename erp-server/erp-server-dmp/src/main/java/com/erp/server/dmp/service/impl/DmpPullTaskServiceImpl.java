package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.server.dmp.mapper.DmpPullTaskMapper;
import com.erp.server.dmp.service.DmpPullTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 中台同步任务表 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@Slf4j
@Service
public class DmpPullTaskServiceImpl extends SuperServiceImpl<DmpPullTaskMapper, DmpPullTaskEntity> implements DmpPullTaskService {

    @Autowired
    private DmpPullTaskMapper dmpPullTaskMapper;

    @Resource
    private MQProducerService mqProducerService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSyncInfo(String id, String syncStatus, String responseMsg) {
        LambdaUpdateWrapper<DmpPullTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DmpPullTaskEntity::getId, id);
        updateWrapper.set(DmpPullTaskEntity::getLastSyncTime, LocalDateTime.now());
        updateWrapper.set(DmpPullTaskEntity::getStatus, syncStatus);
        updateWrapper.set(StrUtil.isNotBlank(responseMsg), DmpPullTaskEntity::getReturnMsg, responseMsg);
        updateWrapper.set(DmpPullTaskEntity::getUpdateTime, LocalDateTime.now());
        this.update(updateWrapper);
    }

    @Override
    public void saveOrUpdateDmpSyncTask(DmpPullTaskEntity dmpSyncTaskEntity) {
        DmpPullTaskEntity found = lambdaQuery()
                .eq(DmpPullTaskEntity::getSourceType, dmpSyncTaskEntity.getSourceType())
                .eq(DmpPullTaskEntity::getSourceId, dmpSyncTaskEntity.getSourceId())
                .eq(DmpPullTaskEntity::getSourcePlatformName, dmpSyncTaskEntity.getSourcePlatformName())
                .eq(DmpPullTaskEntity::getTargetPlatformName, dmpSyncTaskEntity.getTargetPlatformName())
                .eq(DmpPullTaskEntity::getMqTopic, dmpSyncTaskEntity.getMqTopic())
                .eq(DmpPullTaskEntity::getMqTag, dmpSyncTaskEntity.getMqTag())
                .last("LIMIT 1")
                .one();
        //存在则修改
        if (ObjectUtil.isNotEmpty(found)) {
            dmpSyncTaskEntity.setId(found.getId());
        }
        this.saveOrUpdate(dmpSyncTaskEntity);
    }

    /**
     * 新增同步金蝶退货单到wms退货入库单的任务
     * @Author Luo_WG
     * @Date 2023/7/4 19:48
     * @param entity
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncKingdeeReturnOrderToWms(KingdeeReturnOrderEntity entity) {
        //新增发送任务
        DmpPullTaskEntity dmpSyncTaskEntity = new DmpPullTaskEntity();
        dmpSyncTaskEntity.setSourcePlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpSyncTaskEntity.setSourceType(SourceTypeEnum.SAL_RETURNSTOCK.getCode());
        dmpSyncTaskEntity.setSourceId(entity.getFId());
        dmpSyncTaskEntity.setSourceCode(entity.getFBillNo());
        dmpSyncTaskEntity.setTargetPlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskEntity.setStatus(SyncStatusEnum.IN_SYNC.getCode());
        dmpSyncTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
        dmpSyncTaskEntity.setMqTag(RocketMqTagEnum.SYNC_KINGDEE_RETURN_ORDER_TO_WMS_TAG.getName());
        String mqData = JSONObject.toJSONString(entity);
        dmpSyncTaskEntity.setMqData(mqData);
        this.saveOrUpdateDmpSyncTask(dmpSyncTaskEntity);
        DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpSyncTaskEntity.getId(), mqData);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.SYNC_KINGDEE_RETURN_ORDER_TO_WMS_TAG.getName(),
                dmpSyncMqDTO, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    public List<String> listKingdeeCode(Map<String, Object> conditon) {
        List<String> result= new ArrayList<>();

        LambdaQueryWrapper<DmpPullTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DmpPullTaskEntity::getSourceCode);
        queryWrapper.eq(DmpPullTaskEntity::getSourcePlatformName,"金蝶云星空")
                .eq(DmpPullTaskEntity::getTargetPlatformName,"自研ERP")
                .eq(null!=conditon.get("id"), DmpPullTaskEntity::getId, conditon.get("id"))
                .eq(null!=conditon.get("is_deleted"), DmpPullTaskEntity::getIsDeleted, conditon.get("is_deleted"))
                .eq(null!=conditon.get("source_type"), DmpPullTaskEntity::getSourceType, conditon.get("source_type"))
                .eq(null!=conditon.get("source_code"), DmpPullTaskEntity::getSourceCode, conditon.get("source_code"))
                .eq(null!=conditon.get("source_id"), DmpPullTaskEntity::getSourceCode, conditon.get("source_id"))
                .eq(null!=conditon.get("status"), DmpPullTaskEntity::getStatus, conditon.get("status"))
                .eq(null!=conditon.get("mq_tag"), DmpPullTaskEntity::getMqTag, conditon.get("mq_tag"))
                .like(null!=conditon.get("return_msg"), DmpPullTaskEntity::getReturnMsg, conditon.get("return_msg"))
        ;
        queryWrapper.last(null!=conditon.get("lastSql")," and " + conditon.get("lastSql").toString());
        List<DmpPullTaskEntity> queryResult=this.list(queryWrapper);

        if(CollectionUtil.isNotEmpty(queryResult)) {
            queryResult.stream().forEach(item-> result.add(item.getSourceCode()));
        }

        return result;
    }
}
