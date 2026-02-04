package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.KingdeeParamDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastJsonUtil;
import com.common.core.utils.MathUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
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

    @Resource
    private PlmTaskFeign plmTaskFeign;

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
        LinkedList<String> queryFilters = new LinkedList<>();
        String syncKingdeeId = map.get("syncKingdeeId").toString();
        queryFilters.add(String.format("FSubReqId = '%s'", syncKingdeeId));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FBillNo";
        List<Map<String, Object>> queryList = bomApiUtils.queryList(filterStr, fieldKeys, 1, 1, 1);
        if (!queryList.isEmpty()) {
            Map<String, Object> bomMap = queryList.get(0);
            KingdeeUtils.makeFieldJson(json,"Ids",".", bomMap.get("FId"));
            Map<String, Object> bomChangeViewMap = new HashMap<>();
            bomChangeViewMap.put("syncKingdeeId",bomMap.get("FId"));
            JSONObject view = kingdeeCommonService.view(bomApiUtils, platformEntity.getId(), bomChangeViewMap);
            //转换数据
            JSONObject convertData = convertData(view,syncKingdeeId,queryList.get(0).get("FBillNo").toString());

            KingdeeParamDTO.SaveParamDTO saveParam = new KingdeeParamDTO.SaveParamDTO(convertData);
            saveParam.setIsVerifyBaseDataField(Boolean.FALSE);
            //新增
            RepoResult save = bomChangeApiUtils.saveKingDee(saveParam);
            //提交
            kingdeeCommonService.submit(null,bomChangeApiUtils,save.getId(),ApiModuleTypeEnum.SUBCONTRACT_BOM.getCode());
            //审核
            kingdeeCommonService.audit(null,bomChangeApiUtils,save.getId(),ApiModuleTypeEnum.SUBCONTRACT_BOM.getCode());
        }
    }

    public JSONObject convertData(JSONObject view,String syncKingdeeId,String bomBillNo){
        JSONArray ppBomEntries = view.getJSONArray("PPBomEntry");
        JSONArray FEntities = new JSONArray();
        JSONObject entries = new JSONObject();
        SubcontractOrderEntity subcontractOrder = scmTaskFeign.listSubcontractOrderByKingdeeId(syncKingdeeId);
        int counter = 0;
        //原数据行
        for (int i = 0; i < ppBomEntries.size(); i++) {
            if (Objects.nonNull(subcontractOrder)) {
                JSONObject srcEntry = ppBomEntries.getJSONObject(i);
                JSONObject changeBeforPpBom = createChangeBeforePpBomEntry(view,srcEntry, bomBillNo,subcontractOrder.getCode(),counter);
                JSONObject changeAfterPpBom = createChangeAfterPpBomEntry(view,srcEntry, bomBillNo,subcontractOrder.getCode(),counter);
                counter++;
                FEntities.put(changeBeforPpBom);
                FEntities.put(changeAfterPpBom);
            }
        }

        //添加新行
        if (Objects.nonNull(subcontractOrder)) {
            String mainId = subcontractOrder.getId();
            List<SubcontractOrderDetailEntity> subcontractOrderDetails = scmTaskFeign.listSubcontractDetailByMainIds(Collections.singletonList(mainId));
            List<SubcontractOrderDetailEntity> details = subcontractOrderDetails.stream()
                    .filter(item -> StringUtils.isNotBlank(item.getParentId()))
                    .collect(Collectors.toList());
            if (!details.isEmpty()) {
                for (SubcontractOrderDetailEntity subcontractOrderDetail : details) {
                    entries = createNewPpBomEntry(view, FEntities, subcontractOrderDetail, bomBillNo);
                }
            }
        }
        //单据类型
        JSONObject typeJson = new JSONObject();
        typeJson.put("FNumber", "WWYLQDBGD01_SYS");
        entries.put("FBillType", typeJson);
        //单据日期
        entries.put("FDate", LocalDate.now().toString());
        //单据状态
        entries.put("FDocumentStatus", "Z");
        //委外组织
        JSONObject subOrgJson = new JSONObject();
        subOrgJson.put("FNumber", "100");
        entries.put("FSubOrgId", subOrgJson);
        //金蝶工单补充字段
        entries.put("FSUBREQID", view.get("SubReqId"));
        entries.put("FSUBBILLNO", view.get("SubReqBillNO"));
        entries.put("FSUBREQENTRYID", view.get("SubReqEntryId"));
        entries.put("FSUBREQENTRYSEQ", view.get("SubReqEntrySeq"));
        return entries;
    }

    private JSONObject createChangeBeforePpBomEntry(JSONObject view,JSONObject srcEntry, String bomBillNo,String subCode,int counter) {
        JSONObject entry = new JSONObject();
        //物料编码
        JSONObject skuJson = new JSONObject();
        Object kingdeeSkuId = srcEntry.get("MaterialID_Id");
        if (Objects.nonNull(kingdeeSkuId)) {
            ProductDetailEntity productDetail = plmTaskFeign.getSkuBySyncKingdeeId(kingdeeSkuId.toString());
            if (Objects.nonNull(productDetail)) {
                skuJson.put("FNumber", productDetail.getSkuNo());
                entry.put("FMaterialID", skuJson);
                entry.put("FMaterialID2", skuJson);
            }
        }

        //单位
        JSONObject unitJson = new JSONObject();
        unitJson.put("FNumber", "Pcs");
        entry.put("FUnitID", unitJson);
        entry.put("FUnitID2", unitJson);
        //委外用料清单编号
        entry.put("FSUBPPBOMNo", bomBillNo);
        //委外订单号
        entry.put("FSubReqBillNO1", subCode);
        //子项类型
        entry.put("FMaterialType", "1");
        //超发控制方式
        entry.put("FOverControlMode", "1");
        //发料组织
        JSONObject supplyOrgJson = new JSONObject();
        supplyOrgJson.put("FNumber", "100");
        entry.put("FSupplyOrg", supplyOrgJson);
        //发料方式
        //entry.put("FIssueType", "2");
        entry.put("FIssueType", (counter % 2 == 1) ? "2" : "1");
        //变更前
        entry.put("FChangeType","2");
        //分子
        entry.put("FNumerator",srcEntry.get("Numerator"));
        //分母
        entry.put("FFDenominator",srcEntry.get("Denominator"));
        //应发数量
        entry.put("FMustQty",srcEntry.get("MustQty"));
        //生产数量
        //entry.put("FProduceQty",srcEntry.get("ProduceQty"));
        //未领数量
        entry.put("FNoPickedQty",srcEntry.get("NoPickedQty"));
        //用量类型
        //entry.put("FDosageType","1");
        entry.put("FDosageType", (counter % 2 == 1) ? "2" : "1");
        //子项类型
        entry.put("FMaterialType","1");
        //标准用量
        entry.put("FStdQty",srcEntry.get("StdQty"));
        //需求数量
        entry.put("FNeedQty2",srcEntry.get("NeedQty2"));
        //超发控制方式
        entry.put("FOverControlMode","1");
        //货主类型
        entry.put("FOwnerTypeId","BD_OwnerOrg");
        //倒冲时机
        entry.put("FBackFlushType", (counter % 2 == 1) ? "3" : "");
        //领料考虑最小发料批量
        entry.put("FISMinIssueQty", (counter % 2 == 1) ? Boolean.FALSE : Boolean.TRUE);
        //需求日期
        entry.put("FNeedDate2",srcEntry.get("NeedDate"));
        //用料清单类型
        entry.put("FPPBomEntryType", "0");
        //源单类型
        entry.put("FSrcBillType", "SUB_PPBOM");
        //源单编号
        entry.put("FSrcBillNo", bomBillNo);
        //多领退回
        entry.put("FReturnQty", 0);
        //应发数量(最小发料批量)
        entry.put("FMinIssueQty", 0);
        //委外订单分录行号
        entry.put("FSubReqEntrySeq1", srcEntry.get("SubReqEntrySeq"));
        //基本单位应发数量(最小发料批量)
        entry.put("FBaseMinIssueQty", srcEntry.get("BaseMinIssueQty"));
        //原用料清单内码
        entry.put("FSrcPPBOMID", view.get("Id"));
        //原用料清单分录内码
        entry.put("FSrcPPBOMEntryId", srcEntry.get("Id"));
        //是否应发修改
        entry.put("FISMODIFYMQ", Boolean.TRUE);
        //行展开类型
        entry.put("FRowExpandType", 0);
        //工序
        entry.put("FOperID", (counter % 2 == 1) ? 10 : 0);
        //项次
        entry.put("FReplaceGroup", counter + 1);
        //金蝶工单补充字段
        entry.put("FSUBPPBOMEntrySeq", 1);
        entry.put("FSUBPPBOMEntryId", srcEntry.get("Id"));
        entry.put("FSUBPPBOMId",view.get("Id"));
        JSONObject entityLinkEntry = new JSONObject();
        entityLinkEntry.put("FEntity_Link_FFlowId","0b064121-4926-4808-8632-a195b6a202e8");
        entityLinkEntry.put("FEntity_Link_FFlowLineId","14");
        entityLinkEntry.put("FEntity_Link_FRuleId","SUB_PPBOM2PPBOMCHANGE");
        entityLinkEntry.put("FEntity_Link_FSTableName","T_SUB_PPBOMENTRY");
        entityLinkEntry.put("FEntity_Link_FSBillId",view.get("BOMID_Id"));
        entityLinkEntry.put("FEntity_Link_FSId",srcEntry.get("BOMEntryID"));
        entityLinkEntry.put("FEntity_Link_FBaseStdQty",srcEntry.get("StdQty"));
        entry.put("FEntity__Link",entityLinkEntry);
        return entry;
    }

    private JSONObject createChangeAfterPpBomEntry(JSONObject view,JSONObject srcEntry, String bomBillNo,String subCode,int counter) {
        JSONObject entry = new JSONObject();
        //物料编码
        JSONObject skuJson = new JSONObject();
        Object kingdeeSkuId = srcEntry.get("MaterialID_Id");
        if (Objects.nonNull(kingdeeSkuId)) {
            ProductDetailEntity productDetail = plmTaskFeign.getSkuBySyncKingdeeId(kingdeeSkuId.toString());
            if (Objects.nonNull(productDetail)) {
                skuJson.put("FNumber", productDetail.getSkuNo());
                entry.put("FMaterialID", skuJson);
                entry.put("FMaterialID2", skuJson);
            }
        }

        //单位
        JSONObject unitJson = new JSONObject();
        unitJson.put("FNumber", "Pcs");
        entry.put("FUnitID", unitJson);
        entry.put("FUnitID2", unitJson);
        //委外用料清单编号
        entry.put("FSUBPPBOMNo", bomBillNo);
        //委外订单号
        entry.put("FSubReqBillNO1", subCode);
        //子项类型
        entry.put("FMaterialType", "1");
        //超发控制方式
        entry.put("FOverControlMode", "1");
        //发料组织
        JSONObject supplyOrgJson = new JSONObject();
        supplyOrgJson.put("FNumber", "100");
        entry.put("FSupplyOrg", supplyOrgJson);
        //发料方式
        //entry.put("FIssueType", "1");
        entry.put("FIssueType", (counter % 2 == 1) ? "2" : "1");
        //变更后
        entry.put("FChangeType","3");
        //分子
        entry.put("FNumerator",0);
        //分母
        entry.put("FFDenominator",srcEntry.get("Denominator"));
        //应发数量
        entry.put("FMustQty",0);
        //生产数量
        //entry.put("FProduceQty",0);
        //未领数量
        entry.put("FNoPickedQty",1);
        //用量类型
        entry.put("FDosageType", (counter % 2 == 1) ? "2" : "1");
        //需求数量
        entry.put("FNeedQty2",srcEntry.get("NeedQty2"));
        //超发控制方式
        entry.put("FOverControlMode","1");
        //货主类型
        entry.put("FOwnerTypeId","BD_OwnerOrg");
        //倒冲时机
        entry.put("FBackFlushType", (counter % 2 == 1) ? "3" : "");
        //领料考虑最小发料批量
        entry.put("FISMinIssueQty", (counter % 2 == 1) ? Boolean.FALSE : Boolean.TRUE);
        //需求日期
        entry.put("FNeedDate2",srcEntry.get("NeedDate"));
        //用料清单类型
        entry.put("FPPBomEntryType", "0");
        //源单类型
        entry.put("FSrcBillType", "SUB_PPBOM");
        //源单编号
        entry.put("FSrcBillNo", bomBillNo);
        //多领退回
        entry.put("FReturnQty", 0);
        //应发数量(最小发料批量)
        entry.put("FMinIssueQty", 0);
        //委外订单分录行号
        entry.put("FSubReqEntrySeq1", srcEntry.get("SubReqEntrySeq"));
        //基本单位应发数量(最小发料批量)
        entry.put("FBaseMinIssueQty", srcEntry.get("BaseMinIssueQty"));
        //原用料清单内码
        entry.put("FSrcPPBOMID", view.get("Id"));
        //原用料清单分录内码
        entry.put("FSrcPPBOMEntryId", srcEntry.get("Id"));
        //是否应发修改
        entry.put("FISMODIFYMQ", Boolean.TRUE);
        //行展开类型
        entry.put("FRowExpandType", 0);
        //工序
        entry.put("FOperID", (counter % 2 == 1) ? 10 : 0);
        //项次
        entry.put("FReplaceGroup", counter + 1);
        //金蝶工单补充字段
        entry.put("FSUBPPBOMEntrySeq", 1);
        entry.put("FSUBPPBOMEntryId", srcEntry.get("Id"));
        entry.put("FSUBPPBOMId",view.get("Id"));
        JSONObject entityLinkEntry = new JSONObject();
        entityLinkEntry.put("FEntity_Link_FFlowId","0b064121-4926-4808-8632-a195b6a202e8");
        entityLinkEntry.put("FEntity_Link_FFlowLineId","14");
        entityLinkEntry.put("FEntity_Link_FRuleId","SUB_PPBOM2PPBOMCHANGE");
        entityLinkEntry.put("FEntity_Link_FSTableName","T_SUB_PPBOMENTRY");
        entityLinkEntry.put("FEntity_Link_FSBillId",view.get("BOMID_Id"));
        entityLinkEntry.put("FEntity_Link_FSId",srcEntry.get("BOMEntryID"));
        entityLinkEntry.put("FEntity_Link_FBaseStdQty",0);
        entry.put("FEntity__Link",entityLinkEntry);
        return entry;
    }

    private JSONObject createNewPpBomEntry(JSONObject view,JSONArray ppBomEntries,SubcontractOrderDetailEntity detail, String bomBillNo) {
        JSONObject entries = new JSONObject();
        Map<String, Object> entry = new HashMap<>();

        //物料编码
        JSONObject skuJson = new JSONObject();
        skuJson.put("FNumber", detail.getSkuNo());
        entry.put("FMaterialID", skuJson);
        entry.put("FMaterialID2", skuJson);

        //单位
        JSONObject unitJson = new JSONObject();
        unitJson.put("FNumber", "Pcs");
        entry.put("FUnitID", unitJson);
        entry.put("FUnitID2", unitJson);
        //BOM版本
        JSONObject bomVersonJson = new JSONObject();
        bomVersonJson.put("FNumber", "");
        entry.put("FBomId", bomVersonJson);
        //委外用料清单编号
        entry.put("FSUBPPBOMNo", bomBillNo);
        //用料类型
        entry.put("FDosageType", "2");
        //子项类型
        entry.put("FMaterialType", "1");
        //超发控制方式
        entry.put("FOverControlMode", "1");
        //发料组织
        JSONObject supplyOrgJson = new JSONObject();
        supplyOrgJson.put("FNumber", "100");
        entry.put("FSupplyOrg", supplyOrgJson);
        //发料方式
        entry.put("FIssueType", "1");
        //需求日期
        entry.put("FNeedDate2",detail.getPlanDeliveryDate().toString());
        //新增
        entry.put("FChangeType","1");
        //应发数量
        entry.put("FMustQty",detail.getQty());
        //生产数量
        entry.put("FProduceQty",detail.getQty());
        //用料清单类型
        entry.put("FPPBomEntryType","0");
        //基本单位未领数量
        entry.put("FBaseNoPickedQty",detail.getQty());

        ppBomEntries.put(entry);
        entries.put("FEntity", ppBomEntries);
        return entries;
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