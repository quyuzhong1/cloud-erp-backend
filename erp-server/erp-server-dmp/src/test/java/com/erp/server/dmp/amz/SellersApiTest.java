
package com.erp.server.dmp.amz;

import cn.hutool.json.JSONUtil;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.AWSAuthenticationCredentials;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.AWSAuthenticationCredentialsProvider;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAAuthorizationCredentials;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfiguration;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.api.SellersApi;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiConfigUtil;
import com.erp.sdk.oms.amz.spapi.model.sellers.GetMarketplaceParticipationsResponse;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import com.erp.server.dmp.ErpServerDmpApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Test;


/**
 * API tests for SellersApi
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class SellersApiTest {


    /**
     * Returns a list of marketplaces that the seller submitting the request can sell in and information about the seller&#39;s participation in those marketplaces.  **Usage Plan:**  | Rate (requests per second) | Burst | | ---- | ---- | | .016 | 15 |  For more information, see \&quot;Usage Plans and Rate Limits\&quot; in the Selling Partner API documentation.
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void getSellerTest() throws ApiException {
        AmazonSpApiConfigUtil configDTO = new AmazonSpApiConfigUtil();
//        AWSAuthenticationCredentials awsAuthenticationCredentials = configDTO.buildAWSAuthenticationCredentials();
//        AWSAuthenticationCredentialsProvider awsAuthenticationCredentialsProvider = configDTO.buildAWSAuthenticationCredentialsProvider();
//        LWAAuthorizationCredentials lwaAuthorizationCredentials = configDTO.buildLWAAuthorizationCredentials();
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
//        SellersApi sellersApi = new SellersApi.Builder()
//                .awsAuthenticationCredentials(awsAuthenticationCredentials)
//                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
//                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
//                .rateLimitConfigurationOnRequests(rateLimitConfiguration)
//                .endpoint(configDTO.getSpEndPoint())
//                .build();
//
//        GetMarketplaceParticipationsResponse response = sellersApi.getMarketplaceParticipations();
//
//        System.out.println("Seller查询结果：");
//        System.out.println(JSONUtil.toJsonStr(response.getPayload()));

    }

}
