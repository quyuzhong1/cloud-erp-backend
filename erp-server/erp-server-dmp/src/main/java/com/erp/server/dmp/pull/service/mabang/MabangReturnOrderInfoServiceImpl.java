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
import com.erp.model.dmp.enums.RocketMqTagEnum;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.mabang.ReturnOrderEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderItemService;
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
 * 马帮退货订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ORDER_GET_RETURN_ORDER_LIST)
public class MabangReturnOrderInfoServiceImpl implements IReportSaveService<ReturnOrderEntity> {
    @Resource
    private MongoService mongoService;

    @Resource
    private DmpReturnOrderItemService dmpReturnOrderItemService;

    @Autowired
    private MQProducerService<DmpReturnOrderInfoEntity> mqProducerService;

    public static void main(String[] args) {
        MabangReturnOrderInfoServiceImpl getOrderInfoService = new MabangReturnOrderInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.ORDER_GET_RETURN_ORDER_LIST;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
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
    public void pullDataSave(RequestDTO dto) throws Exception {
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
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            ReturnOrderEntity mongoDatum = mongoData.get(0);
            String id = mongoDatum.get_id();
            mongoDatum.set_id(null);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(id);
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
        List<DmpReturnOrderInfoEntity> mabangToMqlist = pushToMqList.parallelStream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        mabangToMqlist.stream().peek(msg ->
                        mqProducerService.asyncClassMsg(RocketMqTopic.DMP_TOPIC, RocketMqTagEnum.MABANG_RETURN_ORDER_TAG.getName(),
                                msg, StrUtil.format("{}_{}",msg.getPlatformOrderId(), msg.getSalesRecordNumber())))
                .collect(Collectors.toList());

    }

    /**
     * 请求马帮退货订单接口
     * @param dto
     * @return
     */
    private List<ReturnOrderEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        return MabangApiUtils.queryReturnOrderList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析订单数据
     **/
    private DmpReturnOrderInfoEntity initOrderInfoEntity(ReturnOrderEntity returnOrderEntity) {
        DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = new DmpReturnOrderInfoEntity();
        BeanUtil.copyProperties(returnOrderEntity, dmpReturnOrderInfoEntity);
        //币种
        dmpReturnOrderInfoEntity.setCurrencyCode(returnOrderEntity.getCurrencyId());
        //退货信息创建时间
        dmpReturnOrderInfoEntity.setReturnCreateTime(returnOrderEntity.getCreateDate());
        //国家英文名称
        dmpReturnOrderInfoEntity.setCountryNameEn(returnOrderEntity.getCountryNameEN());
        //国家中文名称
        dmpReturnOrderInfoEntity.setCountryNameCn(returnOrderEntity.getCountryNameCN());
        // 平台名称
        dmpReturnOrderInfoEntity.setPlatformName(returnOrderEntity.getPlatformId());
        //退货单号
        dmpReturnOrderInfoEntity.setReturnOrderId(returnOrderEntity.getPlatformOrderId());
        //店铺编号
        dmpReturnOrderInfoEntity.setShopNo(returnOrderEntity.getShopId());
        //平台标识
        dmpReturnOrderInfoEntity.setPlatformSign(PlatformEnum.MABANG.getDesc());
        dmpReturnOrderInfoEntity.setIsDeleted(5 == returnOrderEntity.getStatus());
        dmpReturnOrderInfoEntity.setCreateTime(LocalDateTime.now());
        dmpReturnOrderInfoEntity.setItemList(initOrderItem(returnOrderEntity));
        return dmpReturnOrderInfoEntity;
    }

    /**
     * 解析退货订单商品数据
     **/
    public List<DmpReturnOrderItemEntity> initOrderItem(ReturnOrderEntity returnOrderEntity) {
        List<DmpReturnOrderItemEntity> orderItemList = new ArrayList<>();
        HashMap<String, Integer> skuCountMap = new HashMap<>();
        returnOrderEntity.getItem().stream().forEach(orderItemBean -> {
            DmpReturnOrderItemEntity dmpReturnOrderItemEntity = new DmpReturnOrderItemEntity();
            //sku编号
            String skuNo = orderItemBean.getStockSku();
            dmpReturnOrderItemEntity.setSkuNo(skuNo);
            //商品名称
            dmpReturnOrderItemEntity.setItemName(orderItemBean.getTitle());
            //买家购买数量
            dmpReturnOrderItemEntity.setQuantity(orderItemBean.getQuantity());
            //商品单位
            dmpReturnOrderItemEntity.setProductUnit(orderItemBean.getProductUnit());
            //商品图片地址
            dmpReturnOrderItemEntity.setPictureUrl(orderItemBean.getPictureUrl());
            //售价
            dmpReturnOrderItemEntity.setSellPrice(orderItemBean.getSellPrice());
            //物品属性
            dmpReturnOrderItemEntity.setSpecifics(orderItemBean.getSpecifics());
            //状态 1待处理 2验货入库 3自然耗损
            dmpReturnOrderItemEntity.setStatus(orderItemBean.getStatus());
            dmpReturnOrderItemEntity.setIsDeleted(5 == returnOrderEntity.getStatus());
            //erp平台商品id
            String erpOrderItemId = returnOrderEntity.getPlatformOrderId() + "_" + returnOrderEntity.getSalesRecordNumber() + "_" + skuNo;
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, skuNo, erpOrderItemId);
            dmpReturnOrderItemEntity.setErpOrderItemId(erpOrderItemId);
            dmpReturnOrderItemEntity.setAmountAfter(orderItemBean.getSellPrice().multiply(new BigDecimal(orderItemBean.getQuantity())));
            orderItemList.add(dmpReturnOrderItemEntity);
        });
        return orderItemList;
    }
}
