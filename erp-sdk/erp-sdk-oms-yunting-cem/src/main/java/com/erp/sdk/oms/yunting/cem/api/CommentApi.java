package com.erp.sdk.oms.yunting.cem.api;

import com.erp.sdk.oms.yunting.cem.client.ApiClient;
import com.erp.sdk.oms.yunting.cem.client.ApiException;
import com.erp.sdk.oms.yunting.cem.client.ApiResponse;
import com.erp.sdk.oms.yunting.cem.model.CommentPullRequest;
import com.erp.sdk.oms.yunting.cem.model.CommentPullResponse;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;

/**
 * 云听CEM社交媒体评论数据API
 *
 * @author ERP System
 */
@Slf4j
public class CommentApi {

    private ApiClient apiClient;

    /**
     * 构造函数
     */
    public CommentApi() {
        this(new ApiClient());
    }

    /**
     * 构造函数
     *
     * @param apiClient API客户端
     */
    public CommentApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 获取API客户端
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * 设置API客户端
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 拉取社交媒体评论数据
     * <p>
     * 主动拉取各社交平台的公开评论数据
     * 支持平台：小红书、抖音、公众号、微博、Facebook、Instagram、Reddit等
     * <p>
     * 接口频率限制：每分钟最多30次调用（IP限制）
     * 单次返回大小：固定返回1000条数据
     *
     * @param request 拉取请求参数
     * @return CommentPullResponse 评论数据响应
     * @throws ApiException 如果API调用失败
     */
    public CommentPullResponse pullComments(CommentPullRequest request) throws ApiException {
        ApiResponse<CommentPullResponse> response = pullCommentsWithHttpInfo(request);
        return response.getData();
    }

    /**
     * 拉取社交媒体评论数据（带HTTP信息）
     *
     * @param request 拉取请求参数
     * @return ApiResponse&lt;CommentPullResponse&gt; 包含HTTP信息的响应
     * @throws ApiException 如果API调用失败
     */
    public ApiResponse<CommentPullResponse> pullCommentsWithHttpInfo(CommentPullRequest request) throws ApiException {
        // 验证必填参数
        if (request == null) {
            throw new ApiException("缺少必填参数 'request'");
        }
        if (StringUtils.isBlank(request.getProjectId())) {
            throw new ApiException("缺少必填参数 'projectId'");
        }

        // 验证访问令牌
        if (StringUtils.isBlank(apiClient.getAccessToken())) {
            throw new ApiException("缺少访问令牌 'accessToken'，请先调用TokenApi获取");
        }

        // 构建请求路径
        String path = "/api/comment/v1/social/pull";

        // 定义返回类型
        Type returnType = new TypeToken<CommentPullResponse>() {}.getType();

        // 执行POST请求
        log.info("云听CEM - 拉取社交媒体评论数据: projectId={}, startTime={}, endTime={}, pageToken={}",
                request.getProjectId(), request.getStartTime(), request.getEndTime(), 
                StringUtils.isNotBlank(request.getPageToken()) ? "有" : "无");

        return apiClient.executePost(path, request, null, returnType);
    }

    /**
     * 批量拉取所有评论数据（自动分页）
     * <p>
     * 此方法会自动处理分页，直到拉取完所有数据
     *
     * @param projectId 项目ID
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime   结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @param callback  数据回调接口，每次拉取一批数据后回调
     * @throws ApiException 如果API调用失败
     */
    public void pullAllComments(String projectId, String startTime, String endTime, 
                                CommentDataCallback callback) throws ApiException {
        String pageToken = null;
        boolean hasMore = true;
        int batchCount = 0;

        while (hasMore) {
            batchCount++;
            
            // 构建请求
            CommentPullRequest request = CommentPullRequest.builder()
                    .projectId(projectId)
                    .startTime(startTime)
                    .endTime(endTime)
                    .pageToken(pageToken)
                    .build();

            // 拉取数据
            CommentPullResponse response = pullComments(request);

            // 检查响应
            if (response == null || response.getResult() == null) {
                log.warn("云听CEM - 第{}批数据响应为空", batchCount);
                break;
            }

            CommentPullResponse.CommentResult result = response.getResult();
            
            log.info("云听CEM - 第{}批数据拉取成功，数据量：{}, hasMore：{}", 
                    batchCount, result.getCount(), result.getHasMore());

            // 回调处理数据
            if (callback != null && result.getData() != null && !result.getData().isEmpty()) {
                callback.onData(result.getData(), batchCount);
            }

            // 更新分页信息
            hasMore = Boolean.TRUE.equals(result.getHasMore());
            pageToken = result.getPageToken();

            // 避免频率限制，每次请求后休眠
            if (hasMore) {
                try {
                    Thread.sleep(2100); // 休眠2.1秒，避免超过每分钟30次的限制
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ApiException("批量拉取被中断"+e.getMessage());
                }
            }
        }

        log.info("云听CEM - 批量拉取完成，共{}批数据", batchCount);
    }

    /**
     * 评论数据回调接口
     */
    @FunctionalInterface
    public interface CommentDataCallback {
        /**
         * 数据回调
         *
         * @param data       评论数据列表
         * @param batchIndex 批次索引（从1开始）
         */
        void onData(java.util.List<com.erp.sdk.oms.yunting.cem.model.CommentData> data, int batchIndex);
    }
}

