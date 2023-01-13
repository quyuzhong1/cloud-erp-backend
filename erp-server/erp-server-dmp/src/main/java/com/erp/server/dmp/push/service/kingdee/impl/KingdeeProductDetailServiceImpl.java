package com.erp.server.dmp.push.service.kingdee.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.MathUtil;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.dmp.dto.ApiPlmSyncLogDTO;
import com.erp.model.dmp.dto.ApiSyncTaskDTO;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.ApiSyncTaskEntity;
import com.erp.model.dmp.entity.CfgApiFieldMapValueEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiFieldTypeEnum;
import com.erp.model.dmp.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.enums.ApiStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeProductDetailService;
import com.erp.server.dmp.service.*;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 18:03
 */
@Slf4j
@Service
public class KingdeeProductDetailServiceImpl implements KingdeeProductDetailService {

    @Resource
    private PlatformService platformService;

    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    @Resource
    private CfgApiFieldMapValueService cfgApiFieldMapValueService;

    @Resource
    private ApiPlmSyncLogService apiPlmSyncLogService;

    @Resource
    private ApiSyncTaskService apiSyncTaskService;

    public static void main(String[] args) {

        //读取配置，初始化SDK
        K3CloudApi client = new K3CloudApi();
        //用于记录结果
        StringBuilder info = new StringBuilder();
        //业务对象标识
        String formId = "BD_MATERIAL";
        String jsonData = "{\"NeedUpDateFields\":[],\"NeedReturnFields\":[],\"IsDeleteEntry\":\"true\",\"SubSystemId\":\"\",\"IsVerifyBaseDataField\":\"false\",\"IsEntryBatchFill\":\"true\",\"ValidateFlag\":\"true\",\"NumberSearch\":\"true\",\"IsAutoAdjustField\":\"false\",\"InterationFlags\":\"\",\"IgnoreInterationFlag\":\"\",\"IsControlPrecision\":\"false\",\"ValidateRepeatJson\":\"false\",\"Model\":{\"FMATERIALID\":0,\"FCreateOrgId\":{\"FNumber\":\"\"},\"FUseOrgId\":{\"FNumber\":\"\"},\"FNumber\":\"\",\"FName\":\"\",\"FSpecification\":\"\",\"FMnemonicCode\":\"\",\"FOldNumber\":\"\",\"FDescription\":\"\",\"FMaterialGroup\":{\"FNumber\":\"\"},\"FDSMatchByLot\":\"false\",\"FImgStorageType\":\"\",\"FIsSalseByNet\":\"false\",\"FSPUID\":{\"FNUMBER\":\"\"},\"FPinYin\":\"\",\"FForbidReson\":\"\",\"FExtVar\":\"\",\"FSubHeadEntity\":{\"FEntryId\":0,\"FComTypeId_CMK\":{\"FNUMBER\":\"\"},\"FBarCodeHeader_CMK\":\"\",\"FComBrandId_CMK\":{\"FNUMBER\":\"\"},\"FBusinessType_CMK\":{\"FNumber\":\"\"},\"FShoppeID_CMK\":{\"FNUMBER\":\"\"},\"FSellMethod_CMK\":{\"FNumber\":\"\"},\"FCurrencyId_CMK\":{\"FNUMBER\":\"\"},\"FSalePrice_CMK\":0,\"FVIPPrice_CMK\":0,\"FGoodBarCode_CMK\":\"\",\"FPointsRate_CMK\":0,\"FPurPrice_CMK\":0,\"FLSProPrice\":0,\"FMaterialSource\":{\"FNUMBER\":\"\"},\"FIsControlSal\":\"false\",\"FLowerPercent\":0,\"FUpPercent\":0,\"FCalculateBase\":\"\",\"FMaxSalPrice_CMK\":0,\"FMinSalPrice_CMK\":0,\"FIsAutoRemove\":\"false\",\"FIsMailVirtual\":\"false\",\"FIsFreeSend\":\"\",\"FTimeUnit\":\"\",\"FRentFreeDura\":0,\"FPricingStep\":0,\"FMinRentDura\":0,\"FRentBeginPrice\":0,\"FPriceType\":\"\",\"FRentStepPrice\":0,\"FDepositAmount\":0,\"FLogisticsCount\":0,\"FRequestMinPackQty\":0,\"FMinRequestQty\":0,\"FRetailUnitID\":{\"FNUMBER\":\"\"},\"FIsPrinttAg\":\"false\",\"FIsAccessory\":\"false\"},\"SubHeadEntity\":{\"FEntryId\":0,\"FBARCODE\":\"\",\"FErpClsID\":\"\",\"FFeatureItem\":\"\",\"FCONFIGTYPE\":\"\",\"FCategoryID\":{\"FNumber\":\"\"},\"FTaxType\":{\"FNumber\":\"\"},\"FTaxRateId\":{\"FNUMBER\":\"\"},\"FBaseUnitId\":{\"FNumber\":\"\"},\"FIsPurchase\":\"false\",\"FIsInventory\":\"false\",\"FIsSubContract\":\"false\",\"FIsSale\":\"false\",\"FIsProduce\":\"false\",\"FIsAsset\":\"false\",\"FGROSSWEIGHT\":0,\"FNETWEIGHT\":0,\"FWEIGHTUNITID\":{\"FNUMBER\":\"\"},\"FLENGTH\":0,\"FWIDTH\":0,\"FHEIGHT\":0,\"FVOLUME\":0,\"FVOLUMEUNITID\":{\"FNUMBER\":\"\"},\"FSuite\":\"\",\"FCostPriceRate\":0,\"FColor\":\"\",\"FSpreadName\":\"\",\"FNameEn\":\"\",\"FSysModel\":\"\",\"FUseOrgId1\":{\"FNumber\":\"\"}},\"SubHeadEntity1\":{\"FEntryId\":0,\"FStoreUnitID\":{\"FNumber\":\"\"},\"FAuxUnitID\":{\"FNumber\":\"\"},\"FUnitConvertDir\":\"\",\"FStockId\":{\"FNumber\":\"\"},\"FStockPlaceId\":{},\"FIsLockStock\":\"false\",\"FIsCycleCounting\":\"false\",\"FCountCycle\":\"\",\"FCountDay\":0,\"FIsMustCounting\":\"false\",\"FIsBatchManage\":\"false\",\"FBatchRuleID\":{\"FNumber\":\"\"},\"FIsKFPeriod\":\"false\",\"FIsExpParToFlot\":\"false\",\"FExpUnit\":\"\",\"FExpPeriod\":0,\"FOnlineLife\":0,\"FRefCost\":0,\"FCurrencyId\":{\"FNumber\":\"\"},\"FIsEnableMinStock\":\"false\",\"FIsEnableMaxStock\":\"false\",\"FIsEnableSafeStock\":\"false\",\"FIsEnableReOrder\":\"false\",\"FMinStock\":0,\"FSafeStock\":0,\"FReOrderGood\":0,\"FEconReOrderQty\":0,\"FMaxStock\":0,\"FIsSNManage\":\"false\",\"FIsSNPRDTracy\":\"false\",\"FSNCodeRule\":{\"FNumber\":\"\"},\"FSNUnit\":{\"FNumber\":\"\"},\"FSNManageType\":\"\",\"FSNGenerateTime\":\"\",\"FBoxStandardQty\":0,\"FUseOrgId2\":{\"FNumber\":\"\"}},\"SubHeadEntity2\":{\"FEntryId\":0,\"FSaleUnitId\":{\"FNumber\":\"\"},\"FSalePriceUnitId\":{\"FNumber\":\"\"},\"FOrderQty\":0,\"FMinQty\":0,\"FMaxQty\":0,\"FOutStockLmtH\":0,\"FOutStockLmtL\":0,\"FAgentSalReduceRate\":0,\"FIsATPCheck\":\"false\",\"FIsReturnPart\":\"false\",\"FIsInvoice\":\"false\",\"FIsReturn\":\"false\",\"FAllowPublish\":\"false\",\"FISAFTERSALE\":\"false\",\"FISPRODUCTFILES\":\"false\",\"FISWARRANTED\":\"false\",\"FWARRANTY\":0,\"FWARRANTYUNITID\":\"\",\"FOutLmtUnit\":\"\",\"FTaxCategoryCodeId\":{\"FNUMBER\":\"\"},\"FSalGroup\":{\"FNumber\":\"\"},\"FIsTaxEnjoy\":\"false\",\"FTaxDiscountsType\":\"\",\"FUseOrgId3\":{\"FNumber\":\"\"},\"FUnValidateExpQty\":\"false\"},\"SubHeadEntity3\":{\"FEntryId\":0,\"FBaseMinSplitQty\":0,\"FPurchaseUnitId\":{\"FNumber\":\"\"},\"FPurchasePriceUnitId\":{\"FNumber\":\"\"},\"FPurchaseOrgId\":{\"FNumber\":\"\"},\"FPurchaseGroupId\":{\"FNumber\":\"\"},\"FPurchaserId\":{\"FNumber\":\"\"},\"FDefaultVendor\":{\"FNumber\":\"\"},\"FChargeID\":{\"FNumber\":\"\"},\"FIsQuota\":\"false\",\"FQuotaType\":\"\",\"FMinSplitQty\":0,\"FIsVmiBusiness\":\"false\",\"FEnableSL\":\"false\",\"FIsPR\":\"false\",\"FIsReturnMaterial\":\"false\",\"FIsSourceControl\":\"false\",\"FReceiveMaxScale\":0,\"FReceiveMinScale\":0,\"FReceiveAdvanceDays\":0,\"FReceiveDelayDays\":0,\"FPOBillTypeId\":{\"FNUMBER\":\"\"},\"FAgentPurPlusRate\":0,\"FDefBarCodeRuleId\":{\"FNUMBER\":\"\"},\"FPrintCount\":0,\"FMinPackCount\":0,\"FUseOrgId4\":{\"FNumber\":\"\"},\"FDailyOutQtySub\":0,\"FDefaultLineIdSub\":{\"FNUMBER\":\"\"},\"FIsEnableScheduleSub\":\"false\"},\"SubHeadEntity4\":{\"FEntryId\":0,\"FPlanMode\":\"\",\"FBaseVarLeadTimeLotSize\":0,\"FPlanningStrategy\":\"\",\"FMfgPolicyId\":{\"FNumber\":\"\"},\"FOrderPolicy\":\"\",\"FPlanWorkshop\":{\"FNumber\":\"\"},\"FFixLeadTime\":0,\"FFixLeadTimeType\":\"\",\"FVarLeadTime\":0,\"FVarLeadTimeType\":\"\",\"FCheckLeadTime\":0,\"FCheckLeadTimeType\":\"\",\"FOrderIntervalTimeType\":\"\",\"FOrderIntervalTime\":0,\"FMaxPOQty\":0,\"FMinPOQty\":0,\"FIncreaseQty\":0,\"FEOQ\":0,\"FVarLeadTimeLotSize\":0,\"FPlanIntervalsDays\":0,\"FPlanBatchSplitQty\":0,\"FRequestTimeZone\":0,\"FPlanTimeZone\":0,\"FPlanGroupId\":{\"FNumber\":\"\"},\"FATOSchemeId\":{\"FNUMBER\":\"\"},\"FPlanerID\":{\"FNumber\":\"\"},\"FIsMrpComBill\":\"false\",\"FCanLeadDays\":0,\"FIsMrpComReq\":\"false\",\"FLeadExtendDay\":0,\"FReserveType\":\"\",\"FPlanSafeStockQty\":0,\"FAllowPartAhead\":\"false\",\"FCanDelayDays\":0,\"FDelayExtendDay\":0,\"FAllowPartDelay\":\"false\",\"FPlanOffsetTimeType\":\"\",\"FPlanOffsetTime\":0,\"FSupplySourceId\":{\"FNumber\":\"\"},\"FTimeFactorId\":{\"FNumber\":\"\"},\"FQtyFactorId\":{\"FNumber\":\"\"},\"FProductLine\":{\"FNUMBER\":\"\"},\"FWriteOffQty\":0,\"FPlanIdent\":{\"FNumber\":\"\"},\"FProScheTrackId\":{\"FNumber\":\"\"},\"FDailyOutQty\":0,\"FUseOrgId7\":{\"FNumber\":\"\"}},\"SubHeadEntity5\":{\"FEntryId\":0,\"FWorkShopId\":{\"FNumber\":\"\"},\"FProduceUnitId\":{\"FNumber\":\"\"},\"FFinishReceiptOverRate\":0,\"FFinishReceiptShortRate\":0,\"FProduceBillType\":{\"FNUMBER\":\"\"},\"FOrgTrustBillType\":{\"FNUMBER\":\"\"},\"FIsSNCarryToParent\":\"false\",\"FIsProductLine\":\"false\",\"FBOMUnitId\":{\"FNumber\":\"\"},\"FLOSSPERCENT\":0,\"FConsumVolatility\":0,\"FIsMainPrd\":\"false\",\"FIsCoby\":\"false\",\"FIsECN\":\"false\",\"FIssueType\":\"\",\"FBKFLTime\":\"\",\"FPickStockId\":{\"FNumber\":\"\"},\"FPickBinId\":{},\"FOverControlMode\":\"\",\"FMinIssueQty\":0,\"FISMinIssueQty\":\"false\",\"FIsKitting\":\"false\",\"FIsCompleteSet\":\"false\",\"FDefaultRouting\":{\"FNumber\":\"\"},\"FStdLaborPrePareTime\":0,\"FStdLaborProcessTime\":0,\"FStdMachinePrepareTime\":0,\"FStdMachineProcessTime\":0,\"FMinIssueUnitId\":{\"FNUMBER\":\"\"},\"FMdlId\":{\"FNUMBER\":\"\"},\"FMdlMaterialId\":{\"FNUMBER\":\"\"},\"FStandHourUnitId\":\"\",\"FBackFlushType\":\"\",\"FFIXLOSS\":0,\"FUseOrgId6\":{\"FNumber\":\"\"},\"FIsEnableSchedule\":\"false\",\"FDefaultLineId\":{\"FNUMBER\":\"\"}},\"SubHeadEntity7\":{\"FEntryId\":0,\"FSubconUnitId\":{\"FNumber\":\"\"},\"FSubconPriceUnitId\":{\"FNumber\":\"\"},\"FSubBillType\":{\"FNUMBER\":\"\"},\"FUseOrgId8\":{\"FNumber\":\"\"}},\"SubHeadEntity6\":{\"FEntryId\":0,\"FCheckIncoming\":\"false\",\"FCheckProduct\":\"false\",\"FCheckStock\":\"false\",\"FCheckReturn\":\"false\",\"FCheckDelivery\":\"false\",\"FEnableCyclistQCSTK\":\"false\",\"FStockCycle\":0,\"FEnableCyclistQCSTKEW\":\"false\",\"FEWLeadDay\":0,\"FIncSampSchemeId\":{\"FNUMBER\":\"\"},\"FIncQcSchemeId\":{\"FNUMBER\":\"\"},\"FInspectGroupId\":{\"FNUMBER\":\"\"},\"FInspectorId\":{\"FNUMBER\":\"\"},\"FCheckEntrusted\":\"false\",\"FCheckOther\":\"false\",\"FIsFirstInspect\":\"false\",\"FUseOrgId5\":{\"FNumber\":\"\"},\"FCheckReturnMtrl\":\"false\",\"FCheckSubRtnMtrl\":\"false\",\"FFirstQCControlType\":\"\"},\"FBarCodeEntity_CMK\":[{\"FEntryID\":0,\"FCodeType_CMK\":\"\",\"FBarCode_CMK\":\"\",\"FUnitId_CMK\":{\"FNUMBER\":\"\"},\"FPrice_CMK\":0,\"FVIPPrice\":0,\"FProPrice\":0,\"FVIPCardLevel_CMK\":{\"FNUMBER\":\"\"},\"FRemarks_CMK\":\"\"}],\"FSpecialAttributeEntity\":[{\"FEntryID\":0,\"FSpecAttrCategoryID\":{\"FNUMBER\":\"\"},\"FSpecialAttributeID\":{\"FNUMBER\":\"\"}}],\"FEntityAuxPty\":[{\"FEntryID\":0,\"FAuxPropertyId\":{\"FNumber\":\"\"},\"FIsEnable1\":\"false\",\"FIsComControl\":\"false\",\"FIsAffectPrice1\":\"false\",\"FIsAffectPlan1\":\"false\",\"FIsAffectCost1\":\"false\",\"FIsMustInput\":\"false\",\"FUseOrgId11\":{\"FNumber\":\"\"},\"FValueType\":\"\"}],\"FEntityInvPty\":[{\"FEntryID\":0,\"FUseOrgId10\":{\"FNumber\":\"\"},\"FInvPtyId\":{\"FNumber\":\"\"},\"FIsEnable\":\"false\",\"FIsAffectPrice\":\"false\",\"FIsAffectPlan\":\"false\",\"FIsAffectCost\":\"false\"}]}}";
        //调用接口
        String resultJson = null;
        try {
            resultJson = client.save(formId, jsonData);
            System.out.println(resultJson);
        }catch (Exception e) {
            log.info("请求接口地址异常 错误信息：" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void pushProductDetail(Map<String, Object> map) {
        //传入map数据不能为空
        if (ObjectUtils.isEmpty(map) || map.size() == 0) {
            throw new ServiceException(ApiError.Default);
        }
        PlatformEntity platformEntity = platformService.getByName(PlatformEnum.KINGDEE.getName());
        if (ObjectUtils.isEmpty(platformEntity)) {
            throw new ServiceException(ApiError.Default);
        }
        CfgApiFieldMapDTO dto = new CfgApiFieldMapDTO();
        dto.setApiPlatformId(platformEntity.getId());
        dto.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
        List<CfgApiFieldMapDTO> mapList = cfgApiFieldMapService.getByParams(dto);
        if (CollectionUtils.isNotEmpty(mapList)) {
            log.info(ApiError.ERROR_97025.msg);
            //新增定时同步任务
            insertApiSyncTask(platformEntity,map);
            return;
        }
        List<String> fieldMapIds = mapList.stream().map(CfgApiFieldMapDTO::getId).collect(Collectors.toList());

        List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList = cfgApiFieldMapValueService.listByFieldMapIds(fieldMapIds);

        //读取配置，初始化SDK
        K3CloudApi client = new K3CloudApi();
        //用于记录结果
        StringBuilder info = new StringBuilder();
        //业务对象标识
        String formId = "BD_MATERIAL";

        Map<String,Object> resultMap = new LinkedHashMap<>();

        for (CfgApiFieldMapDTO cfgApiFieldMapDTO : mapList) {
            //第三方系统逗号分割多层结构
            String apiField = cfgApiFieldMapDTO.getApiField();
            List<String> apiFields = Arrays.stream(apiField.split(",")).collect(Collectors.toList());
            for (int i = 0; i < apiFields.size() ; i++ ) {
                //给不同结构的外部字段赋值
                handleResultMap(cfgApiFieldMapDTO,cfgApiFieldMapValueList,map,resultMap,apiFields,i);
            }
        }
        //结果集转json字符串
        String jsonData = JSONObject.toJSONString(resultMap);
        //调用接口
        String resultJson = null;
        try {
            resultJson = client.save(formId,jsonData);
            //新增日志信息
            ApiPlmSyncLogDTO apiPlmSyncLogDTO = new ApiPlmSyncLogDTO();
            apiPlmSyncLogDTO.setApiPlatformId(platformEntity.getId());
            apiPlmSyncLogDTO.setApiPlatform(platformEntity.getName());
            apiPlmSyncLogDTO.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
            apiPlmSyncLogDTO.setBusinessId(String.valueOf(map.get("id")));
            apiPlmSyncLogDTO.setStatus(ApiStatusEnum.SUCCESS.getCode());
            apiPlmSyncLogDTO.setMsg("发送成功");
            apiPlmSyncLogDTO.setRequestParamJson(jsonData);
            apiPlmSyncLogService.insert(apiPlmSyncLogDTO);
            //发送成功后删除任务表数据
            Map<String,Object> removeMap = new HashMap<>();
            removeMap.put("apiPlatformId",platformEntity.getId());
            removeMap.put("moduleType",ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
            removeMap.put("businessId",String.valueOf(map.get("id")));
            apiPlmSyncLogService.removeByMap(removeMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("请求接口地址异常 错误信息：" + e.getMessage());
            //新增日志信息
            ApiPlmSyncLogDTO apiPlmSyncLogDTO = new ApiPlmSyncLogDTO();
            apiPlmSyncLogDTO.setApiPlatformId(platformEntity.getId());
            apiPlmSyncLogDTO.setApiPlatform(platformEntity.getName());
            apiPlmSyncLogDTO.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
            apiPlmSyncLogDTO.setBusinessId(String.valueOf(map.get("id")));
            apiPlmSyncLogDTO.setStatus(ApiStatusEnum.FAILURE.getCode());
            apiPlmSyncLogDTO.setMsg("请求接口地址异常 错误信息：" + e.getMessage());
            apiPlmSyncLogDTO.setRequestParamJson(jsonData);
            apiPlmSyncLogService.insert(apiPlmSyncLogDTO);
            //新增定时同步任务
            insertApiSyncTask(platformEntity,map);
        }
    }

    /**
     * @description: 新增定时同步任务
     * @author Will
     * @date: 2023/1/12 16:41
     * @param platformEntity
     * @param map
     */
    private void insertApiSyncTask(PlatformEntity platformEntity,Map<String, Object> map) {
        //新增或更新定时任务数据重新发送
        ApiSyncTaskDTO apiSyncTaskDTO = new ApiSyncTaskDTO();
        apiSyncTaskDTO.setApiPlatformId(platformEntity.getId());
        apiSyncTaskDTO.setApiPlatform(platformEntity.getName());
        apiSyncTaskDTO.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
        apiSyncTaskDTO.setBusinessId(String.valueOf(map.get("id")));
        apiSyncTaskDTO.setRetryCount(MathUtil.ZERO);
        //传入参数转json字符串
        String jsonParam = JSONObject.toJSONString(map);
        apiSyncTaskDTO.setRequestParamJson(jsonParam);
        ApiSyncTaskEntity apiSyncTask = apiSyncTaskService.getByApiSyncTask(apiSyncTaskDTO);
        if (ObjectUtils.isEmpty(apiSyncTask)) {
            apiSyncTaskService.insert(apiSyncTaskDTO);
        } else {
            apiSyncTaskDTO.setId(apiSyncTask.getId());
            apiSyncTaskDTO.setRetryCount(MathUtil.add(apiSyncTask.getRetryCount(),1));
            apiSyncTaskService.update(apiSyncTaskDTO);
        }
    }

    /**
     * @description: 处理结果集Map
     * @author Will
     * @date: 2023/1/12 12:05
     * @param cfgApiFieldMapDTO
     * @param cfgApiFieldMapValueList
     * @param map
     * @param resultMap
     * @param apiFields
     * @param i
     */
    private void handleResultMap(CfgApiFieldMapDTO cfgApiFieldMapDTO,List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList,Map<String,Object> map,Map<String,Object> resultMap,List<String> apiFields,int i) {
        if (i == 0) {
            //第一层结构时
            if (apiFields.size() == 1 ) {
                //如果只有一层结构则直接插入resultMap
                putValueResultMap(cfgApiFieldMapDTO,cfgApiFieldMapValueList,map,resultMap,apiFields,i);
            } else {
                Object obj = resultMap.get(apiFields.get(i));
                if (ObjectUtils.isNull(obj)) {
                    resultMap.put(apiFields.get(i),new LinkedHashMap<>());
                }
            }
        } else if (i == apiFields.size() - 1){
            //获取上一级Map对象
            Map<String, Object> parentMap = getParentMap(resultMap, apiFields, i);
            //如果时最后一层结构则插入值到上一层Map中
            putValueResultMap(cfgApiFieldMapDTO,cfgApiFieldMapValueList,map,parentMap,apiFields,i);
        } else {
            //给非底层结构添加Map
            Object obj = resultMap.get(apiFields.get(i));
            if (ObjectUtils.isNull(obj)) {
                resultMap.put(apiFields.get(i),new LinkedHashMap<>());
            }
        }
    }

    /**
     * @description: 获取上一级Map对象
     * @author Will
     * @date: 2023/1/12 12:03
     * @param map
     * @param apiFields
     * @param i
     * @return Map<Object>
     */
    private Map<String,Object> getParentMap (Map<String,Object> map,List<String> apiFields,int i) {
        Map<String ,Object> resultMap = map;
        for (int j = 0; j < apiFields.size() ; j++ ) {
            //当传入i和j相等时返回map
            if (j == i) {
                return resultMap;
            } else {
                resultMap = (LinkedHashMap) resultMap.get(apiFields.get(j));
            }
        }
        return resultMap;
    }

    /**
     * @description: 给最底层字段赋值
     * @author Will
     * @date: 2023/1/12 12:03
     * @param cfgApiFieldMapDTO
     * @param cfgApiFieldMapValueList
     * @param map
     * @param resultMap
     * @param apiFields
     * @param i
     */
    private void putValueResultMap (CfgApiFieldMapDTO cfgApiFieldMapDTO,List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList,Map<String,Object> map,Map<String,Object> resultMap,List<String> apiFields,int i) {

        if (ApiFieldTypeEnum.FIELD_VALUE_COPY.getCode().equals(cfgApiFieldMapDTO.getFieldType())) {
            resultMap.put(apiFields.get(i),map.get(apiFields.get(i)));
        } else {
            if (CollectionUtils.isEmpty(cfgApiFieldMapValueList)) {
                throw new ServiceException(ApiError.ERROR_97025);
            }
            //根据值映射转换
            String apiValue = cfgApiFieldMapValueList.stream()
                    .filter(obj -> obj.getFieldMapId().equals(cfgApiFieldMapDTO.getId()) && obj.getSelfValue().equals(map.get(apiFields.get(i))))
                    .map(CfgApiFieldMapValueEntity::getApiValue)
                    .findFirst()
                    .orElse(null);
            if (StringUtils.isBlank(apiValue)) {
                throw new ServiceException(ApiError.ERROR_97025);
            }
            resultMap.put(apiFields.get(i),apiValue);
        }
    }

}
