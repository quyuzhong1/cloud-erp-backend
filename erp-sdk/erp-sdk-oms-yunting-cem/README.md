# 云听CEM开放平台SDK

## 简介

云听CEM（Customer Experience Management）开放平台SDK，用于对接云听社交媒体评论数据。

支持的社交媒体平台：
- 小红书
- 抖音
- 公众号
- 微博
- Facebook
- Instagram
- Reddit
等

## 功能特性

1. **访问凭证管理**
   - 获取访问令牌（OAuth2）
   - 自动令牌刷新
   - 令牌过期检测

2. **社交媒体评论数据拉取**
   - 单次拉取（支持分页）
   - 批量拉取（自动分页）
   - 时间范围筛选
   - 频率限制控制（每分钟最多30次）

3. **数据结构完整**
   - 评论基础信息
   - 用户信息
   - 情感分析
   - 主题标签
   - 翻译结果
   - 热词短语

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.cloud.erp</groupId>
    <artifactId>erp-sdk-oms-yunting-cem</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 获取访问凭证

```java
@Resource
private YuntingTokenHandler tokenHandler;

// 获取访问凭证
YuntingCredentialDTO credential = tokenHandler.getCredential(
    "your_source",          // 来源标识（由云听预设）
    "your_third_party_id",  // 第三方应用ID
    "your_project_id"       // 项目ID
);
```

### 3. 拉取评论数据

#### 单次拉取（带分页）

```java
@Resource
private YuntingCommentHandler commentHandler;

// 首次拉取
CommentPullResultDTO result = commentHandler.pullComments(
    credential,
    "2025-12-01 00:00:00",  // 开始时间
    "2025-12-04 23:59:59",  // 结束时间
    null                     // 首次请求不传pageToken
);

// 处理数据
List<YuntingCommentDTO> comments = result.getComments();
for (YuntingCommentDTO comment : comments) {
    System.out.println(comment.getContent());
}

// 如果还有更多数据，使用pageToken继续拉取
if (Boolean.TRUE.equals(result.getHasMore())) {
    CommentPullResultDTO nextResult = commentHandler.pullComments(
        credential,
        "2025-12-01 00:00:00",
        "2025-12-04 23:59:59",
        result.getPageToken()  // 使用上次返回的pageToken
    );
}
```

#### 批量拉取（自动分页）

```java
// 批量拉取所有数据（自动处理分页）
List<YuntingCommentDTO> allComments = commentHandler.pullAllComments(
    credential,
    "2025-12-01 00:00:00",
    "2025-12-04 23:59:59"
);

System.out.println("总共拉取了 " + allComments.size() + " 条评论");
```

### 4. 使用API直接调用

```java
// 创建已认证的CommentApi
CommentApi commentApi = YuntingApiUtils.createAuthenticatedCommentApi(
    "your_source",
    "your_third_party_id"
);

// 构建请求
CommentPullRequest request = CommentPullRequest.builder()
    .projectId("your_project_id")
    .startTime("2025-12-01 00:00:00")
    .endTime("2025-12-04 23:59:59")
    .build();

// 拉取数据
CommentPullResponse response = commentApi.pullComments(request);
```

## API说明

### TokenApi

**获取访问凭证**

- 接口地址：`GET /oauth2/token`
- 参数：
  - `source`: 来源标识（必填）
  - `third_party_id`: 第三方应用ID（必填）
- 返回：`TokenResponse`
  - `access_token`: 访问令牌
  - `expires_in`: 过期时间（秒，默认1800秒）

### CommentApi

**拉取社交媒体评论数据**

- 接口地址：`POST /api/comment/v1/social/pull`
- 认证方式：Bearer Token（Authorization头）
- 请求参数：
  - `projectId`: 项目ID（必填）
  - `startTime`: 开始时间，格式：yyyy-MM-dd HH:mm:ss（选填）
  - `endTime`: 结束时间，格式：yyyy-MM-dd HH:mm:ss（选填）
  - `pageToken`: 分页Token（选填，首次请求不传）
- 频率限制：每分钟最多30次调用（IP限制）
- 单次返回：固定1000条数据

## 错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|---------|
| 20000 | 操作成功 | - |
| 1001 | 参数projectId无效 | 检查projectId是否正确 |
| 1002 | 参数startTime或endTime无效 | 检查时间格式是否为 yyyy-MM-dd HH:mm:ss |
| 1003 | 参数pageToken无效 | 检查pageToken是否有效 |
| 2001 | 认证失败 | access_token已过期，需要重新获取 |
| 2002 | 项目权限未配置 | 联系云听团队配置项目权限 |
| 3001 | 频率限制 | 降低调用频率，建议每次请求间隔≥3秒 |

## 数据模型

### YuntingCommentDTO

评论数据传输对象，包含以下主要字段：

- `unique`: 唯一键（用于去重）
- `publishTime`: 发布时间
- `sourceName`: 来源平台
- `content`: 评论内容
- `userName`: 用户昵称
- `escore`: 情感（正面/中性/负面/混合）
- `dataLevel`: 数据类型（帖子/评论/回复）
- `likes`: 点赞数
- `comments`: 评论数
- `views`: 浏览数
- `topicConfigsJson`: 主题配置（JSON）
- `tagListJson`: 标签列表（JSON）
- `translateListJson`: 翻译结果（JSON）

## 注意事项

1. **访问令牌有效期**：默认30分钟，建议在过期前刷新
2. **频率限制**：每分钟最多30次调用，建议每次请求间隔≥3秒
3. **数据去重**：使用`unique`字段作为主键进行幂等写入
4. **数据更新**：云听可能因模型优化重跑数据，以最新数据为准
5. **时间格式**：严格遵循 `yyyy-MM-dd HH:mm:ss` 格式

## 联系方式

如需获取以下信息，请联系云听CEM团队：
- `source` 来源标识
- `third_party_id` 第三方应用ID
- `projectId` 项目ID
- API权限配置

## License

Copyright © 2025 ERP System

