package com.erp.server.dmp.push.service.kingdee.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.kingdee.KingdeePushService;
import com.erp.server.dmp.service.CfgApiFieldMapService;
import com.erp.server.dmp.service.PlatformService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: BOM发送金蝶
 * @date 2023/3/3 11:06
 */
@Slf4j
@Service("kingdeeBomInfoService")
public class KingdeeBomInfoServiceImpl implements KingdeePushService {

    @Resource
    private PlatformService platformService;

    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    @Resource
    private KingdeeCommonService kingdeeCommonService;


    public static void main(String[] args) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pushKingdee(Map<String, Object> map) {
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
        dto.setModuleType(ApiModuleTypeEnum.BOMMANAGE.getCode());
        List<CfgApiFieldMapDTO> mapList = cfgApiFieldMapService.getByParams(dto);
        //未配置发送字段
        if (CollectionUtils.isEmpty(mapList)) {
            log.error(ApiError.ERROR_97025.msg);
            //新增定时同步任务
            kingdeeCommonService.insertApiSyncTask(platformEntity, map);
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.ENG_BOM.getCode());
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
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param);
            return;
        }
        //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");
        String id = (String) model.get("Id");
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //审核中或已审核则要先反审
            documentStatus = kingdeeCommonService.unAudit(platformEntity, map,apiUtils, id);
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
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param);
        }
    }

}
