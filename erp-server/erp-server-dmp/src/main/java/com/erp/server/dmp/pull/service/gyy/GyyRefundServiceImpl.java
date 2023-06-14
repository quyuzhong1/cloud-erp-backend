package com.erp.server.dmp.pull.service.gyy;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.CountrySiteEnum;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.GyyRefundDTO;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.model.dmp.entity.DmpRefundItemEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.gyy.GyyRefundEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import com.erp.server.dmp.utils.GyyApiUtils;
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
    private MQProducerService<DmpRefundInfoEntity> mqProducerService;
    @Resource
    private DmpShopInfoService dmpShopInfoService;

    public static void main(String[] args) {
        GyyRefundServiceImpl gyyRefundService = new GyyRefundServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.GY_ERP_TRADE_REFUND_GET;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setApiId(11);
        jobTaskDTO.setApiName("管易云退款列表");
        jobTaskDTO.setId(35L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<GyyRefundEntity> orderEntities = null;
        try {
            orderEntities = gyyRefundService.pullDate(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(orderEntities);
    }


    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<GyyRefundEntity> gyyRefundEntityList = pullDate(dto);
        if (CollectionUtil.isEmpty(gyyRefundEntityList)) {
            log.info("拉取管易退款列表数据为空 gyyOrderEntityList.size = 0 ");
            return;
        }
        List<GyyRefundEntity> insertList = new ArrayList<>();
        List<GyyRefundEntity> pushToMqList = new ArrayList<>();
        for (GyyRefundEntity entity : gyyRefundEntityList) {
            GyyRefundDTO orderMongoDTO = new GyyRefundDTO(entity.getCode(), entity.getRefundCode());
            List<GyyRefundEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_REFUND, GyyRefundEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            GyyRefundEntity mongoDatum = mongoData.get(0);
            String id = mongoDatum.get_id();
            mongoDatum.set_id(null);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            GyyRefundDTO updateDto = new GyyRefundDTO(id);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_REFUND, GyyRefundEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            insertList = insertList.stream().distinct().collect(Collectors.toList());
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_GYY_REFUND);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("管易退款订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<DmpRefundInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());

        // 异步推送到MQ
        entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.GYY_REFUND_ORDER_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getRefundCode() + msg.getPlatformOrderId()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
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
    private DmpRefundInfoEntity initOrderInfoEntity(GyyRefundEntity gyyRefundEntity) {
        if (assertOrgIsVijim(gyyRefundEntity.getShopCode())){
            return null;
        }
        DmpRefundInfoEntity dmpRefundInfoEntity = new DmpRefundInfoEntity();
        // 退款单号
        dmpRefundInfoEntity.setRefundCode(gyyRefundEntity.getCode());
        //平台订单编号
        dmpRefundInfoEntity.setPlatformOrderId(gyyRefundEntity.getPlatfromCode());
        //平台退货单号
        dmpRefundInfoEntity.setPlatformRefundCode(gyyRefundEntity.getRefundCode());
        //币别编号
        dmpRefundInfoEntity.setCurrencyCode("CNY");
        //退货金额
        dmpRefundInfoEntity.setRefundAmount(gyyRefundEntity.getAmount());
        //退款类型：1、未收到货部分退款 2、未收到货全额退款 3、已收到货部分退款 4、已收到货全额退款
        /**
         * refund:仅退款
         * return:退货退款
         * deliveried_refund:发货后仅退款
         */
        dmpRefundInfoEntity.setRefundType(0);
        //退款原因
        dmpRefundInfoEntity.setRefundReasonDesc(gyyRefundEntity.getReason());
        //退款备注
        dmpRefundInfoEntity.setRefundRemark(gyyRefundEntity.getNote());
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
        dmpRefundInfoEntity.setRefundStatus(refundStatus);
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        //申请时间
        if (!"null".equalsIgnoreCase(gyyRefundEntity.getCreateDate()) && StrUtil.isNotBlank(gyyRefundEntity.getCreateDate())) {
            dmpRefundInfoEntity.setRefundCreateTime(LocalDateTime.parse(gyyRefundEntity.getCreateDate(), sdf));
        }

        //店铺编号
        dmpRefundInfoEntity.setShopNo(gyyRefundEntity.getShopCode());
        DmpShopInfoEntity shopInfo = dmpShopInfoService.getShopByShopNo(gyyRefundEntity.getShopCode(), PlatformEnum.GYY.getDesc());
        //店铺名称
        dmpRefundInfoEntity.setShopName(null != shopInfo ? shopInfo.getName() : "");
        //平台名称
        dmpRefundInfoEntity.setPlatformName("");
        //退款时间
        dmpRefundInfoEntity.setRefundTime(gyyRefundEntity.getAgreeDate());
        //汇率
        dmpRefundInfoEntity.setCurrencyRate(BigDecimal.ONE);
        //国家 二字码 例如：US
        dmpRefundInfoEntity.setCountryCode(CountrySiteEnum.CHINA.getSite());
        //国家中文名
        dmpRefundInfoEntity.setCountryCn(CountrySiteEnum.CHINA.getCurrencyName());
        //国家英文名
        dmpRefundInfoEntity.setCountryEn(CountrySiteEnum.CHINA.getCurrencyCode());
        //平台交易号
        dmpRefundInfoEntity.setSalesRecordNumber(gyyRefundEntity.getRefundCode());
        //买家用户Id
        dmpRefundInfoEntity.setBuyerUserId("");
        //买家用户名
        dmpRefundInfoEntity.setBuyerName("");
        //原始订单金额
        dmpRefundInfoEntity.setItemTotalOrigin(gyyRefundEntity.getAmount());
        //原始订单运费金额
        dmpRefundInfoEntity.setShippingTotalOrigin(BigDecimal.ZERO);
        //订单时间
        dmpRefundInfoEntity.setOrderTime(null);
        //发货时间
        dmpRefundInfoEntity.setExpressTime(null);
        //退货图片多个用英文 , 隔开
        dmpRefundInfoEntity.setPictureUrl("");
        //平台最后修改时间
        if (!"null".equalsIgnoreCase(gyyRefundEntity.getModifyDate()) && StrUtil.isNotBlank(gyyRefundEntity.getModifyDate())) {
            dmpRefundInfoEntity.setPlatformUpdateTime(LocalDateTime.parse(gyyRefundEntity.getModifyDate(), sdf));
        }
        //包裹单号
        dmpRefundInfoEntity.setTrackNumber("");
        //平台标识
        dmpRefundInfoEntity.setPlatformSign(PlatformEnum.GYY.getDesc());
        dmpRefundInfoEntity.setCreateTime(LocalDateTime.now());
        dmpRefundInfoEntity.setItemList(initOrderItem(gyyRefundEntity));
        dmpRefundInfoEntity.setCancel(gyyRefundEntity.getCancel());
        return dmpRefundInfoEntity;
    }

    /**
     * 解析退款订单商品数据
     **/
    private List<DmpRefundItemEntity> initOrderItem(GyyRefundEntity gyyRefundEntity) {
        List<DmpRefundItemEntity> orderItemList = new ArrayList<>();
        Map<String, Integer> skuCountMap = new HashMap<>();
        gyyRefundEntity.getDetails().forEach(refundDetailsBean -> {
            DmpRefundItemEntity dmpRefundItemEntity = new DmpRefundItemEntity();
            //sku编号
            dmpRefundItemEntity.setSkuNo(refundDetailsBean.getItemCode());
            //订单原始商品数量
            dmpRefundItemEntity.setQuantity(refundDetailsBean.getQty());
            //退款商品数量
            dmpRefundItemEntity.setRefundNum(refundDetailsBean.getQty());
            //是否属于组合sku：0. 否 1. 是
            dmpRefundItemEntity.setIsCombo(0);
            String skuNo = refundDetailsBean.getItemCode();
            String erpOrderItemId = gyyRefundEntity.getCode() + "_" + gyyRefundEntity.getRefundCode() + "_" + refundDetailsBean.getItemCode();
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, skuNo, erpOrderItemId);
            dmpRefundItemEntity.setErpOrderItemId(erpOrderItemId);
            //折扣后金额
            dmpRefundItemEntity.setAmountAfter(new BigDecimal(null != refundDetailsBean.getAmount() ? refundDetailsBean.getAmount() : "0"));
            orderItemList.add(dmpRefundItemEntity);
        });
        return orderItemList;
    }

    public boolean assertOrgIsVijim(String shopCode) {
        DmpShopInfoEntity shopInfo = dmpShopInfoService.getShopByShopNo(shopCode, PlatformEnum.GYY.getDesc());
//        return null != shopInfo && (ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(shopInfo.getUseOrgId().toString()) || ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(shopInfo.getUseOrgId().toString()));
        return null != shopInfo && StrUtil.isNotBlank(shopInfo.getName()) && (shopInfo.getName().contains("小隼") || shopInfo.getName().contains("优至胜"));
    }
}
