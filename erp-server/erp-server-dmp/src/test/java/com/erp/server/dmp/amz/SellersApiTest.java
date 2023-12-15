
package com.erp.server.dmp.amz;

import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfiguration;
import com.erp.sdk.oms.amz.spapi.api.SellersApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.model.sellers.GetMarketplaceParticipationsResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiConfigUtils;
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
        String json = "{\n" +
                "    \"@type\": \"com.erp.model.dmp.dto.AmazonShopInfoDTO\",\n" +
                "    \"accessKeyId\": \"AKIARVXX3YJGDV2NGA4L\",\n" +
                "    \"accessToken\": \"Atza|IwEBILjpmTFYJfM9qKl_jTNOD8F0GkY71TW1x31CT8zg6HyHSWjd_vipOHXABEXhgkCBWOkqaidZCmpbMUAHzQ5jS6iR31mAqm4ESiab9RBEC1YFM9FE1Ap2F78uFudE_182TNZauEzldVMyRxdt15ZF6vI4Q8O9wAx3Prfh48OeUYhPcfKWojmfkhkUoSWnpuChR_W_-ZXriE_0rcR8uzKoBG6IsNVoyJPRLzzl_gSNolrnfhOeXCUWUwZ_JoaJEH0ebJteWvhpCLqZFgx49iTKLxqwYGI8ynZSFPruQQU_v9CdN31UZB5R5F_Tj5LTUo8Jshj-lJ_A8FL0NGVuPn3EZ9vcBbGfXi6gPwuE1PAI9EycrQ\",\n" +
                "    \"authUrl\": \"https://api.amazon.com/auth/o2/token\",\n" +
                "    \"chargeId\": \"178\",\n" +
                "    \"clientId\": \"amzn1.application-oa2-client.aa03ca5c8fd741a49df6e35aac3c3287\",\n" +
                "    \"clientSecret\": \"amzn1.oa2-cs.v1.d9a5911d1548c33ed5b7fd7ede1f2932ef3041fae9f1cd973a32bf6b07b12183\",\n" +
                "    \"dictAreaCode\": \"欧洲区\",\n" +
                "    \"dictCountryCode\": \"TR\",\n" +
                "    \"id\": \"1734788283138838531\",\n" +
                "    \"name\": \"欧洲2站土耳其\",\n" +
                "    \"refreshToken\": \"Atzr|IwEBIPL2pukujrxJ3SjVlDonUMUXUetOYYROZ3UlAS7OcMloWrmBRAn5Npz_93FLWFumdTMIP54XS-Z9i6ysN80xXnWBf0kZi0GWU5_jjfAZ2sGNCmMF7nqCQmDMIYKVS31UAMZBx3_PSCIuYrzhyL3VxQC3ytWetZWI3SWN5vhBjbYI-kKb2St_4wJN7xXnxtihSVVIGrldFpiZFqPm2dpHqzcbdVAc3ZXNK-kiHpKsnIYAHOIXXEKlUeHClG7CjqVXoMI7ZX4JQuGtMUq5UWN4HDqUW8gZlGOlj79nRA_ZTNHSsD5WExZVc_ZN5Hf6fYx4gdc\",\n" +
                "    \"roleStr\": \"arn:aws:iam::115410190924:role/DehouRole\",\n" +
                "    \"secretKey\": \"t6CqSJE9o5OSONJiW+pCz7EFRHwJFtX3RIJSRP4B\",\n" +
                "    \"userStr\": \"arn:aws:iam::115410190924:user/DeHou01\"\n" +
                "}";
        AmazonShopInfoDTO shopInfoDTO = JSONUtil.toBean(json, AmazonShopInfoDTO.class);

        System.out.println(JSONUtil.toJsonStr(shopInfoDTO));
//        SellersApi sellersApi = new SellersApi.Builder()
//                .awsAuthenticationCredentials(awsAuthenticationCredentials)
//                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
//                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
//                .rateLimitConfigurationOnRequests(rateLimitConfiguration)
//                .endpoint(configDTO.get())
//                .build();
//
//        GetMarketplaceParticipationsResponse response = sellersApi.getMarketplaceParticipations();
//
//        System.out.println("Seller查询结果：");
//        System.out.println(JSONUtil.toJsonStr(response.getPayload()));

    }

    public static void main(String[] args) {
        String json = "{\n" +
                "    \"@type\": \"com.erp.model.dmp.dto.AmazonShopInfoDTO\",\n" +
                "    \"accessKeyId\": \"AKIARVXX3YJGDV2NGA4L\",\n" +
                "    \"accessToken\": \"Atza|IwEBILjpmTFYJfM9qKl_jTNOD8F0GkY71TW1x31CT8zg6HyHSWjd_vipOHXABEXhgkCBWOkqaidZCmpbMUAHzQ5jS6iR31mAqm4ESiab9RBEC1YFM9FE1Ap2F78uFudE_182TNZauEzldVMyRxdt15ZF6vI4Q8O9wAx3Prfh48OeUYhPcfKWojmfkhkUoSWnpuChR_W_-ZXriE_0rcR8uzKoBG6IsNVoyJPRLzzl_gSNolrnfhOeXCUWUwZ_JoaJEH0ebJteWvhpCLqZFgx49iTKLxqwYGI8ynZSFPruQQU_v9CdN31UZB5R5F_Tj5LTUo8Jshj-lJ_A8FL0NGVuPn3EZ9vcBbGfXi6gPwuE1PAI9EycrQ\",\n" +
                "    \"authUrl\": \"https://api.amazon.com/auth/o2/token\",\n" +
                "    \"chargeId\": \"178\",\n" +
                "    \"clientId\": \"amzn1.application-oa2-client.aa03ca5c8fd741a49df6e35aac3c3287\",\n" +
                "    \"clientSecret\": \"amzn1.oa2-cs.v1.d9a5911d1548c33ed5b7fd7ede1f2932ef3041fae9f1cd973a32bf6b07b12183\",\n" +
                "    \"dictAreaCode\": \"欧洲区\",\n" +
                "    \"dictCountryCode\": \"TR\",\n" +
                "    \"id\": \"1734788283138838531\",\n" +
                "    \"name\": \"欧洲2站土耳其\",\n" +
                "    \"refreshToken\": \"Atzr|IwEBIPL2pukujrxJ3SjVlDonUMUXUetOYYROZ3UlAS7OcMloWrmBRAn5Npz_93FLWFumdTMIP54XS-Z9i6ysN80xXnWBf0kZi0GWU5_jjfAZ2sGNCmMF7nqCQmDMIYKVS31UAMZBx3_PSCIuYrzhyL3VxQC3ytWetZWI3SWN5vhBjbYI-kKb2St_4wJN7xXnxtihSVVIGrldFpiZFqPm2dpHqzcbdVAc3ZXNK-kiHpKsnIYAHOIXXEKlUeHClG7CjqVXoMI7ZX4JQuGtMUq5UWN4HDqUW8gZlGOlj79nRA_ZTNHSsD5WExZVc_ZN5Hf6fYx4gdc\",\n" +
                "    \"roleStr\": \"arn:aws:iam::115410190924:role/DehouRole\",\n" +
                "    \"secretKey\": \"t6CqSJE9o5OSONJiW+pCz7EFRHwJFtX3RIJSRP4B\",\n" +
                "    \"userStr\": \"arn:aws:iam::115410190924:user/DeHou01\"\n" +
                "}";
        AmazonShopInfoDTO shopInfoDTO = JSONUtil.toBean(json, AmazonShopInfoDTO.class);

        System.out.println(JSONUtil.toJsonStr(shopInfoDTO));
//        SellersApi sellersApi = new SellersApi.Builder()
//                .awsAuthenticationCredentials(awsAuthenticationCredentials)
//                .lwaAuthorizationCredentials(lwaAuthorizationCredentials)
//                .awsAuthenticationCredentialsProvider(awsAuthenticationCredentialsProvider)
//                .rateLimitConfigurationOnRequests(rateLimitConfiguration)
//                .endpoint(configDTO.get())
//                .build();
//
//        GetMarketplaceParticipationsResponse response = sellersApi.getMarketplaceParticipations();
//
//        System.out.println("Seller查询结果：");
//        System.out.println(JSONUtil.toJsonStr(response.getPayload()));

    }

}
