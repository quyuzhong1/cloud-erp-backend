package com.erp.server.dmp.pull.service.gyy;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.LinkedMap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ttc {

    public static void main(String[] args) {
        String fieldKeys = "FID,FBillNo,FDate,FBillTypeID,FDocumentStatus,FCustId,FSaleDeptId,FSalerId,FReceiveAddress,FLinkMan,FLinkPhone,FApproverId,FApproveDate,FCloseStatus,FCancelStatus,FChangerId,FReceiveId,FNote,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId,FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifyDate,FSaleOrgId,FVersionNo,FSignStatus,FSOFrom,F_SHGJ1," +
                "FReturnType,FRowType,FMaterialName,FMaterialGroup,FMaterialId,FMaterialModel,FQty,FPriceUnitQty,FUnitID,FAuxPropId,FPrice,FEntryTaxRate,FTaxPrice,FIsFree,FEntryTaxAmount,FMaterialType,FAmount,FBarcode,FMapName,F_ulz_BaseProperty,FMapId,FBaseUnitId,FOldQty,FTaxNetPrice,FDiscount,FPriceDiscount,FBranchId,FEntryNote,FSrcType,FSrcBillNo,FMinPlanDeliveryDate,FDeliveryStatus";

        String[] split = fieldKeys.split(",");
        Map< String, String > map = new LinkedHashMap();
        for (String val : split) {
            map.put(val,"");
        }
        map.entrySet().stream().forEach(req -> {
            System.out.println(req.getKey());
        });

    }
}
