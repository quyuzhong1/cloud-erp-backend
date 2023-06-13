package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
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
import com.erp.server.dmp.push.service.kingdee.impl.KingdeeCommonServiceImpl;
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
 * 对接金蝶仓库
 * @author Lambda
 * @Classname KingdeeWarehouseConsumer
 * @Description TODO
 * @Date 2023-04-25 14:29
 * @Created by yl
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_customer_group_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_CUSTOMER_GROUP)
public class KingdeeCustomerGroupConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    public static void main(String[] args) {
        //模块类型
        Integer type = ApiModuleTypeEnum.CUSTOMER_INFO.getCode();
        KingdeeCommonService kingdeeCommonService = new KingdeeCommonServiceImpl();
        Map<String, Object> map = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.PUR_PAT.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "CGTJ23050500001"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FPUR_PATENTRY_FEntryID,FMaterialId.FNumber,FSrcEntryID,FIsPriceListPush";
        map.put("groupName", "测试分组");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);
        SaveParam param = new SaveParam(json);
        Boolean aBoolean = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
        System.out.println(aBoolean);

       /* LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Number","CGDD-230413-8806");
        JSONObject viewJson = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        System.out.println(viewJson);
*/
    }

    @Override
    public void onMessage(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.CUSTOMER_GROUP.getCode();

        //业务id
        String  businessId = String.valueOf(map.get("id"));

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
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId,"","未配置同步字段",type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //判断金蝶系统是否已存在该数据
        SaveParam param = new SaveParam(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.queryGroupInfo(apiUtils, (String)map.get("syncKingdeeId"),String.valueOf(map.get("groupName")));
        } catch (Exception e) {

            //更新数据
            kingdeeCommonService.customerGroupSaveOrUpdate(platformEntity, map, apiUtils, json, param, type);

            return;
        }
        String id = String.valueOf(model.get("GroupPkId")) ;
        //主单据id
        KingdeeUtils.makeFieldJson(json,"GroupPkId",".", id);
        StringBuffer allKey = FastJsonUtil.getAllKey(json);
        ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
        param.setNeedUpDateFields(apiFieldList);
        //更新数据
        kingdeeCommonService.customerGroupSaveOrUpdate(platformEntity,map,apiUtils,json,param,type);
    }
}
