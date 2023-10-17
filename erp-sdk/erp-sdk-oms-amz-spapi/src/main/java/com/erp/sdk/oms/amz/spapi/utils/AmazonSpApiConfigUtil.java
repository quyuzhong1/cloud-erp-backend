package com.erp.sdk.oms.amz.spapi.utils;

import com.common.core.utils.UUID;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.AWSAuthenticationCredentials;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.AWSAuthenticationCredentialsProvider;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAAuthorizationCredentials;
import com.erp.sdk.oms.amz.spapi.enums.AmazonEndpointsEnum;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 亚马逊SP-API配置工具类
 * <p>
 * IAM（Identity and Access Management）凭据来进行身份验证和授权
 * LWA (Login with Amazon) 以授权卖家对其亚马逊卖家中心数据的访问
 */
@Data
@Component
public class AmazonSpApiConfigUtil {

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

    @Value("${openApi.amazon.accessKeyId:}")
    public void setAccessKeyId(String accessKeyId) {
        AmazonSpApiConfigUtil.accessKeyId = accessKeyId;
    }

    @Value("${openApi.amazon.secretKey:}")
    public void setSecretKey(String secretKey) {
        AmazonSpApiConfigUtil.secretKey = secretKey;
    }

    @Value("${openApi.amazon.roleArn:}")
    public void setRoleArn(String roleArn) {
        AmazonSpApiConfigUtil.roleArn = roleArn;
    }

    @Value("${openApi.amazon.clientId:}")
    public void setClientId(String clientId) {
        AmazonSpApiConfigUtil.clientId = clientId;
    }

    @Value("${openApi.amazon.clientSecret:}")
    public void setClientSecret(String clientSecret) {
        AmazonSpApiConfigUtil.clientSecret = clientSecret;
    }

    @Value("${openApi.amazon.refreshToken:}")
    public void setRefreshToken(String refreshToken) {
        AmazonSpApiConfigUtil.refreshToken = refreshToken;
    }

    @Value("${openApi.amazon.lwaEndpoint:}")
    public void setLwaEndpoint(String lwaEndpoint) {
        AmazonSpApiConfigUtil.lwaEndpoint = lwaEndpoint;
    }

//    public AmazonSpApiConfigUtil AmazonSpApiConfigUtil(AwsMarketplaceEnum marketplaceEnum) {
//        this.accessKeyId = "AKIA2GGRLY3YDWBPAAZ6";
//        this.secretKey = "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";
//        this.region = marketplaceEnum.getEndpointsEnum().getRegion();
//        this.roleArn = "arn:aws:iam::700518745840:role/SPAPI";
//        this.roleSessionName = UUID.randomUUID().toString();
//        this.clientId = "amzn1.application-oa2-client.8319967435d64aee9ada60c82399d08a";
//        this.clientSecret = "amzn1.oa2-cs.v1.8f226d6b3ea555a913ba62fbcd07ea05ee5ab4ad56e9cffde664636c05c88cda";
//        this.refreshToken = "Atzr|IwEBIB-75TFCxQq-rbqcKVbnQcYDyqSykTQ9cBqhfDEUfUYY-NHFbJt2bXqMnDtdtCYTU-h5rywDn2mfTyzE2MR4jdvLOhBZbel_5qCNn0W9e2dNEqfub3y_gsEXjgTcnA_IO7UW7lxCeWPlD2ZP2HhXG8pINqkLN_nTZKD_j4ZBSBfWEfKsoOYq6YR_fn3c6Q5_OSMb_lqCl36LXcjxJ31RIcMDvmSAiFgP9iMlxSbEm1gTIdNylU7LHt_OyogMrXhbBqhzy8SHqdpxfWFERD6FZsX1feMNpQxn-VVNMxXoN3kYNVYu1W5gQhhTq-oM8B5yCB0";
//        this.lwaEndpoint = "https://api.amazon.com/auth/o2/token";
//        this.spEndPoint = marketplaceEnum.getEndpointsEnum().getEndpointsByProfile();
//    }

    /**
     * 构建AWSAuthenticationCredentials
     */
    public static AWSAuthenticationCredentials buildAWSAuthenticationCredentials(AmazonEndpointsEnum endpointsEnum) {
        //region分北美，欧洲，远东三个AWS区域
        return AWSAuthenticationCredentials.builder()
                //注册成为开发者时生成的AWS访问密钥ID
                .accessKeyId(AmazonSpApiConfigUtil.accessKeyId)
                //注册成为开发者时生成的AWS访问密钥
                .secretKey(AmazonSpApiConfigUtil.secretKey)
                //注意，这里的region分北美(us-east-1)，欧洲(eu-west-1)，远东(us-west-2)
                .region(endpointsEnum.getRegion())
                .build();
    }

    /**
     * 构建AWSAuthenticationCredentialsProvider
     */
    public static AWSAuthenticationCredentialsProvider buildAWSAuthenticationCredentialsProvider() {
        return AWSAuthenticationCredentialsProvider.builder()
                //创建IAM职权的时候会生成这个ARN
                .roleArn(AmazonSpApiConfigUtil.roleArn)
                //唯一值，可以使用UUID
                .roleSessionName(UUID.randomUUID().toString())
                .build();
    }

    /**
     * 构建LWAAuthorizationCredentials
     */
    public static LWAAuthorizationCredentials buildLWAAuthorizationCredentials() {
        return LWAAuthorizationCredentials.builder()
                //查看开发者信息的时候可看到LWA的客户端编码
                .clientId(AmazonSpApiConfigUtil.clientId)
                //查看开发者信息的时候可看到LWA的客户端秘钥
                .clientSecret(AmazonSpApiConfigUtil.clientSecret)
                //根据上面的客户端编码和客户端秘钥请求客户端令牌
                .refreshToken(AmazonSpApiConfigUtil.refreshToken)
                //"https://api.amazon.com/auth/o2/token"
                .endpoint(AmazonSpApiConfigUtil.lwaEndpoint)
                .build();
    }


}