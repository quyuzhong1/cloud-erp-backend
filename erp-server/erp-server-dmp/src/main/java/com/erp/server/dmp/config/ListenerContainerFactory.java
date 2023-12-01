package com.erp.server.dmp.config;

import cn.hutool.core.util.StrUtil;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.*;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.AWSAuthenticationCredentials;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.AWSAuthenticationCredentialsProvider;
import com.erp.sdk.oms.amz.spapi.enums.AmazonEndpointsEnum;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.jms.Session;

/**
 * 亚马逊SQS监听工厂
 */
@Slf4j
//@Configuration
//@EnableJms
//@DependsOn("amazonSpApiConfigUtils")
public class ListenerContainerFactory {


    @Bean
    public SQSConnectionFactory connectionFactory() {
        String region = parseRegionBySqsEndpoint();
        // 创建AWSCredentialsProvider
        AmazonEndpointsEnum endpointsEnum = AmazonEndpointsEnum.getByRegion(region);
        AWSAuthenticationCredentials awsAuthenticationCredentials = AmazonSpApiConfigUtils.buildAWSAuthenticationCredentials(endpointsEnum);
        AWSAuthenticationCredentialsProvider awsAuthenticationCredentialsProvider = AmazonSpApiConfigUtils.buildAWSAuthenticationCredentialsProvider();
        BasicAWSCredentials awsBasicCredentials = new BasicAWSCredentials(awsAuthenticationCredentials.getAccessKeyId(), awsAuthenticationCredentials.getSecretKey());
        return SQSConnectionFactory.builder()
                .withRegion(Region.getRegion(Regions.US_EAST_1))
                .withAWSCredentialsProvider(new STSAssumeRoleSessionCredentialsProvider.Builder(
                        awsAuthenticationCredentialsProvider.getRoleArn(),
                        awsAuthenticationCredentialsProvider.getRoleSessionName())
                        .withStsClient(AWSSecurityTokenServiceClientBuilder.standard()
                                .withRegion(awsAuthenticationCredentials.getRegion())
                                .withCredentials(new AWSStaticCredentialsProvider(awsBasicCredentials)).build())
                        .build())
                .build();
    }


    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(SQSConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setConcurrency("3-10");
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        return factory;
    }

    @Bean
    public JmsTemplate defaultJmsTemplate(SQSConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    /**
     * 从sqsEndpoint解析出地区,如: us-west-1
     */
    private String parseRegionBySqsEndpoint() {
        return StrUtil.subBetween(AmazonSpApiConfigUtils.sqsEndpoint, "sqs.", ".amazonaws.com");
    }

    public static void main(String[] args) {
        String aa = "https://sqs.us-east-1.amazonaws.com/700518745840/test_erp";
        System.out.println(StrUtil.subBetween(aa, "sqs.", ".amazonaws.com"));
    }

}
