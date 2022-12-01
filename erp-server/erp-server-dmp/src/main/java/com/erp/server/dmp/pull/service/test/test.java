package com.erp.server.dmp.pull.service.test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.server.dmp.constant.UrlContant;
import com.erp.server.dmp.entity.dmp.DmpErrorLogEntity;
import com.erp.server.dmp.entity.dmp.GyyAppEntity;
import com.erp.server.dmp.entity.dto.JobTaskDTO;
import com.erp.server.dmp.entity.dto.RequestDTO;
import com.erp.server.dmp.entity.gyy.GyyOrderEntity;
import com.erp.server.dmp.utils.GyyUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import sun.misc.BASE64Decoder;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class test {

	/**
	 * 请求管易云订单接口
	 * @param dto
	 * @return
	 */
	public void pullDate(RequestDTO dto) {
		HttpCommonUtil httpCommonUtil = new HttpCommonUtil();
		String FormId = "SAL_SaleOrder";
		String FieldKeys = "FID,FBillNo,FDate,FBillTypeID,FDocumentStatus,FCustId,FSaleDeptId,FSalerId,FReceiveAddress,FLinkMan,FLinkPhone,FApproverId,FApproveDate,FCloseStatus,FCancelStatus,FChangerId,FReceiveId,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId,FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifyDate,FSaleOrgId,FVersionNo,FSignStatus,FSOFrom," +
				"FReturnType,FRowType,FMaterialName,FMaterialGroup,FMaterialId,FMaterialModel,FQty,FPriceUnitQty,FUnitID,FAuxPropId,FPrice,FEntryTaxRate,FTaxPrice,FIsFree,FEntryTaxAmount,FMaterialType,FAmount,FBarcode,FMapName,FMapId";

		JSONObject jParas = new JSONObject();
		jParas.put("FormId", FormId);
		jParas.put("FieldKeys", FieldKeys);

		String jsonData = JSONObject.toJSONString(jParas);

		//设置请求头
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("Content-Type", "text/json");

		Map<String, Object> stringObjectMap = null;
		try {
			stringObjectMap = httpCommonUtil.sendOkhttp("http://47.106.224.95:8089/k3cloud/html5/index.aspx?ud=e2RiaWQ6JzYwNDU3OGE0YTU0YTZmJyx1c2VybmFtZTon556/6IKy5b+gJyxhcHBpZDonMjM3NDk2XzAxNnA0Ymp0M3FBL1M0WHYyWjByNzcrTTVONzgxQU1vJyxzaWduZWRkYXRhOidjOWQyYjJlZDk5OTBhYzI0NTlkYTdhNDQzN2I2ODA5MDk0ZDU1NjhkJyx0aW1lc3RhbXA6JzE2Njk2OTMzNzInLGxjaWQ6JzIwNTInLG9yaWdpbnR5cGU6J3NpbXBhcyd9",
					jsonData, null, headerMap, RequestMethod.POST);

		} catch (Exception e) {

		}
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		//请求参数（json格式）：{"dbid":"604578a4a54a6f","username":"瞿育忠","appid":"237496_016p4bjt3qA/S4Xv2Z0r77+M5N781AMo","signeddata":"cfd8d46160762833e4539f386071c3da24dd580f","timestamp":"1668231181","lcid":"2052","origintype":"SimPas","entryrole":"","formid":"","formtype":"","pkid":"","otherargs":"|{'permitcount':'0'}","openmode":null}
		Long local = System.currentTimeMillis()/1000L;
		String name = "瞿育忠";
		System.out.println();
		String dbId = "604578a4a54a6f"; //数据中心ID
		String usserName = "瞿育忠"; //用户名称
		String appId = "237496_016p4bjt3qA/S4Xv2Z0r77+M5N781AMo"; //第三方系统应用Id
		String appSecret = "46a860e5bcd144848bc9394c9c4dfbfa"; //第三方系统应用秘钥
		long currentTime=System.currentTimeMillis()/1000; // 当前时间（秒）
		String timestamp=Long.toString(currentTime);
		String[] strArray ={dbId, usserName, appId, appSecret,timestamp};
		Output("Before Sigend",strArray);

		//签名字符串数组需要排序，后生成签名
		Arrays.sort(strArray);
		Output("After Sigend",strArray);
		String combStr=null;
		for(int i=0;i<strArray.length;i++) {
			if(combStr==null || combStr==""){
				combStr = strArray[i];
			}else{
				combStr= combStr+strArray[i];
			}
		}

		byte[] strByte = combStr.getBytes("UTF-8");
		Output("utf8 bytes",strByte);
		byte[] strSign = DigestUtils.sha(strByte);
		String sign = bytesToHexString(strSign);
		Output("signedData",sign);
		String urlPara = String.format(
				"{dbid:'%s',username:'%s',appid:'%s',signeddata:'%s',timestamp:'%s',lcid:'%s',origintype:'simpas'}",
				dbId, usserName, appId, sign, timestamp, "2052");
		Output("urlPara",urlPara);
				// 金蝶云星空入口链接 urlEncode
				Output("urlEncode","http://47.106.224.95:8089/k3cloud/html5/index.aspx?ud="
						+ java.net.URLEncoder.encode(urlPara, "utf-8"));

		// 金蝶云星空入口链接 Base64
		//import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
		Output("base64","http://47.106.224.95:8089/k3cloud/html5/index.aspx?ud="
				+ Base64.encode(urlPara.getBytes("UTF-8")));

		// 默认浏览器打开url
            /*
            try{
                //创建一个URI实例,注意不是URL
                java.net.URI uri=java.net.URI.create(url);
                //获取当前系统桌面扩展
                java.awt.Desktop dp=java.awt.Desktop.getDesktop();
                //判断系统桌面是否支持要执行的功能
                if(dp.isSupported(java.awt.Desktop.Action.BROWSE)){
                    //获取系统默认浏览器打开链接
                    dp.browse(uri);
                }
            }catch(java.lang.NullPointerException e){
                //此为uri为空时抛出异常
            }catch(java.io.IOException e){
                //此为无法获取系统默认浏览器
            }*/

	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static void Output(String title, byte[] ar) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ar.length; i++) {
			sb.append(ar[i] + ",");
		}
		Output(title, sb.toString());
	}

	public static void Output(String title, String[] ar) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ar.length; i++) {
			sb.append(ar[i] + "|");
		}
		Output(title, sb.toString());
	}

	public static void Output(String title, String msg) {
		System.out.println(title + ":\t\t\t\t" + msg);
	}

}
