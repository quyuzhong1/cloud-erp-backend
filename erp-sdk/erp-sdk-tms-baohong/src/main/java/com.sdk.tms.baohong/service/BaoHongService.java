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

}
