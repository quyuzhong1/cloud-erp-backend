package com.sdk.oms.walmart.api;

import jodd.util.StringUtil;
import org.apache.commons.codec.binary.Base64;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @Author Luo_WG
 * @Date 2023/10/17 14:12
 **/
public class WalmartSign {
	private String consumerId = ""; // 8e8dd2f3-bfc5-4e14-bf68-69b136e421a1
	private String baseUrl = "";
	private String privateEncodedStr = ""; // Trimmed for security reasons
	private String timestamp = String.valueOf(System.currentTimeMillis());
	
	public WalmartSign(String consumerId, String baseUrl, String privateEncodedStr) {
		this.consumerId = consumerId;
		this.baseUrl = baseUrl;
		this.privateEncodedStr = privateEncodedStr;
	}
	
	public static void main(String[] args) {
		String httpMethod = "GET";
		String consumerId = "";
		String baseUrl = "";
		String privateEncodedStr = "";
		WalmartSign walmartSign = new WalmartSign(consumerId, baseUrl, privateEncodedStr);
		System.out.println(walmartSign.getSign(httpMethod));
	}
	
	public String getSign(String httpMethod){
		if(StringUtil.isBlank(httpMethod)) {
			httpMethod = "GET";
		}
		String stringToSign = consumerId + "\n" + baseUrl + "\n" + httpMethod + "\n" + timestamp + "\n";
		System.out.println(stringToSign);
		return signData(stringToSign, privateEncodedStr);
	}
	
	private String signData(String stringToBeSigned, String encodedPrivateKey) {
		StringBuffer signatureString = new StringBuffer();
		try {
			byte[] encodedKeyBytes = Base64.decodeBase64(encodedPrivateKey);
			PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(encodedKeyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey myPrivateKey = kf.generatePrivate(privSpec);
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(myPrivateKey);
			byte[] data = stringToBeSigned.getBytes("UTF-8");
			signature.update(data);
			byte[] signedBytes = signature.sign();
			signatureString.append(Base64.encodeBase64String(signedBytes));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return signatureString.toString();
	}

	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getPrivateEncodedStr() {
		return privateEncodedStr;
	}

	public void setPrivateEncodedStr(String privateEncodedStr) {
		this.privateEncodedStr = privateEncodedStr;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
