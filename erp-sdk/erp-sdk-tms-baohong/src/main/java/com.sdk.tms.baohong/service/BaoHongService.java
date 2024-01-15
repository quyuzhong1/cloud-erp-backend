package com.sdk.tms.baohong.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.sdk.tms.baohong.org.example.webservice.agency.GetShippingMethodList;
import com.sdk.tms.baohong.org.example.webservice.agency.HeaderRequest;
import com.sdk.tms.baohong.org.example.webservice.agency.ObjectFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BaoHongService {


    public static void main(String[] args) {
        GetShippingMethodList methodList = new GetShippingMethodList();
        HeaderRequest headerRequest = new HeaderRequest();
        headerRequest.setAppKey("dd7c2bf37aec54d597c850e0b0f0d19d");
        headerRequest.setAppToken("630ACC115898BB5B");
        headerRequest.setCustomerCode("E0207");
        methodList.setHeaderRequest(headerRequest);
        methodList.setPage(1);
        methodList.setPageSize(100);
        methodList.setHeaderRequest(headerRequest);
        System.out.println(methodList);

        ObjectFactory factory = new ObjectFactory();
        GetShippingMethodList getShippingMethodList = factory.createGetShippingMethodList();
        System.out.println(getShippingMethodList);
    }


    /**
     * 功能描述:  发起通用请求
     *
     * @param paramMap 参数 map
     * @param thisPath 请求路径
     * @param actionNo actionNo
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
        Document document = XmlUtil.mapToXml(paramMap, "Request", "http://www.example.org/ServiceForOrder/");
        String xmlStr = XmlUtil.toStr(document);
        log.info("接口调用 path: {} ", thisPath);
        log.info("接口调用 param：{}", xmlStr);
        HttpRequest post = HttpUtil.createPost(thisPath);
        post.header(HttpHeaders.CONTENT_TYPE, "text/xml");
        post.header("customerCode", "E0207");
        post.header("appToken", "630ACC115898BB5B");
        post.header("appKey", "dd7c2bf37aec54d597c850e0b0f0d19d");
/*        String stringBuffer = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:dhcc=\"http://www.dhcc.com.cn\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <dhcc:HIPMessageService>\n" +
                "         <!--Optional:-->\n" +
                "         <dhcc:Action>" + actionNo + "</dhcc:Action>\n" +
                "         <!--Optional:-->\n" +
                "         <dhcc:Data><![CDATA[" + xmlStr +
                "]]></dhcc:Data>\n" +
                "      </dhcc:HIPMessageService>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";*/

/*        String stringBuffer = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://www.example.org/Ecg/\">\n" +
                "<SOAP-ENV:Body>\n" +
                    "<ns1:" + method + ">\n" +
                        "<HeaderRequest>\n" +
                            "<customerCode>" + customerCode + "</customerCode>\n" +
                            "<appToken>" + appToken + "</appToken>\n" +
                            "<appKey>" + appKey + "</appKey>\n" +
                        "</HeaderRequest>\n" +
                        "" + xmlStr + "\n" +
                    "</ns1:" + method + ">\n" +
                "</SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";*/

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

    public static void test() {
        try {
            String urlStr = "http://exoms.globex.cn/default/order-soap/";
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            con.setRequestProperty("customerCode", "E0207");
            con.setRequestProperty("appToken", "630ACC115898BB5B");
            con.setRequestProperty("appKey", "dd7c2bf37aec54d597c850e0b0f0d19d");
            OutputStream oStream = con.getOutputStream();
            //下面这行代码是用字符串拼出要发送的xml，xml的内容是从测试软件里拷贝出来的
            //需要注意的是，有些空格不要弄丢哦，要不然会报500错误的。
            //参数什么的，你可以封装一下方法，自动生成对应的xml脚本
            String soap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<SOAP-ENV:Envelope\n" +
                    "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    xmlns:ns1=\"http://www.example.org/ServiceForOrder/\">\n" +
                    "    <SOAP-ENV:Body>\n" +
                    "        <ns1:getShippingMethodList>\n" +
                    "            <HeaderRequest>\n" +
                    "                <customerCode>E0207</customerCode>\n" +
                    "                <appToken>630ACC115898BB5B</appToken>\n" +
                    "                <appKey>dd7c2bf37aec54d597c850e0b0f0d19d</appKey>\n" +
                    "            </HeaderRequest>\n" +
                    "            <page>1</page>\n" +
                    "            <pageSize>1</pageSize>\n" +
                    "        </ns1:getShippingMethodList>\n" +
                    "    </SOAP-ENV:Body>\n" +
                    "</SOAP-ENV:Envelope>";
            oStream.write(soap.getBytes());
            oStream.close();

            InputStream iStream = con.getInputStream();
            Reader reader = new InputStreamReader(iStream);

            int tempChar;
            String str = new String();
            while((tempChar = reader.read()) != -1){
                str += (char) tempChar;
            }
            //下面这行输出返回的xml到控制台，相关的解析操作大家自己动手喽。
            //如果想要简单的话，也可以用正则表达式取结果出来。
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>returnedxmlstr:"+str);
            iStream.close();
            oStream.close();
            con.disconnect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String ncgConnection(String url,String method){
        URL wsUrl;
        int errCode=0;
        JSONObject resultJson=new JSONObject();
        String result="";
        try {
            wsUrl = new URL(url+"/"+method);
            HttpURLConnection conn = (HttpURLConnection) wsUrl.openConnection();

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            conn.setRequestProperty("customerCode", "E0207");
            conn.setRequestProperty("appToken", "630ACC115898BB5B");
            conn.setRequestProperty("appKey", "dd7c2bf37aec54d597c850e0b0f0d19d");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            OutputStream os = conn.getOutputStream();
            //请求体

            //<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><soapenv:Body><ns1:DeleteCascadeFromCms soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:ns1="urn:ncg"><ncg-code-list xsi:type="xsd:string">["11241525"]</ncg-code-list></ns1:DeleteCascadeFromCms></soapenv:Body></soapenv:Envelope>

            String soap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<SOAP-ENV:Envelope\n" +
                    "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    xmlns:ns1=\"http://www.example.org/ServiceForOrder/\">\n" +
                    "    <SOAP-ENV:Body>\n" +
                    "        <ns1:getShippingMethodList>\n" +
                    "            <HeaderRequest>\n" +
                    "                <customerCode>E0207</customerCode>\n" +
                    "                <appToken>630ACC115898BB5B</appToken>\n" +
                    "                <appKey>dd7c2bf37aec54d597c850e0b0f0d19d</appKey>\n" +
                    "            </HeaderRequest>\n" +
                    "            <page>1</page>\n" +
                    "            <pageSize>1</pageSize>\n" +
                    "        </ns1:getShippingMethodList>\n" +
                    "    </SOAP-ENV:Body>\n" +
                    "</SOAP-ENV:Envelope>";
            os.write(soap.getBytes());
            InputStream is = conn.getInputStream();

            byte[] b = new byte[1024];
            int len = 0;
            String s = "";
            while((len = is.read(b)) != -1){
                String ss = new String(b,0,len,"UTF-8");
                s += ss;
            }
            result=s.split("<response xsi:type=\"xsd:string\">")[1].split("</response>")[0];

            is.close();
            os.close();
            conn.disconnect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("通讯模块1:"+e.getMessage());
            errCode=1;
        }
        resultJson.put("errCode", errCode);
        resultJson.put("data", result);

        return resultJson.toString();
    }
}
