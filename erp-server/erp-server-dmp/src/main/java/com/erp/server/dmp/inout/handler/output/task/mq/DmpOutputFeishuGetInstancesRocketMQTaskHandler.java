package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpFeishuInstanceDetailEntity;
import com.erp.model.dmp.entity.DmpRefPlatformFileEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.service.DmpRefPlatformFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputFeishuGetInstancesRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Resource
    private DmpRefPlatformFileService dmpRefPlatformFileService;

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpFeishuInstanceDetailEntity> dmpEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_feishu_instance_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpFeishuInstanceDetailEntity dmpEntity = (DmpFeishuInstanceDetailEntity) v;
                        dmpEntityMap.put(dmpEntity.getId(), dmpEntity);
                    }
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_feishu_instance_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                }
            }
        }

        // 查询转存的文件
        Map<String, DmpRefPlatformFileEntity> fileEntityMap = new HashMap<>();
        List<String> allCurFileKey = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dmpEntityMap.values())) {
            // 批量校验已存在的文件，过滤掉已存在的文件
            for (DmpFeishuInstanceDetailEntity entity : dmpEntityMap.values()) {
                String formStr = entity.getForm();
                List<String> fileKeyList = DmpFeishuInstanceDetailEntity.fromConvertFileInfoList(formStr, entity.getInstanceCode());
                allCurFileKey.addAll(fileKeyList);
            }
            fileEntityMap = dmpRefPlatformFileService.mapByFileKey("feishu", allCurFileKey);
        }


        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            DmpFeishuInstanceDetailEntity dmpEntity = dmpEntityMap.get(changId);
            JSONObject jsonObject = this.convert(dmpEntity, cfgOutputId, fileEntityMap);
            if (jsonObject != null) {
                map.put(dmpEntity.getId(), JSON.toJSONString(jsonObject));
            }
        }
        return map;

//    	Map<DmpCfgInputConvertEntity, List<Map<String, Object>>> changeConvertInputMongoEntityListMaps = dmpRequest.getChangeConvertInputMongoEntityListMaps();
//    	
//        Map<String, String> map = new HashMap<>();
//        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
//        if(CollUtil.isNotEmpty(changeConvertInputMongoEntityListMaps)) {
//        	for(Map.Entry<DmpCfgInputConvertEntity, List<Map<String, Object>>> changeConvertInputMongoEntityListMap : changeConvertInputMongoEntityListMaps.entrySet()) {
//        		List<Map<String, Object>> value = changeConvertInputMongoEntityListMap.getValue();
//        		if(CollUtil.isNotEmpty(value)) {
//        			for(Map<String, Object> v : value) {
//        				if(this.validateDataBlack(v, cfgOutputId)) {
//        					continue;
//        				}
//        				map.put(v.get("_id").toString(), JSON.toJSONString(v));
//        			}
//        		}
//        	}
//        }
//        return map;
    }

    private JSONObject convert(DmpFeishuInstanceDetailEntity dmpEntity, String cfgOutputId, Map<String, DmpRefPlatformFileEntity> fileEntityMap) {
        if (this.validateDataBlack(dmpEntity, cfgOutputId)) {
            return null;
        }

        String form = dmpEntity.getForm();
        String instanceCode = dmpEntity.getInstanceCode();
        if (StringUtils.isBlank(form)) {
            return JSONUtil.parseObj(dmpEntity);
        }
        JSONArray formArray = JSONUtil.parseArray(form);
        formArray.forEach(obj -> {
            JSONObject field = (JSONObject) obj;
            String fieldType = field.getStr("type");
            if ("fieldList".equals(fieldType)) {
                JSONArray valueArray = field.getJSONArray("value");
                valueArray.forEach(row -> {
                    JSONArray rowFields = (JSONArray) row;
                    rowFields.forEach(subObj -> {
                        JSONObject subField = (JSONObject) subObj;
                        checkAndReplaceFileUrl(subField, instanceCode, fileEntityMap);
                    });
                });
            } else {
                checkAndReplaceFileUrl(field, instanceCode, fileEntityMap);
            }
        });
        dmpEntity.setForm(formArray.toString());
        return JSONUtil.parseObj(dmpEntity);
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("serialNumber");
    }

    /**
     * 根据控件类型提取字段值
     */
    private void checkAndReplaceFileUrl(JSONObject field, String instanceCode, Map<String, DmpRefPlatformFileEntity> finalFileKeyMap) {
        String fieldType = field.getStr("type");
        if ("attachmentV2".equals(fieldType) || "imageV2".equals(fieldType) || "image".equals(fieldType)) {
            replaceFileUrl(field, instanceCode, finalFileKeyMap);
        }
    }

    private void replaceFileUrl(JSONObject valueObj, String instanceCode, Map<String, DmpRefPlatformFileEntity> finalFileKeyMap) {
        // {"id": "widget17530790199490001","name": "证件附件1","type": "attachmentV2","ext": "replay_pid24236.log","value": ["飞书url"]}
        JSONArray fileArray = valueObj.getJSONArray("value");
        if (CollectionUtils.isEmpty(fileArray)) {
            return;
        }
        String id = valueObj.getStr("id");
        String[] names = valueObj.getStr("ext").split(",");
        JSONArray newValueList = new JSONArray();
        for (int i = 0; i < names.length; i++) {
            String sourceUrl = fileArray.get(i).toString();
            String fileName = names[i];
            // 以 “审批实例 ID + 控件 ID + 文件名” 作为复合键判断重复。
            String fileKey = CharSequenceUtil.format("{}|{}|{}", instanceCode, id, fileName);
            DmpRefPlatformFileEntity dmpRefPlatformFileEntity = finalFileKeyMap.get(fileKey);
            if (null == dmpRefPlatformFileEntity) {
                ServiceException.runError("文件key【{}】未转存成功，无法继续后续操作", fileKey);
            }
            String fileUrl = dmpRefPlatformFileEntity.getFileUrl();
            if (StringUtils.isBlank(fileUrl)) {
                ServiceException.runError("文件key【{}】转存为空，无法继续后续操作", fileKey);
            }
            newValueList.add(fileUrl);
        }
        valueObj.set("value", newValueList);
    }


}
