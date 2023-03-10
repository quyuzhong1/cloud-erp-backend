package com.erp.server.dmp.push.consumer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.*;
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
 * @date 2023/3/9 16:24
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_product_detail_tag", consumerGroup = RocketMqTagEnum.SYNC_KINGDEE)
public class KingdeeProductDetailConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private PlatformService platformService;

    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    public static void main(String[] args) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(PlatformApiEnum.BD_MATERIAL.getTaskName());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FNumber = '%s'", "testtes"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FUseOrgId,FUseOrgId.FNumber,FUseOrgId.FName,FNumber,FName,FSubHeadEntity_FEntryId," +
                "SubHeadEntity_FEntryId,SubHeadEntity1_FEntryId,SubHeadEntity2_FEntryId,SubHeadEntity3_FEntryId,SubHeadEntity4_FEntryId,SubHeadEntity5_FEntryId," +
                "SubHeadEntity6_FEntryId,SubHeadEntity7_FEntryId,FBarCodeEntity_CMK_FEntryId,FSpecialAttributeEntity_FEntryId,FCategoryID,FNETWEIGHT,FLENGTH," +
                "FWIDTH,F_ulz_Qty1,F_PRVD_Assistant1.FNumber,F_PRVD_Assistant1.FDataValue,F_PRVD_Assistant1.FId,F_PRVD_Assistant.FId,SubHeadEntity_FErpClsID_FNumber";
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
        Integer type = ApiModuleTypeEnum.PRODUCTDETAIL.getCode();
        dto.setModuleType(type);
        List<CfgApiFieldMapDTO> mapList = cfgApiFieldMapService.getByParams(dto);
        //未配置发送字段
        if (CollectionUtils.isEmpty(mapList)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            kingdeeCommonService.insertFailureLog(platformEntity, map,"","未配置同步字段",type);
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_MATERIAL.getCode());
        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, mapList);

        //判断金蝶系统是否已存在该数据
        String skuNo = (String)map.get("skuNo");
        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("number",skuNo);
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
                kingdeeCommonService.insertFailureLog(platformEntity, map,JSONObject.toJSONString(json),JSONObject.toJSONString(ex),type);
                return;
            }
            //新增成功后编辑二级类目
            String id = save.getResult().getId();
            //主单据id
            KingdeeUtils.makeFieldJson(json,"FMATERIALID",".",id);
            //需要修改字段添加二级类目
            ArrayList<String> needUpDateFields = new ArrayList<>();
            String secondLevelCategory = mapList.stream().filter(obj -> "secondLevelCategory".equals(obj.getSelfField())).map(CfgApiFieldMapDTO::getApiField).findFirst().orElse("");
            String secondLevelCategoryCode = mapList.stream().filter(obj -> "secondLevelCategoryCode".equals(obj.getSelfField())).map(CfgApiFieldMapDTO::getApiField).findFirst().orElse("");
            String grossWeight = mapList.stream().filter(obj -> "grossWeight".equals(obj.getSelfField())).map(CfgApiFieldMapDTO::getApiField).findFirst().orElse("");
            String mainSupplier = mapList.stream().filter(obj -> "mainSupplier".equals(obj.getSelfField())).map(CfgApiFieldMapDTO::getApiField).findFirst().orElse("");
            needUpDateFields.addAll(Arrays.stream(grossWeight.split("\\.")).collect(Collectors.toList()));
            needUpDateFields.addAll(Arrays.stream(mainSupplier.split("\\.")).collect(Collectors.toList()));
            ArrayList<String> fields =(ArrayList<String>) Arrays.stream(secondLevelCategory.split("\\.")).collect(Collectors.toList());
            needUpDateFields.addAll(fields);
            ArrayList<String> codeFields =(ArrayList<String>) Arrays.stream(secondLevelCategoryCode.split("\\.")).collect(Collectors.toList());
            needUpDateFields.addAll(codeFields);
            param.setNeedUpDateFields(needUpDateFields);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            return;
        }
        //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");
        String id = (String) model.get("Id");
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //审核中或已审核则要先反审
            documentStatus = kingdeeCommonService.unAudit(platformEntity, map,apiUtils, id,type);
        }
        //创建状态则直接修改
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus)) {

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

            //更新业务单据状态 TODO
        }
    }
}
