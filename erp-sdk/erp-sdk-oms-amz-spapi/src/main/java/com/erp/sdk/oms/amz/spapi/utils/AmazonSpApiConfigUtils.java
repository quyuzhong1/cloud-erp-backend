package com.erp.sdk.oms.amz.spapi.utils;

import com.common.core.utils.UUID;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.AWSAuthenticationCredentials;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.AWSAuthenticationCredentialsProvider;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAAuthorizationCredentials;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.ScopeConstants;
import com.erp.sdk.oms.amz.spapi.enums.AmazonEndpointsEnum;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import cn.hutool.core.util.StrUtil;

/**
 * 亚马逊SP-API配置工具类
 * <p>
 * IAM（Identity and Access Management）凭据来进行身份验证和授权
 * LWA (Login with Amazon) 以授权卖家对其亚马逊卖家中心数据的访问
 */
@Data
@Component
public class AmazonSpApiConfigUtils {

    /**
     * AWS访问密钥编码
     */
    public static String accessKeyId;

    /**
     * AWS访问密钥
     */
    public static String secretKey;

    /**
     * IAM职权ARN(Amazon Resource Name)
     */
    public static String roleArn;

    /**
     * LWA客户端编码
     */
    public static String clientId;

    /**
     * LWA客户端秘钥
     */
    public static String clientSecret;

    /**
     * LWA客户端令牌
     */
    public static String refreshToken;

    /**
     * LWA授权服务器的节点地址
     */
    public static String lwaEndpoint;

    /**
     * SQS队列地址
     */
    public static String sqsEndpoint;

    /**
     * 构建免授权
     * <a href="https://developer-docs.amazon.com/sp-api/docs/connecting-to-the-selling-partner-api-using-a-generated-java-sdk">来源</a>
     * 免授权操作
     * 操作名称	HTTP 方法和路径
     * createDestination	POST /notifications/v1/destinations
     * deleteDestination	DELETE /notifications/v1/destinations/{destinationId}
     * deleteSubscriptionById	DELETE /notifications/v2/subscriptions/{notificationType}/{subscriptionId}
     * getDestination	GET /notifications/v1/destinations/{destinationId}
     * getDestinations	GET /notifications/v1/destinations
     * getSubscriptionById	GET /notifications/v1/subscriptions/{notificationType}/{subscriptionId}
     * getAuthorizationCode	GET /authorization/v1/authorizationCode
     */
    public static LWAAuthorizationCredentials buildLWAAuthorizationScopeCredentials() {
        return LWAAuthorizationCredentials.builder()
                //查看开发者信息的时候可看到LWA的客户端编码
                .clientId(AmazonSpApiConfigUtils.clientId)
                //查看开发者信息的时候可看到LWA的客户端秘钥
                .clientSecret(AmazonSpApiConfigUtils.clientSecret)
                .withScopes(ScopeConstants.SCOPE_NOTIFICATIONS_API, ScopeConstants.SCOPE_MIGRATION_API)
                //"https://api.amazon.com/auth/o2/token"
                .endpoint(AmazonSpApiConfigUtils.lwaEndpoint)
                .build();
    }

    /**
     * 构建AWSAuthenticationCredentialsProvider
     */
//    public static AWSAuthenticationCredentialsProvider buildAWSAuthenticationCredentialsProvider() {
//        return AWSAuthenticationCredentialsProvider.builder()
//                //创建IAM职权的时候会生成这个ARN
////                .roleArn(AmazonSpApiConfigUtils.roleArn)
//                .roleArn("arn:aws:iam::115410190924:role/DehouRole")
//                //唯一值，可以使用UUID
//                .roleSessionName(UUID.randomUUID().toString())
//                .build();
//    }

    /**
     * 构建LWAAuthorizationCredentials
     */
//    public static LWAAuthorizationCredentials buildLWAAuthorizationCredentials() {
//        return LWAAuthorizationCredentials.builder()
//                //查看开发者信息的时候可看到LWA的客户端编码
////                .clientId(AmazonSpApiConfigUtils.clientId)
//                .clientId("amzn1.application-oa2-client.aa03ca5c8fd741a49df6e35aac3c3287")
//                //查看开发者信息的时候可看到LWA的客户端秘钥
////                .clientSecret(AmazonSpApiConfigUtils.clientSecret)
//                .clientSecret("amzn1.oa2-cs.v1.d9a5911d1548c33ed5b7fd7ede1f2932ef3041fae9f1cd973a32bf6b07b12183")
//                //根据上面的客户端编码和客户端秘钥请求客户端令牌
////                .refreshToken(AmazonSpApiConfigUtils.refreshToken)
//                .refreshToken("Atzr|IwEBIIAk0ZC6REzfuMKktAqsxNSwMAKUhtU60y0BV0BDBwhK70zqmX_10F_xz9xV-G2dosTUdhK0GJvdSDGNcnHO50t_9bYqulua1PdhNDSQsNnTXd9xpWeTbYhZfzdmQSy3UgmG1S0kKpOhy2XFyC1f93Tuq-uminDGYrqvitG1bbqqB9nBfd7eiK2csTVqrxpxLlmM49xm8a7iTrC8o7F7nmf3mDSz4biyE4qQZnIDd1oEv0q_rEz0QFXxm80cs8CcvLj15CJgOG-R7C5SfbaDXDaLFigamEEVp5JS5FAy7v6E8PUNkLQstrVwlxO9XEuQ7CNsX0aRy08jRkH01eb2Pamk")
//                //"https://api.amazon.com/auth/o2/token"
////                .endpoint(AmazonSpApiConfigUtils.lwaEndpoint)
//                .endpoint("https://api.amazon.com/auth/o2/token")
//                .build();
//    }

    /**
     * 从sqsEndpoint解析出地区,如: us-west-1
     */
    public static String parseRegionBySqsEndpoint() {
        return StrUtil.subBetween(AmazonSpApiConfigUtils.sqsEndpoint, "sqs.", ".amazonaws.com");
    }


}