package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.KingdeeParamDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastJsonUtil;
import com.common.core.utils.MathUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApi;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeeSubcontractBOMConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.kingdee.bos.webapi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wtr
 * @Date: 2026/1/19 10:20
 * @Param:
 * @Return:
 * @Description:
 **/
@Service
@Slf4j
public class KingdeeSubcontractBOMConsumerServiceImpl implements KingdeeSubcontractBOMConsumerService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @KingdeeApi
    public void executeConsumer(Map<String, Object> map) {

        //模块类型
        Integer type = ApiModuleTypeEnum.SUBCONTRACT_BOM.getCode();

        //操作项
        String operate = (String) map.get("operate");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils bomApiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SUBCONTRACT_BOM.getCode());
        KingdeeApiUtils bomChangeApiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SUBCONTRACT_BOM_CHANGE.getCode());
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SUB_SUBREQORDER.getCode());

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(),type);

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.MAPPING_NOT_SET_PUSH_FORBIDDEN.getMsg());
            //错误日志
            throw new ServiceException(ApiError.DMP_KINGDEE_FIELD_NOT_FOUND);
        }

        /**
         * 下推
         */
        if (SyncOperateEnum.OPERATE_PUSH.getCode().equals(operate)) {
            operatePush(bomApiUtils,bomChangeApiUtils,apiUtils,platformEntity, map,json,type);
        }

    }

    /**
     * 下推
     */
    public void operatePush(KingdeeApiUtils bomApiUtils,KingdeeApiUtils bomChangeApiUtils,KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,JSONObject json,Integer type) {
        //判断金蝶系统是否已存在该数据
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        LinkedList<String> queryFilters = new LinkedList<>();
        String syncKingdeeId = map.get("syncKingdeeId").toString();
        queryFilters.add(String.format("FSubReqId = '%s'", syncKingdeeId));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FBillNo";
        List<Map<String, Object>> queryList = bomApiUtils.queryList(filterStr, fieldKeys, 1, 1, 1);
        if (!queryList.isEmpty()) {
            KingdeeUtils.makeFieldJson(json,"Ids",".", queryList.get(0).get("FId"));
            //下推
            RepoResult result = bomApiUtils.push(json);
            //数据id
            String id = result.getResponseStatus().getSuccessEntitys().get(0).getId();
            Map<String, Object> bomChangeViewMap = new HashMap<>();
            bomChangeViewMap.put("syncKingdeeId",id);
            JSONObject view = kingdeeCommonService.view(bomChangeApiUtils, platformEntity.getId(), bomChangeViewMap);
            JSONObject convertData = convertData(view,syncKingdeeId);

            KingdeeParamDTO.SaveParamDTO saveParam = new KingdeeParamDTO.SaveParamDTO(convertData);
            RepoResult save = apiUtils.saveKingDee(saveParam);
            if (!save.getResponseStatus().isIsSuccess()) {
                throw new ServiceException(ApiError.DMP_KINGDEE_ADD_FAILED);
            }
        }
    }

    public JSONObject convertData(JSONObject view,String syncKingdeeId){
        // 处理现有的PPBomEntry,添加FDeleteEntry=true
        view.put("PPBomEntry", new JSONArray());
        JSONArray ppBomEntries = view.getJSONArray("PPBomEntry");
//        for (int i = 0; i < ppBomEntries.size(); i++) {
//            JSONObject entry = ppBomEntries.getJSONObject(i);
//            entry.put("FDeleteEntry", true);
//        }

        //添加新行
        SubcontractOrderEntity subcontractOrder = scmTaskFeign.listSubcontractOrderByKingdeeId(syncKingdeeId);
        if (Objects.nonNull(subcontractOrder)) {
            String mainId = subcontractOrder.getId();
            List<SubcontractOrderDetailEntity> subcontractOrderDetails = scmTaskFeign.listSubcontractDetailByMainIds(Collections.singletonList(mainId));
            if (!subcontractOrderDetails.isEmpty()) {
                String subReqBillNO = view.get("SubReqBillNO").toString();
                for (SubcontractOrderDetailEntity subcontractOrderDetail : subcontractOrderDetails) {
                    createNewPpBomEntry(view,ppBomEntries,subcontractOrderDetail,subReqBillNO);
                }
            }
        }
        return view;
    }

    private void createNewPpBomEntry(JSONObject view,JSONArray ppBomEntries,SubcontractOrderDetailEntity detail, String subReqBillNO) {
        Map<String, Object> entry = new HashMap<>();

        //物料编码
        entry.put("FMaterialId", detail.getSkuNo());
        //单位
        entry.put("FUnitID", "Pcs");
        //BOM版本
        entry.put("FBomId", "1");
        //BOM版本
        entry.put("FQty", detail.getRepairQty() == 0 ? detail.getQty() : detail.getRepairQty());
        //委外用料清单编号
        entry.put("FSUBPPBOMNo", subReqBillNO);
        //用料类型
        entry.put("FDosageType", "");
        //子项类型
        entry.put("FMaterialType", "");
        //超发控制方式
        entry.put("FOverControlMode", "");
        //发料组织
        entry.put("FSupplyOrg", "");
        //发料方式
        entry.put("FIssueType", "");
        //需求日期
        entry.put("FNeedDate2",detail.getPlanDeliveryDate().toString());
        ppBomEntries.put(entry);
        view.put("PPBomEntry", ppBomEntries);
    }

    /**
     * @description: 给明细id赋值
     * @author Will
     * @date: 2023/6/26 17:11
     * @param apiUtils
     * @param map
     * @return JSONArray
     */
    public JSONArray setDetailIdForJSONObject (KingdeeApiUtils apiUtils,Map<String, Object> map) {

        JSONArray list = JSONUtil.parseArray(map.get("list"));
        String id = (String)map.get("syncKingdeeId");

        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FId = '%s'", id));
        String filterStr = String.join(" and ", queryFilters);
        //查询子单据id
        String fieldKeys = "FTreeEntity_FEntryID";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);
        if (CollectionUtils.isEmpty(queryList)) {
            //错误日志
            throw new ServiceException(ApiError.DMP_KINGDEE_DETAIL_ID_NOT_FOUND);
        }
        JSONArray removeObj = new JSONArray();
        JSONArray addObj = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            JSONObject newJson = new JSONObject(new LinkedHashMap<>());
            if (list.size() >= queryList.size()) {
                //金蝶明细id赋值
                newJson.set("kingdeeDetailId",queryList.get(i).get("FTreeEntity_FEntryID"));
            }
            newJson.putAll(jsonObject);
            removeObj.set(obj);
            addObj.set(newJson);
        }
        list.removeAll(removeObj);
        list.addAll(addObj);
        return list;
    }

    /**
     * 更新明细id
     */
    public void updateKingdeeDetailId (JSONArray jsonArray) {
        //更新业务单据状态
        Map<String,Object> params = new HashMap<>(MathUtil.THREE);
        params.put("code",ApiModuleTypeEnum.SUBCONTRACT_ORDER.getCode().toString());
        params.put("details",jsonArray);
        scmTaskFeign.updateBusinessSyncKingdeeStatus(params);
    }


    /**
     * 新增
     */
    public Boolean saveOrUpdate (KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type,JSONObject json,KingdeeParamDTO.SaveParamDTO param) {

        Boolean isAdd = kingdeeCommonService.saveAndAutoApprove(platformEntity,map,apiUtils,json,param,type);
        if (isAdd) {
            //给明细id赋值
            JSONArray jsonArray = setDetailIdForJSONObject(apiUtils, map);
            //更新明细id
            updateKingdeeDetailId(jsonArray);
        }
        return  isAdd;
    }

}