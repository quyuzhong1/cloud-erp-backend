package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.SaveData;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import com.erp.model.dmp.entity.BiReturnOrderItemEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.mabang.ReturnOrderEntity;
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
 * 马帮退货订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ORDER_GET_RETURN_ORDER_LIST)
public class MabangReturnOrderInfoServiceImpl implements IReportSaveService<ReturnOrderEntity> {
    @Resource
    private MongoService mongoService;

    @Autowired
    private MQProducerService<BiReturnOrderInfoEntity> mqProducerService;
    @Resource
    private CfgSettingService cfgSettingService;

    public static void main(String[] args) {
        MabangReturnOrderInfoServiceImpl getOrderInfoService = new MabangReturnOrderInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.ORDER_GET_RETURN_ORDER_LIST;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<ReturnOrderEntity> orderEntities = null;
        try {
            orderEntities = getOrderInfoService.pullDate(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(orderEntities);
    }
    /**
     * 拉取退货订单数据
     * @param dto 任务信息
     * @return
     */
    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<ReturnOrderEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮退货订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮退货订单列表数据 entityList.size = {} ", entityList.size());
        List<ReturnOrderEntity> insertList = new ArrayList<>();
        List<ReturnOrderEntity> pushToMqList = new ArrayList<>();
        for (ReturnOrderEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByOrderIdAndSaleNum(entity.getPlatformOrderId(), entity.getSalesRecordNumber());
            List<ReturnOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_RETURN_ORDER, ReturnOrderEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            ReturnOrderEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_RETURN_ORDER, ReturnOrderEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_RETURN_ORDER);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("马帮退货订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<BiReturnOrderInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<BiReturnOrderInfoEntity> biReturnOrderInfoEntityList = entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_RETURN_ORDER_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getReturnCode(), msg.getPlatformOrderId()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("马帮退货订单：{}" , biReturnOrderInfoEntityList);

    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<ReturnOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_MABANG_RETURN_ORDER, ReturnOrderEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (ReturnOrderEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(ReturnOrderEntity mongoDatum) {
        BiReturnOrderInfoEntity returnOrderInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
        if(null == returnOrderInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_RETURN_ORDER, ReturnOrderEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_RETURN_ORDER, ReturnOrderEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_RETURN_ORDER_TAG.getName(),
                returnOrderInfo, StrUtil.format("{}_{}", returnOrderInfo.getReturnCode(), returnOrderInfo.getPlatformOrderId()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求马帮退货订单接口
     * @param dto
     * @return
     */
    private List<ReturnOrderEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        return MabangApiUtils.queryReturnOrderList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析订单数据
     **/
    private BiReturnOrderInfoEntity initOrderInfoEntity(ReturnOrderEntity returnOrderEntity) {
        BiReturnOrderInfoEntity biReturnOrderInfoEntity = new BiReturnOrderInfoEntity();
        BeanUtil.copyProperties(returnOrderEntity, biReturnOrderInfoEntity);
        //币种
        biReturnOrderInfoEntity.setCurrencyCode(returnOrderEntity.getCurrencyId());
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        //退货信息创建时间
        if (!"null".equalsIgnoreCase(returnOrderEntity.getCreateDate()) && StrUtil.isNotBlank(returnOrderEntity.getCreateDate())) {
            biReturnOrderInfoEntity.setReturnCreateTime(LocalDateTime.parse(returnOrderEntity.getCreateDate(), sdf));
        }
        //国家英文名称
        biReturnOrderInfoEntity.setCountryNameEn(returnOrderEntity.getCountryNameEN());
        //国家中文名称
        biReturnOrderInfoEntity.setCountryNameCn(returnOrderEntity.getCountryNameCN());
        // 平台名称
        biReturnOrderInfoEntity.setPlatformName(returnOrderEntity.getPlatformId());
        //退货单号
        biReturnOrderInfoEntity.setPlatformOrderId(returnOrderEntity.getPlatformOrderId());
        // 平台退款单号
        biReturnOrderInfoEntity.setPlatformReturnCode(returnOrderEntity.getPlatformReturnOrder());
        // 马帮退款单号
        biReturnOrderInfoEntity.setReturnCode(returnOrderEntity.getReturnOrderId());
        //店铺编号
        biReturnOrderInfoEntity.setShopNo(returnOrderEntity.getShopId());
        //平台标识
        biReturnOrderInfoEntity.setPlatformSign(PlatformEnum.MABANG.getDesc());
        biReturnOrderInfoEntity.setIsDeleted(null != returnOrderEntity.getStatus() && 5 == returnOrderEntity.getStatus());
        biReturnOrderInfoEntity.setCreateTime(LocalDateTime.now());
        biReturnOrderInfoEntity.setItemList(initOrderItem(returnOrderEntity));
        return biReturnOrderInfoEntity;
    }

    /**
     * 解析退货订单商品数据
     **/
    public List<BiReturnOrderItemEntity> initOrderItem(ReturnOrderEntity returnOrderEntity) {
        List<BiReturnOrderItemEntity> orderItemList = new ArrayList<>();
        HashMap<String, Integer> skuCountMap = new HashMap<>();
        returnOrderEntity.getItem().stream().forEach(orderItemBean -> {
            BiReturnOrderItemEntity biReturnOrderItemEntity = new BiReturnOrderItemEntity();
            //sku编号
            String skuNo = orderItemBean.getStockSku();
            biReturnOrderItemEntity.setSkuNo(skuNo);
            //商品名称
            biReturnOrderItemEntity.setItemName(orderItemBean.getTitle());
            //买家购买数量
            biReturnOrderItemEntity.setQuantity(orderItemBean.getQuantity());
            //商品单位
            biReturnOrderItemEntity.setProductUnit(orderItemBean.getProductUnit());
            //商品图片地址
            biReturnOrderItemEntity.setPictureUrl(orderItemBean.getPictureUrl());
            //售价
            biReturnOrderItemEntity.setSellPrice(orderItemBean.getSellPrice());
            //物品属性
            biReturnOrderItemEntity.setSpecifics(orderItemBean.getSpecifics());
            //状态 1待处理 2验货入库 3自然耗损
            biReturnOrderItemEntity.setStatus(orderItemBean.getStatus());
            biReturnOrderItemEntity.setIsDeleted(null != returnOrderEntity.getStatus() && 5 == returnOrderEntity.getStatus());
            //erp平台商品id
            String erpOrderItemId = returnOrderEntity.getPlatformOrderId() + "_" + returnOrderEntity.getSalesRecordNumber() + "_" + skuNo;
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, skuNo, erpOrderItemId);
            biReturnOrderItemEntity.setErpOrderItemId(erpOrderItemId);
            biReturnOrderItemEntity.setAmountAfter(orderItemBean.getSellPrice().multiply(new BigDecimal(orderItemBean.getQuantity())));
            orderItemList.add(biReturnOrderItemEntity);
        });
        return orderItemList;
    }
}
