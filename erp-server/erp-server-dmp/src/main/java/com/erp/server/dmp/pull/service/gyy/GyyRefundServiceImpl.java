package com.erp.server.dmp.pull.service.gyy;


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
import com.common.core.enums.CountrySiteEnum;
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
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.gyy.GyyRefundEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.BiDmpShopInfoService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.erp.server.dmp.utils.GyyApiUtils;
import com.erp.server.dmp.utils.MapCountUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管易云退款列表
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GY_ERP_TRADE_REFUND_GET)
public class GyyRefundServiceImpl implements IReportSaveService<GyyRefundEntity> {
    @Resource
    private MongoService mongoService;

    @Autowired
    private MQProducerService<BiRefundInfoEntity> mqProducerService;
    @Resource
    private BiDmpShopInfoService biDmpShopInfoService;
    @Resource
    private CfgSettingService cfgSettingService;

    public static void main(String[] args) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time = LocalDateTime.now();
        String startTime = "2024-03-29 18:00:00";
        String endTime = "2024-03-29 21:00:00";
        LocalDateTime start = LocalDateTime.parse(startTime,df);
        LocalDateTime end = LocalDateTime.parse(endTime,df);

        GyyRefundServiceImpl gyyRefundService = new GyyRefundServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.GY_ERP_TRADE_REFUND_GET;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setPlatformApiId("11");
        jobTaskDTO.setApiName("管易云退款列表");
        jobTaskDTO.setId("35");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(start);
        jobTaskDTO.setNextTime(end);
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<GyyRefundEntity> orderEntities = null;
        try {
            orderEntities = gyyRefundService.pullDate(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        for (GyyRefundEntity orderEntity : orderEntities) {
            if (orderEntity.getCode().equals("RMO717930794631")) {
                System.out.println(JSONUtil.toJsonStr(orderEntity));
            }
        }
    }


    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<GyyRefundEntity> gyyRefundEntityList = pullDate(dto);
        if (CollectionUtil.isEmpty(gyyRefundEntityList)) {
            log.info("拉取管易退款列表数据为空 gyyOrderEntityList.size = 0 ");
            return;
        }
        List<GyyRefundEntity> insertList = new ArrayList<>();
        List<GyyRefundEntity> pushToMqList = new ArrayList<>();
        //去重
        List<GyyRefundEntity> gyyRefundEntities = gyyRefundEntityList.stream().distinct().collect(Collectors.toList());
        for (GyyRefundEntity entity : gyyRefundEntities) {
            GyyRefundDTO orderMongoDTO = new GyyRefundDTO(entity.getCode(), entity.getRefundCode());
            List<GyyRefundEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_REFUND, GyyRefundEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            GyyRefundEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            GyyRefundDTO updateDto = new GyyRefundDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_REFUND, GyyRefundEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_GYY_REFUND);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("管易退款订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<BiRefundInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<BiRefundInfoEntity> biRefundInfoEntityList = entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.GYY_REFUND_ORDER_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getRefundCode() + msg.getPlatformOrderId()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("gyyRefund发送数据为：{}" , JSON.toJSONString(biRefundInfoEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<GyyRefundEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_GYY_REFUND, GyyRefundEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (GyyRefundEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(GyyRefundEntity mongoDatum) {
        BiRefundInfoEntity refundInfo = initOrderInfoEntity(mongoDatum);
        GyyRefundDTO updateDto = new GyyRefundDTO(mongoDatum.get_id());
        if(null == refundInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_REFUND, GyyRefundEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_REFUND, GyyRefundEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.GYY_REFUND_ORDER_TAG.getName(),
                refundInfo, StrUtil.format("{}_{}", refundInfo.getRefundCode() + refundInfo.getPlatformOrderId()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }
    /**
     * 请求管易云退款信息接口
     * @param dto
     * @return
     */
    private List<GyyRefundEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        return GyyApiUtils.queryRefundList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析退款订单数据
     **/
    private BiRefundInfoEntity initOrderInfoEntity(GyyRefundEntity gyyRefundEntity) {
        if (assertOrgIsVijim(gyyRefundEntity.getShopCode())){
            return null;
        }
        BiRefundInfoEntity biRefundInfoEntity = new BiRefundInfoEntity();
        // 退款单号
        biRefundInfoEntity.setRefundCode(gyyRefundEntity.getCode());
        //平台订单编号
        biRefundInfoEntity.setPlatformOrderId(gyyRefundEntity.getPlatfromCode());
        //平台退货单号
        biRefundInfoEntity.setPlatformRefundCode(gyyRefundEntity.getRefundCode());
        //币别编号
        biRefundInfoEntity.setCurrencyCode("CNY");
        //退货金额
        biRefundInfoEntity.setRefundAmount(gyyRefundEntity.getAmount());
        //退款类型：1、未收到货部分退款 2、未收到货全额退款 3、已收到货部分退款 4、已收到货全额退款
        /**
         * refund:仅退款
         * return:退货退款
         * deliveried_refund:发货后仅退款
         */
        biRefundInfoEntity.setRefundType(0);
        //退款原因
        biRefundInfoEntity.setRefundReasonDesc(gyyRefundEntity.getReason());
        //退款备注
        biRefundInfoEntity.setRefundRemark(gyyRefundEntity.getNote());
        int refundStatus = 4;
        if (gyyRefundEntity.getApprove()) {
            refundStatus = 3;
        } else {
            refundStatus = 2;
        }
        if (gyyRefundEntity.getCancel()) {
            refundStatus = 6;
        }
        if (null != gyyRefundEntity.getAgreeRefuse()) {
            int agreeRefuse = gyyRefundEntity.getAgreeRefuse();
            if (agreeRefuse == 1) {
                refundStatus = 4;
            } else if (agreeRefuse == 2) {
                refundStatus = 5;
            }
        }
        //退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
        biRefundInfoEntity.setRefundStatus(refundStatus);
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        //申请时间
        if (!"null".equalsIgnoreCase(gyyRefundEntity.getCreateDate()) && StrUtil.isNotBlank(gyyRefundEntity.getCreateDate())) {
            biRefundInfoEntity.setRefundCreateTime(LocalDateTime.parse(gyyRefundEntity.getCreateDate(), sdf));
        }

        //店铺编号
        biRefundInfoEntity.setShopNo(gyyRefundEntity.getShopCode());
        BiShopInfoEntity shopInfo = biDmpShopInfoService.getShopByShopNo(gyyRefundEntity.getShopCode());
        //店铺名称
        biRefundInfoEntity.setShopName(null != shopInfo ? shopInfo.getName() : "");
        //平台名称
        biRefundInfoEntity.setPlatformName("");
        //退款时间
        biRefundInfoEntity.setRefundTime(gyyRefundEntity.getAgreeDate());
        //汇率
        biRefundInfoEntity.setCurrencyRate(BigDecimal.ONE);
        //国家 二字码 例如：US
        biRefundInfoEntity.setCountryCode(CountrySiteEnum.CHINA.getSite());
        //国家中文名
        biRefundInfoEntity.setCountryCn(CountrySiteEnum.CHINA.getCurrencyName());
        //国家英文名
        biRefundInfoEntity.setCountryEn(CountrySiteEnum.CHINA.getCurrencyCode());
        //平台交易号
        biRefundInfoEntity.setSalesRecordNumber(gyyRefundEntity.getRefundCode());
        //买家用户Id
        biRefundInfoEntity.setBuyerUserId("");
        //买家用户名
        biRefundInfoEntity.setBuyerName("");
        //原始订单金额
        biRefundInfoEntity.setItemTotalOrigin(gyyRefundEntity.getAmount());
        //原始订单运费金额
        biRefundInfoEntity.setShippingTotalOrigin(BigDecimal.ZERO);
        //订单时间
        biRefundInfoEntity.setOrderTime(null);
        //发货时间
        biRefundInfoEntity.setExpressTime(null);
        //退货图片多个用英文 , 隔开
        biRefundInfoEntity.setPictureUrl("");
        //平台最后修改时间
        if (!"null".equalsIgnoreCase(gyyRefundEntity.getModifyDate()) && StrUtil.isNotBlank(gyyRefundEntity.getModifyDate())) {
            biRefundInfoEntity.setPlatformUpdateTime(LocalDateTime.parse(gyyRefundEntity.getModifyDate(), sdf));
        }
        //包裹单号
        biRefundInfoEntity.setTrackNumber("");
        //平台标识
        biRefundInfoEntity.setPlatformSign(PlatformEnum.GYY.getDesc());
        biRefundInfoEntity.setCreateTime(LocalDateTime.now());
        biRefundInfoEntity.setItemList(initOrderItem(gyyRefundEntity));
        biRefundInfoEntity.setCancel(gyyRefundEntity.getCancel());
        return biRefundInfoEntity;
    }

    /**
     * 解析退款订单商品数据
     **/
    private List<BiRefundItemEntity> initOrderItem(GyyRefundEntity gyyRefundEntity) {
        List<BiRefundItemEntity> orderItemList = new ArrayList<>();
        Map<String, Integer> skuCountMap = new HashMap<>();
        gyyRefundEntity.getDetails().forEach(refundDetailsBean -> {
            BiRefundItemEntity biRefundItemEntity = new BiRefundItemEntity();
            //sku编号
            biRefundItemEntity.setSkuNo(refundDetailsBean.getItemCode());
            //订单原始商品数量
            biRefundItemEntity.setQuantity(refundDetailsBean.getQty());
            //退款商品数量
            biRefundItemEntity.setRefundNum(refundDetailsBean.getQty());
            //是否属于组合sku：0. 否 1. 是
            biRefundItemEntity.setIsCombo(0);
            String skuNo = refundDetailsBean.getItemCode();
            String erpOrderItemId = gyyRefundEntity.getCode() + "_" + gyyRefundEntity.getRefundCode() + "_" + refundDetailsBean.getItemCode();
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, skuNo, erpOrderItemId);
            biRefundItemEntity.setErpOrderItemId(erpOrderItemId);
            //折扣后金额
            biRefundItemEntity.setAmountAfter(new BigDecimal(null != refundDetailsBean.getAmount() ? refundDetailsBean.getAmount() : "0"));
            orderItemList.add(biRefundItemEntity);
        });
        return orderItemList;
    }

    private boolean assertOrgIsVijim(String shopCode) {
        BiShopInfoEntity shopInfo = biDmpShopInfoService.getShopByShopNo(shopCode);
//        return null != shopInfo && (ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(shopInfo.getUseOrgId().toString()) || ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(shopInfo.getUseOrgId().toString()));
        return null != shopInfo && StrUtil.isNotBlank(shopInfo.getName()) && (shopInfo.getName().contains("小隼") || shopInfo.getName().contains("优至胜"));
    }
}
