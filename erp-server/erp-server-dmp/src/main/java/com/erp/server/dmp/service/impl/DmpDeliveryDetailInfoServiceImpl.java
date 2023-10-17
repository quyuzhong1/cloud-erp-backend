package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.server.dmp.pull.mapper.DmpDeliveryDetailInfoMapper;
import com.erp.server.dmp.service.DmpDeliveryDetailInfoService;
import com.erp.server.dmp.service.DmpDeliveryDetailItemService;
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
public class DmpDeliveryDetailInfoServiceImpl extends ServiceImpl<DmpDeliveryDetailInfoMapper, DmpDeliveryDetailInfoEntity>
    implements DmpDeliveryDetailInfoService {

    @Resource
    private DmpDeliveryDetailItemService dmpDeliveryDetailItemService;

    @Resource
    private DmpPullTaskService dmpPullTaskService;

    @Resource
    private MQProducerService mqProducerService;



    /**
     * 添加发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpDeliveryDetailInfoEntity
     * @return java.lang.Boolean
     **/
    @Override
    public String add(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity) {
        this.save(dmpDeliveryDetailInfoEntity);
        return dmpDeliveryDetailInfoEntity.getId();
    }

    /**
     * 根据单据编号查询发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param dmpDeliveryDetailInfoEntity
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    @Override
    public DmpDeliveryDetailInfoEntity getDeliveryDetailByBillNo(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity) {
        LambdaQueryWrapper<DmpDeliveryDetailInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getBillNo, dmpDeliveryDetailInfoEntity.getBillNo());
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getOrderNo, dmpDeliveryDetailInfoEntity.getOrderNo());
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
    public DmpDeliveryDetailInfoEntity getDeliveryDetailOrderNo(String orderNo) {
        LambdaQueryWrapper<DmpDeliveryDetailInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getOrderNo, orderNo);
        lambdaQueryWrapper.last("LIMIT 1");
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据单据编号修改发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpDeliveryDetailInfoEntity
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateDeliveryDetailByBillNo(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity) {
        LambdaQueryWrapper<DmpDeliveryDetailInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getBillNo, dmpDeliveryDetailInfoEntity.getBillNo());
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getOrderNo, dmpDeliveryDetailInfoEntity.getOrderNo());
        return this.update(dmpDeliveryDetailInfoEntity, lambdaQueryWrapper);
    }

    /**
     * 校验发货详情信息在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String checkOrder(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity) {
        String deliveryDetailId = "";
        DmpDeliveryDetailInfoEntity deliveryDetailInfoEntity = getDeliveryDetailByBillNo(dmpDeliveryDetailInfoEntity);
        if (null != deliveryDetailInfoEntity) {
            //如果数据有变动需要更新数据库订单信息
            if (!deliveryDetailInfoEntity.toString().equals(dmpDeliveryDetailInfoEntity.toString())) {
                dmpDeliveryDetailInfoEntity.setId(deliveryDetailInfoEntity.getId());
                updateById(dmpDeliveryDetailInfoEntity);
                deliveryDetailId = deliveryDetailInfoEntity.getId();
            } else {
                return deliveryDetailInfoEntity.getId() ;
            }
        } else {
            deliveryDetailId = add(dmpDeliveryDetailInfoEntity);
        }
        if(StrUtil.isBlank(deliveryDetailId)){
            throw new RuntimeException("DmpDeliveryDetailInfoServiceImpl>>>checkOrder>>>发货订单保存失败");
        }
        List<DmpDeliveryDetailItemEntity> itemList = dmpDeliveryDetailInfoEntity.getDetails();
        if (CollectionUtil.isEmpty(itemList)){
            return deliveryDetailId;
        }
        String orderId = deliveryDetailId;
        itemList.stream().peek(entity -> entity.setDeliveryDetailId(orderId)).collect(Collectors.toList());
        dmpDeliveryDetailItemService.deleteDeliveryDetailItemByDetailId(deliveryDetailId);

        dmpDeliveryDetailItemService.batchAdd(itemList, dmpDeliveryDetailInfoEntity.getPlatformSign());
        return deliveryDetailId;
    }

    @Override
    public DmpDeliveryDetailInfoEntity getByPlatformOrderId(String platformOrderId) {
        LambdaQueryWrapper<DmpDeliveryDetailInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getPlatformOrderId, platformOrderId);
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncTask(KingdeeDeliveryDetailEntity ext) {


        //新增发送任务
        DmpPullTaskEntity dmpSyncTaskEntity = new DmpPullTaskEntity();
        dmpSyncTaskEntity.setSourcePlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpSyncTaskEntity.setSourceType(SourceTypeEnum.SAL_OUTSTOCK.getCode());
        dmpSyncTaskEntity.setSourceId(ext.getFId());
        dmpSyncTaskEntity.setSourceCode(ext.getFBillNo());
        dmpSyncTaskEntity.setTargetPlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskEntity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
        dmpSyncTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
        dmpSyncTaskEntity.setMqTag(RocketMqTagEnum.SYNC_KINGDEE_SO_OUTSTOCK_TAG.getName());
        String mqData = JSONObject.toJSONString(ext);
        dmpSyncTaskEntity.setMqData(mqData);
        dmpPullTaskService.saveOrUpdateDmpSyncTask(dmpSyncTaskEntity);
        DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpSyncTaskEntity.getId(), mqData);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.SYNC_KINGDEE_SO_OUTSTOCK_TAG.getName(),
                dmpSyncMqDTO, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }

    }

    @Override
    public void removeDeliveryByCodes(List<String> codes) {
        //删除订单
        LambdaQueryWrapper<DmpDeliveryDetailInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DmpDeliveryDetailInfoEntity::getPlatformOrderId, codes);
        List<DmpDeliveryDetailInfoEntity> list = baseMapper.selectList(queryWrapper);
        log.info("删除 dmp_delivery_detail_info 订单：{}", JSON.toJSONString(list));
        if (CollectionUtils.isNotEmpty(list)) {
            //删除明细记录
            list.forEach(dmpDeliveryDetailInfoEntity -> {
                List<DmpDeliveryDetailItemEntity> itemEntities = dmpDeliveryDetailItemService.getItemByMainId(dmpDeliveryDetailInfoEntity.getId());
                dmpDeliveryDetailItemService.removeByIds(itemEntities.stream().map(DmpDeliveryDetailItemEntity::getId).collect(Collectors.toList()));
                this.removeById(dmpDeliveryDetailInfoEntity.getId());
            });
        }
    }
}




