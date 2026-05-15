package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.KingdeeParamDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApi;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeeSubcontractBOMConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.kingdee.bos.webapi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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

            //转换数据
            for (Map<String, Object> query : queryList) {
                Map<String, Object> bomChangeViewMap = new HashMap<>();
                bomChangeViewMap.put("syncKingdeeId",query.get("FId"));
                JSONObject view = kingdeeCommonService.view(bomApiUtils, platformEntity.getId(), bomChangeViewMap);
                log.warn("委外用料清单变更单查询报文：{}" , view.toString());
                //处理旧单
                JSONObject convertOldData = convertOldData(view,skuApiUtils,platformEntity.getId(),syncKingdeeId,query.get("FBillNo").toString());
                KingdeeParamDTO.SaveParamDTO saveOldParam = new KingdeeParamDTO.SaveParamDTO(convertOldData);
                saveOldParam.setIsVerifyBaseDataField(Boolean.FALSE);
                //新增
                RepoResult saveOld = bomChangeApiUtils.saveKingDee(saveOldParam);
                //提交
                kingdeeCommonService.submit(null,bomChangeApiUtils,saveOld.getId(),ApiModuleTypeEnum.SUBCONTRACT_BOM.getCode());
                //审核
                kingdeeCommonService.audit(null,bomChangeApiUtils,saveOld.getId(),ApiModuleTypeEnum.SUBCONTRACT_BOM.getCode());

                //新增新单
                JSONObject convertNewData = convertNewData(view,syncKingdeeId,query.get("FBillNo").toString());
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

    public JSONObject convertOldData(JSONObject view,KingdeeApiUtils skuApiUtils,String platformId,String syncKingdeeId,String bomBillNo){
        JSONArray ppBomEntries = view.getJSONArray("PPBomEntry");
        JSONArray FEntities = new JSONArray();
        JSONObject entries = new JSONObject();
        SubcontractOrderEntity subcontractOrder = scmTaskFeign.listSubcontractOrderByKingdeeId(syncKingdeeId);
        SysAccountingCompanyEntity sysAccountingCompany = sysUserFeign.getCompanyByKindgeeId(view.get("SubOrgId_Id").toString());
        if (Objects.isNull(sysAccountingCompany)) {
            throw new ServiceException(ApiError.COMMON_COMPANY_NOT_FOUND);
        }
        int counter = 0;
        //原数据行
        for (int i = 0; i < ppBomEntries.size(); i++) {
            if (Objects.nonNull(subcontractOrder)) {
                JSONObject srcEntry = ppBomEntries.getJSONObject(i);
                JSONObject changeBeforPpBom = createChangeBeforePpBomEntry(view,skuApiUtils, platformId,srcEntry, bomBillNo,subcontractOrder.getCode(),sysAccountingCompany,counter);
                JSONObject changeAfterPpBom = createChangeAfterPpBomEntry(view,skuApiUtils, platformId,srcEntry, bomBillNo,subcontractOrder.getCode(),sysAccountingCompany,counter);
                counter++;
                FEntities.put(changeBeforPpBom);
                FEntities.put(changeAfterPpBom);
            }
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

    public JSONObject convertNewData(JSONObject view,String syncKingdeeId,String bomBillNo){
        JSONArray FEntities = new JSONArray();
        JSONObject entries = new JSONObject();
        SubcontractOrderEntity subcontractOrder = scmTaskFeign.listSubcontractOrderByKingdeeId(syncKingdeeId);
        SysAccountingCompanyEntity sysAccountingCompany = sysUserFeign.getCompanyByKindgeeId(view.get("SubOrgId_Id").toString());
        if (Objects.isNull(sysAccountingCompany)) {
            throw new ServiceException(ApiError.COMMON_COMPANY_NOT_FOUND);
        }

        if (Objects.nonNull(subcontractOrder)){
            String mainId = subcontractOrder.getId();
            List<SubcontractOrderDetailEntity> subcontractOrderDetails = scmTaskFeign.listSubcontractDetailByMainIds(Collections.singletonList(mainId));
            
            List<SubcontractOrderDetailEntity> parentList = subcontractOrderDetails.stream()
                    .filter(item -> StringUtils.isBlank(item.getParentId()))
                    .collect(Collectors.toList());

            List<SubcontractOrderDetailEntity> childList = subcontractOrderDetails.stream()
                    .filter(item -> StringUtils.isNotBlank(item.getParentId()))
                    .collect(Collectors.toList());

            if (!parentList.isEmpty()) {
                for (SubcontractOrderDetailEntity parentDetail : parentList) {
                    SupplierEntity supplier = scmTaskFeign.getSupplierById(parentDetail.getSupplierId());
                    List<ProductDetailEntity> productDetails = plmTaskFeign.getByIdList(Collections.singletonList(parentDetail.getSkuId()));
                    //委外用料清单单头供应商,sku,数量相等,测试返回的id不一样，所以用名称
                    if (Objects.nonNull(supplier) && !productDetails.isEmpty()) {
                        JSONObject materialId = view.getJSONObject("MaterialID");
                        String skuNo = materialId.get("Number").toString();

                        JSONObject supplierId = view.getJSONObject("SupplierId");
                        JSONArray valueArray = supplierId.getJSONArray("Name");
                        JSONObject firstElement = valueArray.getJSONObject(0);
                        String supplierName = firstElement.get("Value").toString();

                        String intStr = view.get("Qty").toString().split("\\.")[0]; // 按小数点分割，取整数部分

                        if (Objects.equals(skuNo,productDetails.get(0).getSkuNo())
                                && Objects.equals(supplierName,supplier.getName())
                                && Integer.parseInt(intStr) == parentDetail.getRepairQty()) {
                            List<SubcontractOrderDetailEntity> filterChildList = childList.stream()
                                    .filter(item -> Objects.equals(item.getParentId(), parentDetail.getId()))
                                    .collect(Collectors.toList());
                            for (SubcontractOrderDetailEntity subcontractOrderDetail : filterChildList) {
                                entries = createNewPpBomEntry(FEntities, subcontractOrder, parentDetail, subcontractOrderDetail, sysAccountingCompany, bomBillNo);
                            }
                        }
                    }
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

    private JSONObject createChangeBeforePpBomEntry(JSONObject view,KingdeeApiUtils skuApiUtils,String platformId,JSONObject srcEntry, String bomBillNo,String subCode,SysAccountingCompanyEntity sysAccountingCompany,int counter) {
        JSONObject entry = new JSONObject();
        //物料编码
        JSONObject skuJson = new JSONObject();
        Object kingdeeSkuId = srcEntry.get("MaterialID_Id");
        if (Objects.nonNull(kingdeeSkuId)) {
            //ProductDetailEntity productDetail = plmTaskFeign.getSkuBySyncKingdeeId(kingdeeSkuId.toString());
            Map<String, Object> skuViewMap = new HashMap<>();
            skuViewMap.put("syncKingdeeId",kingdeeSkuId.toString());
            JSONObject skuView = kingdeeCommonService.view(skuApiUtils, platformId, skuViewMap);

            if (Objects.nonNull(skuView)) {
                skuJson.put("FNumber", skuView.get("Number"));
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
        supplyOrgJson.put("FNumber", sysAccountingCompany.getKingdeeCode());
        entry.put("FSupplyOrg", supplyOrgJson);
        //发料方式
        entry.put("FIssueType", "2");
        //变更前
        entry.put("FChangeType","2");
        //分子
        entry.put("FNumerator",srcEntry.get("Numerator"));
        //分母
        entry.put("FDenominator",srcEntry.get("Denominator"));
        //应发数量
        entry.put("FMustQty",srcEntry.get("MustQty"));
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
        entry.put("FBackFlushType", "3");
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

    private JSONObject createChangeAfterPpBomEntry(JSONObject view,KingdeeApiUtils skuApiUtils,String platformId,JSONObject srcEntry, String bomBillNo,String subCode,SysAccountingCompanyEntity sysAccountingCompany,int counter) {
        JSONObject entry = new JSONObject();
        //物料编码
        JSONObject skuJson = new JSONObject();
        Object kingdeeSkuId = srcEntry.get("MaterialID_Id");
        if (Objects.nonNull(kingdeeSkuId)) {
            Map<String, Object> skuViewMap = new HashMap<>();
            skuViewMap.put("syncKingdeeId",kingdeeSkuId.toString());
            JSONObject skuView = kingdeeCommonService.view(skuApiUtils, platformId, skuViewMap);

            if (Objects.nonNull(skuView)) {
                skuJson.put("FNumber", skuView.get("Number"));
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
        supplyOrgJson.put("FNumber", sysAccountingCompany.getKingdeeCode());
        entry.put("FSupplyOrg", supplyOrgJson);
        //发料方式
        entry.put("FIssueType", "2");
        //变更后
        entry.put("FChangeType","3");
        //分子
        entry.put("FNumerator",0);
        //分母
        entry.put("FDenominator",srcEntry.get("Denominator"));
        //应发数量
        entry.put("FMustQty",0);
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
        entry.put("FBackFlushType", "3");
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

    private JSONObject createNewPpBomEntry(JSONArray ppBomEntries,SubcontractOrderEntity subcontractOrder,SubcontractOrderDetailEntity parentDetail,SubcontractOrderDetailEntity chilDetail,SysAccountingCompanyEntity sysAccountingCompany, String bomBillNo) {
        JSONObject entries = new JSONObject();
        Map<String, Object> entry = new HashMap<>();

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
        entry.put("FDosageType", "2");
        //子项类型
        entry.put("FMaterialType", "1");
        //超发控制方式
        entry.put("FOverControlMode", "1");
        //发料组织
        JSONObject supplyOrgJson = new JSONObject();
        supplyOrgJson.put("FNumber", sysAccountingCompany.getKingdeeCode());
        entry.put("FSupplyOrg", supplyOrgJson);
        //发料方式
        entry.put("FIssueType", "2");
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