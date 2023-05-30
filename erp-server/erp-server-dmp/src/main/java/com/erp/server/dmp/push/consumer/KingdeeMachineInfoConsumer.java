package com.erp.server.dmp.push.consumer;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.FastJsonUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 直接调拨单推送至金蝶
 * @author Will
 * @date: 2023/5/24 18:17
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_machine_info_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_MACHINE_INFO)
public class KingdeeMachineInfoConsumer implements RocketMQListener<Map<String, Object>> {
    @Resource
    private KingdeeCommonService kingdeeCommonService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_ASSEMBLEDAPP.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "ZZCX001369"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FID,FBillNo";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,0);
        System.out.println(queryList);

    }

    @Override
    public void onMessage(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.MACHINE_INFO.getCode();

        log.info("加工单开始推送金蝶 map = {}", JSONUtil.toJsonStr(map));

        //业务编码
        String code = (String) map.get("code");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_ASSEMBLEDAPP.getCode());

        //操作项
        String operate = (String) map.get("operate");
        /**
         * 作废
         */
        if (SyncKingdeeOperateEnum.OPERATE_INVALID.getCode().equals(operate)) {
            operateInvalid(apiUtils,platformEntity,map,type,code,operate);
        }
        /**
         * 反审核
         */
        if (SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            operateDisapprove(apiUtils,platformEntity, map,type);
        }
        /**
         * 审核
         */
        if (SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode().equals(operate)) {
            operateApprove(apiUtils,platformEntity, map,type);
        }
    }


    /**
     * @description: 作废
     * @author Will
     * @date: 2023/5/24 17:57
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @param code
     * @param operate
     */
    private void operateInvalid(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type, String code,String operate) {
        //作废
        kingdeeCommonService.excuteOperation(apiUtils,platformEntity,map,type,code,operate);
        return;
    }

    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/5/24 17:57
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     */
    private void operateDisapprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        String syncKingdeeId = (String) map.get("syncKingdeeId");
        if (StringUtils.isBlank(syncKingdeeId)) {
            return;
        }
        //反审核
        kingdeeCommonService.unAudit(platformEntity, map, apiUtils, syncKingdeeId, type);
        return;
    }


    /**
     * @description: 审核
     * @author Will
     * @date: 2023/5/24 18:10
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     */
    private void operateApprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        //业务id
        String  businessId = String.valueOf(map.get("id"));

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(),type);

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId,"","未配置同步字段",type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }

        //判断金蝶系统是否已存在该数据
        SaveParam param = new SaveParam(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils,(String)map.get("syncKingdeeId"),(String)map.get("code"));
        } catch (Exception e) {
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            return;
        }
        //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id")) ;
        Boolean flag = Boolean.FALSE;

        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            flag = kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, type);
        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //给修改json对象赋值ID
            KingdeeUtils.makeFieldJson(json,"FId",".", id);

            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
        }
    }
}