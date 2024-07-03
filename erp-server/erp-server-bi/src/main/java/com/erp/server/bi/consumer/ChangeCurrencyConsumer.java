package com.erp.server.bi.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.bi.entity.BiSettlementExchangeRateEntity;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiRefundInfoEntity;
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpRefundInfoService;
import com.erp.server.bi.service.DmpReturnOrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: 金蝶物料清单同步
 * @date 2023/3/9 16:20
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_UPDATE_TOPIC, selectorExpression = "change_currency_tag", consumerGroup = RocketMqConsumerGroup.CHANGE_CURRENCY)
public class ChangeCurrencyConsumer implements RocketMQListener<JSONObject> {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Resource
    private DmpRefundInfoService dmpRefundInfoService;

    @Resource
    private DmpReturnOrderInfoService dmpReturnOrderInfoService;


    @Override
    public void onMessage(JSONObject jsonObject) {
        List<BiSettlementExchangeRateEntity> entityList = JSONArray.parseArray(JSONUtil.toJsonStr(jsonObject.get("list")),BiSettlementExchangeRateEntity.class);
        //更新销售数据中的启用日期后的店铺业务负责人
        updateSaleCurrency(entityList);
        //更新退款数据中的启用日期后的店铺业务负责人
        updateRefundCurrency(entityList);
        //更新退货数据中启用日期后的店铺业务负责人
        updateReturnOrderCurrency(entityList);
    }

    /**
     * 更新订单结算币别
     */
    private void updateSaleCurrency(List<BiSettlementExchangeRateEntity> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        List<BiOrderInfoEntity> updateList = new ArrayList<>();
        entityList.forEach(obj->{
            List<BiOrderInfoEntity> list =  dmpOrderInfoService.lambdaQuery()
                    .eq(BiOrderInfoEntity::getCurrencyCode,obj.getSourceCurrencyCode())
                    .ge(BiOrderInfoEntity::getPlatformCreateTime, obj.getSettlementDateBegin())
                    .le(BiOrderInfoEntity::getPlatformCreateTime, LocalDateUtil.endLocalDateTime(obj.getSettlementDateEnd()))
                    .select(BiOrderInfoEntity::getId, BiOrderInfoEntity::getCreateTime)
                    .ne(BiOrderInfoEntity::getCnySettleRate,obj.getExchangeRate())
                    .list();
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            List<BiOrderInfoEntity> collect = list.stream().map(x -> new BiOrderInfoEntity(x, obj.getExchangeRate())).collect(Collectors.toList());
            updateList.addAll(collect);
        });
        //更新
        update(updateList,0,1000, MathUtil.ONE);
    }

    /**
     * 更新退货单结算币别
     */
    private void updateRefundCurrency(List<BiSettlementExchangeRateEntity> entityList) {
        List<BiRefundInfoEntity> updateList = new ArrayList<>();
        entityList.forEach(obj-> {
            List<BiRefundInfoEntity> list = dmpRefundInfoService.lambdaQuery()
                    .eq(BiRefundInfoEntity::getCurrencyCode, obj.getSourceCurrencyCode())
                    .ge(BiRefundInfoEntity::getOrderTime, obj.getSettlementDateBegin())
                    .le(BiRefundInfoEntity::getOrderTime, LocalDateUtil.endLocalDateTime(obj.getSettlementDateEnd()))
                    .ne(BiRefundInfoEntity::getCnySettleRate, obj.getExchangeRate())
                    .select(BiRefundInfoEntity::getId)
                    .list();

            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            List<BiRefundInfoEntity> collect = list.stream().map(x -> new BiRefundInfoEntity(x, obj.getExchangeRate())).collect(Collectors.toList());
            updateList.addAll(collect);
        });
        //更新
        update(updateList,0,1000, MathUtil.TWO);
    }

    /**
     * 更新退款单结算币别
     */
    private void updateReturnOrderCurrency(List<BiSettlementExchangeRateEntity> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        List<BiReturnOrderInfoEntity> updateList = new ArrayList<>();
        entityList.forEach(obj-> {
            List<BiReturnOrderInfoEntity> list = dmpReturnOrderInfoService.lambdaQuery()
                    .eq(BiReturnOrderInfoEntity::getCurrencyCode, obj.getSourceCurrencyCode())
                    .ge(BiReturnOrderInfoEntity::getOrderTime, obj.getSettlementDateBegin())
                    .le(BiReturnOrderInfoEntity::getOrderTime, LocalDateUtil.endLocalDateTime(obj.getSettlementDateEnd()))
                    .ne(BiReturnOrderInfoEntity::getCnySettleRate, obj.getExchangeRate())
                    .select(BiReturnOrderInfoEntity::getId)
                    .list();

            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            List<BiReturnOrderInfoEntity> collect = list.stream().map(x -> new BiReturnOrderInfoEntity(x, obj.getExchangeRate())).collect(Collectors.toList());
            updateList.addAll(collect);
        });
        //更新
        update(updateList,0,1000, MathUtil.THREE);
    }


    public void update (List<?> all, int start, int end,Integer type){

        if(all.size() <= end){
            end = all.size();
        }
        if (start > end) {
            return;
        }

        //截取start ~ end条数据
        List<?> collect = all.subList(start, end);
        if(CollUtil.isEmpty(collect)){
            return;
        }

        //批量更新数据的方法
        updateList(collect,type);
        //递归 每次插入1500条数据，这里1500写死了，就是不灵活的地方
        update(all,start + 1000,end + 1000,type);
    }



    /**
     * 更新订单
     */
    private void updateList (List<?> collect,Integer type) {
        if (MathUtil.ONE.equals(type)) {
            List<BiOrderInfoEntity> list = (List<BiOrderInfoEntity>)collect;
            dmpOrderInfoService.updateBatchById(list);
        }
        if (MathUtil.TWO.equals(type)) {
            List<BiRefundInfoEntity> list = (List<BiRefundInfoEntity>)collect;
            dmpRefundInfoService.updateBatchById(list);
        }
        if (MathUtil.THREE.equals(type)) {
            List<BiReturnOrderInfoEntity> list = (List<BiReturnOrderInfoEntity>)collect;
            dmpReturnOrderInfoService.updateBatchById(list);
        }
    }

}
