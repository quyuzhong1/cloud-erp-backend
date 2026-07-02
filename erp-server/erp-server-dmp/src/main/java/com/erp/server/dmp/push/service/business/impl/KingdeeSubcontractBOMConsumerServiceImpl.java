package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.KingdeeParamDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ConvertUtil;
import com.common.core.utils.MathUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.KingdeeSubcontractBomBackFlushTypeEnum;
import com.erp.model.dmp.enums.KingdeeSubcontractBomDosageTypeEnum;
import com.erp.model.dmp.enums.KingdeeSubcontractBomIssueTypeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.util.SubcontractOrderKingdeeLineSeqUtils;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApi;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeeSubcontractBOMConsumerService;
import com.erp.server.dmp.push.service.business.model.RepairBomConvertContext;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.kingdee.bos.webapi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
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

    @Resource
    private SysUserFeign sysUserFeign;


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
        KingdeeApiUtils skuApiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_MATERIAL.getCode());

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
        operatePush(bomApiUtils,bomChangeApiUtils,skuApiUtils,platformEntity, map,json,type);
    }

    /**
     * 下推
     */
    public void operatePush(KingdeeApiUtils bomApiUtils,KingdeeApiUtils bomChangeApiUtils,KingdeeApiUtils skuApiUtils,PlatformEntity platformEntity,Map<String, Object> map,JSONObject json,Integer type) {
        //判断金蝶系统是否已存在该数据
        LinkedList<String> queryFilters = new LinkedList<>();

        String syncKingdeeId = map.get("syncKingdeeId").toString();
        queryFilters.add(String.format("FSubReqId = '%s'", syncKingdeeId));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FBillNo";

        int pageIndex = 1;
        int pageSize = 1000;
        boolean hasNextPage = Boolean.TRUE;
        List<Map<String, Object>> queryList = bomApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, pageSize);
        while (hasNextPage) {
            if (queryList.size() == pageSize) {
                pageIndex++;
                // 查询下一页
                queryList = bomApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, pageSize);
                queryList.addAll(queryList);
            } else {
                // 如果返回的记录数小于pageSize，说明没有更多数据了
                hasNextPage = false;
            }
        }

        if (!queryList.isEmpty()) {
            Map<String, Object> bomMap = queryList.get(0);
            KingdeeUtils.makeFieldJson(json,"Ids",".", bomMap.get("FId"));

            // syncKingdeeId 在循环中保持不变，相关委外订单 / 明细统一在循环外预加载，避免在 convertOldData / convertNewData 内重复 Feign 调用
            SubcontractOrderEntity subcontractOrder = scmTaskFeign.listSubcontractOrderByKingdeeId(syncKingdeeId);
            if (Objects.isNull(subcontractOrder)) {
                throw new ServiceException(ApiError.PO_SUBCONTRACT_ORDER_NOT_FOUND);
            }
            // Feign 降级 / 超时可能返回 null，未取到明细同样视为异常，避免后续 stream() NPE
            List<SubcontractOrderDetailEntity> subcontractOrderDetails = scmTaskFeign.listSubcontractDetailByMainIds(Collections.singletonList(subcontractOrder.getId()));
            if (CollectionUtils.isEmpty(subcontractOrderDetails)) {
                throw new ServiceException(ApiError.PO_SUBCONTRACT_DETAIL_NOT_FOUND);
            }

            //转换数据
            for (Map<String, Object> query : queryList) {
                Map<String, Object> bomChangeViewMap = new HashMap<>();
                bomChangeViewMap.put("syncKingdeeId",query.get("FId"));
                JSONObject view = kingdeeCommonService.view(bomApiUtils, platformEntity.getId(), bomChangeViewMap);
                log.warn("委外用料清单变更单查询报文：{}" , view.toString());
                RepairBomConvertContext convertContext = buildRepairBomConvertContext(view, subcontractOrderDetails, map);
                //处理旧单
                JSONObject convertOldData = convertOldData(view, skuApiUtils, platformEntity.getId(),
                        subcontractOrder, query.get("FBillNo").toString(), convertContext);
                KingdeeParamDTO.SaveParamDTO saveOldParam = new KingdeeParamDTO.SaveParamDTO(convertOldData);
                saveOldParam.setIsVerifyBaseDataField(Boolean.FALSE);
                //新增
                RepoResult saveOld = bomChangeApiUtils.saveKingDee(saveOldParam);
                //提交
                kingdeeCommonService.submit(null,bomChangeApiUtils,saveOld.getId(),ApiModuleTypeEnum.SUBCONTRACT_BOM.getCode());
                //审核
                kingdeeCommonService.audit(null,bomChangeApiUtils,saveOld.getId(),ApiModuleTypeEnum.SUBCONTRACT_BOM.getCode());

                //新增新单
                JSONObject convertNewData = convertNewData(view, subcontractOrder,
                        query.get("FBillNo").toString(), convertContext);
                KingdeeParamDTO.SaveParamDTO saveNewParam = new KingdeeParamDTO.SaveParamDTO(convertNewData);
                saveNewParam.setIsVerifyBaseDataField(Boolean.FALSE);
                //新增
                RepoResult saveNew = bomChangeApiUtils.saveKingDee(saveNewParam);
                //提交
                kingdeeCommonService.submit(null,bomChangeApiUtils,saveNew.getId(),ApiModuleTypeEnum.SUBCONTRACT_BOM.getCode());
                //审核
                kingdeeCommonService.audit(null,bomChangeApiUtils,saveNew.getId(),ApiModuleTypeEnum.SUBCONTRACT_BOM.getCode());
            }

        }
    }

    public JSONObject convertOldData(JSONObject view, KingdeeApiUtils skuApiUtils, String platformId,
            SubcontractOrderEntity subcontractOrder, String bomBillNo, RepairBomConvertContext convertContext) {
        JSONArray ppBomEntries = view.getJSONArray("PPBomEntry");
        JSONArray FEntities = new JSONArray();
        JSONObject entries = new JSONObject();
        SysAccountingCompanyEntity sysAccountingCompany = sysUserFeign.getCompanyByKindgeeId(view.get("SubOrgId_Id").toString());
        if (Objects.isNull(sysAccountingCompany)) {
            throw new ServiceException(ApiError.COMMON_COMPANY_NOT_FOUND);
        }
        Map<String, JSONObject> skuJsonCache = new HashMap<>();
        // 同一 BOM 内多条子行可能对应相同 SKU（跨父行或同父行重复子料），记录已匹配的 SCM 子行避免重复消费
        Set<String> usedChildDetailIds = new HashSet<>();
        int entryIndex = 0;
        //原数据行
        for (int i = 0; i < ppBomEntries.size(); i++) {
            JSONObject srcEntry = ppBomEntries.getJSONObject(i);
            JSONObject skuJson = resolveSkuJson(skuApiUtils, platformId, srcEntry, skuJsonCache);
            String skuNumber = extractSkuNumber(skuJson);
            SubcontractOrderDetailEntity matchedDetail = matchChangeChildDetail(skuNumber, srcEntry, view,
                    convertContext, usedChildDetailIds);
            if (matchedDetail != null && StringUtils.isNotBlank(matchedDetail.getId())) {
                usedChildDetailIds.add(matchedDetail.getId());
            }
            JSONObject stockFields = resolveScmStockFields(matchedDetail, convertContext.getDetailStockFieldMap());
            logChangeChildDetailMatchResult(view, srcEntry, skuNumber, matchedDetail, stockFields, convertContext);
            JSONObject changeBeforPpBom = createChangeBeforePpBomEntry(view, srcEntry, bomBillNo,
                    subcontractOrder.getCode(), sysAccountingCompany, entryIndex, skuJson, stockFields);
            JSONObject changeAfterPpBom = createChangeAfterPpBomEntry(view, srcEntry, bomBillNo,
                    subcontractOrder.getCode(), sysAccountingCompany, entryIndex, skuJson, stockFields);
            entryIndex++;
            FEntities.put(changeBeforPpBom);
            FEntities.put(changeAfterPpBom);
        }
        entries.put("FEntity",FEntities);
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
        subOrgJson.put("FNumber", sysAccountingCompany.getKingdeeCode());
        entries.put("FSubOrgId", subOrgJson);
        //是否自动调整JSON字段顺序
        entries.put("IsAutoAdjustField", Boolean.TRUE);
        //金蝶工单补充字段
        entries.put("FSUBREQID", view.get("SubReqId"));
        entries.put("FSUBBILLNO", view.get("SubReqBillNO"));
        entries.put("FSUBREQENTRYID", view.get("SubReqEntryId"));
        entries.put("FSUBREQENTRYSEQ", view.get("SubReqEntrySeq"));
        return entries;
    }

    public JSONObject convertNewData(JSONObject view, SubcontractOrderEntity subcontractOrder,
            String bomBillNo, RepairBomConvertContext convertContext) {
        JSONArray FEntities = new JSONArray();
        JSONObject entries = new JSONObject();
        SysAccountingCompanyEntity sysAccountingCompany = sysUserFeign.getCompanyByKindgeeId(view.get("SubOrgId_Id").toString());
        if (Objects.isNull(sysAccountingCompany)) {
            throw new ServiceException(ApiError.COMMON_COMPANY_NOT_FOUND);
        }

        for (RepairBomConvertContext.ParentChildGroup group : convertContext.getMatchedGroups()) {
            SubcontractOrderDetailEntity parentDetail = group.getParentDetail();
            for (SubcontractOrderDetailEntity subcontractOrderDetail : group.getChildDetails()) {
                entries = createNewPpBomEntry(FEntities, subcontractOrder, parentDetail, subcontractOrderDetail,
                        sysAccountingCompany, bomBillNo, convertContext.getDetailStockFieldMap());
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
        subOrgJson.put("FNumber", sysAccountingCompany.getKingdeeCode());
        entries.put("FSubOrgId", subOrgJson);
        //是否自动调整JSON字段顺序
        entries.put("IsAutoAdjustField", Boolean.TRUE);
        //金蝶工单补充字段
        entries.put("FSUBREQID", view.get("SubReqId"));
        entries.put("FSUBBILLNO", view.get("SubReqBillNO"));
        entries.put("FSUBREQENTRYID", view.get("SubReqEntryId"));
        entries.put("FSUBREQENTRYSEQ", view.get("SubReqEntrySeq"));
        return entries;
    }

    private JSONObject createChangeBeforePpBomEntry(JSONObject view, JSONObject srcEntry, String bomBillNo, String subCode,
            SysAccountingCompanyEntity sysAccountingCompany, int entryIndex, JSONObject skuJson, JSONObject stockFields) {
        JSONObject entry = new JSONObject(new LinkedHashMap<>());
        applySkuJson(entry, skuJson);

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
        supplyOrgJson.put("FNumber", sysAccountingCompany.getKingdeeCode());
        entry.put("FSupplyOrg", supplyOrgJson);
        //发料方式：变更前取源单原值
        entry.put("FIssueType", resolveIssueType(srcEntry));
        //变更前
        entry.put("FChangeType","2");
        //分子
        entry.put("FNumerator",srcEntry.get("Numerator"));
        //分母
        entry.put("FDenominator",srcEntry.get("Denominator"));
        //应发数量
        entry.put("FMustQty",srcEntry.get("MustQty"));
        //未领数量
        entry.put("FNoPickedQty", resolveNoPickedQty(srcEntry, 0));
        //用量类型
        entry.put("FDosageType", resolveDosageType(srcEntry));
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
        //倒冲时机：变更前取源单原值
        entry.put("FBackFlushType", resolveBackFlushType(srcEntry));
        //领料考虑最小发料批量
        entry.put("FISMinIssueQty", resolveConsiderMinIssueQty(srcEntry));
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
        entry.put("FOperID", resolveOperId(srcEntry));
        //项次
        entry.put("FReplaceGroup", resolveReplaceGroup(srcEntry, entryIndex));
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
        mergeStockFields(entry, stockFields);
        entry.put("FEntity__Link",entityLinkEntry);
        return entry;
    }

    private JSONObject createChangeAfterPpBomEntry(JSONObject view, JSONObject srcEntry, String bomBillNo, String subCode,
            SysAccountingCompanyEntity sysAccountingCompany, int entryIndex, JSONObject skuJson, JSONObject stockFields) {
        JSONObject entry = new JSONObject(new LinkedHashMap<>());
        applySkuJson(entry, skuJson);

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
        supplyOrgJson.put("FNumber", sysAccountingCompany.getKingdeeCode());
        entry.put("FSupplyOrg", supplyOrgJson);
        //发料方式/倒冲时机：与变更前一致，便于金蝶变更后行落库仓库/库位
        entry.put("FIssueType", resolveIssueType(srcEntry));
        //变更后
        entry.put("FChangeType","3");
        //分子固定为 0（业务要求，不可改）
        entry.put("FNumerator", 0);
        //分母
        entry.put("FDenominator", srcEntry.get("Denominator"));
        //应发数量：变更后行清零，与 FNumerator=0 语义一致
        entry.put("FMustQty", 0);
        //未领数量
        entry.put("FNoPickedQty", resolveNoPickedQty(srcEntry, 1));
        //用量类型
        entry.put("FDosageType", resolveDosageType(srcEntry));
        //标准用量：变更后行清零，与变更前字段对齐
        entry.put("FStdQty", 0);
        //需求数量
        entry.put("FNeedQty2",srcEntry.get("NeedQty2"));
        //超发控制方式
        entry.put("FOverControlMode","1");
        //货主类型
        entry.put("FOwnerTypeId","BD_OwnerOrg");
        //倒冲时机：与变更前一致
        entry.put("FBackFlushType", resolveBackFlushType(srcEntry));
        //领料考虑最小发料批量
        entry.put("FISMinIssueQty", resolveConsiderMinIssueQty(srcEntry));
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
        entry.put("FOperID", resolveOperId(srcEntry));
        //项次
        entry.put("FReplaceGroup", resolveReplaceGroup(srcEntry, entryIndex));
        //金蝶工单补充字段
        entry.put("FSUBPPBOMEntrySeq", 1);
        entry.put("FSUBPPBOMEntryId", srcEntry.get("Id"));
        entry.put("FSUBPPBOMId",view.get("Id"));
        mergeStockFields(entry, stockFields);
        JSONObject entityLinkEntry = new JSONObject();
        entityLinkEntry.put("FEntity_Link_FFlowId","0b064121-4926-4808-8632-a195b6a202e8");
        entityLinkEntry.put("FEntity_Link_FFlowLineId","14");
        entityLinkEntry.put("FEntity_Link_FRuleId","SUB_PPBOM2PPBOMCHANGE");
        entityLinkEntry.put("FEntity_Link_FSTableName","T_SUB_PPBOMENTRY");
        entityLinkEntry.put("FEntity_Link_FSBillId",view.get("BOMID_Id"));
        entityLinkEntry.put("FEntity_Link_FSId",srcEntry.get("BOMEntryID"));
        entityLinkEntry.put("FEntity_Link_FBaseStdQty", 0);
        entry.put("FEntity__Link",entityLinkEntry);
        return entry;
    }

    private JSONObject createNewPpBomEntry(JSONArray ppBomEntries, SubcontractOrderEntity subcontractOrder,
            SubcontractOrderDetailEntity parentDetail, SubcontractOrderDetailEntity chilDetail,
            SysAccountingCompanyEntity sysAccountingCompany, String bomBillNo,
            Map<String, JSONObject> detailStockFieldMap) {
        JSONObject entries = new JSONObject();
        Map<String, Object> entry = new LinkedHashMap<>();

        //物料编码
        JSONObject skuJson = new JSONObject();
        skuJson.put("FNumber", chilDetail.getSkuNo());
        entry.put("FMaterialID", skuJson);
        entry.put("FMaterialID2", skuJson);

        //单位
        JSONObject unitJson = new JSONObject();
        unitJson.put("FNumber", "Pcs");
        entry.put("FUnitID", unitJson);
        entry.put("FUnitID2", unitJson);
        //委外用料清单编号
        entry.put("FSUBPPBOMNo", bomBillNo);
        //用料类型
        entry.put("FDosageType", KingdeeSubcontractBomDosageTypeEnum.VARIABLE.getCode());
        //子项类型
        entry.put("FMaterialType", "1");
        //超发控制方式
        entry.put("FOverControlMode", "1");
        //发料组织
        JSONObject supplyOrgJson = new JSONObject();
        supplyOrgJson.put("FNumber", sysAccountingCompany.getKingdeeCode());
        entry.put("FSupplyOrg", supplyOrgJson);
        //发料方式
        //发料方式：直接倒冲
        entry.put("FIssueType", KingdeeSubcontractBomIssueTypeEnum.DIRECT_BACKFLUSH.getCode());
        //需求日期
        entry.put("FNeedDate2",subcontractOrder.getBillDate().toString());
        //新增
        entry.put("FChangeType","1");
        //用料清单类型
        entry.put("FPPBomEntryType","0");
        //源单类型
        entry.put("FSrcBillType", "SUB_PPBOM");
        //源单编号
        entry.put("FSrcBillNo", bomBillNo);
        //分子 分母
        if (chilDetail.getQty() > parentDetail.getRepairQty()) {
            entry.put("FNumerator", chilDetail.getQty() / parentDetail.getRepairQty());
        } else {
            entry.put("FDenominator", parentDetail.getRepairQty() / chilDetail.getQty());
        }

        if (chilDetail.getQty() == 1) {
            entry.put("FDenominator", parentDetail.getRepairQty());
        }

        if (parentDetail.getRepairQty() == 1) {
            entry.put("FNumerator", parentDetail.getRepairQty());
        }

        // 仓库/仓位降级策略由 SCM 侧统一判定并写入 list；此处仅透传已组装的字段，不再重复 warn
        JSONObject stockFields = resolveDetailStockFields(detailStockFieldMap, chilDetail.getId(), parentDetail.getId());
        mergeStockFields(entry, stockFields);

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

    private String resolveDosageType(JSONObject srcEntry) {
        Object dosageType = srcEntry.get("DosageType");
        if (dosageType != null && StringUtils.isNotBlank(String.valueOf(dosageType))) {
            String code = String.valueOf(dosageType).trim();
            if (KingdeeSubcontractBomDosageTypeEnum.getByCode(code) != null) {
                return code;
            }
            log.warn("委外用料清单用量类型无效: {}, 使用默认值: {}", code, KingdeeSubcontractBomDosageTypeEnum.VARIABLE.getCode());
        }
        return KingdeeSubcontractBomDosageTypeEnum.VARIABLE.getCode();
    }

    private String resolveIssueType(JSONObject srcEntry) {
        Object issueType = srcEntry.get("IssueType");
        if (issueType != null && StringUtils.isNotBlank(String.valueOf(issueType))) {
            String code = String.valueOf(issueType).trim();
            if (KingdeeSubcontractBomIssueTypeEnum.getByCode(code) != null) {
                return code;
            }
            log.warn("委外用料清单发料方式无效: {}, 使用默认值: {}", code, KingdeeSubcontractBomIssueTypeEnum.DIRECT_BACKFLUSH.getCode());
        }
        return KingdeeSubcontractBomIssueTypeEnum.DIRECT_BACKFLUSH.getCode();
    }

    private String resolveBackFlushType(JSONObject srcEntry) {
        Object backFlushType = srcEntry.get("BackFlushType");
        if (backFlushType != null && StringUtils.isNotBlank(String.valueOf(backFlushType))) {
            String code = String.valueOf(backFlushType).trim();
            if (KingdeeSubcontractBomBackFlushTypeEnum.getByCode(code) != null) {
                return code;
            }
            log.warn("委外用料清单倒冲时机无效: {}, 使用默认值: {}", code, KingdeeSubcontractBomBackFlushTypeEnum.INSTOCK_BACKFLUSH.getCode());
        }
        return KingdeeSubcontractBomBackFlushTypeEnum.INSTOCK_BACKFLUSH.getCode();
    }

    private boolean resolveConsiderMinIssueQty(JSONObject srcEntry) {
        Object isMinIssueQty = srcEntry.get("ISMinIssueQty");
        if (isMinIssueQty instanceof Boolean) {
            return (Boolean) isMinIssueQty;
        }
        if (isMinIssueQty != null && StringUtils.isNotBlank(String.valueOf(isMinIssueQty))) {
            Boolean parsed = parseKingdeeBoolean(String.valueOf(isMinIssueQty));
            if (parsed != null) {
                return parsed;
            }
            // 无法识别的字符串落入下方 BaseMinIssueQty 兜底，避免被 Boolean.parseBoolean 静默判为 false
        }
        Object baseMinIssueQty = srcEntry.get("BaseMinIssueQty");
        if (baseMinIssueQty == null || StringUtils.isBlank(String.valueOf(baseMinIssueQty))) {
            return Boolean.FALSE;
        }
        BigDecimal minIssueQty = ConvertUtil.toBigDecimal(baseMinIssueQty, BigDecimal.ZERO);
        return minIssueQty.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 解析金蝶下发的非标准布尔字符串：
     * - "true" / "1" / "y" / "yes" / "t" → {@code Boolean.TRUE}
     * - "false" / "0" / "n" / "no" / "f" → {@code Boolean.FALSE}
     * - 其它无法识别的值 → {@code null}（让调用方走兜底）
     */
    private Boolean parseKingdeeBoolean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim().toLowerCase();
        if (trimmed.isEmpty()) {
            return null;
        }
        switch (trimmed) {
            case "true":
            case "1":
            case "y":
            case "yes":
            case "t":
                return Boolean.TRUE;
            case "false":
            case "0":
            case "n":
            case "no":
            case "f":
                return Boolean.FALSE;
            default:
                return null;
        }
    }

    private int resolveOperId(JSONObject srcEntry) {
        return ConvertUtil.toInt(srcEntry.get("OperID"), 0);
    }

    private int resolveNoPickedQty(JSONObject srcEntry, int defaultValue) {
        Object noPickedQty = srcEntry.get("NoPickedQty");
        if (noPickedQty == null) {
            return defaultValue;
        }
        if (noPickedQty instanceof Number) {
            return ((Number) noPickedQty).intValue();
        }
        String noPickedQtyStr = String.valueOf(noPickedQty).trim();
        if (StringUtils.isBlank(noPickedQtyStr)) {
            return defaultValue;
        }
        return ConvertUtil.toInt(noPickedQtyStr, defaultValue);
    }

    private int resolveReplaceGroup(JSONObject srcEntry, int entryIndex) {
        return ConvertUtil.toInt(srcEntry.get("ReplaceGroup"), entryIndex + 1);
    }

    /**
     * 解析金蝶 Qty 字段为整数（取小数点前整数部分）；无法解析时返回 null，避免 parseInt 导致 MQ 消费失败。
     */
    private Integer parseKingdeeQtyInt(Object qty) {
        if (qty == null) {
            return null;
        }
        if (qty instanceof Number) {
            return ((Number) qty).intValue();
        }
        String qtyStr = String.valueOf(qty).trim();
        if (StringUtils.isBlank(qtyStr)) {
            return null;
        }
        int dotIndex = qtyStr.indexOf('.');
        if (dotIndex >= 0) {
            qtyStr = qtyStr.substring(0, dotIndex);
        }
        return ConvertUtil.toInt(qtyStr, null);
    }

    /**
     * 从 SCM 组装的 list 中按 detailId 提取仓库/仓位（FStockId、FStockLocId），避免与 FEntity 下标对齐。
     */
    private Map<String, JSONObject> buildDetailStockFieldMap(Map<String, Object> map) {
        if (CollectionUtils.isEmpty(map)) {
            return Collections.emptyMap();
        }
        Object listObj = map.get("list");
        if (listObj == null) {
            return Collections.emptyMap();
        }
        JSONArray list = JSONUtil.parseArray(JSONUtil.toJsonStr(listObj));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        Map<String, JSONObject> result = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            JSONObject listItem = list.getJSONObject(i);
            String detailId = listItem.getStr("detailId");
            if (StringUtils.isBlank(detailId)) {
                continue;
            }
            JSONObject stockFields = buildStockFieldsFromListItem(listItem);
            if (!stockFields.isEmpty()) {
                result.put(detailId, stockFields);
            }
        }
        return result;
    }

    private JSONObject buildStockFieldsFromListItem(JSONObject listItem) {
        String warehouseCode = listItem.getStr("warehouseCode");
        String warehouseLocation = listItem.getStr("warehouseLocation");
        if (StringUtils.isBlank(warehouseCode) && StringUtils.isBlank(warehouseLocation)) {
            return new JSONObject();
        }
        JSONObject stockFields = new JSONObject(new LinkedHashMap<>());
        if (StringUtils.isNotBlank(warehouseCode)) {
            KingdeeUtils.makeFieldJson(stockFields, "FStockId.FNumber", ".", warehouseCode);
        }
        if (StringUtils.isNotBlank(warehouseLocation)) {
            KingdeeUtils.makeFieldJson(stockFields, "FStockLocId.FSTOCKLOCID__FF100014.FNumber", ".", warehouseLocation);
        }
        return stockFields;
    }

    private JSONObject resolveDetailStockFields(Map<String, JSONObject> detailStockFieldMap,
            String childDetailId, String parentDetailId) {
        JSONObject stockFields = detailStockFieldMap.get(childDetailId);
        if (stockFields != null && !stockFields.isEmpty()) {
            return stockFields;
        }
        if (StringUtils.isNotBlank(parentDetailId)) {
            return detailStockFieldMap.get(parentDetailId);
        }
        return null;
    }

    private JSONObject resolveScmStockFields(SubcontractOrderDetailEntity matchedDetail,
            Map<String, JSONObject> detailStockFieldMap) {
        if (matchedDetail == null || CollectionUtils.isEmpty(detailStockFieldMap)) {
            return new JSONObject();
        }
        String parentDetailId = StringUtils.isBlank(matchedDetail.getParentId()) ? null : matchedDetail.getParentId();
        JSONObject stockFields = resolveDetailStockFields(detailStockFieldMap, matchedDetail.getId(), parentDetailId);
        return stockFields == null ? new JSONObject() : stockFields;
    }

    /**
     * 构建委外用料清单变更单转换上下文。
     * <p>
     * 父行匹配分两阶段：
     * <ol>
     *   <li>优先按金蝶 BOM 头 {@code SubReqEntryId}/{@code SubReqEntrySeq} 定位当前用料清单所属父行；</li>
     *   <li>若分录行号无法命中（如 SCM 父行尚未回写 {@code kingdeeDetailId}），回退至 SKU + 供应商 + 返修数量匹配。</li>
     * </ol>
     * 业务上允许不同父行下存在相同子料 SKU，因此不能仅按 SKU 聚合全部子行。
     */
    private RepairBomConvertContext buildRepairBomConvertContext(JSONObject view,
            List<SubcontractOrderDetailEntity> subcontractOrderDetails, Map<String, Object> map) {
        Map<String, JSONObject> detailStockFieldMap = buildDetailStockFieldMap(map);
        String bomBillNo = extractBillNoFromMap(map);
        List<RepairBomConvertContext.ParentChildGroup> matchedGroups = new ArrayList<>();

        List<SubcontractOrderDetailEntity> parentList = SubcontractOrderKingdeeLineSeqUtils.sortParentDetails(
                subcontractOrderDetails.stream()
                        .filter(item -> item != null && StringUtils.isBlank(item.getParentId()))
                        .collect(Collectors.toList()));
        List<SubcontractOrderDetailEntity> childList = subcontractOrderDetails.stream()
                .filter(item -> item != null && StringUtils.isNotBlank(item.getParentId()))
                .collect(Collectors.toList());
        Map<String, Integer> parentEntrySeqMap = buildParentEntrySeqMap(map, parentList);
        Map<String, Integer> childLineSeqMap = buildChildLineSeqMap(map, subcontractOrderDetails);

        if (CollectionUtils.isEmpty(parentList)) {
            return new RepairBomConvertContext(detailStockFieldMap, matchedGroups, parentEntrySeqMap, bomBillNo,
                    childLineSeqMap, childList);
        }

        List<String> supplierIds = parentList.stream()
                .map(SubcontractOrderDetailEntity::getSupplierId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<String> skuIds = parentList.stream()
                .map(SubcontractOrderDetailEntity::getSkuId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        List<SupplierEntity> supplierList = CollectionUtils.isEmpty(supplierIds)
                ? Collections.emptyList()
                : scmTaskFeign.getSupplierByIdList(supplierIds);
        Map<String, SupplierEntity> supplierMap = CollectionUtils.isEmpty(supplierList)
                ? Collections.emptyMap()
                : supplierList.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(SupplierEntity::getId, e -> e, (oldValue, newValue) -> oldValue));

        List<ProductDetailEntity> productList = CollectionUtils.isEmpty(skuIds)
                ? Collections.emptyList()
                : plmTaskFeign.getByIdList(skuIds);
        Map<String, ProductDetailEntity> productMap = CollectionUtils.isEmpty(productList)
                ? Collections.emptyMap()
                : productList.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(ProductDetailEntity::getId, e -> e, (oldValue, newValue) -> oldValue));

        JSONObject materialId = view.getJSONObject("MaterialID");
        JSONObject supplierId = view.getJSONObject("SupplierId");
        if (materialId == null || supplierId == null) {
            return new RepairBomConvertContext(detailStockFieldMap, matchedGroups, parentEntrySeqMap, bomBillNo,
                    childLineSeqMap, childList);
        }
        String viewSkuNo = materialId.get("Number") == null ? null : materialId.get("Number").toString();
        JSONArray valueArray = supplierId.getJSONArray("Name");
        if (valueArray == null || valueArray.isEmpty()) {
            return new RepairBomConvertContext(detailStockFieldMap, matchedGroups, parentEntrySeqMap, bomBillNo,
                    childLineSeqMap, childList);
        }
        JSONObject firstElement = valueArray.getJSONObject(0);
        String supplierName = firstElement.get("Value") == null ? null : firstElement.get("Value").toString();
        Integer kingdeeQty = parseKingdeeQtyInt(view.get("Qty"));

        List<RepairBomConvertContext.ParentChildGroup> skuMatchedGroups = new ArrayList<>();
        for (SubcontractOrderDetailEntity parentDetail : parentList) {
            SupplierEntity supplier = supplierMap.get(parentDetail.getSupplierId());
            ProductDetailEntity productDetail = productMap.get(parentDetail.getSkuId());
            if (Objects.isNull(supplier) || Objects.isNull(productDetail)) {
                log.warn("委外用料清单转换时未命中供应商或产品，parentDetailId={}, supplierId={}, skuId={}",
                        parentDetail.getId(), parentDetail.getSupplierId(), parentDetail.getSkuId());
                if (Objects.isNull(supplier)) {
                    throw new ServiceException(ApiError.SUPPLIER_NOT_FOUND);
                }
                throw new ServiceException(ApiError.PRODUCT_SKU_NOT_FOUND, parentDetail.getSkuNo());
            }
            if (!Objects.equals(viewSkuNo, productDetail.getSkuNo())
                    || !Objects.equals(supplierName, supplier.getName())
                    || !Objects.equals(kingdeeQty, parentDetail.getRepairQty())) {
                continue;
            }
            List<SubcontractOrderDetailEntity> filterChildList = SubcontractOrderKingdeeLineSeqUtils.sortChildDetails(
                    childList.stream()
                            .filter(item -> Objects.equals(item.getParentId(), parentDetail.getId()))
                            .collect(Collectors.toList()));
            if (CollectionUtils.isEmpty(filterChildList)) {
                continue;
            }
            RepairBomConvertContext.ParentChildGroup group =
                    new RepairBomConvertContext.ParentChildGroup(parentDetail, filterChildList);
            // skuMatchedGroups：宽松匹配结果，供分录行号未命中时回退
            skuMatchedGroups.add(group);
            // matchedGroups：严格匹配，仅保留与当前金蝶 BOM 分录对应的父行
            if (matchesViewParentLine(parentDetail, view, parentEntrySeqMap)) {
                matchedGroups.add(group);
            }
        }
        // 分录行号与 SCM 未对齐时（如 kingdeeDetailId 缺失），仅唯一父行候选时允许回退
        if (CollectionUtils.isEmpty(matchedGroups) && !CollectionUtils.isEmpty(skuMatchedGroups)) {
            if (skuMatchedGroups.size() == 1) {
                log.warn("委外用料清单变更单按分录行号未命中父行，回退至SKU/供应商/数量匹配（唯一父行），{}, viewSubReqEntryId={}, viewSubReqEntrySeq={}",
                        formatBomMatchLogContext(view, null, map, null, bomBillNo),
                        extractJsonString(view, "SubReqEntryId"), parseKingdeeQtyInt(view.get("SubReqEntrySeq")));
                matchedGroups.addAll(skuMatchedGroups);
            } else {
                log.error("委外用料清单变更单按分录行号未命中父行且存在多个候选父行，{}, viewSubReqEntryId={}, viewSubReqEntrySeq={}, fallbackGroupCount={}",
                        formatBomMatchLogContext(view, null, map, null, bomBillNo),
                        extractJsonString(view, "SubReqEntryId"), parseKingdeeQtyInt(view.get("SubReqEntrySeq")),
                        skuMatchedGroups.size());
                throw new ServiceException(ApiError.DMP_KINGDEE_SUBCONTRACT_BOM_PARENT_MATCH_AMBIGUOUS,
                        StringUtils.defaultIfBlank(bomBillNo, extractJsonString(view, "BillNo")),
                        skuMatchedGroups.size());
            }
        }
        return new RepairBomConvertContext(detailStockFieldMap, matchedGroups, parentEntrySeqMap, bomBillNo,
                childLineSeqMap, childList);
    }

    private String extractBillNoFromMap(Map<String, Object> map) {
        if (map == null || map.get("FBillNo") == null) {
            return null;
        }
        String billNo = map.get("FBillNo").toString().trim();
        return StringUtils.isBlank(billNo) ? null : billNo;
    }

    /**
     * 构建 SCM 父行 detailId 与金蝶委外订单分录行号（SubReqEntrySeq）的映射。
     * <p>
     * 优先读取 SCM MQ list 中的 {@code parentLineSeq}；旧消息无该字段或部分缺失时，全量回退本地排序编行号。
     */
    private Map<String, Integer> buildParentEntrySeqMap(Map<String, Object> map,
            List<SubcontractOrderDetailEntity> parentList) {
        List<SubcontractOrderDetailEntity> sortedParents = SubcontractOrderKingdeeLineSeqUtils.sortParentDetails(parentList);
        Map<String, Integer> fromMqList = extractParentEntrySeqMapFromMqList(map, sortedParents);
        Set<String> parentIds = sortedParents.stream()
                .map(SubcontractOrderDetailEntity::getId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(sortedParents)) {
            return Collections.emptyMap();
        }
        Map<String, Integer> fallback = SubcontractOrderKingdeeLineSeqUtils.buildParentLineSeqMap(sortedParents);
        return resolveLineSeqMap(fromMqList, parentIds, fallback, "parentLineSeq");
    }

    /**
     * 构建 SCM 子行 detailId 与同父行内 childLineSeq 的映射；MQ 完整时采用 MQ，缺失或部分缺失时全量回退本地排序。
     */
    private Map<String, Integer> buildChildLineSeqMap(Map<String, Object> map,
            List<SubcontractOrderDetailEntity> subcontractOrderDetails) {
        List<SubcontractOrderDetailEntity> childList = subcontractOrderDetails == null
                ? Collections.emptyList()
                : subcontractOrderDetails.stream()
                        .filter(item -> item != null && StringUtils.isNotBlank(item.getParentId()))
                        .collect(Collectors.toList());
        Map<String, Integer> fromMqList = extractChildLineSeqMapFromMqList(map, childList);
        Set<String> childIds = childList.stream()
                .map(SubcontractOrderDetailEntity::getId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(childList)) {
            return Collections.emptyMap();
        }
        Map<String, Integer> fallback = SubcontractOrderKingdeeLineSeqUtils.buildChildLineSeqMap(subcontractOrderDetails);
        return resolveLineSeqMap(fromMqList, childIds, fallback, "childLineSeq");
    }

    /**
     * MQ 行号完整时采用 MQ；缺失或部分缺失时全量回退本地排序，避免新旧 MQ 行号混用。
     */
    private Map<String, Integer> resolveLineSeqMap(Map<String, Integer> fromMqList, Set<String> expectedIds,
            Map<String, Integer> fallback, String lineSeqFieldName) {
        if (CollectionUtils.isEmpty(expectedIds)) {
            return Collections.emptyMap();
        }
        if (!fromMqList.isEmpty() && fromMqList.size() == expectedIds.size()) {
            return fromMqList;
        }
        if (fromMqList.isEmpty()) {
            log.warn("MQ list未携带{}，回退至与委外订单推送一致的排序编行号", lineSeqFieldName);
        } else {
            log.warn("MQ list {}不完整，回退至与委外订单推送一致的排序编行号, mqSize={}, expectedSize={}",
                    lineSeqFieldName, fromMqList.size(), expectedIds.size());
        }
        return fallback == null ? Collections.emptyMap() : fallback;
    }

    /**
     * 记录子行匹配与仓库回填结果；MQ 已携带仓库却未匹配 SCM 明细时打 error 便于运维排查（不中断同步）。
     */
    private void logChangeChildDetailMatchResult(JSONObject view, JSONObject srcEntry, String skuNumber,
            SubcontractOrderDetailEntity matchedDetail, JSONObject stockFields,
            RepairBomConvertContext convertContext) {
        if (matchedDetail != null) {
            if ((stockFields == null || stockFields.isEmpty())
                    && hasMqStockFieldForDetail(matchedDetail.getId(), convertContext)) {
                log.warn("委外用料清单变更单子行已匹配SCM明细但仓库字段为空，{}, skuNo={}, detailId={}",
                        formatBomMatchLogContext(view, srcEntry, null, convertContext, null), skuNumber,
                        matchedDetail.getId());
            }
            return;
        }
        if (StringUtils.isBlank(skuNumber)) {
            return;
        }
        if (hasMqStockFieldForSku(skuNumber, convertContext)) {
            log.error("委外用料清单变更单子行未能匹配SCM明细但MQ已携带仓库字段，变更行将无仓库回填，{}, skuNo={}, ppBomEntryId={}",
                    formatBomMatchLogContext(view, srcEntry, null, convertContext, null), skuNumber,
                    extractJsonString(srcEntry, "Id"));
            return;
        }
        log.warn("委外用料清单变更单子行未匹配SCM明细且无MQ仓库字段，跳过仓库回填，{}, skuNo={}",
                formatBomMatchLogContext(view, srcEntry, null, convertContext, null), skuNumber);
    }

    private boolean hasMqStockFieldForSku(String skuNumber, RepairBomConvertContext convertContext) {
        if (StringUtils.isBlank(skuNumber) || convertContext == null
                || CollectionUtils.isEmpty(convertContext.getAllChildDetails())
                || CollectionUtils.isEmpty(convertContext.getDetailStockFieldMap())) {
            return false;
        }
        Map<String, JSONObject> stockMap = convertContext.getDetailStockFieldMap();
        return convertContext.getAllChildDetails().stream()
                .filter(item -> Objects.equals(skuNumber, item.getSkuNo()))
                .anyMatch(item -> hasNonEmptyStockFields(stockMap.get(item.getId())));
    }

    private boolean hasMqStockFieldForDetail(String detailId, RepairBomConvertContext convertContext) {
        if (StringUtils.isBlank(detailId) || convertContext == null
                || CollectionUtils.isEmpty(convertContext.getDetailStockFieldMap())) {
            return false;
        }
        return hasNonEmptyStockFields(convertContext.getDetailStockFieldMap().get(detailId));
    }

    private boolean hasNonEmptyStockFields(JSONObject stockFields) {
        return stockFields != null && !stockFields.isEmpty();
    }

    private Map<String, Integer> extractChildLineSeqMapFromMqList(Map<String, Object> map,
            List<SubcontractOrderDetailEntity> childList) {
        if (CollectionUtils.isEmpty(map) || CollectionUtils.isEmpty(childList)) {
            return Collections.emptyMap();
        }
        Object listObj = map.get("list");
        if (listObj == null) {
            return Collections.emptyMap();
        }
        JSONArray list = JSONUtil.parseArray(JSONUtil.toJsonStr(listObj));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        Set<String> childIds = childList.stream()
                .map(SubcontractOrderDetailEntity::getId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, Integer> childLineSeqMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            JSONObject listItem = list.getJSONObject(i);
            if (listItem == null) {
                continue;
            }
            String detailId = listItem.getStr("detailId");
            if (StringUtils.isBlank(detailId) || !childIds.contains(detailId)) {
                continue;
            }
            Integer childLineSeq = parseKingdeeQtyInt(listItem.get("childLineSeq"));
            if (childLineSeq != null) {
                childLineSeqMap.put(detailId, childLineSeq);
            }
        }
        return childLineSeqMap;
    }

    private Map<String, Integer> extractParentEntrySeqMapFromMqList(Map<String, Object> map,
            List<SubcontractOrderDetailEntity> parentList) {
        if (CollectionUtils.isEmpty(map) || CollectionUtils.isEmpty(parentList)) {
            return Collections.emptyMap();
        }
        Object listObj = map.get("list");
        if (listObj == null) {
            return Collections.emptyMap();
        }
        JSONArray list = JSONUtil.parseArray(JSONUtil.toJsonStr(listObj));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        Set<String> parentIds = parentList.stream()
                .map(SubcontractOrderDetailEntity::getId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, Integer> parentEntrySeqMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            JSONObject listItem = list.getJSONObject(i);
            if (listItem == null) {
                continue;
            }
            String detailId = listItem.getStr("detailId");
            if (StringUtils.isBlank(detailId) || !parentIds.contains(detailId)) {
                continue;
            }
            Integer parentLineSeq = parseKingdeeQtyInt(listItem.get("parentLineSeq"));
            if (parentLineSeq != null) {
                parentEntrySeqMap.put(detailId, parentLineSeq);
            }
        }
        return parentEntrySeqMap;
    }

    /**
     * 判断 SCM 父行是否为当前金蝶 BOM 所属委外订单分录。
     * <p>
     * 匹配优先级：{@code SubReqEntryId}（对应 SCM {@code kingdeeDetailId}） &gt; {@code SubReqEntrySeq}。
     * 金蝶 BOM 头未携带分录标识时返回 {@code true}，保持与历史逻辑兼容。
     */
    private boolean matchesViewParentLine(SubcontractOrderDetailEntity parentDetail, JSONObject view,
            Map<String, Integer> parentEntrySeqMap) {
        String viewSubReqEntryId = extractJsonString(view, "SubReqEntryId");
        Integer viewSubReqEntrySeq = parseKingdeeQtyInt(view.get("SubReqEntrySeq"));
        // 无分录标识：无法进一步收窄，沿用 SKU/供应商/数量维度的全部命中父行
        if (StringUtils.isBlank(viewSubReqEntryId) && viewSubReqEntrySeq == null) {
            return true;
        }
        // 优先用金蝶分录内码与 SCM 已回写的 kingdeeDetailId 精确匹配
        if (StringUtils.isNotBlank(viewSubReqEntryId) && StringUtils.isNotBlank(parentDetail.getKingdeeDetailId())) {
            return Objects.equals(viewSubReqEntryId, parentDetail.getKingdeeDetailId());
        }
        // 兜底：按委外订单分录行号匹配（历史单无 kingdeeDetailId 时依赖 parentLineSeq 与金蝶 SubReqEntrySeq 对齐）
        if (viewSubReqEntrySeq != null && parentEntrySeqMap != null) {
            Integer parentSeq = parentEntrySeqMap.get(parentDetail.getId());
            boolean matched = Objects.equals(viewSubReqEntrySeq, parentSeq);
            if (!matched && StringUtils.isBlank(parentDetail.getKingdeeDetailId())) {
                log.warn("委外用料清单父行按SubReqEntrySeq未对齐且缺少kingdeeDetailId，parentDetailId={}, scmSeq={}, kingdeeSeq={}",
                        parentDetail.getId(), parentSeq, viewSubReqEntrySeq);
            }
            return matched;
        }
        return false;
    }

    /**
     * 将金蝶 BOM 子行映射到 SCM 委外订单子明细，用于回填仓库/仓位。
     * <p>
     * 消歧策略（依次降级）：
     * <ol>
     *   <li>按 {@link #resolveParentGroup} 限定父行范围；</li>
     *   <li>按 SKU 过滤，并排除 {@code usedChildDetailIds} 中已匹配的行；</li>
     *   <li>按金蝶应发数量（MustQty/StdQty）与 SCM 子行 qty 对齐；</li>
     *   <li>按金蝶子行序号（Seq / ReplaceGroup / EntrySeq）与 MQ childLineSeq 对齐；</li>
     *   <li>仍有多条候选时按 childLineSeq 与金蝶行号就近匹配，再取未消费首条（不中断同步）。</li>
     * </ol>
     */
    private SubcontractOrderDetailEntity matchChangeChildDetail(String skuNumber, JSONObject srcEntry, JSONObject view,
            RepairBomConvertContext convertContext, Set<String> usedChildDetailIds) {
        if (StringUtils.isBlank(skuNumber) || convertContext == null
                || CollectionUtils.isEmpty(convertContext.getChangeChildDetails())) {
            return null;
        }
        RepairBomConvertContext.ParentChildGroup parentGroup = resolveParentGroup(srcEntry, view, convertContext);
        List<SubcontractOrderDetailEntity> searchScope = parentGroup == null
                ? convertContext.getChangeChildDetails()
                : parentGroup.getChildDetails();
        List<SubcontractOrderDetailEntity> candidates = sortChildCandidatesByLineSeq(searchScope.stream()
                .filter(item -> Objects.equals(skuNumber, item.getSkuNo()))
                .filter(item -> usedChildDetailIds == null || !usedChildDetailIds.contains(item.getId()))
                .collect(Collectors.toList()), convertContext);
        if (CollectionUtils.isEmpty(candidates)) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        List<SubcontractOrderDetailEntity> qtyMatched = sortChildCandidatesByLineSeq(
                filterChildDetailByKingdeeQty(candidates, srcEntry), convertContext);
        if (qtyMatched.size() == 1) {
            return qtyMatched.get(0);
        }
        // 同父行重复 SKU 且数量也无法区分时，尝试按 childLineSeq 与金蝶 ReplaceGroup 对齐
        List<SubcontractOrderDetailEntity> pickFrom = sortChildCandidatesByLineSeq(
                qtyMatched.size() > 1 ? qtyMatched : candidates, convertContext);
        SubcontractOrderDetailEntity seqMatchedDetail = matchChildDetailByLineSeq(pickFrom, srcEntry, convertContext);
        if (seqMatchedDetail != null) {
            return seqMatchedDetail;
        }
        Integer kingdeeChildLineSeq = resolveKingdeeChildLineSeq(srcEntry);
        SubcontractOrderDetailEntity picked = pickChildByNearestLineSeq(pickFrom, kingdeeChildLineSeq, convertContext);
        log.warn("委外用料清单变更单子行SKU存在多条候选，按childLineSeq就近/顺序取未消费首条，{}, skuNo={}, kingdeeChildLineSeq={}, candidateDetailIds={}, pickedDetailId={}, pickedChildLineSeq={}",
                formatBomMatchLogContext(view, srcEntry, null, convertContext, null), skuNumber,
                kingdeeChildLineSeq,
                pickFrom.stream().map(SubcontractOrderDetailEntity::getId).collect(Collectors.joining(",")),
                picked.getId(), convertContext.getChildLineSeqMap().get(picked.getId()));
        return picked;
    }

    /**
     * 序号无法精确对齐时，按 childLineSeq 与金蝶行号差值就近选取，避免总是取排序首条。
     */
    private SubcontractOrderDetailEntity pickChildByNearestLineSeq(List<SubcontractOrderDetailEntity> pickFrom,
            Integer kingdeeChildLineSeq, RepairBomConvertContext convertContext) {
        if (CollectionUtils.isEmpty(pickFrom)) {
            return null;
        }
        if (pickFrom.size() == 1 || kingdeeChildLineSeq == null || convertContext == null) {
            return pickFrom.get(0);
        }
        Map<String, Integer> childLineSeqMap = convertContext.getChildLineSeqMap();
        return pickFrom.stream()
                .min(Comparator
                        .comparing((SubcontractOrderDetailEntity item) -> {
                            Integer scmSeq = childLineSeqMap.get(item.getId());
                            if (scmSeq == null) {
                                return Integer.MAX_VALUE;
                            }
                            return Math.abs(scmSeq - kingdeeChildLineSeq);
                        })
                        .thenComparing(item -> childLineSeqMap.get(item.getId()),
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(SubcontractOrderDetailEntity::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(pickFrom.get(0));
    }

    /**
     * 同 SKU 多条候选时按 childLineSeq 稳定排序，配合 {@code usedChildDetailIds} 实现「第一条对第一条、第二条对第二条」。
     */
    private List<SubcontractOrderDetailEntity> sortChildCandidatesByLineSeq(
            List<SubcontractOrderDetailEntity> candidates, RepairBomConvertContext convertContext) {
        if (CollectionUtils.isEmpty(candidates)) {
            return candidates;
        }
        Map<String, Integer> childLineSeqMap = convertContext == null
                ? Collections.emptyMap()
                : convertContext.getChildLineSeqMap();
        return candidates.stream()
                .sorted(Comparator
                        .comparing((SubcontractOrderDetailEntity item) -> childLineSeqMap.get(item.getId()),
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(SubcontractOrderDetailEntity::getCreateTime,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(SubcontractOrderDetailEntity::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    /**
     * 按金蝶项次（ReplaceGroup / SubPPBOMEntrySeq）与 SCM childLineSeq 精确匹配子行。
     */
    private SubcontractOrderDetailEntity matchChildDetailByLineSeq(List<SubcontractOrderDetailEntity> candidates,
            JSONObject srcEntry, RepairBomConvertContext convertContext) {
        if (CollectionUtils.isEmpty(candidates) || srcEntry == null || convertContext == null) {
            return null;
        }
        Integer kingdeeChildLineSeq = resolveKingdeeChildLineSeq(srcEntry);
        if (kingdeeChildLineSeq == null) {
            return null;
        }
        Map<String, Integer> childLineSeqMap = convertContext.getChildLineSeqMap();
        if (CollectionUtils.isEmpty(childLineSeqMap)) {
            return null;
        }
        List<SubcontractOrderDetailEntity> seqMatched = candidates.stream()
                .filter(item -> Objects.equals(kingdeeChildLineSeq, childLineSeqMap.get(item.getId())))
                .collect(Collectors.toList());
        if (seqMatched.size() == 1) {
            return seqMatched.get(0);
        }
        if (seqMatched.size() > 1) {
            log.warn("委外用料清单变更单子行按序号匹配仍有多条候选, kingdeeChildLineSeq={}, candidateDetailIds={}",
                    kingdeeChildLineSeq,
                    seqMatched.stream().map(SubcontractOrderDetailEntity::getId).collect(Collectors.joining(",")));
        }
        return null;
    }

    /**
     * 解析金蝶 BOM 子行序号，用于与 SCM childLineSeq 对齐。
     * <p>
     * 优先级：Seq &gt; ReplaceGroup &gt; EntrySeq；SubPPBOMEntrySeq 在变更单中常写死为 1，仅在大于 1 时采用。
     */
    private Integer resolveKingdeeChildLineSeq(JSONObject srcEntry) {
        if (srcEntry == null) {
            return null;
        }
        Integer seq = parseKingdeeQtyInt(srcEntry.get("Seq"));
        if (seq != null) {
            return seq;
        }
        Integer replaceGroup = parseKingdeeQtyInt(srcEntry.get("ReplaceGroup"));
        if (replaceGroup != null && replaceGroup > 0) {
            return replaceGroup;
        }
        Integer entrySeq = parseKingdeeQtyInt(srcEntry.get("EntrySeq"));
        if (entrySeq != null) {
            return entrySeq;
        }
        Integer subPpBomEntrySeq = parseKingdeeQtyInt(srcEntry.get("SubPPBOMEntrySeq"));
        if (subPpBomEntrySeq != null && subPpBomEntrySeq > 1) {
            return subPpBomEntrySeq;
        }
        return null;
    }

    /**
     * 根据金蝶 BOM 分录行上的委外订单分录标识，定位对应的 SCM 父行分组。
     * <p>
     * 仅当 {@code matchedGroups} 存在多个父行时才需要消歧；单组时直接返回。
     * 分录标识优先取 {@code srcEntry}，缺失时回退至 BOM 头 {@code view}。
     */
    private RepairBomConvertContext.ParentChildGroup resolveParentGroup(JSONObject srcEntry, JSONObject view,
            RepairBomConvertContext convertContext) {
        List<RepairBomConvertContext.ParentChildGroup> groups = convertContext.getMatchedGroups();
        if (CollectionUtils.isEmpty(groups)) {
            return null;
        }
        if (groups.size() == 1) {
            return groups.get(0);
        }
        String subReqEntryId = extractJsonString(srcEntry, "SubReqEntryId");
        if (StringUtils.isBlank(subReqEntryId)) {
            subReqEntryId = extractJsonString(view, "SubReqEntryId");
        }
        if (StringUtils.isNotBlank(subReqEntryId)) {
            for (RepairBomConvertContext.ParentChildGroup group : groups) {
                SubcontractOrderDetailEntity parentDetail = group.getParentDetail();
                if (parentDetail != null && Objects.equals(subReqEntryId, parentDetail.getKingdeeDetailId())) {
                    return group;
                }
            }
        }
        Integer subReqEntrySeq = parseKingdeeQtyInt(srcEntry == null ? null : srcEntry.get("SubReqEntrySeq"));
        if (subReqEntrySeq == null && view != null) {
            subReqEntrySeq = parseKingdeeQtyInt(view.get("SubReqEntrySeq"));
        }
        if (subReqEntrySeq != null) {
            Map<String, Integer> parentEntrySeqMap = convertContext.getParentEntrySeqMap();
            for (RepairBomConvertContext.ParentChildGroup group : groups) {
                SubcontractOrderDetailEntity parentDetail = group.getParentDetail();
                if (parentDetail == null) {
                    continue;
                }
                Integer parentSeq = parentEntrySeqMap.get(parentDetail.getId());
                if (Objects.equals(subReqEntrySeq, parentSeq)) {
                    return group;
                }
            }
        }
        return null;
    }

    /**
     * 按金蝶应发数量过滤 SCM 子行候选，用于同父行下重复 SKU 的二次消歧。
     * <p>
     * 优先取 {@code MustQty}，缺失时取 {@code StdQty}；若无数量字段或过滤后为空，返回原候选列表。
     */
    private List<SubcontractOrderDetailEntity> filterChildDetailByKingdeeQty(
            List<SubcontractOrderDetailEntity> candidates, JSONObject srcEntry) {
        if (CollectionUtils.isEmpty(candidates) || srcEntry == null) {
            return candidates;
        }
        Integer kingdeeQty = parseKingdeeQtyInt(srcEntry.get("MustQty"));
        if (kingdeeQty == null) {
            kingdeeQty = parseKingdeeQtyInt(srcEntry.get("StdQty"));
        }
        if (kingdeeQty == null) {
            return candidates;
        }
        Integer finalKingdeeQty = kingdeeQty;
        List<SubcontractOrderDetailEntity> qtyMatched = candidates.stream()
                .filter(item -> item.getQty() != null && Objects.equals(finalKingdeeQty, item.getQty()))
                .collect(Collectors.toList());
        return CollectionUtils.isEmpty(qtyMatched) ? candidates : qtyMatched;
    }

    private String extractJsonString(JSONObject json, String key) {
        if (json == null || json.get(key) == null) {
            return null;
        }
        String value = json.get(key).toString().trim();
        return StringUtils.isBlank(value) ? null : value;
    }

    /**
     * 组装 BOM 匹配相关 warn 日志上下文，便于按单号/分录定位歧义匹配。
     */
    private String formatBomMatchLogContext(JSONObject view, JSONObject srcEntry, Map<String, Object> map,
            RepairBomConvertContext convertContext, String bomBillNo) {
        if (StringUtils.isBlank(bomBillNo) && convertContext != null) {
            bomBillNo = convertContext.getBomBillNo();
        }
        if (StringUtils.isBlank(bomBillNo)) {
            bomBillNo = extractBillNoFromMap(map);
        }
        if (StringUtils.isBlank(bomBillNo) && view != null) {
            bomBillNo = extractJsonString(view, "BillNo");
        }
        String bomId = view == null ? null : extractJsonString(view, "Id");
        String subReqEntryId = extractJsonString(srcEntry, "SubReqEntryId");
        if (StringUtils.isBlank(subReqEntryId) && view != null) {
            subReqEntryId = extractJsonString(view, "SubReqEntryId");
        }
        Integer subReqEntrySeq = parseKingdeeQtyInt(srcEntry == null ? null : srcEntry.get("SubReqEntrySeq"));
        if (subReqEntrySeq == null && view != null) {
            subReqEntrySeq = parseKingdeeQtyInt(view.get("SubReqEntrySeq"));
        }
        String ppBomEntryId = extractJsonString(srcEntry, "Id");
        if (StringUtils.isBlank(ppBomEntryId)) {
            ppBomEntryId = extractJsonString(srcEntry, "BOMEntryID");
        }
        return String.format("bomBillNo=%s, bomId=%s, subReqEntryId=%s, subReqEntrySeq=%s, ppBomEntryId=%s",
                bomBillNo, bomId, subReqEntryId, subReqEntrySeq, ppBomEntryId);
    }

    private JSONObject resolveSkuJson(KingdeeApiUtils skuApiUtils, String platformId, JSONObject srcEntry,
            Map<String, JSONObject> skuJsonCache) {
        Object kingdeeSkuId = srcEntry.get("MaterialID_Id");
        if (kingdeeSkuId == null) {
            return null;
        }
        String cacheKey = kingdeeSkuId.toString();
        JSONObject cachedSkuJson = skuJsonCache.get(cacheKey);
        if (cachedSkuJson != null) {
            return cachedSkuJson;
        }
        Map<String, Object> skuViewMap = new HashMap<>();
        skuViewMap.put("syncKingdeeId", cacheKey);
        JSONObject skuView = kingdeeCommonService.view(skuApiUtils, platformId, skuViewMap);
        if (skuView == null || skuView.get("Number") == null) {
            return null;
        }
        JSONObject skuJson = new JSONObject();
        skuJson.put("FNumber", skuView.get("Number"));
        skuJsonCache.put(cacheKey, skuJson);
        return skuJson;
    }

    private String extractSkuNumber(JSONObject skuJson) {
        if (skuJson == null || skuJson.get("FNumber") == null) {
            return null;
        }
        return skuJson.get("FNumber").toString();
    }

    private void applySkuJson(Map<String, Object> entry, JSONObject skuJson) {
        if (skuJson == null || skuJson.isEmpty()) {
            return;
        }
        entry.put("FMaterialID", skuJson);
        entry.put("FMaterialID2", skuJson);
    }

    private void mergeStockFields(Map<String, Object> entry, JSONObject stockFields) {
        applyStockFields(entry, stockFields);
    }

    private void applyStockFields(Map<String, Object> entry, JSONObject stockFields) {
        if (stockFields == null || stockFields.isEmpty() || entry == null) {
            return;
        }
        // 金蝶要求 FStockId 必须在 FStockLocId 之前，先移除仓位再按序写入
        entry.remove("FStockLocId");
        entry.remove("FStockId");
        Object fStockId = stockFields.get("FStockId");
        if (fStockId != null) {
            entry.put("FStockId", fStockId);
        }
        Object fStockLocId = stockFields.get("FStockLocId");
        if (fStockLocId != null) {
            entry.put("FStockLocId", fStockLocId);
        }
    }

}
