
package com.erp.server.oms.amz;

import cn.hutool.json.JSONUtil;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.AWSAuthenticationCredentials;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.AWSAuthenticationCredentialsProvider;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAAuthorizationCredentials;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfiguration;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.api.SellersApi;
import com.erp.sdk.oms.amz.spapi.config.AmazonAuthorConfigDTO;
import com.erp.sdk.oms.amz.spapi.model.sellers.GetMarketplaceParticipationsResponse;
import org.junit.Ignore;
import org.junit.Test;


/**
 * API tests for SellersApi
 */
@Ignore
public class SellersApiTest {


    /**
     * Returns a list of marketplaces that the seller submitting the request can sell in and information about the seller&#39;s participation in those marketplaces.  **Usage Plan:**  | Rate (requests per second) | Burst | | ---- | ---- | | .016 | 15 |  For more information, see \&quot;Usage Plans and Rate Limits\&quot; in the Selling Partner API documentation.
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void getSellerTest() throws ApiException {
        AmazonAuthorConfigDTO configDTO = new AmazonAuthorConfigDTO();
        AWSAuthenticationCredentials awsAuthenticationCredentials = configDTO.buildAWSAuthenticationCredentials();
        AWSAuthenticationCredentialsProvider awsAuthenticationCredentialsProvider = configDTO.buildAWSAuthenticationCredentialsProvider();
        LWAAuthorizationCredentials lwaAuthorizationCredentials = configDTO.buildLWAAuthorizationCredentials();
        RateLimitConfiguration rateLimitConfiguration = new RateLimitConfiguration() {
            @Override
            public Double getRateLimitPermit() {
                return null;
            }

            @Override
            public Long getTimeOut() {
                return null;
            }
        };
        SellersApi sellersApi = new SellersApi.Builder()
                .awsAuthenticationCredentials(awsAuthenticationCredentials)
                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
                .rateLimitConfigurationOnRequests(rateLimitConfiguration)
                .endpoint(configDTO.getSpEndPoint())
                .build();

        GetMarketplaceParticipationsResponse response = sellersApi.getMarketplaceParticipations();

        System.out.println("Seller查询结果：");
        System.out.println(JSONUtil.toJsonStr(response.getPayload()));

    }

}
