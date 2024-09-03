package com.erp.sdk.oms.amz.spapi.api.extend;

import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundPlanSummary;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.ListInboundPlansResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * FBA扩展APi
 *
 * @author Jim
 * @date 2024/9/3 11:41
 */
public class FbaInboundExtendApi extends FbaInboundApi {


    public FbaInboundExtendApi(ApiClient apiClient) {
        super(apiClient);
    }


    /**
     * 分页查询所有入库计划
     * Provides a list of inbound plans with minimal information.  **Usage Plan:**  | Rate (requests per second) | Burst | | ---- | ---- | | 2 | 6 |  The &#x60;x-amzn-RateLimit-Limit&#x60; response header returns the usage plan rate limits that were applied to the requested operation, when available. The table above indicates the default rate and burst values for this operation. Selling partners whose business demands require higher throughput may see higher rate and burst values than those shown here. For more information, refer to [Usage Plans and Rate Limits in the Selling Partner API](https://developer-docs.amazon.com/sp-api/docs/usage-plans-and-rate-limits-in-the-sp-api).
     *
     * @param status    The status of an inbound plan. (optional)
     * @param sortBy    Sort by field. (optional)
     * @param sortOrder The sort order. (optional)
     * @return ApiResponse&lt;ListInboundPlansResponse&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @throws LWAException If calls to fetch LWA access token fails
     */
    public List<InboundPlanSummary> allListInboundPlansWithHttpInfo(String status, String sortBy, String sortOrder) throws ApiException, LWAException {
        Integer pageSize = 30;
        String paginationToken = null;
        ApiResponse<ListInboundPlansResponse> apiResponse = super.listInboundPlansWithHttpInfo(pageSize, paginationToken, status, sortBy, sortOrder);
        ListInboundPlansResponse data = apiResponse.getData();
        List<InboundPlanSummary> resultList = new LinkedList<>(data.getInboundPlans());
        paginationToken = data.getPagination().getNextToken();
        while (StringUtils.isNotBlank(paginationToken) ) {
            ApiResponse<ListInboundPlansResponse> curResp = super.listInboundPlansWithHttpInfo(pageSize, paginationToken, null, null, null);
            ListInboundPlansResponse curData = curResp.getData();
            paginationToken = curData.getPagination().getNextToken();
            resultList.addAll(curData.getInboundPlans());
        }
        return resultList;
    }

}
