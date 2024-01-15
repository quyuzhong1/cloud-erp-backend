package com.sdk.tms.baohong.service;

import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class testClient {

    public static void main(String[] args) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("test1", "1");
        paramMap.put("name", "张三");
        paramMap.put("age", "18");
        String path = "http://exoms.globex.cn/default/order-soap/web-service";
        String method = "getShippingMethodList";
        String customerCode = "E0207";
        String appToken = "630ACC115898BB5B";
        String appKey = "dd7c2bf37aec54d597c850e0b0f0d19d";
        Map<String, Object> objectMap = convertReqUtil(paramMap, path, method, customerCode, appToken, appKey);

    }


    /**
     * 功能描述:  发起通用请求
     *
     * @param paramMap 参数 map
     * @param thisPath 请求路径
     * @return : org.springblade.core.tool.api.R<cn.hutool.json.JSONObject>
     * @author : yzd e-mail: 121665820@qq.com
     * @create : 2023/6/19 11:17
     */

    public static Map<String, Object> convertReqUtil(Map<String, Object> paramMap,
                                                     String thisPath,
                                                     String method,
                                                     String customerCode,
                                                     String appToken,
                                                     String appKey) {
        if (ObjectUtils.isEmpty(paramMap)) {
            log.error("接口调用 path: {},param：{}", thisPath, paramMap);
            throw new ServiceException("请求参数不能为空");
        }
        Document document = XmlUtil.mapToXml(paramMap, "Request", "http://www.example.org/ServiceForOrder/web-service");
        String xmlStr = XmlUtil.toStr(document);
        log.info("接口调用 path: {} ", thisPath);
        log.info("接口调用 param：{}", xmlStr);
        HttpRequest post = HttpUtil.createPost(thisPath);
        post.header(HttpHeaders.CONTENT_TYPE, "text/xml");
        post.header("customerCode", "E0207");
        post.header("appToken", "630ACC115898BB5B");

        String stringBuffer = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope\n" +
                "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "xmlns:ns1=\"http://www.example.org/ServiceForOrder/\">\n" +
                "<SOAP-ENV:Body>\n" +
                "<ns1:getShippingMethodList>\n" +
                    "<HeaderRequest>\n" +
                        "<customerCode>E0207</customerCode>\n" +
                        "<appToken>630ACC115898BB5B</appToken>\n" +
                        "<appKey>dd7c2bf37aec54d597c850e0b0f0d19d</appKey>\n" +
                    "</HeaderRequest>\n" +
                    "<page>1</page>\n" +
                    "<pageSize>1</pageSize>\n" +
                "</ns1:getShippingMethodList>\n" +
                "</SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
        post.body(stringBuffer);
        try {
            HttpResponse execute = post.execute();
            String trim = execute.body().trim();
            log.info("接口调用 resp: {}", trim);
            return XmlUtil.xmlToMap(trim);
        } catch (Exception e) {
            log.error("接口调用 error:", e);
            throw new ServiceException("服务器异常,请联系管理员");
        }
    }
}
