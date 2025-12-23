package com.erp.sdk.oms.yunting.cem.example;

import com.erp.sdk.oms.yunting.cem.api.CommentApi;
import com.erp.sdk.oms.yunting.cem.api.TokenApi;
import com.erp.sdk.oms.yunting.cem.client.ApiException;
import com.erp.sdk.oms.yunting.cem.dto.CommentPullResultDTO;
import com.erp.sdk.oms.yunting.cem.dto.YuntingCommentDTO;
import com.erp.sdk.oms.yunting.cem.dto.YuntingCredentialDTO;
import com.erp.sdk.oms.yunting.cem.handler.YuntingCommentHandler;
import com.erp.sdk.oms.yunting.cem.handler.YuntingTokenHandler;
import com.erp.sdk.oms.yunting.cem.model.CommentPullRequest;
import com.erp.sdk.oms.yunting.cem.model.CommentPullResponse;
import com.erp.sdk.oms.yunting.cem.model.TokenResponse;
import com.erp.sdk.oms.yunting.cem.utils.YuntingApiUtils;

import java.util.List;

/**
 * 云听CEM SDK使用示例
 * 
 * 注意：本示例仅供参考，实际使用时需要替换为真实的凭证信息
 *
 * @author ERP System
 */
public class YuntingCemExample {

    public static void main(String[] args) {
        // ==================== 示例配置（需要替换为真实值） ====================
        String source = "ed3076da31e84193abbf8f66066ccedb";              // 由云听预设的来源标识
        String thirdPartyId = "ulanzi"; // 第三方应用ID
        String projectId = "bd7496c9a5a6410eab699bcc2276566a";        // 项目ID
        String startTime = "2025-12-01 00:00:00";    // 开始时间
        String endTime = "2025-12-04 23:59:59";      // 结束时间

        try {
            // ==================== 方式一：使用Handler（推荐） ====================
            System.out.println("========== 方式一：使用Handler ==========");
            exampleWithHandler(source, thirdPartyId, projectId, startTime, endTime);

            // ==================== 方式二：使用API直接调用 ====================
            System.out.println("\n========== 方式二：使用API直接调用 ==========");
            exampleWithApi(source, thirdPartyId, projectId, startTime, endTime);

            // ==================== 方式三：使用工具类 ====================
            System.out.println("\n========== 方式三：使用工具类 ==========");
            exampleWithUtils(source, thirdPartyId, projectId, startTime, endTime);

        } catch (ApiException e) {
            System.err.println("API调用异常: " + YuntingApiUtils.handleApiException(e));
            e.printStackTrace();
        }
    }

    /**
     * 方式一：使用Handler（推荐）
     */
    private static void exampleWithHandler(String source, String thirdPartyId, String projectId,
                                           String startTime, String endTime) throws ApiException {
        // 1. 创建Handler实例
        YuntingTokenHandler tokenHandler = new YuntingTokenHandler();
        YuntingCommentHandler commentHandler = new YuntingCommentHandler();

        // 2. 获取访问凭证
        System.out.println("正在获取访问凭证...");
        YuntingCredentialDTO credential = tokenHandler.getCredential(source, thirdPartyId, projectId);
        System.out.println("访问凭证获取成功！");
        System.out.println("  - AccessToken: " + maskToken(credential.getAccessToken()));
        System.out.println("  - 过期时间: " + credential.getExpiresIn() + "秒");
        System.out.println("  - 剩余时间: " + credential.getRemainingTime() + "秒");

        // 3. 拉取评论数据（单次）
        System.out.println("\n正在拉取评论数据（单次）...");
        CommentPullResultDTO result = commentHandler.pullComments(credential, startTime, endTime, null);
        System.out.println("拉取成功！");
        System.out.println("  - 本批次数量: " + result.getCount());
        System.out.println("  - 还有更多: " + result.getHasMore());
        System.out.println("  - TraceId: " + result.getTraceId());

        // 显示前5条评论
        displayComments(result.getComments(), 5);

        // 4. 批量拉取所有数据（自动分页）
        System.out.println("\n正在批量拉取所有数据（自动分页）...");
        List<YuntingCommentDTO> allComments = commentHandler.pullAllComments(credential, startTime, endTime);
        System.out.println("批量拉取完成！总共: " + allComments.size() + " 条评论");
    }

    /**
     * 方式二：使用API直接调用
     */
    private static void exampleWithApi(String source, String thirdPartyId, String projectId,
                                       String startTime, String endTime) throws ApiException {
        // 1. 获取访问令牌
        System.out.println("正在获取访问令牌...");
        TokenApi tokenApi = new TokenApi();
        TokenResponse tokenResponse = tokenApi.getToken(source, thirdPartyId);

        if (tokenResponse.getCode() != 20000) {
            System.err.println("获取令牌失败: " + tokenResponse.getMsg());
            return;
        }

        String accessToken = tokenResponse.getResult().getAccessToken();
        System.out.println("访问令牌获取成功: " + maskToken(accessToken));

        // 2. 创建CommentApi并设置令牌
        CommentApi commentApi = new CommentApi();
        commentApi.getApiClient().setAccessToken(accessToken);

        // 3. 构建请求
        CommentPullRequest request = CommentPullRequest.builder()
                .projectId(projectId)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        // 4. 拉取数据
        System.out.println("正在拉取评论数据...");
        CommentPullResponse response = commentApi.pullComments(request);

        if (response.getCode() != 20000) {
            System.err.println("拉取数据失败: " + response.getMsg());
            return;
        }

        System.out.println("拉取成功！");
        System.out.println("  - 数据量: " + response.getResult().getCount());
        System.out.println("  - 还有更多: " + response.getResult().getHasMore());
    }

    /**
     * 方式三：使用工具类
     */
    private static void exampleWithUtils(String source, String thirdPartyId, String projectId,
                                         String startTime, String endTime) throws ApiException {
        // 1. 创建已认证的CommentApi
        System.out.println("正在创建已认证的CommentApi...");
        CommentApi commentApi = YuntingApiUtils.createAuthenticatedCommentApi(source, thirdPartyId);
        System.out.println("CommentApi创建成功！");

        // 2. 使用自动分页功能
        System.out.println("正在使用自动分页拉取所有数据...");
        commentApi.pullAllComments(projectId, startTime, endTime, (data, batchIndex) -> {
            System.out.println("  - 第" + batchIndex + "批: " + data.size() + "条数据");

            // 处理每批数据
            if (batchIndex == 1) {
                // 只显示第一批的前3条
                System.out.println("    前3条示例:");
                data.stream().limit(3).forEach(comment -> {
                    System.out.println("      * " + comment.getSourceName() + " - " + 
                                     (comment.getContent() != null && comment.getContent().length() > 30 
                                         ? comment.getContent().substring(0, 30) + "..." 
                                         : comment.getContent()));
                });
            }
        });

        System.out.println("批量拉取完成！");
    }

    /**
     * 显示评论列表
     */
    private static void displayComments(List<YuntingCommentDTO> comments, int limit) {
        if (comments == null || comments.isEmpty()) {
            System.out.println("  - 暂无评论数据");
            return;
        }

        System.out.println("\n  前" + Math.min(limit, comments.size()) + "条评论:");
        comments.stream().limit(limit).forEach(comment -> {
            System.out.println("  ----------------------------------------");
            System.out.println("  唯一键: " + comment.getUnique());
            System.out.println("  平台: " + comment.getSourceName());
            System.out.println("  发布时间: " + comment.getPublishTime());
            System.out.println("  用户: " + comment.getUserName());
            System.out.println("  内容: " + (comment.getContent() != null && comment.getContent().length() > 50 
                    ? comment.getContent().substring(0, 50) + "..." 
                    : comment.getContent()));
            System.out.println("  情感: " + comment.getEscore());
            System.out.println("  类型: " + comment.getDataLevel());
            System.out.println("  点赞: " + comment.getLikes() + " | 评论: " + comment.getComments() + 
                             " | 浏览: " + comment.getViews());
        });
        System.out.println("  ----------------------------------------");
    }

    /**
     * 脱敏显示Token（只显示前后各10个字符）
     */
    private static String maskToken(String token) {
        if (token == null || token.length() <= 20) {
            return "***";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }
}

