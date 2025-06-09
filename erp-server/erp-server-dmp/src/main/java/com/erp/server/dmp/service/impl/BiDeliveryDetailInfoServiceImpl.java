package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.BiDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.BiDeliveryDetailItemEntity;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.server.dmp.pull.mapper.BiDeliveryDetailInfoMapper;
import com.erp.server.dmp.service.BiDeliveryDetailInfoService;
import com.erp.server.dmp.service.BiDeliveryDetailItemService;
import com.erp.server.dmp.service.DmpPullTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 货详情信息
 */
@Slf4j
@Service
public class BiDeliveryDetailInfoServiceImpl extends ServiceImpl<BiDeliveryDetailInfoMapper, BiDeliveryDetailInfoEntity>
    implements BiDeliveryDetailInfoService {

    @Resource
    private BiDeliveryDetailItemService biDeliveryDetailItemService;

    @Resource
    private DmpPullTaskService dmpPullTaskService;

    @Resource
    private MQProducerService mqProducerService;



    /**
     * 添加发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param biDeliveryDetailInfoEntity
     * @return java.lang.Boolean
     **/
    @Override
    public String add(BiDeliveryDetailInfoEntity biDeliveryDetailInfoEntity) {
        this.save(biDeliveryDetailInfoEntity);
        return biDeliveryDetailInfoEntity.getId();
    }

    /**
     * 根据单据编号查询发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param biDeliveryDetailInfoEntity
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    @Override
    public BiDeliveryDetailInfoEntity getDeliveryDetailByBillNo(BiDeliveryDetailInfoEntity biDeliveryDetailInfoEntity) {
        LambdaQueryWrapper<BiDeliveryDetailInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(BiDeliveryDetailInfoEntity::getBillNo, biDeliveryDetailInfoEntity.getBillNo());
        lambdaQueryWrapper.eq(BiDeliveryDetailInfoEntity::getOrderNo, biDeliveryDetailInfoEntity.getOrderNo());
        return this.getOne(lambdaQueryWrapper);
    }


    /**
     * 根据订单编号查询发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param orderNo
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    @Override
    public BiDeliveryDetailInfoEntity getDeliveryDetailOrderNo(String orderNo) {
        LambdaQueryWrapper<BiDeliveryDetailInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(BiDeliveryDetailInfoEntity::getOrderNo, orderNo);
        lambdaQueryWrapper.last("LIMIT 1");
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据单据编号修改发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param biDeliveryDetailInfoEntity
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateDeliveryDetailByBillNo(BiDeliveryDetailInfoEntity biDeliveryDetailInfoEntity) {
        LambdaQueryWrapper<BiDeliveryDetailInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(BiDeliveryDetailInfoEntity::getBillNo, biDeliveryDetailInfoEntity.getBillNo());
        lambdaQueryWrapper.eq(BiDeliveryDetailInfoEntity::getOrderNo, biDeliveryDetailInfoEntity.getOrderNo());
        return this.update(biDeliveryDetailInfoEntity, lambdaQueryWrapper);
    }

    /**
     * 校验发货详情信息在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String checkOrder(BiDeliveryDetailInfoEntity biDeliveryDetailInfoEntity) {
        String deliveryDetailId = "";
        BiDeliveryDetailInfoEntity deliveryDetailInfoEntity = getDeliveryDetailByBillNo(biDeliveryDetailInfoEntity);
        if (null != deliveryDetailInfoEntity) {
            //如果数据有变动需要更新数据库订单信息
            if (!deliveryDetailInfoEntity.toString().equals(biDeliveryDetailInfoEntity.toString())) {
                biDeliveryDetailInfoEntity.setId(deliveryDetailInfoEntity.getId());
                updateById(biDeliveryDetailInfoEntity);
                deliveryDetailId = deliveryDetailInfoEntity.getId();
            } else {
                return deliveryDetailInfoEntity.getId() ;
            }
        } else {
            deliveryDetailId = add(biDeliveryDetailInfoEntity);
        }
        if(StrUtil.isBlank(deliveryDetailId)){
            throw new RuntimeException("DmpDeliveryDetailInfoServiceImpl>>>checkOrder>>>发货订单保存失败");
        }
        List<BiDeliveryDetailItemEntity> itemList = biDeliveryDetailInfoEntity.getDetails();
        if (CollectionUtil.isEmpty(itemList)){
            return deliveryDetailId;
        }
        String orderId = deliveryDetailId;
        List<BiDeliveryDetailItemEntity> biDeliveryDetailItemEntityList = itemList.stream().peek(entity -> entity.setDeliveryDetailId(orderId)).collect(Collectors.toList());
        log.debug("发货明细：{}" , JSON.toJSONString(biDeliveryDetailItemEntityList));
        biDeliveryDetailItemService.deleteDeliveryDetailItemByDetailId(deliveryDetailId);

        biDeliveryDetailItemService.batchAdd(itemList, biDeliveryDetailInfoEntity.getPlatformSign());
        return deliveryDetailId;
    }

    @Override
    public BiDeliveryDetailInfoEntity getByPlatformOrderId(String platformOrderId) {
        LambdaQueryWrapper<BiDeliveryDetailInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(BiDeliveryDetailInfoEntity::getPlatformOrderId, platformOrderId);
        lambdaQueryWrapper.last("LIMIT 1");
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncTask(KingdeeDeliveryDetailEntity ext) {


        //新增发送任务
        DmpPullTaskEntity dmpPullTaskEntity = new DmpPullTaskEntity();
        dmpPullTaskEntity.setSourcePlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpPullTaskEntity.setSourceType(SourceTypeEnum.SAL_OUTSTOCK.getCode());
        dmpPullTaskEntity.setSourceId(ext.getFId());
        dmpPullTaskEntity.setSourceCode(ext.getFBillNo());
        dmpPullTaskEntity.setTargetPlatformName(PlatformEnum.ERP.getDesc());
        dmpPullTaskEntity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
        dmpPullTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
        dmpPullTaskEntity.setMqTag(RocketMqTagEnum.SYNC_KINGDEE_SO_OUTSTOCK_TAG.getName());
        String mqData = JSONUtil.toJsonStr(ext);
        dmpPullTaskEntity.setMqData(mqData);
        dmpPullTaskService.saveOrUpdateDmpSyncTask(dmpPullTaskEntity);
        // 发送推送同步任务消息
        JSONObject jsonObject = JSONUtil.parseObj(dmpPullTaskEntity.getMqData());
        jsonObject.set("dmpSyncTaskId",dmpPullTaskEntity.getId());
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.SYNC_KINGDEE_SO_OUTSTOCK_TAG.getName(),
                jsonObject, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }

    }

    @Override
    public void removeDeliveryByCodes(List<String> codes) {
        //删除订单
        LambdaQueryWrapper<BiDeliveryDetailInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BiDeliveryDetailInfoEntity::getPlatformOrderId, codes);
        List<BiDeliveryDetailInfoEntity> list = baseMapper.selectList(queryWrapper);
        log.info("删除 bi_delivery_detail_info 订单：{}", JSON.toJSONString(list));
        if (CollectionUtils.isNotEmpty(list)) {
            //删除明细记录
            list.forEach(dmpDeliveryDetailInfoEntity -> {
                List<BiDeliveryDetailItemEntity> itemEntities = biDeliveryDetailItemService.getItemByMainId(dmpDeliveryDetailInfoEntity.getId());
                biDeliveryDetailItemService.removeByIds(itemEntities.stream().map(BiDeliveryDetailItemEntity::getId).collect(Collectors.toList()));
                this.removeById(dmpDeliveryDetailInfoEntity.getId());
            });
        }
    }
}




