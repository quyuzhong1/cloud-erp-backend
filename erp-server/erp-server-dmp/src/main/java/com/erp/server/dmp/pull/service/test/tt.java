package com.erp.server.dmp.pull.service.test;

import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.HttpCommonUtil;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

public class tt {
    public static void main(String[] args) {
        HttpCommonUtil httpCommonUtil = new HttpCommonUtil();
        String FormId = "SAL_SaleOrder";
        String FieldKeys = "FID,FBillNo,FDate,FBillTypeID,FDocumentStatus,FCustId,FSaleDeptId,FSalerId,FReceiveAddress,FLinkMan,FLinkPhone,FApproverId,FApproveDate,FCloseStatus,FCancelStatus,FChangerId,FReceiveId,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId,FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifyDate,FSaleOrgId,FVersionNo,FSignStatus,FSOFrom," +
                "FReturnType,FRowType,FMaterialName,FMaterialGroup,FMaterialId,FMaterialModel,FQty,FPriceUnitQty,FUnitID,FAuxPropId,FPrice,FEntryTaxRate,FTaxPrice,FIsFree,FEntryTaxAmount,FMaterialType,FAmount,FBarcode,FMapName,F_ulz_BaseProperty,FMapId";

        JSONObject jParas = new JSONObject();
        jParas.put("FormId", FormId);
        jParas.put("FieldKeys", FieldKeys);

        String jsonData = JSONObject.toJSONString(jParas);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "text/json");

        Map<String, Object> stringObjectMap = null;
        try {
            stringObjectMap = httpCommonUtil.sendOkhttp("http://47.106.224.95:8089/k3cloud/html5/index.aspx?ud=%7Bdbid%3A%27604578a4a54a6f%27%2Cusername%3A%27%E7%9E%BF%E8%82%B2%E5%BF%A0%27%2Cappid%3A%27237496_016p4bjt3qA%2FS4Xv2Z0r77%2BM5N781AMo%27%2Csigneddata%3A%27227b8d410c0189f51424caf0e70e425ec9cd1a68%27%2Ctimestamp%3A%271669694839%27%2Clcid%3A%272052%27%2Corigintype%3A%27simpas%27%7D",
                    jsonData, null, headerMap, RequestMethod.POST);

            System.out.println(stringObjectMap);

        } catch (Exception e) {

        }
    }
}
