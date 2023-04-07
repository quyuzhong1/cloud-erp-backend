package com.erp.server.dmp.push.consumer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.enums.ApiError;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.CfgApiFieldMapService;
import com.erp.server.dmp.service.PlatformService;
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
 * @description: 金蝶辅助资料同步
 * @date 2023/3/13 14:45
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_category_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_CATEGORY)
public class KingdeeAuxiliaryDataConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private PlatformService platformService;

    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    @Resource
    private KingdeeCommonService kingdeeCommonService;


    public static void main(String[] args) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BOS_ASSISTANTDATA_DETAIL.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FNumber = '%s'", "SouthChina"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FEntryId,FNumber,FDataValue,FId,FId.FNumber,FId.FName,FParentId";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,1);

        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("number","SouthChina");
        JSONObject viewJson = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        System.out.println(queryList);
        System.out.println(viewJson);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(Map<String, Object> map) {

        //模块类型
        Integer type = (Integer)map.get("moduleType");

        //传入map数据不能为空
        if (CollectionUtils.isEmpty(map)) {
            log.error("同步数据不存在！");
            return;
        }
        PlatformEntity platformEntity = platformService.getByName(PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            log.error("第三方平台【{}】未找到！",PlatformEnum.KINGDEE.getDesc());
            kingdeeCommonService.insertFailureLog(platformEntity, map,"",String.format("第三方平台【{}】未找到！",PlatformEnum.KINGDEE.getDesc()),type);
            return;
        }
        CfgApiFieldMapDTO dto = new CfgApiFieldMapDTO();
        dto.setApiPlatformId(platformEntity.getId());
        //模块默认辅助资料
        dto.setModuleType(ApiModuleTypeEnum.ASSISTANT_DATA.getCode());
        List<CfgApiFieldMapDTO> mapList = cfgApiFieldMapService.getByParams(dto);
        //未配置发送字段
        if (CollectionUtils.isEmpty(mapList)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            kingdeeCommonService.insertFailureLog(platformEntity, map,"","未配置同步字段",type);
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BOS_ASSISTANTDATA_DETAIL.getCode());

        //判断是否存在上级
        Boolean isExistParent = (Boolean)map.get("isExistParent");
        if (isExistParent) {
            //根据上级编码查询上级id
            String parentCode = (String)map.get("parentCode");
            LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
            viewMap.put("number",parentCode);
            JSONObject model;
            try {
                model = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
            } catch (Exception e) {
                //更新数据
                kingdeeCommonService.insertFailureLog(platformEntity, map,"","未找到上级辅助资料",type);
                return;
            }
            String id = (String) model.get("Id");
            map.put("pid",id);
        }

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, mapList);

        //判断金蝶系统是否已存在该数据
        String syncKingdeeId = (String)map.get("syncKingdeeId");
        String code = (String)map.get("code");
        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(syncKingdeeId)) {
            viewMap.put("id",syncKingdeeId);
        } else {
            viewMap.put("number",code);
        }
        JSONObject model;
        SaveParam param = new SaveParam(json);
        try {
            model = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        } catch (Exception e) {
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            return;
        }
        //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");
        String id = (String) model.get("Id");
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //审核中或已审核则要先反审
            documentStatus = kingdeeCommonService.unAudit(platformEntity, map,apiUtils, String.valueOf(id),type);
        }
        //创建状态则直接修改
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus)) {
            //主单据id
            KingdeeUtils.makeFieldJson(json,"FEntryId",".", id);
            //需要更新的字段
            List<String> apiFieldList = mapList.stream().map(CfgApiFieldMapDTO::getApiField).sorted().distinct().collect(Collectors.toList());
            ArrayList<String> needUpDateFields = new ArrayList<>();
            for (String field:apiFieldList) {
                ArrayList<String> splitFields =(ArrayList<String>) Arrays.stream(field.split("\\.")).collect(Collectors.toList());
                needUpDateFields.addAll(splitFields);
            }
            param.setNeedUpDateFields(needUpDateFields);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
        }
    }

}
