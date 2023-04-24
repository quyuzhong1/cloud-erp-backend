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
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpRefundInfoService;
import com.erp.server.bi.service.DmpReturnOrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(JSONObject jsonObject) {
        List<BiSettlementExchangeRateEntity> entityList = (List<BiSettlementExchangeRateEntity>)jsonObject.get("list");
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
        List<DmpOrderInfoEntity> updateList = new ArrayList<>();
        entityList.forEach(obj->{
            List<DmpOrderInfoEntity> list =  dmpOrderInfoService.lambdaQuery()
                    .eq(DmpOrderInfoEntity::getCurrencyCode,obj.getSourceCurrencyCode())
                    .ge(DmpOrderInfoEntity::getPlatformCreateTime, obj.getSettlementDateBegin())
                    .le(DmpOrderInfoEntity::getPlatformCreateTime, LocalDateUtil.endLocalDateTime(obj.getSettlementDateEnd()))
                    .select(DmpOrderInfoEntity::getId,DmpOrderInfoEntity::getCreateTime)
                    .list();
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            List<DmpOrderInfoEntity> collect = list.stream().map(x -> new DmpOrderInfoEntity(x, obj.getExchangeRate())).collect(Collectors.toList());
            updateList.addAll(collect);
        });
        //更新
        update(updateList,0,1000, MathUtil.ONE);
    }

    /**
     * 更新退货单结算币别
     */
    private void updateRefundCurrency(List<BiSettlementExchangeRateEntity> entityList) {
        List<DmpRefundInfoEntity> updateList = new ArrayList<>();
        entityList.forEach(obj-> {
            List<DmpRefundInfoEntity> list = dmpRefundInfoService.lambdaQuery()
                    .eq(DmpRefundInfoEntity::getCurrencyCode, obj.getSourceCurrencyCode())
                    .ge(DmpRefundInfoEntity::getOrderTime, obj.getSettlementDateBegin())
                    .le(DmpRefundInfoEntity::getOrderTime, LocalDateUtil.endLocalDateTime(obj.getSettlementDateEnd()))
                    .ne(DmpRefundInfoEntity::getCnySettleRate, obj.getExchangeRate())
                    .select(DmpRefundInfoEntity::getId)
                    .list();

            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            List<DmpRefundInfoEntity> collect = list.stream().map(x -> new DmpRefundInfoEntity(x, obj.getExchangeRate())).collect(Collectors.toList());
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
        List<DmpReturnOrderInfoEntity> updateList = new ArrayList<>();
        entityList.forEach(obj-> {
            List<DmpReturnOrderInfoEntity> list = dmpReturnOrderInfoService.lambdaQuery()
                    .eq(DmpReturnOrderInfoEntity::getCurrencyCode, obj.getSourceCurrencyCode())
                    .ge(DmpReturnOrderInfoEntity::getOrderTime, obj.getSettlementDateBegin())
                    .le(DmpReturnOrderInfoEntity::getOrderTime, LocalDateUtil.endLocalDateTime(obj.getSettlementDateEnd()))
                    .ne(DmpReturnOrderInfoEntity::getCnySettleRate, obj.getExchangeRate())
                    .select(DmpReturnOrderInfoEntity::getId)
                    .list();

            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            List<DmpReturnOrderInfoEntity> collect = list.stream().map(x -> new DmpReturnOrderInfoEntity(x, obj.getExchangeRate())).collect(Collectors.toList());
            updateList.addAll(collect);
        });
        //更新
        update(updateList,0,1000, MathUtil.THREE);
    }


    public void update (List all, int start, int end,Integer type){

        if(all.size() <= end){
            end = all.size();
        }
        if (start > end) {
            return;
        }

        //截取start ~ end条数据
        List<T> collect = all.subList(start, end);
        if(CollUtil.isEmpty(collect)){
            return;
        }

        //批量更新数据的方法
        updateList(collect,type);
        //递归 每次插入1500条数据，这里1500写死了，就是不灵活的地方
        update(all,start + 1500,end + 1500,type);
    }



    /**
     * 更新订单
     */
    private void updateList (List<T> collect,Integer type) {
        if (MathUtil.ONE.equals(type)) {
            List<DmpOrderInfoEntity> list = JSONArray.parseArray(JSONUtil.toJsonStr(collect),DmpOrderInfoEntity.class);
            dmpOrderInfoService.updateBatchById(list);
        }
        if (MathUtil.TWO.equals(type)) {
            List<DmpRefundInfoEntity> list = JSONArray.parseArray(JSONUtil.toJsonStr(collect),DmpRefundInfoEntity.class);
            dmpRefundInfoService.updateBatchById(list);
        }
        if (MathUtil.THREE.equals(type)) {
            List<DmpReturnOrderInfoEntity> list = JSONArray.parseArray(JSONUtil.toJsonStr(collect),DmpReturnOrderInfoEntity.class);
            dmpReturnOrderInfoService.updateBatchById(list);
        }
    }

}
