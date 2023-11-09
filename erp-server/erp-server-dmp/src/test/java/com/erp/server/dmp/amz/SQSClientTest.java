//package com.erp.server.dmp.amz;
//
//
//import cn.hutool.json.JSONUtil;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.sqs.SqsClient;
//import software.amazon.awssdk.services.sqs.model.*;
//
//import java.util.List;
//
//public class SQSClientTest {
//
//    public static void main(String[] args) {
//        // 配置所需的 Region 和 Endpoint
//        Region region = Region.US_EAST_1; // 你可以根据需要设置不同的 Region
//
//        // 指定 AWS 访问密钥和密钥 ID
//        String accessKey = "AKIA2GGRLY3YDWBPAAZ6";
//        String secretKey = "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";
//
//        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
//
//        // 创建 SQS 客户端并指定 endpoint
//        SqsClient sqsClient = SqsClient.builder()
//                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
//                .region(region)
////                .endpointOverride(endpointUrl)
//                .build();
//
//        // 指定队列
//        String queueUrl = "https://sqs.us-east-1.amazonaws.com/700518745840/erpNotifications"; // 你的队列 URL
//
//        // 接收消息
//        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
//                .queueUrl(queueUrl)
//                .maxNumberOfMessages(100)
//                .waitTimeSeconds(600)
//                .build();
//
//        ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(receiveMessageRequest);
//        System.out.println(JSONUtil.toJsonStr(receiveMessageResponse));
//        List<software.amazon.awssdk.services.sqs.model.Message> messages = receiveMessageResponse.messages();
//        System.out.println(JSONUtil.toJsonStr(messages));
//        // 关闭 SQS 客户端
//        sqsClient.close();
//    }
//
//}
