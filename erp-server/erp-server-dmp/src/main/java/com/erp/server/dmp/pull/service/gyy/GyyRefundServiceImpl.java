package com.erp.server.dmp.pull.service.gyy;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.constant.RocketMqTopic;
import com.common.core.enums.CountrySiteEnum;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.RocketMqTagEnum;
import com.erp.model.dmp.dto.GyyRefundDTO;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.model.dmp.entity.DmpRefundItemEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.gyy.GyyRefundEntity;
import com.erp.model.dmp.gyy.bean.RefundDetailsBean;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpRefundInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpRefundItemService;
import com.erp.server.dmp.service.mq.MQProducerService;
import com.erp.server.dmp.utils.GyyApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Resource
    private DmpRefundInfoService dmpRefundInfoService;

    @Resource
    private DmpRefundItemService dmpRefundItemService;

    @Autowired
    private MQProducerService<DmpRefundInfoEntity> rocketMQTemplate;
    @Resource
    @Qualifier("gyyRefundServiceImpl")
    private IReportSaveService reportSaveService;

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
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<GyyRefundEntity> gyyRefundEntityList = pullDate(dto);
        if (CollectionUtil.isEmpty(gyyRefundEntityList)) {
            log.info("拉取管易退款列表数据为空 gyyOrderEntityList.size = 0 ");
            return;
        }
        List<GyyRefundEntity> insertList = new ArrayList<>();
        List<GyyRefundEntity> pushToMqList = new ArrayList<>();
        for (GyyRefundEntity entity : gyyRefundEntityList) {
            GyyRefundDTO orderMongoDTO = new GyyRefundDTO(entity.getPlatfromCode(), entity.getRefundCode());
            List<GyyRefundEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_REFUND, GyyRefundEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            GyyRefundEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
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
        // 构造订单结构
        List<DmpRefundInfoEntity> mabangToMqlist = pushToMqList.parallelStream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        mabangToMqlist.stream().peek(msg ->
                        rocketMQTemplate.asyncClassMsg(RocketMqTopic.DMP_TOPIC, RocketMqTagEnum.GYY_REFUND_ORDER_TAG.getName(),
                                msg, StrUtil.format("{}_{}",msg.getPlatformOrderId() + msg.getSalesRecordNumber())))
                .collect(Collectors.toList());
    }

    /**
     * 请求管易云退款信息接口
     * @param dto
     * @return
     */
    private List<GyyRefundEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        return GyyApiUtils.queryRefundList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析退款订单数据
     **/
    private DmpRefundInfoEntity initOrderInfoEntity(GyyRefundEntity gyyRefundEntity) {
        DmpRefundInfoEntity dmpRefundInfoEntity = new DmpRefundInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());
        //平台订单编号
        dmpRefundInfoEntity.setPlatformOrderId(gyyRefundEntity.getCode());
        //退货单号
        dmpRefundInfoEntity.setRefundId(gyyRefundEntity.getRefundCode());
        //币别编号
        dmpRefundInfoEntity.setCurrencyCode("CNY");
        //退货金额
        dmpRefundInfoEntity.setRefundAmount(gyyRefundEntity.getAmount());
        //退款类型：1、未收到货部分退款 2、未收到货全额退款 3、已收到货部分退款 4、已收到货全额退款
        dmpRefundInfoEntity.setRefundType(0);
        //退款原因
        dmpRefundInfoEntity.setRefundReasonDesc(gyyRefundEntity.getReason());
        //退款备注
        dmpRefundInfoEntity.setRefundRemark(gyyRefundEntity.getNote());
        Integer refundStatus = 4;
        if (gyyRefundEntity.getApprove()) {
            refundStatus = 3;
        } else {
            refundStatus = 2;
        }
        if (gyyRefundEntity.getCancel()) {
            refundStatus = 6;
        }
        if (gyyRefundEntity.getCancel()) {
            refundStatus = 6;
        }
        if (null != gyyRefundEntity.getAgreeRefuse()) {
            Integer agreeRefuse = gyyRefundEntity.getAgreeRefuse();
            if (agreeRefuse == 1) {
                refundStatus = 4;
            } else if (agreeRefuse == 1) {
                refundStatus = 5;
            }
        }
        //退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
        dmpRefundInfoEntity.setRefundStatus(refundStatus);
        //申请时间
        dmpRefundInfoEntity.setRefundCreateTime(gyyRefundEntity.getCreateDate());
        //店铺编号
        dmpRefundInfoEntity.setShopNo(gyyRefundEntity.getShopId());
        //店铺名称
        dmpRefundInfoEntity.setShopName(gyyRefundEntity.getShopCode());
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
        dmpRefundInfoEntity.setPlatformUpdateTime(gyyRefundEntity.getModifyDate());
        //包裹单号
        dmpRefundInfoEntity.setTrackNumber("");
        //平台标识
        dmpRefundInfoEntity.setPlatformSign(PlatformEnum.GYY.getDesc());
        dmpRefundInfoEntity.setCreateTime(LocalDateTime.now());
        dmpRefundInfoEntity.setItemList(initOrderItem(gyyRefundEntity));
        return dmpRefundInfoEntity;
    }

    /**
     *         //新增订单信息
     *         String refundInfoId = dmpRefundInfoService.checkOrder(dmpRefundInfoEntity);
     *         if (StringUtils.isNotBlank(refundInfoId)) {
     *             //新增订单商品信息
     *             analysisRefundOrderItem(gyyRefundEntity.getDetails(), refundInfoId);
     *         }
     */

    /**
     * 解析退款订单商品数据
     **/
    private List<DmpRefundItemEntity> initOrderItem(GyyRefundEntity gyyRefundEntity) {
        List<DmpRefundItemEntity> orderItemList = new ArrayList<>();
        gyyRefundEntity.getDetails().stream().forEach(refundDetailsBean -> {
            DmpRefundItemEntity dmpRefundItemEntity = new DmpRefundItemEntity();
            //sku编号
            dmpRefundItemEntity.setSkuNo(refundDetailsBean.getItemCode());
            //订单原始商品数量
            dmpRefundItemEntity.setQuantity(refundDetailsBean.getQty());
            //退款商品数量
            dmpRefundItemEntity.setRefundNum(refundDetailsBean.getQty());
            //是否属于组合sku：0. 否 1. 是
            dmpRefundItemEntity.setIsCombo(0);
            //折扣后金额
            dmpRefundItemEntity.setAmountAfter(new BigDecimal(refundDetailsBean.getAmount()));
            orderItemList.add(dmpRefundItemEntity);
        });
        return orderItemList;
    }


    /**
     * checkOrderItem(orderItemList, refundInfoId);
     * 校验退款商品数据在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    public void checkOrderItem(List<DmpRefundItemEntity> orderItem, String returnOrderId) {
        dmpRefundItemService.deleteRefundItemByRefundId(returnOrderId);
        dmpRefundItemService.batchAdd(orderItem);
    }
}
