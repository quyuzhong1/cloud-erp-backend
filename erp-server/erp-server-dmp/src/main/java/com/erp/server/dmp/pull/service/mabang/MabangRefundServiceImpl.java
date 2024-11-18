package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.SaveData;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.GyyRefundDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.BiRefundInfoEntity;
import com.erp.model.dmp.entity.BiRefundItemEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.mabang.RefundOrderEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.erp.server.dmp.utils.MabangApiUtils;
import com.erp.server.dmp.utils.MapCountUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 马帮退款列表
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ORDER_GET_REFUND_LIST)
public class MabangRefundServiceImpl implements IReportSaveService<RefundOrderEntity> {
    @Resource
    private MongoService mongoService;

    @Autowired
    private MQProducerService<BiRefundInfoEntity> mqProducerService;
    @Resource
    private CfgSettingService cfgSettingService;

    public static void main(String[] args) {
        MabangRefundServiceImpl gyyOrderInfoService = new MabangRefundServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.ORDER_GET_REFUND_LIST;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setPlatformApiId("7");
        jobTaskDTO.setApiName("管易云查询订单列表");
        jobTaskDTO.setId("32");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<RefundOrderEntity> refundOrderEntities = null;
        try {
            refundOrderEntities = gyyOrderInfoService.pullDate(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(refundOrderEntities);
    }

    /**
     * 拉取退款数据
     *
     * @param dto 任务信息
     * @return
     */
    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<RefundOrderEntity> entityList = pullDate(dto);

        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮退款订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮退款订单列表数据 entityList.size = {} ", entityList.size());
        List<RefundOrderEntity> insertList = new ArrayList<>();
        List<RefundOrderEntity> pushToMqList = new ArrayList<>();
        for (RefundOrderEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = new OrderMongoDTO(entity.getId());
            List<RefundOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_REFUND, RefundOrderEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            RefundOrderEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_REFUND, RefundOrderEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_REFUND);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("马帮退款订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<BiRefundInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<BiRefundInfoEntity> biRefundInfoEntityList = entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_REFUND_ORDER_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getRefundCode(), msg.getPlatformOrderId()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("马帮退款数据为：{}" , JSON.toJSONString(biRefundInfoEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<RefundOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_MABANG_REFUND, RefundOrderEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (RefundOrderEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(RefundOrderEntity mongoDatum) {
        BiRefundInfoEntity refundInfo = initOrderInfoEntity(mongoDatum);
        GyyRefundDTO updateDto = new GyyRefundDTO(mongoDatum.getId());
        if(null == refundInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil,MongoTableNameContant.ORIGINAL_MABANG_REFUND, RefundOrderEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_REFUND, RefundOrderEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_REFUND_ORDER_TAG.getName(),
                refundInfo, StrUtil.format("{}_{}", refundInfo.getRefundCode(), refundInfo.getPlatformOrderId()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求马帮退款信息接口
     *
     * @param dto
     * @return
     */
    private List<RefundOrderEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        return MabangApiUtils.queryRefundList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析退款订单数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public BiRefundInfoEntity initOrderInfoEntity(RefundOrderEntity refundOrderEntity) {
        BiRefundInfoEntity biRefundInfoEntity = new BiRefundInfoEntity();
        BeanUtil.copyProperties(refundOrderEntity, biRefundInfoEntity);
        biRefundInfoEntity.setRefundCode(refundOrderEntity.getId());
        //币别编号
        biRefundInfoEntity.setCurrencyCode(refundOrderEntity.getCurrencyId());
        //退款单号
        biRefundInfoEntity.setPlatformRefundCode(refundOrderEntity.getRefundplatformOrderId());
        biRefundInfoEntity.setPlatformOrderId(refundOrderEntity.getPlatformOrderId());
        //退货金额
        biRefundInfoEntity.setRefundAmount(refundOrderEntity.getApplyRefundMoney());
        //退款备注
        biRefundInfoEntity.setRefundRemark(refundOrderEntity.getNote());
        //退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
        biRefundInfoEntity.setRefundStatus(refundOrderEntity.getFlag());
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        //申请时间
        if (!"null".equalsIgnoreCase(refundOrderEntity.getCreateTime()) && StrUtil.isNotBlank(refundOrderEntity.getCreateTime())) {
            biRefundInfoEntity.setRefundCreateTime(LocalDateTime.parse(refundOrderEntity.getCreateTime(), sdf));
        }
        //店铺编号
        biRefundInfoEntity.setShopNo(refundOrderEntity.getShopId());
        //平台最后修改时间
        if (!"null".equalsIgnoreCase(refundOrderEntity.getUpdateTime()) && StrUtil.isNotBlank(refundOrderEntity.getUpdateTime())) {
            biRefundInfoEntity.setPlatformUpdateTime(LocalDateTime.parse(refundOrderEntity.getUpdateTime(), sdf));
        }
        //平台标识
        biRefundInfoEntity.setPlatformSign(PlatformEnum.MABANG.getDesc());
        biRefundInfoEntity.setCreateTime(LocalDateTime.now());
        biRefundInfoEntity.setItemList(initOrderItem(refundOrderEntity));
        return biRefundInfoEntity;
    }

    /**
     * 解析退款订单商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public List<BiRefundItemEntity> initOrderItem(RefundOrderEntity refundOrderEntity) {
        List<BiRefundItemEntity> orderItemList = new ArrayList<>();
        HashMap<String, Integer> skuCountMap = new HashMap<>();
        refundOrderEntity.getProductList().stream().forEach( refundOrderItemEntity -> {
            BiRefundItemEntity biRefundItemEntity = new BiRefundItemEntity();
            //sku编号
            String skuNo = refundOrderItemEntity.getRefundStock();
            biRefundItemEntity.setSkuNo(skuNo);
            //订单原始商品数量
            biRefundItemEntity.setQuantity(refundOrderItemEntity.getStock_quantity());
            //退款商品数量
            biRefundItemEntity.setRefundNum(refundOrderItemEntity.getRefund_num());
            //是否属于组合sku：0. 否 1. 是
            biRefundItemEntity.setIsCombo(refundOrderItemEntity.getIsCombo());
            biRefundItemEntity.setAmountAfter(BigDecimal.ZERO);
            //erp平台商品id
            String erpOrderItemId = refundOrderEntity.getPlatformOrderId() + "_" + refundOrderEntity.getRefundplatformOrderId() + "_" + skuNo;
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap,skuNo,erpOrderItemId);
            biRefundItemEntity.setErpOrderItemId(erpOrderItemId);
            orderItemList.add(biRefundItemEntity);
        });
       return orderItemList;
    }
}
