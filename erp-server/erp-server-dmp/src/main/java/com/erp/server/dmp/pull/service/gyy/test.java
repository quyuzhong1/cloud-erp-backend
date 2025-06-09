package com.erp.server.dmp.pull.service.gyy;

import com.alibaba.fastjson.JSONObject;
import com.kingdee.bos.webapi.entity.QueryParam;
import com.kingdee.bos.webapi.sdk.K3CloudApi;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class test {

    public static void main(String[] args) throws Exception {
        //读取配置，初始化SDK
        K3CloudApi client = new K3CloudApi();
        //用于记录结果
        StringBuilder Info = new StringBuilder();
        //业务对象标识
        String formId = "SAL_SaleOrder";
        String formIdT = "BD_MATERIAL";
        String formIdS = "AR_REFUNDBILL";
        String fieldKey = "FSalePhaseID,FID,FBillNo,FDate,FBillTypeId.FName,FDocumentStatus,FCustId.FName,FSaleDeptId.FName,FSalerId.FName,FReceiveAddress,FLinkMan,FLinkPhone,FApproverId.FName,FApproveDate,FCloseStatus,FCloseDate,FCancelStatus,FChangerId,FReceiveId.FName,FNote,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId,FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifyDate,FSaleOrgId,FSaleOrgId.FName,FVersionNo,FSignStatus,FSOFrom,F_SK_Date,F_SHGJ1,FExchangeRate,FSettleCurrId.FCode";
        String fieldKeys = "FID,FBillTypeID,FBillTypeID.FName,FBillNo,FDate,FSaleOrgId,FSaleOrgId.FName,FCustomerID,FCustomerID.FName,FSalesManID,FSalesManID.FName,FSaleDeptID.FName,FReceiverID.FName,FTransferBizType.FName,F_ulz_BaseProperty2,FLinkPhone,FLinkMan,FBussinessType,FDocumentStatus,FNote,FReceiveAddress,FCreatorId.FName,FCreateDate,FModifierId.FName,FModifyDate,FApproverID.FName,FApproveDate,FCancelStatus,FGYDATE,FLogisticsNos,F_ulz_Text3,FSettleCurrID.FCode,FBillAmount,FExchangeRate";
        String fieldKeyst = "FSalCostPrice,FSoorDerno,FSrcBillNo,F_ulz_BaseProperty1,FCustMatID,FCustMatName,FMaterialID,FMaterialID.FNumber,FMaterialID.FName,FBarcode,FMateriaModel,FMateriaType,FRealQty,FUnitID.FName,FPrice,FIsFree,FArrivalStatus,FArrivalDate,FAmount,FStockStatusID,FStockStatusID.FName,FStockID.FName,F_ulz_Text1";
        String test = "FNumber,FErpClsID";
        LinkedList<String> queryfilters = new LinkedList<>();

        queryfilters.add(String.format("FNumber = '%s'", "0005"));
        queryfilters.add(String.format("FNumber = '%s'", "0011010001"));
        queryfilters.add(String.format("FNumber = '%s'", "0073"));
        queryfilters.add(String.format("FNumber = '%s'", "fw"));
        String filterStr = String.join(" or ",  queryfilters );

        //请求参数，示例使用的是SDK提供的模板类，还可以使用字符串拼接等方式
        QueryParam param = new QueryParam();

        param.setFormId(formIdT);
        param.setFieldKeys(test);
        param.setFilterString(filterStr);
        param.setLimit(10000);
        param.setStartRow(0);
        param.setTopRowCount(10000);
        String s = JSONObject.toJSONString(param);
        System.out.println(s);
        //String view = client.view(fieldKeys, s);
        String s2 = client.executeBillQueryJson(s);
        System.out.println(s2);

        List<List<Object>> s1 = client.executeBillQuery(s);
        for (List<Object> objects : s1) {
            System.out.println("第一个for：" + objects);
        }
    }

    /**
     * 简单MD5
     *
     * @param str
     * @return
     */
    public static String md5(String str) {

        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] array = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
    // 得到sign的字符串
    public static String sign2(String str, String secret) {
        StringBuilder enValue = new StringBuilder();
        enValue.append(secret);
        enValue.append(str);
        enValue.append(secret);
        return md5(enValue.toString());
    }

    // 得到sign的字符串
    public static String sign(String str, String secret) {
        StringBuilder enValue = new StringBuilder();
        enValue.append(secret);
        enValue.append(str);
        enValue.append(secret);
        return encryptByMD5(enValue.toString());
    }

    // MD5
    private static String encryptByMD5(String data) {
        StringBuilder sign = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(data.getBytes("UTF-8"));
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(bytes[i] & 0xFF);
                if (hex.length() == 1) {
                    sign.append("0");
                }
                sign.append(hex.toUpperCase());
            }
        } catch (Exception e) {
        	log.error("MD5错误" , e);
        }
        return sign.toString();
    }

    public static void sendPost(String url, String data) {
        try {
            CloseableHttpClient httpclient = null;
            CloseableHttpResponse httpresponse = null;
            try {
                httpclient = HttpClients.createDefault();
                HttpPost httppost = new HttpPost(url);
                StringEntity stringentity = new StringEntity(data,
                        ContentType.create("text/json", "UTF-8"));
                httppost.setEntity(stringentity);
                httpresponse = httpclient.execute(httppost);
                String response = EntityUtils.toString(httpresponse.getEntity());
                System.out.println("response: " + response);
            } finally {
                if (httpclient != null) {
                    httpclient.close();
                }
                if (httpresponse != null) {
                    httpresponse.close();
                }
            }
        } catch (Exception e) {
        	log.error("POST错误" , e);
        }

    }
}
