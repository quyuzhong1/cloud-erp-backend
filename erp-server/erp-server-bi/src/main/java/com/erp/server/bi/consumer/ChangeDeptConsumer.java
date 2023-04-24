package com.erp.server.bi.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.server.bi.service.DmpOrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_UPDATE_TOPIC, selectorExpression = "shop_info_change_dept_tag", consumerGroup = RocketMqConsumerGroup.SHOP_INFO_CHANGE)
public class ChangeDeptConsumer implements RocketMQListener<JSONObject> {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(JSONObject jsonObject) {

        String chargeId = (String)jsonObject.get("chargeId");
        LocalDate enableTime = LocalDateTimeUtil.parseDate((String)jsonObject.get("enableTime"));
        String deptId = (String)jsonObject.get("deptId");
        String deptName = (String)jsonObject.get("deptName");

        //更新销售数据中的启用日期后的店铺业务部门
        updateSaleCharge(chargeId, enableTime, deptId,deptName);
    }

    /**
     * 更新订单负责人
     */
    private void updateSaleCharge(String chargeId, LocalDate enableTime, String deptId, String deptName ) {

        //更新启动时间后的订单负责人部门
        List<DmpOrderInfoEntity> list = dmpOrderInfoService.lambdaQuery()
                .eq(DmpOrderInfoEntity::getChargeId, chargeId)
                .ge(DmpOrderInfoEntity::getPlatformCreateTime, enableTime)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return ;
        }
        list.forEach(obj -> {
            obj.setDeptId(deptId);
            obj.setDeptName(deptName);
        });
        //新增
        update(list,0,1000);
    }

    public void update (List all, int start, int end){

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
        updateList(collect);
        //递归 每次插入1500条数据，这里1500写死了，就是不灵活的地方
        update(all,start + 1500,end + 1500);
    }



    /**
     * 更新订单
     */
    private void updateList (List<T> collect) {
        List<DmpOrderInfoEntity> list = JSONArray.parseArray(JSONUtil.toJsonStr(collect),DmpOrderInfoEntity.class);
        dmpOrderInfoService.updateBatchById(list);
    }

}
