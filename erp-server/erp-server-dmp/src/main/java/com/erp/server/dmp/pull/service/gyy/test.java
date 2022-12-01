package com.erp.server.dmp.pull.service.gyy;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.kingdee.bos.webapi.entity.QueryParam;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class test {

    public static void main(String[] args) throws Exception {
        //读取配置，初始化SDK
        K3CloudApi client = new K3CloudApi();
        //用于记录结果
        StringBuilder Info = new StringBuilder();
        //业务对象标识
        String formId = "SAL_SaleOrder";

        String currency = "FNumber,FName,FCODE,FPRICEDIGITS,FAMOUNTDIGITS,FPRIORITY,FIsShowCSymbol";

        LinkedList<String> queryfilters = new LinkedList<>();
        queryfilters.add(String.format("FModifyDate >= '%s'", "2022-06-27 00:00:00"));
        queryfilters.add(String.format("FModifyDate <= '%s'", "2022-06-27 23:59:59"));

        String filterStr = String.join(" and ",  queryfilters );

//FSaleOrderEntry
//查询字段集合，即返回哪些数据，不能为空，根据不同业务单据填写不同的字段名，以下仅为示例
        String fieldKeys = "fCancelStatus.Fname,FExchangeRate,FSettleCurrId,FID,FBillNo,FDate,FBillTypeID,FDocumentStatus,FCustId,FSaleDeptId,FSalerId,FReceiveAddress,FLinkMan,FLinkPhone,FApproverId,FApproveDate,FCloseStatus,FCancelStatus,FChangerId,FReceiveId,FNote,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId,FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifyDate,FSaleOrgId,FVersionNo,FSignStatus,FSOFrom,F_SHGJ1," +
                "FReturnType,FRowType,FMaterialName,FMaterialGroup,FMaterialId,FMaterialModel,FQty,FPriceUnitQty,FUnitID,FAuxPropId,FPrice,FEntryTaxRate,FTaxPrice,FIsFree,FEntryTaxAmount,FMaterialType,FAmount,FBarcode,FMapName,F_ulz_BaseProperty,FMapId,FBaseUnitId,FOldQty,FTaxNetPrice,FDiscount,FPriceDiscount,FBranchId,FEntryNote,FSrcType,FSrcBillNo,FMinPlanDeliveryDate,FDeliveryStatus";

        String test = "FSettleCurrId.FName,FSOStockId,FSOStockLocalId,FExchangeRate,FSettleCurrId,FID,FBillNo,FDate,FBillTypeID,FDocumentStatus,FCustId,FSaleDeptId,FSalerId,FReceiveAddress,FLinkMan,FLinkPhone,FApproverId,FApproveDate,FCloseStatus,FCancelStatus,FChangerId,FReceiveId,FNote,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId,FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifyDate,FSaleOrgId,FVersionNo,FSignStatus,FSOFrom,F_SHGJ1," +
                "";

        //请求参数，示例使用的是SDK提供的模板类，还可以使用字符串拼接等方式
        QueryParam param = new QueryParam();

        param.setFormId(formId);
        param.setFieldKeys(test);
        //param.setFilterString(filterStr);
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
            e.printStackTrace();
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
            e.printStackTrace();
        }

    }
}
