package com.erp.sdk.oms.amz.spapi.config;

import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.AWSAuthenticationCredentials;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.AWSAuthenticationCredentialsProvider;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAAuthorizationCredentials;
import lombok.Data;

import java.util.UUID;

/**
 * 亚马逊SP-API配置信息
 */
@Data
public class AmazonAuthorConfigDTO {

    /**
     * AWS访问密钥编码
     */
    private String accessKeyId;

    /**
     * AWS访问密钥
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * IAM职权ARN(Amazon Resource Name)
     */
    private String roleArn;

    /**
     * IAM职权名称
     */
    private String roleSessionName;

    /**
     * LWA客户端编码
     */
    private String clientId;

    /**
     * LWA客户端秘钥
     */
    private String clientSecret;

    /**
     * LWA客户端令牌
     */
    private String refreshToken;

    /**
     * LWA授权服务器的节点地址
     */
    private String lwaEndpoint;

    /**
     * SP授权服务器节点地址
     */
    private String spEndPoint;

    public AmazonAuthorConfigDTO() {
        this.accessKeyId = "AKIA2GGRLY3YDWBPAAZ6";
        this.secretKey = "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";
        this.region = "us-east-1";
        this.roleArn = "arn:aws:iam::700518745840:role/SPAPI";
        this.roleSessionName = UUID.randomUUID().toString();
        this.clientId = "amzn1.application-oa2-client.8319967435d64aee9ada60c82399d08a";
        this.clientSecret = "amzn1.oa2-cs.v1.8f226d6b3ea555a913ba62fbcd07ea05ee5ab4ad56e9cffde664636c05c88cda";
        this.refreshToken = "Atzr|IwEBIB-75TFCxQq-rbqcKVbnQcYDyqSykTQ9cBqhfDEUfUYY-NHFbJt2bXqMnDtdtCYTU-h5rywDn2mfTyzE2MR4jdvLOhBZbel_5qCNn0W9e2dNEqfub3y_gsEXjgTcnA_IO7UW7lxCeWPlD2ZP2HhXG8pINqkLN_nTZKD_j4ZBSBfWEfKsoOYq6YR_fn3c6Q5_OSMb_lqCl36LXcjxJ31RIcMDvmSAiFgP9iMlxSbEm1gTIdNylU7LHt_OyogMrXhbBqhzy8SHqdpxfWFERD6FZsX1feMNpQxn-VVNMxXoN3kYNVYu1W5gQhhTq-oM8B5yCB0";
        this.lwaEndpoint = "https://api.amazon.com/auth/o2/token";
        this.spEndPoint = "https://sellingpartnerapi-na.amazon.com";
//        this.spEndPoint = "https://sandbox.sellingpartnerapi-na.amazon.com";
    }

    /**
     * 构建AWSAuthenticationCredentials
     */
    public AWSAuthenticationCredentials buildAWSAuthenticationCredentials() {
        //region分北美，欧洲，远东三个AWS区域
        return AWSAuthenticationCredentials.builder()
                //注册成为开发者时生成的AWS访问密钥ID
                .accessKeyId(this.getAccessKeyId())
                //注册成为开发者时生成的AWS访问密钥
                .secretKey(this.getSecretKey())
                //注意，这里的region分北美(us-east-1)，欧洲(eu-west-1)，远东(us-west-2)
                .region(this.getRegion())
                .build();
    }

    /**
     * 构建AWSAuthenticationCredentialsProvider
     */
    public AWSAuthenticationCredentialsProvider buildAWSAuthenticationCredentialsProvider() {
        return AWSAuthenticationCredentialsProvider.builder()
                //创建IAM职权的时候会生成这个ARN
                .roleArn(this.getRoleArn())
                //唯一值，可以使用UUID
                .roleSessionName(this.getRoleSessionName())
                .build();
    }

    /**
     * 构建LWAAuthorizationCredentials
     */
    public LWAAuthorizationCredentials buildLWAAuthorizationCredentials() {
        return LWAAuthorizationCredentials.builder()
                //查看开发者信息的时候可看到LWA的客户端编码
                .clientId(this.getClientId())
                //查看开发者信息的时候可看到LWA的客户端秘钥
                .clientSecret(this.getClientSecret())
                //根据上面的客户端编码和客户端秘钥请求客户端令牌
                .refreshToken(this.getRefreshToken())
                //"https://api.amazon.com/auth/o2/token"
                .endpoint(this.getLwaEndpoint())
                .build();
    }


}