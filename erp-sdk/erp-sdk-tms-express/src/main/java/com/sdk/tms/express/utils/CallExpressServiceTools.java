package com.sdk.tms.express.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
/**
 * @author zdy
 * @ClassName CallExpressServiceTools

 * @date 2023年10月30日
 * @version: 1.0
 */
public class CallExpressServiceTools {


    private CallExpressServiceTools() {}

    private static CallExpressServiceTools tools = new CallExpressServiceTools();

    public static CallExpressServiceTools getInstance() {
        synchronized (CallExpressServiceTools.class) {
            if (tools == null)
                tools = new CallExpressServiceTools();
        }
        return tools;
    }

    public static String callSfExpressServiceByCSIM(String reqURL, String reqXML, String clientCode, String checkword) {
        String result = null;
        String verifyCode = VerifyCodeUtil.md5EncryptAndBase64(String.valueOf(reqXML) + checkword);
        result = querySFAPIservice(reqURL, reqXML, verifyCode);
        return result;
    }

    public static String querySFAPIservice(String url, String xml, String verifyCode) {
        HttpClientUtil httpclient = new HttpClientUtil();
        if (url == null)
            url = "http://bsp-oisp.sf-express.com/bsp-oisp/sfexpressService";
        String result = null;
        try {
            result = httpclient.postSFAPI(url, xml, verifyCode);
            return result;
        } catch (Exception e) {
            return null;
        }
    }


    public static String getMsgDigest(String msgData, String timeStamp, String md5Key) throws UnsupportedEncodingException {
        String msgDigest = VerifyCodeUtil.md5EncryptAndBase64(URLEncoder.encode(String.valueOf(msgData) + timeStamp + md5Key, "UTF-8"));
        return msgDigest;
    }
}
