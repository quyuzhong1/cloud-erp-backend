package com.erp.server.dmp.pull.service.gyy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.message.constant.RocketMqTopic;
import com.common.core.enums.CountrySiteEnum;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.gyy.GyyReturnOrderEntity;
import com.erp.model.dmp.gyy.bean.ReturnOrderDetailsBean;
import com.erp.model.dmp.gyy.bean.ReturnOrderPayments;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.common.message.service.mq.MQProducerService;
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
    private MQProducerService<DmpReturnOrderInfoEntity> mqProducerService;

    public static void main(String[] args) {
        GyyReturnOrderInfoServiceImpl gyyReturnOrderInfoService = new GyyReturnOrderInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.GY_ERP_TRADE_RETURN_GET;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setApiId(9);
        jobTaskDTO.setApiName("获取退货订单数据");
        jobTaskDTO.setId(34L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
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
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<GyyReturnOrderEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取管易退货订单列表数据为空 entityList.size = 0 ");
            return;
        }
        List<GyyReturnOrderEntity> insertList = new ArrayList<>();
        List<GyyReturnOrderEntity> pushToMqList = new ArrayList<>();
        for (GyyReturnOrderEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = new OrderMongoDTO(entity.getPlatformCode(), entity.getCode());
            List<GyyReturnOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_RETURN_ORDER, GyyReturnOrderEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            GyyReturnOrderEntity mongoDatum = mongoData.get(0);
            String id = mongoDatum.get_id();
            mongoDatum.set_id(null);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(id);
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
        List<DmpReturnOrderInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        entityToMqlist.stream().peek(msg -> {
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.GYY_RETURN_ORDER_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getPlatformOrderId(), msg.getSalesRecordNumber()));
            if (!SendStatus.SEND_OK .equals(result.getSendStatus())){
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
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
    private DmpReturnOrderInfoEntity initOrderInfoEntity(GyyReturnOrderEntity gyyReturnOrderEntity) {
        DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = new DmpReturnOrderInfoEntity();
        //平台订单编号
        dmpReturnOrderInfoEntity.setPlatformOrderId(gyyReturnOrderEntity.getCode());
        //退货单号
        dmpReturnOrderInfoEntity.setReturnOrderId(gyyReturnOrderEntity.getPlatformRefundId());
        //店铺编号
        dmpReturnOrderInfoEntity.setShopNo(gyyReturnOrderEntity.getShopCode());
        //店铺名称
        dmpReturnOrderInfoEntity.setShopName(gyyReturnOrderEntity.getShopName());
        List<ReturnOrderPayments> payments = gyyReturnOrderEntity.getPayments();
        if (payments.size() > 0) {
            //付款时间
            dmpReturnOrderInfoEntity.setPaidTime(payments.get(0).getPayTime());
        }
        //发货时间
        dmpReturnOrderInfoEntity.setExpressTime(null);
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
        dmpReturnOrderInfoEntity.setStatus(status);
        //平台交易号
        dmpReturnOrderInfoEntity.setSalesRecordNumber(gyyReturnOrderEntity.getPlatformCode());
        List<ReturnOrderDetailsBean> details = gyyReturnOrderEntity.getDetails();
        BigDecimal amount = new BigDecimal(BigInteger.ZERO);
        for (ReturnOrderDetailsBean detail : details) {
            amount = amount.add(detail.getAmount());
        }
        //订单金额
        dmpReturnOrderInfoEntity.setOrderFee(amount);
        //订单重量
        dmpReturnOrderInfoEntity.setOrderWeight(new BigDecimal(BigInteger.ZERO));
        //平台名称
        dmpReturnOrderInfoEntity.setPlatformName("");
        //国家英文名称
        dmpReturnOrderInfoEntity.setCountryNameEn(CountrySiteEnum.CHINA.getCurrencyName());
        //国家英文名称
        dmpReturnOrderInfoEntity.setCountryNameCn(CountrySiteEnum.CHINA.getCurrencyCode());
        //买家账号
        dmpReturnOrderInfoEntity.setBuyerUserId(gyyReturnOrderEntity.getVipCode());
        //买家姓名
        dmpReturnOrderInfoEntity.setBuyerName(gyyReturnOrderEntity.getReceiverName());
        //登记人编号
        dmpReturnOrderInfoEntity.setEmployeeId("");
        //登记人名称
        dmpReturnOrderInfoEntity.setEmployeeName(gyyReturnOrderEntity.getBusinessMan());
        //备注
        dmpReturnOrderInfoEntity.setRemark(gyyReturnOrderEntity.getNote());
        //退货信息创建时间
        dmpReturnOrderInfoEntity.setReturnCreateTime(gyyReturnOrderEntity.getCreateDate());
        //退款时间
        dmpReturnOrderInfoEntity.setRefundTime(gyyReturnOrderEntity.getApproveDate());
        //币种
        dmpReturnOrderInfoEntity.setCurrencyCode("CNY");
        //汇率
        dmpReturnOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);
        //平台标识
        dmpReturnOrderInfoEntity.setPlatformSign(PlatformEnum.GYY.getDesc());
        dmpReturnOrderInfoEntity.setIsDeleted(Boolean.FALSE);
        dmpReturnOrderInfoEntity.setCreateTime(LocalDateTime.now());
        dmpReturnOrderInfoEntity.setItemList(initOrderItem(gyyReturnOrderEntity));
        return dmpReturnOrderInfoEntity;
    }

    /**
     * 解析退货订单商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    private List<DmpReturnOrderItemEntity> initOrderItem(GyyReturnOrderEntity gyyReturnOrderEntity) {
        List<DmpReturnOrderItemEntity> orderItemList = new ArrayList<>();
        Map<String, Integer> skuCountMap = new HashMap<>();
        gyyReturnOrderEntity.getDetails().stream().forEach(orderItemBean -> {
            DmpReturnOrderItemEntity dmpReturnOrderItemEntity = new DmpReturnOrderItemEntity();
            //sku编号
            String skuNo = orderItemBean.getItemCode();
            dmpReturnOrderItemEntity.setSkuNo(skuNo);
            //商品名称
            dmpReturnOrderItemEntity.setItemName(orderItemBean.getItemName());
            //买家购买数量
            dmpReturnOrderItemEntity.setQuantity(orderItemBean.getQty());
            //商品单位
            dmpReturnOrderItemEntity.setProductUnit("");
            //商品图片地址
            dmpReturnOrderItemEntity.setPictureUrl("");
            //售价
            dmpReturnOrderItemEntity.setSellPrice(orderItemBean.getTotalCostPrice());
            //物品属性
            dmpReturnOrderItemEntity.setSpecifics("");
            //状态 1待处理 2验货入库 3自然耗损
            dmpReturnOrderItemEntity.setStatus(0);
            //erp平台商品id
            String erpOrderItemId = gyyReturnOrderEntity.getCode() + "_" + gyyReturnOrderEntity.getPlatformCode() + "_" + skuNo;
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, skuNo, erpOrderItemId);
            dmpReturnOrderItemEntity.setErpOrderItemId(erpOrderItemId);
            dmpReturnOrderItemEntity.setIsDeleted(Boolean.FALSE);
            //折扣后金额
            dmpReturnOrderItemEntity.setAmountAfter(new BigDecimal(orderItemBean.getAmountAfter()));
            orderItemList.add(dmpReturnOrderItemEntity);
        });
       return orderItemList;
    }


}
