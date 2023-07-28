package com.erp.server.dmp.push.consumer;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
import com.kingdee.bos.webapi.entity.OperateParam;
import com.kingdee.bos.webapi.entity.OperatorResult;
import com.kingdee.bos.webapi.entity.SaveParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对接金蝶仓库
 *
 * @author Lambda
 * @Classname KingdeeWarehouseConsumer
 * @Date 2023-04-25 14:29
 * @Created by yl
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_customer_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_CUSTOMER_INFO, consumeMode = ConsumeMode.ORDERLY)
public class KingdeeCustomerConsumer implements RocketMQListener<Map<String, Object>> {
    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils("BD_Customer");
        LinkedList<String> queryFilters = new LinkedList<>();
        // queryFilters.add(String.format("FNumber = '%s'", "CUST23060900001"));
        queryFilters.add(StrUtil.format("FNumber in ({})", "'CUST5188'"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FCUSTID,FForbidStatus";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 11);
        System.out.println(queryList);
        /* JSONObject entries = apiUtils.customerGroupDelete("");*/


      /* LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Number","CGDD-230413-8806");
        JSONObject viewJson = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        System.out.println(viewJson);*/

    }

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    public void onMessage(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.CUSTOMER_INFO.getCode();

        //业务id
        String businessId = String.valueOf(map.get("id"));

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_CUSTOMER.getCode());

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId, JSONUtil.toJsonStr(map), "未配置同步字段", type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //判断金蝶系统是否已存在该数据
        SaveParam param = new SaveParam(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
            map.put("syncKingdeeId", String.valueOf(model.get("Id")));
            json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);
            param = new SaveParam(json);
        } catch (Exception e) {

            //更新数据
            Boolean saveOrUpdateResult = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            if (saveOrUpdateResult) {
                //启用、禁用
                excuteOperation(apiUtils, platformEntity, map, type);
            }

            return;
        }

        //查找到数据后，判断其审核状态
        String documentStatus = (String) model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id"));
        String forbidStatus = String.valueOf(model.get("ForbidStatus"));
        Boolean flag = Boolean.FALSE;
        // A启用 B禁用
        Boolean kingdeeForbidStatus = "B".equals(forbidStatus) ? Boolean.TRUE : Boolean.FALSE;
        Boolean erpForbidStatus = ObjectUtil.isNotEmpty(map.get("disabled")) ? (Boolean) map.get("disabled") : Boolean.FALSE;
        //操作项
        String operate = (String) map.get("operate");
        boolean allowUnApproveStatus = KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus);

        if (SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            // 判断状态是否为审核中或已审核
            if (!allowUnApproveStatus) {
                //反审核
                log.warn("单据状态为[{}], 无法反审核，跳过反审核操作", KingdeeDocStatusEnum.getByCode(documentStatus));
                kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId, "", "金蝶数据可修改不需要反审核", type, ApiSendStatusEnum.SUCCESS.getCode());
                return;
            }
            // 判断禁用状态已禁用数据 先启用再反审核
            if (kingdeeForbidStatus) {
                // 同步ERP禁用状态
                excuteOperation(apiUtils, platformEntity, map, type);
            }
            //反审核
            kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, type);
            return;
        }
        if (SyncKingdeeOperateEnum.OPERATE_DISABLE.getCode().equals(operate) || SyncKingdeeOperateEnum.OPERATE_ENABLE.getCode().equals(operate)) {
            // 判断禁用状态是否与金蝶系统一致 A启用 B禁用
            if (erpForbidStatus.equals(kingdeeForbidStatus)) {
                log.warn("金蝶禁用状态为[{}] ERP禁用状态为[{}], 无需{}，跳过{}操作", forbidStatus, map.get("disabled"), operate, operate);
                kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId, JSONUtil.toJsonStr(map), "金蝶状态与ERP相同不需要修改", type, ApiSendStatusEnum.SUCCESS.getCode());
                return;
            }
            //启用、禁用
            excuteOperation(apiUtils,platformEntity,map,type);
            return;
        }

        //审核中或已审核则要先反审
        if (allowUnApproveStatus) {
            // 判断禁用状态已禁用数据 先启用再反审核
            if (kingdeeForbidStatus) {
                // 同步ERP禁用状态
                excuteOperation(apiUtils, platformEntity, map, type);
            }
            flag = kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, type);
        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //主单据id
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            if (!erpForbidStatus.equals(kingdeeForbidStatus)) {
                //启用、禁用
                excuteOperation(apiUtils,platformEntity,map,type);
            }
        }
    }

    /**
     * 启用、禁用
     */
    private void excuteOperation(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type) {
        //仓库状态 true禁用,false启用
        Object disabled = map.get("disabled");
        if (ObjectUtils.isEmpty(disabled)) {
            return;
        }

        String code = (String) map.get("code");
        String operate = null;
        //启用
        if (!(Boolean) disabled) {
            operate = SyncKingdeeOperateEnum.OPERATE_ENABLE.getCode();
        }
        //禁用
        if ((Boolean) disabled) {
            operate = SyncKingdeeOperateEnum.OPERATE_DISABLE.getCode();
        }
        if (StringUtils.isNotBlank(operate)) {
            kingdeeCommonService.excuteOperation(apiUtils, platformEntity, map, type, code, operate);
        }
    }

}
