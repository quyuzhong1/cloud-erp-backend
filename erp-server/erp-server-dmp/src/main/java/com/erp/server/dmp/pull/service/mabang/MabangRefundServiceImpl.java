package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.constant.RocketMqTopic;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.model.dmp.entity.DmpRefundItemEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.RocketMqTagEnum;
import com.erp.model.dmp.mabang.RefundOrderEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpRefundInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpRefundItemService;
import com.erp.server.dmp.service.mq.MQProducerService;
import com.erp.server.dmp.utils.MabangApiUtils;
import com.erp.server.dmp.utils.MapCountUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Resource
    private DmpRefundItemService dmpRefundItemService;

    @Autowired
    private MQProducerService<DmpRefundInfoEntity> mqProducerService;

    public static void main(String[] args) {
        MabangRefundServiceImpl gyyOrderInfoService = new MabangRefundServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.ORDER_GET_REFUND_LIST;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setApiId(7);
        jobTaskDTO.setApiName("管易云查询订单列表");
        jobTaskDTO.setId(32L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
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
    public void pullDataSave(RequestDTO dto) throws Exception {
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
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            RefundOrderEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
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
        List<DmpRefundInfoEntity> mabangToMqlist = pushToMqList.parallelStream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        mabangToMqlist.stream().peek(msg ->
                        mqProducerService.asyncClassMsg(RocketMqTopic.DMP_TOPIC, RocketMqTagEnum.MABANG_REFUND_ORDER_TAG.getName(),
                                msg, StrUtil.format("{}_{}", msg.getPlatformOrderId(), msg.getSalesRecordNumber())))
                .collect(Collectors.toList());

    }

    /**
     * 请求马帮退款信息接口
     *
     * @param dto
     * @return
     */
    private List<RefundOrderEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        return MabangApiUtils.queryRefundList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析退款订单数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public DmpRefundInfoEntity initOrderInfoEntity(RefundOrderEntity refundOrderEntity) {
        DmpRefundInfoEntity dmpRefundInfoEntity = new DmpRefundInfoEntity();
        BeanUtil.copyProperties(refundOrderEntity, dmpRefundInfoEntity);
        //币别编号
        dmpRefundInfoEntity.setCurrencyCode(refundOrderEntity.getCurrencyId());
        //退款单号
        dmpRefundInfoEntity.setRefundId(refundOrderEntity.getRefundplatformOrderId());
        //退货金额
        dmpRefundInfoEntity.setRefundAmount(refundOrderEntity.getApplyRefundMoney());
        //退款备注
        dmpRefundInfoEntity.setRefundRemark(refundOrderEntity.getNote());
        //退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
        dmpRefundInfoEntity.setRefundStatus(refundOrderEntity.getFlag());
        //申请时间
        dmpRefundInfoEntity.setRefundCreateTime(refundOrderEntity.getCreateTime());
        //店铺编号
        dmpRefundInfoEntity.setShopNo(refundOrderEntity.getShopId());
        //平台最后修改时间
        dmpRefundInfoEntity.setPlatformUpdateTime(refundOrderEntity.getUpdateTime());
        //平台标识
        dmpRefundInfoEntity.setPlatformSign(PlatformEnum.MABANG.getDesc());
        dmpRefundInfoEntity.setCreateTime(LocalDateTime.now());
        dmpRefundInfoEntity.setItemList(initOrderItem(refundOrderEntity));
        return dmpRefundInfoEntity;
    }

    /**
     * 解析退款订单商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public List<DmpRefundItemEntity> initOrderItem(RefundOrderEntity refundOrderEntity) {
        List<DmpRefundItemEntity> orderItemList = new ArrayList<>();
        HashMap<String, Integer> skuCountMap = new HashMap<>();
        refundOrderEntity.getProductList().stream().forEach( refundOrderItemEntity -> {
            DmpRefundItemEntity dmpRefundItemEntity = new DmpRefundItemEntity();
            //sku编号
            String skuNo = refundOrderItemEntity.getRefundStock();
            dmpRefundItemEntity.setSkuNo(skuNo);
            //订单原始商品数量
            dmpRefundItemEntity.setQuantity(refundOrderItemEntity.getStock_quantity());
            //退款商品数量
            dmpRefundItemEntity.setRefundNum(refundOrderItemEntity.getRefund_num());
            //是否属于组合sku：0. 否 1. 是
            dmpRefundItemEntity.setIsCombo(refundOrderItemEntity.getIsCombo());
            dmpRefundItemEntity.setAmountAfter(BigDecimal.ZERO);
            //erp平台商品id
            String erpOrderItemId = refundOrderEntity.getPlatformOrderId() + "_" + refundOrderEntity.getRefundplatformOrderId() + "_" + skuNo;
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap,skuNo,erpOrderItemId);
            dmpRefundItemEntity.setErpOrderItemId(erpOrderItemId);
            orderItemList.add(dmpRefundItemEntity);
        });
       return orderItemList;
    }
}
