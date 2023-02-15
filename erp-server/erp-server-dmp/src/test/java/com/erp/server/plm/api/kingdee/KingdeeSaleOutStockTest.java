package com.erp.server.plm.api.kingdee;

import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.OperateParam;
import com.kingdee.bos.webapi.entity.OperatorResult;
import com.kingdee.bos.webapi.entity.QueryParam;
import com.kingdee.bos.webapi.sdk.K3CloudApi;

import java.util.*;


/**
 * 销售出库单 测试
 */
public class KingdeeSaleOutStockTest {
    public static void main(String[] args) {
        K3CloudApi client = new K3CloudApi();

        // 显示的字段
        String fieldKeys = "FBillTypeID,FBillTypeID.FName,FBillNo,FSoorDerno,FDate,FSaleOrgId,FSaleOrgId.FName,FCustomerID,FCustomerID.FName,FSaleDeptID.FName,FSalesManID,FSalesManID.FName,FReceiverID.FName,FTransferBizType.FName,F_ulz_BaseProperty2,FLinkPhone,FLinkMan,FBussinessType,FDocumentStatus,FNote,FReceiveAddress,FCreatorId.FName,FCreateDate,FModifierId.FName,FModifyDate,FApproverID.FName,FApproveDate,FCancelStatus,FGYDATE,FLogisticsNos,F_ulz_Text3,FSettleCurrID.FCode,FExchangeRate,"
                + "FSrcBillNo,F_ulz_BaseProperty1,FCustMatID,FCustMatName,FMaterialID,FMaterialID.FNumber,FMaterialID.FName,FBarcode,FMateriaModel,FMateriaType,FRealQty,FUnitID.FName,FPrice,FIsFree,FArrivalStatus,FArrivalDate,FAmount,FStockStatusID,FStockStatusID.FName,FStockID.FName,F_ulz_Text1,FEntryCostAmount,FEntrynote";

        // 过滤条件
        LinkedList<String> queryfilters = new LinkedList<>();
        queryfilters.add(String.format("FModifyDate >= '%s'", "2023-01-10 00:00:00"));
        queryfilters.add(String.format("FModifyDate < '%s'", "2023-01-12 00:00:00"));
        queryfilters.add(String.format("FCreatorId = '%s'", "16394"));  //Administrator
        queryfilters.add(String.format("FSaleOrgId = '%s'", "236226")); //香港唯迹
//        queryfilters.add(String.format("FBillNo = '%s'", "XSCKD1098249"));
//        queryfilters.add(String.format("FDocumentStatus = '%s'", "A"));   //A:创建
//        queryfilters.add(String.format("FDocumentStatus = '%s'", "B")); //B:审核中
//        queryfilters.add(String.format("FDocumentStatus = '%s'", "C")); //C:已审核
        queryfilters.add(String.format("FDocumentStatus = '%s'", "D"));   //D:重新审核
        String filterStr = String.join(" and ", queryfilters);


        Boolean dataSign = true;
        //当前页数
        Integer pageIndex = 0;

        //每次最多获取100条
        Integer pageSize = 1000;
        while (dataSign) {
            //请求参数，示例使用的是SDK提供的模板类，还可以使用字符串拼接等方式
            QueryParam param = new QueryParam();
            String formId="SAL_OUTSTOCK";
            param.setFormId(formId);
            param.setFieldKeys(fieldKeys);
            param.setFilterString(filterStr);
            param.setLimit(pageSize);
            //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20

//            param.setStartRow(pageIndex * pageSize);
            param.setStartRow(0);
            String s = JSONObject.toJSONString(param);

            Map<String, Object> stringObjectMap = null;
            try {
                List<List<Object>> result = client.executeBillQuery(s);
                if (!result.isEmpty()) {
                    if (result.size() == 1 && result.get(0).get(0).toString().contains("IsSuccess=false")) {
                        dataSign = false;
                        throw new RuntimeException(" ===== 金蝶云星空解析出库详情信息数据失败 ===== " + result);
                    }

                    List<String> numberList=new ArrayList<>();
                    for (List<Object> objects : result) {
                        Map<String, String> valMap = KingdeeUtils.keySetValByLinked(fieldKeys, objects);
                        numberList.add(valMap.get("FBillNo"));


//                        Map<String, String> valMap = KingdeeUtils.keySetValByLinked(fieldKeys, objects);
//                        KingdeeDeliveryDetailEntity kingdeeReturnOrderEntity = new KingdeeDeliveryDetailEntity();
//                        kingdeeReturnOrderEntity.setFBillTypeID(valMap.get("FBillTypeID"));
//                        kingdeeReturnOrderEntity.setFBillTypeName(valMap.get("FBillTypeID.FName"));
//                        kingdeeReturnOrderEntity.setFBillNo(valMap.get("FBillNo"));
//                        kingdeeReturnOrderEntity.setFSoorDerno(valMap.get("FSoorDerno"));
//                        kingdeeReturnOrderEntity.setFDate(valMap.get("FDate"));
//                        kingdeeReturnOrderEntity.setFSaleOrgId(valMap.get("FSaleOrgId"));
//                        kingdeeReturnOrderEntity.setFSaleOrgName(valMap.get("FSaleOrgId.FName"));
//                        kingdeeReturnOrderEntity.setFCustomerID(valMap.get("FCustomerID"));
//                        kingdeeReturnOrderEntity.setFCustomerName(valMap.get("FCustomerID.FName"));
//                        kingdeeReturnOrderEntity.setFSaleDeptName(valMap.get("FSaleDeptID.FName"));
//                        kingdeeReturnOrderEntity.setFSalesManID(valMap.get("FSalesManID"));
//                        kingdeeReturnOrderEntity.setFSalesManName(valMap.get("FSalesManID.FName"));
//                        kingdeeReturnOrderEntity.setFReceiverName(valMap.get("FReceiverID.FName"));
//                        kingdeeReturnOrderEntity.setFTransferBizTypeName(valMap.get("FTransferBizType.FName"));
//                        kingdeeReturnOrderEntity.setF_ulz_BaseProperty2(valMap.get("F_ulz_BaseProperty2"));
//                        kingdeeReturnOrderEntity.setFLinkPhone(valMap.get("FLinkPhone"));
//                        kingdeeReturnOrderEntity.setFLinkMan(valMap.get("FLinkMan"));
//                        kingdeeReturnOrderEntity.setFBussinessType(valMap.get("FBussinessType"));
//                        kingdeeReturnOrderEntity.setFDocumentStatus(valMap.get("FDocumentStatus"));
//                        kingdeeReturnOrderEntity.setFNote(valMap.get("FNote"));
//                        kingdeeReturnOrderEntity.setFReceiveAddress(valMap.get("FReceiveAddress"));
//                        kingdeeReturnOrderEntity.setFCreatorName(valMap.get("FCreatorId.FName"));
//                        kingdeeReturnOrderEntity.setFCreateDate(valMap.get("FCreateDate"));
//                        kingdeeReturnOrderEntity.setFModifierName(valMap.get("FModifierId.FName"));
//                        kingdeeReturnOrderEntity.setFModifyDate(valMap.get("FModifyDate"));
//                        kingdeeReturnOrderEntity.setFApproverName(valMap.get("FApproverID.FName"));
//                        kingdeeReturnOrderEntity.setFApproveDate(valMap.get("FApproveDate"));
//                        kingdeeReturnOrderEntity.setFCancelStatus(valMap.get("FCancelStatus"));
//                        kingdeeReturnOrderEntity.setFGYDATE(valMap.get("FGYDATE"));
//                        kingdeeReturnOrderEntity.setFLogisticsNos(valMap.get("FLogisticsNos"));
//                        kingdeeReturnOrderEntity.setF_ulz_Text3(valMap.get("F_ulz_Text3"));
//                        kingdeeReturnOrderEntity.setFSettleCurrCode(valMap.get("FSettleCurrID.FCode"));
//                        kingdeeReturnOrderEntity.setFExchangeRate(valMap.get("FExchangeRate"));
//
//                        //商品信息
//                        KingdeeDeliveryDetailItemEntity itemEntity = new KingdeeDeliveryDetailItemEntity();
//                        itemEntity.setFBillNo(valMap.get("FBillNo"));
//                        itemEntity.setFSrcBillNo(valMap.get("FSrcBillNo"));
//                        itemEntity.setF_ulz_BaseProperty1(valMap.get("F_ulz_BaseProperty1"));
//                        itemEntity.setFCustMatID(valMap.get("FCustMatID"));
//                        itemEntity.setFCustMatName(valMap.get("FCustMatName"));
//                        itemEntity.setFMaterialID(valMap.get("FMaterialID"));
//                        itemEntity.setFMaterialNumber(valMap.get("FMaterialID.FNumber"));
//                        itemEntity.setFMaterialName(valMap.get("FMaterialID.FName"));
//                        itemEntity.setFBarcode(valMap.get("FBarcode"));
//                        itemEntity.setFMateriaModel(valMap.get("FMateriaModel"));
//                        itemEntity.setFMateriaType(valMap.get("FMateriaType"));
//                        itemEntity.setFRealQty(valMap.get("FRealQty"));
//                        itemEntity.setFUnitName(valMap.get("FUnitID.FName"));
//                        itemEntity.setFPrice(valMap.get("FPrice"));
//                        itemEntity.setFIsFree(valMap.get("FIsFree"));
//                        itemEntity.setFArrivalStatus(valMap.get("FArrivalStatus"));
//                        itemEntity.setFArrivalDate(valMap.get("FArrivalDate"));
//                        itemEntity.setFAmount(valMap.get("FAmount"));
//                        itemEntity.setFStockStatusID(valMap.get("FStockStatusID"));
//                        itemEntity.setFStockStatusName(valMap.get("FStockStatusID.FName"));
//                        itemEntity.setFStockName(valMap.get("FStockID.FName"));
//                        itemEntity.setF_ulz_Text1(valMap.get("F_ulz_Text1"));
//                        itemEntity.setFEntryCostAmount(valMap.get("FEntryCostAmount"));
//                        itemEntity.setFEntrynote(valMap.get("FEntrynote"));

//                        KingdeeDeliveryDetailEntity entity = infoArrayList.stream().filter(a -> a.getFBillNo().equals(valMap.get("FBillNo")) && a.getFSoorDerno().equals(valMap.get("FSoorDerno"))).findFirst().orElse(null);
//                        if (entity != null) {
//                            entity.getKingdeeOutStockItemEntityList().add(itemEntity);
//                        } else {
//                            List<KingdeeDeliveryDetailItemEntity> outStockItemEntityList = new ArrayList();
//                            outStockItemEntityList.add(itemEntity);
//                            kingdeeReturnOrderEntity.setKingdeeOutStockItemEntityList(outStockItemEntityList);
//                            infoArrayList.add(kingdeeReturnOrderEntity);
//                        }
                    }
                    delete(client,formId,numberList);
//                    unAudit(client, formId, numberList);

                } else {
                    dataSign = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                dataSign = false;
            }
            pageIndex++;
        }

    }

    private static void unAudit(K3CloudApi client, String formId, List<String> numberList) throws Exception {
        OperateParam params =new OperateParam();
        params.setNumbers(numberList);
        OperatorResult operatorResult= client.unAudit(formId,params);
        System.out.println(operatorResult.isSuccessfully()+":"+operatorResult.getResult().toString());
        System.out.println("unAudit finished "+numberList.size());
    }
    private static void delete(K3CloudApi client, String formId, List<String> numberList) throws Exception {
        OperateParam params =new OperateParam();
        params.setNumbers(numberList);
        OperatorResult operatorResult= client.delete(formId,params);
        System.out.println(operatorResult.isSuccessfully()+":"+operatorResult.getResult().toString());
        System.out.println("delete finished "+numberList.size());
    }
}
