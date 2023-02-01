package com.erp.server.plm.api.kingdee;

import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeDocStatusEnum;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.OperateParam;
import com.kingdee.bos.webapi.entity.OperatorResult;
import com.kingdee.bos.webapi.entity.QueryParam;
import com.kingdee.bos.webapi.sdk.K3CloudApi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 应收单 测试
 */
public class KingdeePaymentTest {
    public static void main(String[] args) {
        KingdeeApiUtils saleOrderApi=new KingdeeApiUtils("AR_receivable");

        // 列表字段
        String fieldKeys = "FID,FBillNo,FDocumentStatus,FSaleOrgId,FSaleOrgId.FNumber,FSaleOrgId.FName";

        // 过滤条件
        LinkedList<String> queryfilters = new LinkedList<>();
        queryfilters.add(String.format("FModifyDate >= '%s'", "2020-01-10 00:00:00"));
        queryfilters.add(String.format("FModifyDate < '%s'", "2023-01-12 00:00:00"));
//        queryfilters.add(String.format("FCreatorId_Name = '%s'", "16394"));  //Administrator
        queryfilters.add(String.format("FSaleOrgId.FNumber <> '%s'", "101")); //优至胜
        queryfilters.add(String.format("FSaleOrgId.FNumber <> '%s'", "105")); //小隼
//        queryfilters.add(String.format("FDocumentStatus <> '%s'", KingdeeDocStatusEnum.APPROVED.getCode()));
//        queryfilters.add(String.format("FBillNo = '%s'", "AR00000004"));

        String filterStr = String.join(" and ", queryfilters);


        while (true){
            List<Map<String, Object>> dataList = saleOrderApi.queryList(filterStr,fieldKeys,2000,0);
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
                OperatorResult unAuditResult=saleOrderApi.unAuditById(unAuditIdList,true);
                System.out.println("反审核:"+ unAuditResult.isSuccessfully()+",数量："+delIdList.size());
            }
//
//            //删除
            if(!delIdList.isEmpty()){
                OperatorResult delResult=saleOrderApi.deleteById(delIdList,true);
                System.out.println("删除:"+ delResult.isSuccessfully()+",数量："+delIdList.size());
            }

        }

    }


}
