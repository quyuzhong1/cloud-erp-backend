package com.erp.sdk.oms.yunting.cem.handler;

import com.alibaba.fastjson.JSON;
import com.erp.sdk.oms.yunting.cem.api.CommentApi;
import com.erp.sdk.oms.yunting.cem.client.ApiException;
import com.erp.sdk.oms.yunting.cem.dto.CommentPullResultDTO;
import com.erp.sdk.oms.yunting.cem.dto.YuntingCommentDTO;
import com.erp.sdk.oms.yunting.cem.dto.YuntingCredentialDTO;
import com.erp.sdk.oms.yunting.cem.enums.YuntingErrorCodeEnum;
import com.erp.sdk.oms.yunting.cem.model.CommentData;
import com.erp.sdk.oms.yunting.cem.model.CommentPullRequest;
import com.erp.sdk.oms.yunting.cem.model.CommentPullResponse;
import com.erp.sdk.oms.yunting.cem.utils.YuntingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 云听CEM社交媒体评论处理器
 *
 * @author ERP System
 */
@Slf4j
@Component
public class YuntingCommentHandler {

    /**
     * 拉取社交媒体评论数据
     *
     * @param credential 云听凭证
     * @param startTime  开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime    结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @param pageToken  分页Token（选填）
     * @return 评论拉取结果
     * @throws ApiException API异常
     */
    public CommentPullResultDTO pullComments(YuntingCredentialDTO credential, String startTime, 
                                             String endTime, String pageToken) throws ApiException {
        // 验证凭证
        validateCredential(credential);

        // 创建API实例
        CommentApi commentApi = createCommentApi(credential);

        // 构建请求
        CommentPullRequest request = CommentPullRequest.builder()
                .projectId(credential.getProjectId())
                .startTime(startTime)
                .endTime(endTime)
                .pageToken(pageToken)
                .build();

        // 执行请求
        log.info("云听CEM - 开始拉取评论数据: projectId={}, startTime={}, endTime={}", 
                credential.getProjectId(), startTime, endTime);

        CommentPullResponse response = commentApi.pullComments(request);

        // 验证响应
        validateResponse(response);

        // 转换结果
        return convertToResult(response);
    }

    /**
     * 批量拉取所有评论数据（自动分页）
     *
     * @param credential 云听凭证
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 所有评论数据
     * @throws ApiException API异常
     */
    public List<YuntingCommentDTO> pullAllComments(YuntingCredentialDTO credential, 
                                                   String startTime, String endTime) throws ApiException {
        List<YuntingCommentDTO> allComments = new ArrayList<>();
        
        // 创建API实例
        CommentApi commentApi = createCommentApi(credential);

        // 使用批量拉取方法
        commentApi.pullAllComments(credential.getProjectId(), startTime, endTime, (data, batchIndex) -> {
            log.info("云听CEM - 处理第{}批数据，数量：{}", batchIndex, data.size());
            List<YuntingCommentDTO> batchComments = convertToCommentDTOList(data);
            allComments.addAll(batchComments);
        });

        log.info("云听CEM - 批量拉取完成，总数据量：{}", allComments.size());
        return allComments;
    }

    /**
     * 创建CommentApi实例
     */
    private CommentApi createCommentApi(YuntingCredentialDTO credential) throws ApiException {
        // 检查Token是否过期
        if (credential.isExpired()) {
            log.warn("云听CEM - 访问令牌已过期，正在刷新...");
            return YuntingApiUtils.createAuthenticatedCommentApi(
                    credential.getSource(), 
                    credential.getThirdPartyId()
            );
        }

        // 使用现有Token创建API
        CommentApi commentApi = new CommentApi();
        commentApi.getApiClient().setAccessToken(credential.getAccessToken());
        return commentApi;
    }

    /**
     * 验证凭证
     */
    private void validateCredential(YuntingCredentialDTO credential) throws ApiException {
        if (credential == null) {
            throw new ApiException("云听凭证不能为空");
        }
        if (credential.getProjectId() == null) {
            throw new ApiException("项目ID不能为空");
        }
    }

    /**
     * 验证响应
     */
    private void validateResponse(CommentPullResponse response) throws ApiException {
        if (response == null) {
            throw new ApiException("响应为空");
        }

        Integer code = response.getCode();
        if (!YuntingErrorCodeEnum.isSuccess(code)) {
            YuntingErrorCodeEnum errorCode = YuntingErrorCodeEnum.getByCode(code);
            throw new ApiException(code, "云听CEM API调用失败: " + errorCode.getDescription() + " - " + response.getMsg());
        }

        if (response.getResult() == null) {
            throw new ApiException("响应结果为空");
        }
    }

    /**
     * 转换响应为结果DTO
     */
    private CommentPullResultDTO convertToResult(CommentPullResponse response) {
        CommentPullResponse.CommentResult result = response.getResult();

        List<YuntingCommentDTO> comments = new ArrayList<>();
        if (!CollectionUtils.isEmpty(result.getData())) {
            comments = convertToCommentDTOList(result.getData());
        }

        return CommentPullResultDTO.builder()
                .count(result.getCount())
                .comments(comments)
                .pageToken(result.getPageToken())
                .hasMore(result.getHasMore())
                .traceId(response.getTraceId())
                .pullTimestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 转换评论数据为DTO列表
     */
    private List<YuntingCommentDTO> convertToCommentDTOList(List<CommentData> dataList) {
        if (CollectionUtils.isEmpty(dataList)) {
            return new ArrayList<>();
        }

        return dataList.stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换单个评论数据为DTO
     */
    private YuntingCommentDTO convertToCommentDTO(CommentData data) {
        return YuntingCommentDTO.builder()
                .unique(data.getUnique())
                .publishTime(data.getPublishTime())
                .sourceName(data.getSourceName())
                .connectionName(data.getConnectionName())
                .projectName(data.getProjectName())
                .url(data.getUrl())
                .title(data.getTitle())
                .content(data.getContent())
                .userId(data.getUserId())
                .userName(data.getUserName())
                .userImg(data.getUserImg())
                .group(data.getGroup())
                .parent(data.getParent())
                .oid(data.getOid())
                .dataLevel(data.getDataLevel())
                .isDefault(data.getIsDefault())
                .escore(data.getEscore())
                .pictures(data.getPictures())
                .videos(data.getVideos())
                .comments(data.getComments())
                .likes(data.getLikes())
                .views(data.getViews())
                .insertTimestamp(data.getInsertTimestamp())
                .topicConfigsJson(JSON.toJSONString(data.getTopicConfigs()))
                .tagListJson(JSON.toJSONString(data.getTagList()))
                .textRanksJson(JSON.toJSONString(data.getTextRanks()))
                .phrasesJson(JSON.toJSONString(data.getPhrases()))
                .translateListJson(JSON.toJSONString(data.getTranslateList()))
                .build();
    }

    /**
     * 处理API异常并返回友好的错误信息
     */
    public String handleException(ApiException e) {
        return YuntingApiUtils.handleApiException(e);
    }
}

