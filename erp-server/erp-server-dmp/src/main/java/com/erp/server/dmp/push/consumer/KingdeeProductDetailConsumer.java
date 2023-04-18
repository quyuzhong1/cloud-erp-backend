package com.erp.server.dmp.push.consumer;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.google.gson.Gson;
import com.kingdee.bos.webapi.entity.RepoRet;
import com.kingdee.bos.webapi.entity.SaveParam;
import com.kingdee.bos.webapi.entity.SaveResult;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.erp.model.dmp.dto.GoodcangDTO.ResultDTO.fail;

/**
 * @author Will
 * @version 1.0
 * @description: 金蝶物料同步
 * @date 2023/3/9 16:24
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_product_detail_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_PRODUCT_DETAIL)
public class KingdeeProductDetailConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeCommonService kingdeeCommonService;



//    public static void main(String[] args) {
//        Map<String, Object> resultMap = new LinkedHashMap<>();
//        //读取配置，初始化SDK
//        KingdeeApiUtils apiUtils = new KingdeeApiUtils(PlatformApiEnum.BD_MATERIAL.getTaskName());
//        LinkedList<String> queryFilters = new LinkedList<>();
//        queryFilters.add(String.format("FNumber = '%s'", "testtes"));
//        String filterStr = String.join(" and ", queryFilters);
//        String fieldKeys = "FUseOrgId,FUseOrgId.FNumber,FUseOrgId.FName,FNumber,FName,FSubHeadEntity_FEntryId," +
//                "SubHeadEntity_FEntryId,SubHeadEntity1_FEntryId,SubHeadEntity2_FEntryId,SubHeadEntity3_FEntryId,SubHeadEntity4_FEntryId,SubHeadEntity5_FEntryId," +
//                "SubHeadEntity6_FEntryId,SubHeadEntity7_FEntryId,FBarCodeEntity_CMK_FEntryId,FSpecialAttributeEntity_FEntryId,FCategoryID,FNETWEIGHT,FLENGTH," +
//                "FWIDTH,F_ulz_Qty1,F_PRVD_Assistant1.FNumber,F_PRVD_Assistant1.FDataValue,F_PRVD_Assistant1.FId,F_PRVD_Assistant.FId,SubHeadEntity_FErpClsID_FNumber";
//        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,1);
//        System.out.println(queryList);
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(Map<String, Object> map) {
        //同步模块类型
        Integer type = ApiModuleTypeEnum.PRODUCT_DETAIL.getCode();
        //业务id
        String  businessId = String.valueOf(map.get("id"));

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_MATERIAL.getCode());

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map,platformEntity.getId(),type);
        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId,"","未配置同步字段",type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //判断金蝶系统是否已存在该数据
        JSONObject model;
        SaveParam param = new SaveParam(json);
        try {
            model = kingdeeCommonService.view(apiUtils,(String)map.get("syncKingdeeId"),(String)map.get("skuNo"));
        } catch (Exception e) {
            //未查找到数据，新增数据
            SaveResult save;
            try {
                save = apiUtils.save(param);
            } catch (Exception ex) {
                //新增失败时添加日志及定时任务
                kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId,JSONObject.toJSONString(json),JSONObject.toJSONString(ex),type, ApiSendStatusEnum.FAILURE.getCode());
                return;
            }
            //新增成功后编辑二级类目
            String id = save.getResult().getId();
            //主单据id
            KingdeeUtils.makeFieldJson(json,"FMATERIALID",".",id);
            //需要修改字段
            ArrayList<String> apiFieldList = (ArrayList<String>) json.keySet().stream().collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            return;
        }
        //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");
        Integer id = (Integer) model.get("Id");
        Boolean flag = Boolean.FALSE;
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //审核中或已审核则要先反审
            flag = kingdeeCommonService.unAudit(platformEntity, map,apiUtils,String.valueOf(id),type);
        }
        //创建状态则直接修改
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {

            LinkedList<String> queryFilters = new LinkedList<>();
            queryFilters.add(String.format("FMATERIALID = '%s'", id));
            String filterStr = String.join(" and ", queryFilters);
            //查询子单据id
            String fieldKeys = "FSubHeadEntity_FEntryId,SubHeadEntity_FEntryId,SubHeadEntity1_FEntryId,SubHeadEntity2_FEntryId,SubHeadEntity3_FEntryId,SubHeadEntity4_FEntryId,SubHeadEntity5_FEntryId," +
                    "SubHeadEntity6_FEntryId,SubHeadEntity7_FEntryId";
            List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);
            if (CollectionUtils.isEmpty(queryList)) {
                return;
            }
            Map<String, Object> queryMap = queryList.get(0);
            //主单据id
            KingdeeUtils.makeFieldJson(json,"FMATERIALID",".", id);
            Iterator iter = queryMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                KingdeeUtils.makeFieldJson(json, String.valueOf(entry.getKey()),".",entry.getValue());
            }
            //需要修改字段
            ArrayList<String> apiFieldList = (ArrayList<String>) json.keySet().stream().collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
        }
    }


    public static void main(String[] args) {
        //注意 1：此处不再使用参数形式传入用户名及密码等敏感信息，改为在登录配置文件中设置。
        //注意 2：必须先配置第三方系统登录授权信息后，再进行业务操作，详情参考各语言版本SDK介绍中的登录配置文件说明。
        //读取配置，初始化SDK
        K3CloudApi client = new K3CloudApi();
        //请求参数，要求为json字符串
        String jsonData = "{\"Number\":\"ZJDB021077\"}";
        try{
            //业务对象标识
            String formId = "STK_TransferDirect";
            //调用接口
            String resultJson = client.view(formId,jsonData);

            //用于记录结果
            Gson gson = new Gson();
            //对返回结果进行解析和校验
            RepoRet repoRet = gson.fromJson(resultJson, RepoRet.class);
            if (repoRet.getResult().getResponseStatus().isIsSuccess()) {
                System.out.printf("接口返回结果: %s%n", gson.toJson(repoRet.getResult()));
            } else {
                fail("接口返回结果: " + gson.toJson(repoRet.getResult().getResponseStatus()));
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
