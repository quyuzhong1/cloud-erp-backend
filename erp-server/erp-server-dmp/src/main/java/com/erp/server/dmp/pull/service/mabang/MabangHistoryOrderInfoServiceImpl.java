package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.erp.common.business.constant.RocketMqTopic;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.RocketMqTagEnum;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.service.mq.MQProducerService;
import com.erp.server.dmp.utils.MabangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 马帮订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GET_HISTORY_ORDER_LIST)
public class MabangHistoryOrderInfoServiceImpl implements IReportSaveService<OrderEntity> {
    @Resource
    private MongoService mongoService;

    @Autowired
    private MQProducerService<DmpOrderInfoEntity> mqProducerService;
    /**
     * 拉取订单数据
     *
     * @param dto 任务信息
     */
    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        List<OrderEntity> entityList = MabangApiUtils.queryHistorySalesList(dto.getPlatformApiEnum().getTaskName(), nextTime);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮历史销售订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮历史销售订单列表数据 entityList.size = {} ", entityList.size());
        List<OrderEntity> insertList = new ArrayList<>();
        List<OrderEntity> pushToMqList = new ArrayList<>();
        for (OrderEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByOrderIdAndSaleNum(entity.getPlatformOrderId(), entity.getSalesRecordNumber());
            List<OrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            OrderEntity mongoDatum = mongoData.get(0);
            String id = mongoDatum.get_id();
            mongoDatum.set_id(null);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(id);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_ORDER);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("马帮销售订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<DmpOrderInfoEntity> mabangToMqlist = pushToMqList.parallelStream()
                .map(MabangOrderInfoServiceImpl::initOrderInfoEntity)
                .collect(Collectors.toList());

        // 异步推送到MQ
        mabangToMqlist.stream().peek(msg ->
                        mqProducerService.asyncClassMsg(RocketMqTopic.DMP_TOPIC, RocketMqTagEnum.MABANG_SALE_ORDER_TAG.getName(),
                                msg, StrUtil.format("{}_{}", msg.getPlatformOrderId(), msg.getSalesRecordNumber())))
                .collect(Collectors.toList());
    }
}
