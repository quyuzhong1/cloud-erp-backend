package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.utils.RedisUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * B2C 销售出库 金蝶同步到WMS
 *
 * @author Lambda
 * @Classname KingdeeB2CSoOutstockConsumer
 * @Date 2023-06-27 10:21
 * @Created by yl
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_SYNC_TASK_TOPIC, selectorExpression = "sync_kingdee_so_outatock_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SO_OUTSTOCK_TO_WMS)
public class KingdeeB2CSoOutstockConsumer implements RocketMQListener<Object> {

    @Resource
    private SyncB2CSoOutstockService syncB2CSoOutstockService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public void onMessage(Object ext) {


        //json字符串
        String jsonStr = JSONUtil.toJsonStr(ext);
        //json数据
        JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
        String dmpSyncTaskId = jsonObject.get("dmpSyncTaskId").toString();

        DmpSyncMqDTO.ParamDTO paramDTO = new DmpSyncMqDTO.ParamDTO();
        paramDTO.setDmpSyncTaskId(dmpSyncTaskId);

        //由于实体对象上有别名，所以转对象无法用hutool,需要用fastjson
        KingdeeDeliveryDetailEntity entity= JSONUtil.toBean(jsonStr,KingdeeDeliveryDetailEntity.class);

        //获取锁
        boolean isLocked = analysisDataCreateRedisLock(entity.getFBillNo());
        if (isLocked) {
            try {
                log.info("监听到金蝶B2C销售出库单要同步：entity>>>>>{}", ext);
                syncB2CSoOutstockService.syncKingdeeSoOutstock(entity);
                //同步成功
                paramDTO.setSyncStatus(SyncStatusEnum.SUCCESS_SYNC.getCode());
                paramDTO.setResponseMsg("同步成功");
                dmpTaskFeign.updateSyncInfo(paramDTO);
            } catch (Exception e) {
                log.error("金蝶B2C销售出库单同步失败，msg = {}", StringUtils.isBlank(e.getMessage()) ? e : e.getMessage());
                //同步失败
                paramDTO.setSyncStatus(SyncStatusEnum.FAILED_SYNC.getCode());
                paramDTO.setResponseMsg(StringUtils.isBlank(e.getMessage()) ? ExceptionUtil.stacktraceToOneLineString(e, 10) : e.getMessage());
                dmpTaskFeign.updateSyncInfo(paramDTO);
                //错误预警
                dmpTaskFeign.sendWarnMsg(dmpSyncTaskId);
            } finally {
                // 确保最终释放锁
                redisUtil.unLock(entity.getFBillNo(), entity.getFBillNo());
            }
        } else {
            log.error("金蝶B2C销售出库单同步ERP失败，单号 = {}，未获取到锁", entity.getFBillNo());
            //同步失败
            paramDTO.setSyncStatus(SyncStatusEnum.FAILED_SYNC.getCode());
            paramDTO.setResponseMsg("未获取到锁");
            dmpTaskFeign.updateSyncInfo(paramDTO);
            //错误预警
            dmpTaskFeign.sendWarnMsg(dmpSyncTaskId);
        }
    }


    /**
     * 解析数据增加分布式锁
     * @param primaryKey
     * @return
     */
    private boolean analysisDataCreateRedisLock(String primaryKey) {
        Boolean lockValue = null;
        try {
            lockValue = redisUtil.lockAutoUnlock(primaryKey, primaryKey, 60, false);

            int count = 0;
            //如果未获取到锁等待重试
            while (!lockValue && count < 10) {
                Thread.sleep(10 * 1000);
                lockValue = redisUtil.lockAutoUnlock(primaryKey, primaryKey, 60, false);
                count++;
            }

            //如果超过重试次数结束还未获取到锁就释放原有的锁避免死锁
            if (!lockValue || count > 9) {
                redisUtil.unLock(primaryKey, primaryKey);
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
