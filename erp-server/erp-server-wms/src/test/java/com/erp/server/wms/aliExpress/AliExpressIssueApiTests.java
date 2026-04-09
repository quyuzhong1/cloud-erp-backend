package com.erp.server.wms.aliExpress;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.enums.Protocol;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AliExpressIssueApiTests {

    private static final String APP_KEY = "";
    private static final String APP_SECRET = "";
    private static final String BASE_URL = "https://api-sg.aliexpress.com";
    private static final String TOKEN = "";

    @Test
    public void queryIssueListByOrderNo() throws Exception {
        String orderNo = "";
        String issueStatus = "";

        IopClient client = new IopClientImpl(BASE_URL, APP_KEY, APP_SECRET);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.issue.issuelist.get");

        Map<String, Object> queryDto = new HashMap<>();
        queryDto.put("current_page", 1);
        queryDto.put("page_size", 50);
        queryDto.put("order_no", orderNo);
        if (issueStatus != null && !issueStatus.trim().isEmpty()) {
            queryDto.put("issue_status", issueStatus);
        }

        request.addApiParameter("query_dto", JSONUtil.toJsonStr(queryDto));
        IopResponse response = client.execute(request, TOKEN, Protocol.TOP);

        System.out.println("request.query_dto = " + JSONUtil.toJsonStr(queryDto));
        System.out.println("response.body = " + response.getBody());
    }

    @Test
    public void queryIssueDetail() throws Exception {
        String buyerLoginId = "";
        String issueId = "";
        String channelSellerId = "";

        IopClient client = new IopClientImpl(BASE_URL, APP_KEY, APP_SECRET);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.issue.detail.get");
        request.addApiParameter("buyer_login_id", buyerLoginId);
        request.addApiParameter("issue_id", issueId);
        if (channelSellerId != null && !channelSellerId.trim().isEmpty()) {
            request.addApiParameter("channel_seller_id", channelSellerId);
        }

        IopResponse response = client.execute(request, TOKEN, Protocol.TOP);
        System.out.println("request.buyer_login_id = " + buyerLoginId);
        System.out.println("request.issue_id = " + issueId);
        System.out.println("response.body = " + response.getBody());

        JSONObject body = JSONUtil.parseObj(response.getBody());
        System.out.println("pretty.body = " + body.toStringPretty());
    }
}
