package com.erp.server.bi.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiRefundInfoEntity;
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import com.erp.server.bi.service.BiOrderInfoService;
import com.erp.server.bi.service.BiRefundInfoService;
import com.erp.server.bi.service.BiReturnOrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 金蝶物料清单同步
 * @date 2023/3/9 16:20
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_UPDATE_TOPIC, selectorExpression = "shop_info_change_charge_tag", consumerGroup = RocketMqConsumerGroup.SHOP_INFO_CHANGE_CHARGE)
public class ChangeChargeConsumer implements RocketMQListener<JSONObject> {

    @Resource
    private BiOrderInfoService biOrderInfoService;

    @Resource
    private BiRefundInfoService biRefundInfoService;


    @Resource
    private BiReturnOrderInfoService biReturnOrderInfoService;


    @Override
    public void onMessage(JSONObject jsonObject) {

        String shopNo = (String)jsonObject.get("shopNo");
        LocalDate enableTime = LocalDateTimeUtil.parseDate((String)jsonObject.get("enableTime"));
        String userId = (String)jsonObject.get("userId");
        String userName = (String)jsonObject.get("userName");
        String deptId = (String)jsonObject.get("deptId");
        String deptName = (String)jsonObject.get("deptName");

        //更新销售数据中的启用日期后的店铺业务负责人
        updateSaleCharge(shopNo, enableTime, userId, userName, deptId,deptName);
        //更新退款数据中的启用日期后的店铺业务负责人
        updateRefundCharge(shopNo, enableTime, userId, userName);
        //更新退货数据中启用日期后的店铺业务负责人
        updateReturnOrderCharge(shopNo, enableTime, userId, userName);

    }

    /**
     * 更新订单负责人
     */
    private void updateSaleCharge(String shopNo, LocalDate enableTime, String userId, String userName, String deptId, String deptName ) {

        List<BiOrderInfoEntity> list = biOrderInfoService.lambdaQuery()
                .eq(BiOrderInfoEntity::getShopNo, shopNo)
                .ge(BiOrderInfoEntity::getPlatformCreateTime, enableTime)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(obj -> {
            if (StringUtils.isNotBlank(deptId)) {
                obj.setDeptId(deptId);
                obj.setDeptName(deptName);
            }
            obj.setChargeId(userId);
            obj.setChargeName(userName);
        });
        //新增
        update(list,0,1000,MathUtil.ONE);
    }

    public void update (List all, int start, int end, Integer type){

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
     * 更新退款单负责人
     */
    private void updateRefundCharge(String shopNo, LocalDate enableTime, String userId, String userName) {
        List<BiRefundInfoEntity> list = biRefundInfoService.lambdaQuery()
                .eq(BiRefundInfoEntity::getShopNo, shopNo)
                .ge(BiRefundInfoEntity::getOrderTime, enableTime)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(obj -> {
            obj.setChargeId(userId);
            obj.setChargeName(userName);
        });
        //更新
        update(list,0,1000,MathUtil.ONE);
    }

    /**
     * 更新退货单负责人
     */
    private void updateReturnOrderCharge(String shopNo, LocalDate enableTime, String userId, String userName) {
        List<BiReturnOrderInfoEntity> list = biReturnOrderInfoService.lambdaQuery()
                .eq(BiReturnOrderInfoEntity::getShopNo, shopNo)
                .ge(BiReturnOrderInfoEntity::getOrderTime, enableTime)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(obj -> {
            obj.setChargeId(userId);
            obj.setChargeName(userName);
        });
        //更新
        update( list,0,1000,MathUtil.ONE);
    }

    /**
     * 根据类型
     */
    private void updateList (List<T> collect ,Integer type) {
        if (MathUtil.ONE.equals(type)) {
            List<BiOrderInfoEntity> list = JSONArray.parseArray(JSONUtil.toJsonStr(collect), BiOrderInfoEntity.class);
            biOrderInfoService.updateBatchById(list);
        }
        if (MathUtil.TWO.equals(type)) {
            List<BiRefundInfoEntity> list = JSONArray.parseArray(JSONUtil.toJsonStr(collect), BiRefundInfoEntity.class);
            biRefundInfoService.updateBatchById(list);
        }
        if (MathUtil.THREE.equals(type)) {
            List<BiReturnOrderInfoEntity> list = JSONArray.parseArray(JSONUtil.toJsonStr(collect), BiReturnOrderInfoEntity.class);
            biReturnOrderInfoService.updateBatchById(list);
        }
    }

}
