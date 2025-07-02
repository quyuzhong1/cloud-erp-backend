package com.erp.server.dmp.pull.service.gyy;

import com.alibaba.fastjson.JSONObject;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;

import java.util.List;
import java.util.Map;

public class ttc {

    public static void main(String[] args) throws Exception {
        //读取配置，初始化SDK
        K3CloudApi client = new K3CloudApi();

        String formId = "STK_TransferDirect";
        String fieldKey = "FId,FBillNo,FBizType,FTransferDirect,FTransferBizType,FSaleOrgId,FSaleOrgId.FName," +
                "FSettleOrgId,FSettleOrgId.FName,FStockOutOrgId,FStockOutOrgId.FName,FOwnerOutIdHead,FOwnerOutIdHead.FName," +
                "FStockOrgId,FStockOrgId.FName,FSettleCurrId,FSettleCurrId.FName,FExchangeTypeId,FExchangeTypeId.FName,FExchangeRate," +
                "FDate,FNote,FBaseCurrId,FBaseCurrId.FName,FDocumentStatus,FDocumentStatus.FCaption,FCreateDate,FModifierId,FModifyDate,FCancelStatus,FCancelStatus.FCaption,FCancelDate," +
                "FBillEntry_FEntryID,FSrcStockId,FSrcStockId.FName,FDestStockId,FDestStockId.FName," +
                "FRowType,FMaterialId,FMaterialId.FName,FUnitID,FUnitID.FName,FQty," +
                "FSrcStockStatusId,FSrcStockStatusId.FName,FDestStockStatusId,FDestStockStatusId.FName,FBusinessDate,FIsFree,FDestMaterialId,FDestMaterialId.FName";

        KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(formId);
        List<Map<String, Object>>objects = kingdeeApiUtils.queryList("", fieldKey, 1000, 1,0);
        System.out.println(JSONObject.toJSONString(objects));


    }
}
