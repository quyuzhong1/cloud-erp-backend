package com.erp.server.dmp.push.consumer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.MathUtil;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/10 11:56
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_sys_user_info_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SYS_USER_INFO)
public class KingdeeSysUserInfoConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_EMPINFO.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FNumber = '%s'", "23041200001"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FNumber,FForbidDate";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,1);

        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Number","23041200001");
        JSONObject viewJson = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
       System.out.println(queryList);
        //System.out.println(viewJson);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(Map<String, Object> map) {

        //模块类型
        Integer type = ApiModuleTypeEnum.SYS_USER_INFO.getCode();
        //业务id
        String  businessId = String.valueOf(map.get("id"));
        //业务编码
        String code = (String) map.get("code");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_EMPINFO.getCode());

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
            //启用、禁用
            excuteOperation(apiUtils,platformEntity,map,type);
            return;
        }

        //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");
        //禁用日期（用于判断是否禁用）
        String FForbidDate = (String)model.get("ForbidDate");
        String id = String.valueOf(model.get("Id")) ;
        Boolean flag = Boolean.FALSE;

        //操作项
        String operate = (String) map.get("operate");
        if (SyncKingdeeOperateEnum.OPERATE_DISABLE.getCode().equals(operate) || SyncKingdeeOperateEnum.OPERATE_ENABLE.getCode().equals(operate)) {
            //启用、禁用
            excuteOperation(apiUtils,platformEntity,map,type);
            return;
        }
        //禁用的需要先反禁用
        if (StringUtils.isNotBlank(FForbidDate)) {
            kingdeeCommonService.excuteOperation(apiUtils,platformEntity,map,type,code,SyncKingdeeOperateEnum.OPERATE_ENABLE.getCode());
        }
        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            flag = kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, type);
        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //删除
            if (SyncKingdeeOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
                kingdeeCommonService.delete(apiUtils,platformEntity,map,type,code);
                return;
            }
            //主单据id
            KingdeeUtils.makeFieldJson(json,"FId",".", id);
            ArrayList<String> apiFieldList = (ArrayList<String>) json.keySet().stream().collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            //启用、禁用
            excuteOperation(apiUtils,platformEntity,map,type);
        }
    }

    /**
     * 启用、禁用
     */
    private void excuteOperation (KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        //用户状态 1：正常 0：禁用
        Object userState = map.get("userState");
        if (ObjectUtils.isEmpty(userState)) {
            return;
        }
        String code = (String) map.get("code");
        String operate = null;
        //启用
        if (String.valueOf(MathUtil.ONE).equals(String.valueOf(userState))) {
            operate = SyncKingdeeOperateEnum.OPERATE_ENABLE.getCode();
        }
        //禁用
        if (String.valueOf(MathUtil.ZERO).equals(String.valueOf(userState))) {
            operate = SyncKingdeeOperateEnum.OPERATE_DISABLE.getCode();
        }
        if (StringUtils.isNotBlank(operate)) {
             kingdeeCommonService.excuteOperation(apiUtils,platformEntity,map,type,code,operate);
        }
    }
}
