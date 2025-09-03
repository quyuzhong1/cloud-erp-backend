package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.KingdeeParamDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastJsonUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeApi;
import com.erp.sdk.third.kingdee.utils.KingdeeApiThreadLocal;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeePurchaseChangeConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: 采购变更消费实现
 * @date 2023/9/28 18:02
 */
@Service
@Slf4j
public class KingdeePurchaseChangeConsumerServiceImpl implements KingdeePurchaseChangeConsumerService {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @KingdeeApi(KingdeePushModuleEnum.PUR_POXCHANGE)
    public void executeConsumer(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.PURCHASE_CHANGE.getCode();
        //操作项
        String operate = (String) map.get("operate");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = KingdeeApiThreadLocal.get();

        /**
         * 审核
         */
        if (SyncOperateEnum.OPERATE_APPROVE.getCode().equals(operate)) {
            operateApprove(apiUtils,platformEntity, map,type);
        }
    }

    /**
     * 审核
     */
    public void operateApprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        //查询采购订单信息
        handlePurchaseOrderData(map);

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_FIELD);
        }

        //判断金蝶系统是否已存在该数据
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
        } catch (Exception e) {

            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            return;
        }

        //查找到数据后，判断其审核状态
        String documentStatus = (String) model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id"));
        Boolean flag = Boolean.FALSE;
        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            flag = kingdeeCommonService.unAudit(apiUtils, id);
        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //给修改json对象赋值ID
            KingdeeUtils.makeFieldJson(json, "FId", ".", id);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
        }
    }

    /**
     * 查询采购订单财务信息
     */
    private void handlePurchaseOrderData (Map<String, Object> map) {
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.PUR_PURCHASEORDER.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", map.get("sourceCode")));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FPOOrderFinance_FEntryID,FExchangeRate,FPayConditionId.FNumber," +
                "FIinstallment_FENTRYID,FYFDATE,FYFRATIO,FYFAMOUNT,FISPREPAYMENT,FInsPrepaidAmount,FRemarks,FPayMaterialId.FNUMBER,FMATERIALSEQ,FPayPlanQty,FPayPlanPrice,FPURCHASEORDERNO,FOrderEntryId,FinsPayAdvanceRate,FInsPayAdvanceAmount,FPAYPLANPRICEUNITID.FNumber,FBasePriceUnit.FNumber,FPayMaterialDesc,FBasePayPlanQty,FPayAuxPropId,FPayChargeProjectID.FNUMBER,FOrderActualPaySubEntity_FDetailID,FPAYBILLID,FPAYBILLENTITYID,FPOORDERID,FAmount,FPREAMOUNT,FPPSettleOrgId.FNumber,FAPPLYBILLNO,FPREPAYBillNo,FPAPPLYAMOUNT,FPPayJoinAmount";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 20);
        if (CollectionUtils.isEmpty(queryList)) {
            throw new ServiceException(10000, StrUtil.format("未找到采购订单{}",map.get("sourceCode").toString()));
        }
        Object financeId = queryList.get(0).get("FPOOrderFinance_FEntryID");
        Object exchangeRate = queryList.get(0).get("FExchangeRate");
        Object payConditionId = queryList.get(0).get("FPayConditionId.FNumber");
        map.put("financeId",financeId);
        map.put("exchangeRate",exchangeRate);
        map.put("payConditionId",payConditionId);



        /**
         * 采购订单下推付款申请后，变更单无法审核通过，提示以下推付款申请单不能删除
         * 考虑将付款计划查出来推到变更单，但是这个清空会导致变更后采购订单的付款计划被删除，只传传付款计划id会被删，传了明细id也会被删
         * 现暂时不用，代码先存储，表配置删除。
         */
        List<JSONObject> fIinstallmentList = new ArrayList<>();
        Map<Object, List<Map<String, Object>>> resultMap = queryList.stream().collect(Collectors.groupingBy(obj -> obj.get("FIinstallment_FENTRYID")));
        for (Map.Entry<Object, List<Map<String, Object>>> entry : resultMap.entrySet()) {
            Map<String, Object> stringObjectMap = entry.getValue().get(0);
            JSONObject actualPayJson = new JSONObject();
            //付款计划id
            actualPayJson.set("FENTRYID",entry.getKey());
            //应付日期
            actualPayJson.set("FYFDATE",stringObjectMap.get("FYFDATE"));
            //应付比例(%)
            actualPayJson.set("FYFRATIO",stringObjectMap.get("FYFRATIO"));
            //应付金额
            actualPayJson.set("FYFAMOUNT",stringObjectMap.get("FYFAMOUNT"));
            //是否预付
            actualPayJson.set("FISPREPAYMENT",stringObjectMap.get("FISPREPAYMENT"));
            //单次预付额度
            actualPayJson.set("FInsPrepaidAmount",stringObjectMap.get("FInsPrepaidAmount"));
            //备注
            actualPayJson.set("FRemarks",stringObjectMap.get("FRemarks"));
            //物料编码
            actualPayJson.set("FPayMaterialId",stringObjectMap.get("FPayMaterialId.FNUMBER"));
            //物料行号
            actualPayJson.set("FMATERIALSEQ",stringObjectMap.get("FMATERIALSEQ"));
            //数量
            actualPayJson.set("FPayPlanQty",stringObjectMap.get("FPayPlanQty"));
            //含税单价
            actualPayJson.set("FPayPlanPrice",stringObjectMap.get("FPayPlanPrice"));
            //采购订单号
            actualPayJson.set("FPURCHASEORDERNO",stringObjectMap.get("FPURCHASEORDERNO"));
            //订单明细行内码
            actualPayJson.set("FOrderEntryId",stringObjectMap.get("FOrderEntryId"));
            //预付比例%
            actualPayJson.set("FinsPayAdvanceRate",stringObjectMap.get("FinsPayAdvanceRate"));
            //预付款
            actualPayJson.set("FInsPayAdvanceAmount",stringObjectMap.get("FInsPayAdvanceAmount"));
            //计价单位
            actualPayJson.set("FPAYPLANPRICEUNITID",stringObjectMap.get("FPAYPLANPRICEUNITID"));
            //计价基本单位
            actualPayJson.set("FBasePriceUnit",stringObjectMap.get("FBasePriceUnit"));
            //物料说明
            actualPayJson.set("FPayMaterialDesc",stringObjectMap.get("FPayMaterialDesc"));
            //数量(基本单位)
            actualPayJson.set("FBasePayPlanQty",stringObjectMap.get("FBasePayPlanQty"));
            //辅助属性
            actualPayJson.set("FPayAuxPropId",stringObjectMap.get("FPayAuxPropId"));
            //费用项目
            actualPayJson.set("FPayChargeProjectID",stringObjectMap.get("FPayChargeProjectID"));

            //付款计划明细id
            List<JSONObject> fDetailIdList = new ArrayList<>();
            for (Map<String, Object> detailMap : entry.getValue()) {
                JSONObject detailJson = new JSONObject();
                //主键id
                detailJson.set("FDetailID",detailMap.get("FOrderActualPaySubEntity_FDetailID"));
                //付款单内码
                detailJson.set("FPAYBILLID",detailMap.get("FPAYBILLID"));
                //付款单分录内码
                detailJson.set("FPAYBILLENTITYID",detailMap.get("FPAYBILLENTITYID"));
                //采购订单内码
                detailJson.set("FPOORDERID",detailMap.get("FPOORDERID"));
                //实付预付金额
                detailJson.set("FAmount",detailMap.get("FAmount"));

                //预分配金额
                detailJson.set("FPREAMOUNT",detailMap.get("FPREAMOUNT"));
                //结算组织
                detailJson.set("FPPSettleOrgId",detailMap.get("FPPSettleOrgId.FNumber"));
                //付款申请单号
                detailJson.set("FAPPLYBILLNO",detailMap.get("FAPPLYBILLNO"));
                //预付单号
                detailJson.set("FPREPAYBillNo",detailMap.get("FPREPAYBillNo"));
                //付款申请关联金额
                detailJson.set("FPAPPLYAMOUNT",detailMap.get("FPAPPLYAMOUNT"));
                //付款关联金额
                detailJson.set("FPPayJoinAmount",detailMap.get("FPPayJoinAmount"));

                fDetailIdList.add(detailJson);
            }
            actualPayJson.set("fDetailIdList",fDetailIdList);
            fIinstallmentList.add(actualPayJson);
        }
        map.put("fIinstallmentList",fIinstallmentList);
    }
}
