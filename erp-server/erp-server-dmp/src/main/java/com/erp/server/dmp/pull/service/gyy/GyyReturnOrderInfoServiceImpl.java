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
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import com.erp.model.dmp.entity.BiReturnOrderItemEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.gyy.GyyReturnOrderEntity;
import com.erp.model.dmp.gyy.bean.ReturnOrderDetailsBean;
import com.erp.model.dmp.gyy.bean.ReturnOrderPayments;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.erp.server.dmp.utils.GyyApiUtils;
import com.erp.server.dmp.utils.MapCountUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管易云退货订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GY_ERP_TRADE_RETURN_GET)
public class GyyReturnOrderInfoServiceImpl implements IReportSaveService<GyyReturnOrderEntity> {
    @Resource
    private MongoService mongoService;

    @Resource
    private MQProducerService<BiReturnOrderInfoEntity> mqProducerService;
    @Resource
    private CfgSettingService cfgSettingService;

    public static void main(String[] args) {
        GyyReturnOrderInfoServiceImpl gyyReturnOrderInfoService = new GyyReturnOrderInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.GY_ERP_TRADE_RETURN_GET;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setPlatformApiId("9");
        jobTaskDTO.setApiName("获取退货订单数据");
        jobTaskDTO.setId("34");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<GyyReturnOrderEntity> orderEntities = null;
        try {
            orderEntities = gyyReturnOrderInfoService.pullDate(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(orderEntities);
    }

    /**
     * 拉取退货订单数据
     *
     * @param dto 任务信息
     */
    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<GyyReturnOrderEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取管易退货订单列表数据为空 entityList.size = 0 ");
            return;
        }
        List<GyyReturnOrderEntity> insertList = new ArrayList<>();
        List<GyyReturnOrderEntity> pushToMqList = new ArrayList<>();
        //去重
        List<GyyReturnOrderEntity> entities = entityList.stream().distinct().collect(Collectors.toList());
        for (GyyReturnOrderEntity entity : entities) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByCode(entity.getCode());
            List<GyyReturnOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_RETURN_ORDER, GyyReturnOrderEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            GyyReturnOrderEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            entity.set_id(null);
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_RETURN_ORDER, GyyReturnOrderEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_GYY_RETURN_ORDER);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("管易退货订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<BiReturnOrderInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<BiReturnOrderInfoEntity> biReturnOrderInfoEntityList = entityToMqlist.stream().peek(msg -> {
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.GYY_RETURN_ORDER_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getReturnCode(), msg.getPlatformOrderId()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("gyyReturn发送数据为：{}" , JSON.toJSONString(biReturnOrderInfoEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<GyyReturnOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_GYY_RETURN_ORDER, GyyReturnOrderEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (GyyReturnOrderEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(GyyReturnOrderEntity mongoDatum) {
        BiReturnOrderInfoEntity returnOrderInfo = initOrderInfoEntity(mongoDatum);
        GyyRefundDTO updateDto = new GyyRefundDTO(mongoDatum.get_id());
        if(null == returnOrderInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_RETURN_ORDER, GyyReturnOrderEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_RETURN_ORDER, GyyReturnOrderEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.GYY_RETURN_ORDER_TAG.getName(),
                returnOrderInfo, StrUtil.format("{}_{}", returnOrderInfo.getReturnCode(), returnOrderInfo.getPlatformOrderId()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求管易云退货订单接口
     *
     * @param dto
     * @return
     */
    private List<GyyReturnOrderEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        return GyyApiUtils.queryReturnOrderList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析订单数据
     **/
    private BiReturnOrderInfoEntity initOrderInfoEntity(GyyReturnOrderEntity gyyReturnOrderEntity) {
        if (assertOrgIsVijim(gyyReturnOrderEntity.getShopName())){
            return null;
        }
        BiReturnOrderInfoEntity biReturnOrderInfoEntity = new BiReturnOrderInfoEntity();
        //退货单号
        biReturnOrderInfoEntity.setReturnCode(gyyReturnOrderEntity.getCode());
        //平台订单编号
        biReturnOrderInfoEntity.setPlatformOrderId(gyyReturnOrderEntity.getPlatformCode());
        // 平台退款单号
        biReturnOrderInfoEntity.setPlatformReturnCode(gyyReturnOrderEntity.getPlatformRefundId());
        // erp平台销售单号
        biReturnOrderInfoEntity.setOrderCode(gyyReturnOrderEntity.getOrderCode());
        //店铺编号
        biReturnOrderInfoEntity.setShopNo(gyyReturnOrderEntity.getShopCode());
        //店铺名称
        biReturnOrderInfoEntity.setShopName(gyyReturnOrderEntity.getShopName());
        List<ReturnOrderPayments> payments = gyyReturnOrderEntity.getPayments();
        if (payments.size() > 0) {
            //付款时间
            biReturnOrderInfoEntity.setPaidTime(payments.get(0).getPayTime());
        }
        //发货时间
        biReturnOrderInfoEntity.setExpressTime(null);
        //0:未处理 1:同意退货 2:拒绝退货
        Integer status = 4;
        if (gyyReturnOrderEntity.getAgreeRefuse() != null) {
            switch (gyyReturnOrderEntity.getAgreeRefuse()) {
                case 0:
                    status = 1;
                    break;
                case 1:
                    status = 2;
                    break;
                case 2:
                    status = 4;
                    break;
                default:
                    break;
            }
        }
        //状态：1待处理 2已退款 3已重发 4已完成 5已作废
        biReturnOrderInfoEntity.setStatus(status);
        //平台交易号
        biReturnOrderInfoEntity.setSalesRecordNumber(gyyReturnOrderEntity.getPlatformCode());
        List<ReturnOrderDetailsBean> details = gyyReturnOrderEntity.getDetails();
        BigDecimal amount = new BigDecimal(BigInteger.ZERO);
        for (ReturnOrderDetailsBean detail : details) {
            amount = amount.add(detail.getAmount());
        }
        //订单金额
        biReturnOrderInfoEntity.setOrderFee(amount);
        //订单重量
        biReturnOrderInfoEntity.setOrderWeight(new BigDecimal(BigInteger.ZERO));
        //平台名称
        biReturnOrderInfoEntity.setPlatformName("");
        //国家英文名称
        biReturnOrderInfoEntity.setCountryNameEn(CountrySiteEnum.CHINA.getCurrencyCode());
        //国家英文名称
        biReturnOrderInfoEntity.setCountryNameCn(CountrySiteEnum.CHINA.getCurrencyName());
        //买家账号
        biReturnOrderInfoEntity.setBuyerUserId(gyyReturnOrderEntity.getVipCode());
        //买家姓名
        biReturnOrderInfoEntity.setBuyerName(gyyReturnOrderEntity.getReceiverName());
        //登记人编号
        biReturnOrderInfoEntity.setEmployeeId("");
        //登记人名称
        biReturnOrderInfoEntity.setEmployeeName(gyyReturnOrderEntity.getBusinessMan());
        //备注
        biReturnOrderInfoEntity.setRemark(StrUtil.format("Note = {}_refundCodes =【{}】", gyyReturnOrderEntity.getNote(), JSONUtil.toJsonStr(gyyReturnOrderEntity.getRefundCodes())));
        //退货信息创建时间
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        if (!"null".equalsIgnoreCase(gyyReturnOrderEntity.getCreateDate()) && StrUtil.isNotBlank(gyyReturnOrderEntity.getCreateDate())) {
            biReturnOrderInfoEntity.setReturnCreateTime(LocalDateTime.parse(gyyReturnOrderEntity.getCreateDate(), sdf));
        }
        //退款时间
        if (!"null".equalsIgnoreCase(gyyReturnOrderEntity.getApproveDate()) && StrUtil.isNotBlank(gyyReturnOrderEntity.getApproveDate())) {
            biReturnOrderInfoEntity.setRefundTime(LocalDateTime.parse(gyyReturnOrderEntity.getApproveDate(), sdf));
        }
        //币种
        biReturnOrderInfoEntity.setCurrencyCode("CNY");
        //汇率
        biReturnOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);
        //平台标识
        biReturnOrderInfoEntity.setPlatformSign(PlatformEnum.GYY.getDesc());
        biReturnOrderInfoEntity.setIsDeleted(Boolean.FALSE);
        biReturnOrderInfoEntity.setCreateTime(LocalDateTime.now());
        biReturnOrderInfoEntity.setItemList(initOrderItem(gyyReturnOrderEntity));
        return biReturnOrderInfoEntity;
    }

    /**
     * 解析退货订单商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    private List<BiReturnOrderItemEntity> initOrderItem(GyyReturnOrderEntity gyyReturnOrderEntity) {
        List<BiReturnOrderItemEntity> orderItemList = new ArrayList<>();
        Map<String, Integer> skuCountMap = new HashMap<>();
        gyyReturnOrderEntity.getDetails().stream().forEach(orderItemBean -> {
            BiReturnOrderItemEntity biReturnOrderItemEntity = new BiReturnOrderItemEntity();
            //sku编号
            String skuNo = orderItemBean.getItemCode();
            biReturnOrderItemEntity.setSkuNo(skuNo);
            //商品名称
            biReturnOrderItemEntity.setItemName(orderItemBean.getItemName());
            //买家购买数量
            biReturnOrderItemEntity.setQuantity(orderItemBean.getQty());
            //商品单位
            biReturnOrderItemEntity.setProductUnit("");
            //商品图片地址
            biReturnOrderItemEntity.setPictureUrl("");
            //售价
            biReturnOrderItemEntity.setSellPrice(orderItemBean.getTotalCostPrice());
            //物品属性
            biReturnOrderItemEntity.setSpecifics("");
            //状态 1待处理 2验货入库 3自然耗损
            biReturnOrderItemEntity.setStatus(0);
            //erp平台商品id
            String erpOrderItemId = gyyReturnOrderEntity.getCode() + "_" + gyyReturnOrderEntity.getPlatformCode() + "_" + skuNo;
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, skuNo, erpOrderItemId);
            biReturnOrderItemEntity.setErpOrderItemId(erpOrderItemId);
            biReturnOrderItemEntity.setIsDeleted(Boolean.FALSE);
            //折扣后金额
            biReturnOrderItemEntity.setAmountAfter(new BigDecimal(orderItemBean.getAmountAfter()));
            orderItemList.add(biReturnOrderItemEntity);
        });
       return orderItemList;
    }
    private boolean assertOrgIsVijim(String shopCode) {
        return StrUtil.isNotBlank(shopCode) && (shopCode.contains("小隼") || shopCode.contains("优至胜"));
//        DmpShopInfoEntity shopInfo = dmpShopInfoService.getShopByShopNo(shopCode, PlatformEnum.GYY.getDesc());
//        return null != shopInfo && (ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(shopInfo.getUseOrgId().toString()) || ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(shopInfo.getUseOrgId().toString()));
    }

}
