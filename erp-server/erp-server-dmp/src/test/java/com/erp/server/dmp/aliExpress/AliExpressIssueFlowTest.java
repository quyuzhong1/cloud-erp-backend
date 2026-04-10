package com.erp.server.dmp.aliExpress;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.pull.mongo.MongoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class AliExpressIssueFlowTest {

    private static final String SHOP_ID = "1740195030494089218";
    private static final String START_TIME = "2026-03-26 00:00:00";
    private static final String END_TIME = "2026-03-26 23:59:59";

    @Resource
    private MongoService mongoService;
    @Resource
    private AliExpressOrderService aliExpressOrderService;

    @Test
    public void queryIssueListLikeDmp() throws Exception {
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData("nextLevelId", "nextLevelId", PannoEnum.EQ, SHOP_ID));
        paramDataList.add(new ParamData("mongoUpdateTime", "mongoUpdateTime", PannoEnum.GTE, START_TIME));
        paramDataList.add(new ParamData("mongoUpdateTime", "mongoUpdateTime", PannoEnum.LTE, END_TIME));

        List<Map<String, Object>> orderMongoList = mongoService.findMongoData(paramDataList, "aliexpress_order_data");
        log.warn("速卖通纠纷测试: parent mongo 数量={}", orderMongoList.size());

        Map<String, String> orderIssueStatusMap = new LinkedHashMap<>();
        for (Map<String, Object> orderMongo : orderMongoList) {
            Object productListObj = orderMongo.get("product_list");
            if (!(productListObj instanceof List)) {
                continue;
            }
            List<Map<String, Object>> productList = (List<Map<String, Object>>) productListObj;
            for (Map<String, Object> product : productList) {
                Object childId = product.get("child_id");
                Object issueStatusObj = product.get("issue_status");
                if (childId == null || issueStatusObj == null) {
                    continue;
                }
                String issueStatus = issueStatusObj.toString();
                if ("IN_ISSUE".equals(issueStatus) || "END_ISSUE".equals(issueStatus)) {
                    orderIssueStatusMap.put(childId.toString(), issueStatus);
                }
            }
        }

        log.warn("速卖通纠纷测试: issue 候选订单数={}", orderIssueStatusMap.size());
        if (orderIssueStatusMap.isEmpty()) {
            return;
        }

        AliExpressShopInfoDTO shopInfoDTO = aliExpressOrderService.getShopInfoByShopId(SHOP_ID);
        IopClient client = new IopClientImpl(shopInfoDTO.getBaseUrl(), shopInfoDTO.getClientId(), shopInfoDTO.getClientSecret());

        JSONArray allIssues = new JSONArray();
        for (Map.Entry<String, String> entry : orderIssueStatusMap.entrySet()) {
            Map<String, Object> queryDto = new HashMap<>();
            queryDto.put("current_page", 1);
            queryDto.put("page_size", 50);
            queryDto.put("order_no", entry.getKey());
            if ("END_ISSUE".equals(entry.getValue())) {
                queryDto.put("issue_status", "finish");
            }

            IopRequest request = new IopRequest();
            request.setApiName("aliexpress.issue.issuelist.get");
            request.addApiParameter("query_dto", JSON.toJSONString(queryDto));
            IopResponse response = client.execute(request, shopInfoDTO.getToken(), Protocol.TOP);

            JSONObject body = JSON.parseObject(response.getBody());
            JSONObject data = body.getJSONObject("aliexpress_issue_issuelist_get_response");
            JSONArray issueArray = null;
            if (data != null) {
                JSONObject dataList = data.getJSONObject("data_list");
                if (dataList != null) {
                    issueArray = dataList.getJSONArray("issue_api_issue_dto");
                }
            }

            log.warn("速卖通纠纷测试: orderNo={}, issueStatus={}, request={}, response={}",
                    entry.getKey(), entry.getValue(), JSON.toJSONString(queryDto), response.getBody());

            if (issueArray != null) {
                allIssues.addAll(issueArray);
            }
        }

        log.warn("速卖通纠纷测试: issueList 总数={}", allIssues.size());
        System.out.println(allIssues.toJSONString());
    }

    @Test
    public void queryIssueDetail() throws Exception {
        String buyerLoginId = "";
        String issueId = "";
        String channelSellerId = "";

        AliExpressShopInfoDTO shopInfoDTO = aliExpressOrderService.getShopInfoByShopId(SHOP_ID);
        IopClient client = new IopClientImpl(shopInfoDTO.getBaseUrl(), shopInfoDTO.getClientId(), shopInfoDTO.getClientSecret());

        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.issue.detail.get");
        request.addApiParameter("buyer_login_id", buyerLoginId);
        request.addApiParameter("issue_id", issueId);
        if (channelSellerId != null && !channelSellerId.trim().isEmpty()) {
            request.addApiParameter("channel_seller_id", channelSellerId);
        }

        IopResponse response = client.execute(request, shopInfoDTO.getToken(), Protocol.TOP);
        log.warn("速卖通纠纷详情测试: buyerLoginId={}, issueId={}, response={}", buyerLoginId, issueId, response.getBody());
        System.out.println(response.getBody());
    }
}
