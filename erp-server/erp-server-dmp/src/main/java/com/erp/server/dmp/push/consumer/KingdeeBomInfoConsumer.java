package com.erp.server.dmp.push.consumer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.CfgApiFieldMapService;
import com.erp.server.dmp.service.PlatformService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import com.kingdee.bos.webapi.entity.SaveResult;
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
 * @date 2023/3/9 16:20
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_bom_info_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_BOM_INFO)
public class KingdeeBomInfoConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private PlatformService platformService;

    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    public static void main(String[] args) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.ENG_BOM.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FNumber = '%s'", "1001010005_1"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FUseOrgId,FUseOrgId.FNumber,FBOMCATEGORY,FBOMUSE,FMATERIALID.FNumber,FMATERIALIDCHILD.FNumber,FId,FDENOMINATOR,FNUMERATOR";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,1);
        System.out.println(queryList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(Map<String, Object> map) {
        //传入map数据不能为空
        if (ObjectUtils.isEmpty(map) || map.size() == 0) {
            throw new ServiceException(ApiError.Default);
        }
        PlatformEntity platformEntity = platformService.getByName(PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            throw new ServiceException(ApiError.Default);
        }
        CfgApiFieldMapDTO dto = new CfgApiFieldMapDTO();
        dto.setApiPlatformId(platformEntity.getId());
        Integer type = ApiModuleTypeEnum.BOMMANAGE.getCode();
        dto.setModuleType(type);
        List<CfgApiFieldMapDTO> mapList = cfgApiFieldMapService.getByParams(dto);
        //未配置发送字段
        if (CollectionUtils.isEmpty(mapList)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity,String.valueOf(map.get("id")),"","未配置同步字段",type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.ENG_BOM.getCode());
        // 根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, mapList);

        //判断金蝶系统是否已存在该数据
        String version = (String)map.get("version");
        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("number",version);
        //默认唯迹科技
        viewMap.put("CreateOrgId",1);
        JSONObject model;
        SaveParam param = new SaveParam(json);
        try {
            model = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        } catch (Exception e) {
            //未查找到数据，新增数据
            SaveResult save;
            try {
                save = apiUtils.save(param);
            } catch (Exception ex) {
                //新增失败时添加日志及定时任务
                kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")),JSONObject.toJSONString(json),JSONObject.toJSONString(ex),type,ApiSendStatusEnum.FAILURE.getCode());
                return;
            }
            //新增成功后编辑用量
            String id = save.getResult().getId();
            //给修改json对象赋值ID
            setQueryJSONObject(Integer.valueOf(id),apiUtils,platformEntity,map,type,json);
            //需要修改字段添加二级类目
            ArrayList<String> needUpDateFields = new ArrayList<>();
            needUpDateFields.add("FNUMERATOR");
            param.setNeedUpDateFields(needUpDateFields);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            return;
        }
        //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");
        Integer id = (Integer) model.get("Id");
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //审核中或已审核则要先反审
            documentStatus = kingdeeCommonService.unAudit(platformEntity, map,apiUtils, String.valueOf(id),type);
        }
        //创建状态则直接修改
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus)) {
            //给修改json对象赋值ID
            setQueryJSONObject(id,apiUtils,platformEntity,map,type,json);
            //需要更新的字段
            List<String> apiFieldList = mapList.stream().map(obj -> obj.getApiField()).sorted().distinct().collect(Collectors.toList());
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

    private void setQueryJSONObject (Integer id, KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type,JSONObject json) {
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FId = '%s'", id));
        String filterStr = String.join(" and ", queryFilters);
        //查询子单据id
        String fieldKeys = "FTreeEntity_FEntryId,FMATERIALIDCHILD.FNumber";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);
        if (CollectionUtils.isEmpty(queryList)) {
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")),filterStr,"未查询到子单据id",type,ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //主单据id
        KingdeeUtils.makeFieldJson(json,"FId",".", id);
        //比较
        for (Map<String, Object> queryMap: queryList) {
            ArrayList obj = (ArrayList) json.get("FTreeEntity");
            for (Object o : obj) {
                JSONObject jsonObject = (JSONObject) o;
                Object o1 = queryMap.get("FMATERIALIDCHILD.FNumber");
                JSONObject o2 = (JSONObject)jsonObject.get("FMATERIALIDCHILD");
                Object fNumber = o2.get("FNumber");
                if (o1.equals(fNumber)) {
                    jsonObject.put("FEntryId",queryMap.get("FTreeEntity_FEntryId"));
                }
            }
        }
    }

}
