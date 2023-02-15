package com.erp.server.plm.api.kingdee;

import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.kingdee.bos.webapi.entity.OperatorResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 直接调拨单 测试
 */
public class KingdeeTransferDirectTest {
    public static void main(String[] args) {
        KingdeeApiUtils saleOrderApi=new KingdeeApiUtils("STK_TransferDirect");

        // 列表字段
        String fieldKeys = "FID,FBillNo,FDocumentStatus,FSaleOrgId,FStockOutOrgId,FStockOrgId,FDate";

        // 过滤条件
        LinkedList<String> queryfilters = new LinkedList<>();
        queryfilters.add(String.format("FModifyDate >= '%s'", "2023-01-10 00:00:00"));
//        queryfilters.add(String.format("FModifyDate < '%s'", "2023-01-12 00:00:00"));
        queryfilters.add(String.format("FCreatorId = '%s'", "16394"));  //Administrator
        queryfilters.add(String.format("FSaleOrgId = '%s'", "236226")); //香港唯迹
//        queryfilters.add(String.format("FDocumentStatus <> '%s'", KingdeeDocStatusEnum.APPROVED.getCode()));
//        queryfilters.add(String.format("FBillNo = '%s'", "XSCKD1098249"));

        String filterStr = String.join(" and ", queryfilters);


        while (true){
            List<Map<String, Object>> dataList = saleOrderApi.queryList(filterStr,fieldKeys,2000,0,0);
            if (dataList.isEmpty()){
                break;
            }

            List<String> unAuditIdList=new ArrayList<>();
            List<String> delIdList=new ArrayList<>();
            for (Map<String,Object> data:dataList) {
                delIdList.add(String.valueOf(data.get("FID")));
//                System.out.println(data.get("FBillNo"));

                if(KingdeeDocStatusEnum.APPROVING.getCode().equalsIgnoreCase(String.valueOf(data.get("FDocumentStatus")))
                        || KingdeeDocStatusEnum.APPROVED.getCode().equalsIgnoreCase(String.valueOf(data.get("FDocumentStatus")))){
                    unAuditIdList.add(String.valueOf(data.get("FID")));
                }
            }

            //反审核
            if(!unAuditIdList.isEmpty()){
                OperatorResult unAuditResult=saleOrderApi.unAuditById(unAuditIdList);
                System.out.println("反审核:"+ unAuditResult.isSuccessfully()+",数量："+delIdList.size());
            }

            //删除
            if(!delIdList.isEmpty()){
                OperatorResult delResult=saleOrderApi.deleteById(delIdList);
                System.out.println("删除:"+ delResult.isSuccessfully()+",数量："+delIdList.size());
            }

        }

    }


}
