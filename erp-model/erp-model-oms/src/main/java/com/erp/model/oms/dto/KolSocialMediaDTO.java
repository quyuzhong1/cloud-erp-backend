package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 达人社媒数据表请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
*/
@Data
@NoArgsConstructor
public class KolSocialMediaDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 类型：manual-手动，auto-自动
        */
        private String type;

        /**
        * 来源平台，如小红书、微博、抖音等（对应第三方sourceName）
        */
        private String mediaPlateform;

        /**
        * 第三方平台
        */
        private String thridPlateform;

        /**
        * 账号ID（对应第三方userId）
        */
        private String platformAccountId;

        /**
        * 账号名称（对应第三方userName）
        */
        private String platformAccountName;

        /**
        * 发布时间，格式：yyyy-MM-dd HH:mm:ss（对应第三方publishTime）
        */
        private String publishTime;

        /**
        * 原平台链接（完整链接，对应第三方url）
        */
        private String url;

        /**
        * 原平台链接哈希值（MD5或SHA256，用于查询索引）
        */
        private String urlHash;

        /**
        * 帖子标题
        */
        private String title;

        /**
        * 入库时间，毫秒时间戳（对应第三方insertTimestamp）
        */
        private Long insertTimestamp;

        /**
        * 粉丝量
        */
        private Long followerCount;

        /**
        * 阅读量（对应第三方views）
        */
        private Long viewCount;

        /**
        * 播放量
        */
        private Long playCount;

        /**
        * 评论数（对应第三方comments）
        */
        private Long commentCount;

        /**
        * 点赞数（对应第三方likes）
        */
        private Long likeCount;

        /**
        * 转发量
        */
        private Long repostCount;

        /**
        * 唯一键，用于去重覆盖（对应第三方unique）。云听系统可能因模型优化等原因重跑数据，同一条数据的字段可能更新。此类变更不会影响unique值，但会更新insertTimestamp入库时间，因此，请始终以最新拉取的数据为准，按unique主键进行幂等性写入，覆盖本地旧数据
        */
        private String uniqueKey;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 类型：manual-手动，auto-自动
        */
        @NotBlank(message = "类型：manual不能为空")
        @Size(max = 20,message = "类型：manual最大长度不能超过20位")
        private String type;

        /**
        * 来源平台，如小红书、微博、抖音等（对应第三方sourceName）
        */
        @NotBlank(message = "来源平台，如小红书、微博、抖音等（对应第三方sourceName）不能为空")
        @Size(max = 100,message = "来源平台，如小红书、微博、抖音等（对应第三方sourceName）最大长度不能超过100位")
        private String mediaPlateform;

        /**
        * 第三方平台
        */
        @NotBlank(message = "第三方平台不能为空")
        @Size(max = 100,message = "第三方平台最大长度不能超过100位")
        private String thridPlateform;

        /**
        * 账号ID（对应第三方userId）
        */
        @NotBlank(message = "账号ID（对应第三方userId）不能为空")
        @Size(max = 100,message = "账号ID（对应第三方userId）最大长度不能超过100位")
        private String platformAccountId;

        /**
        * 账号名称（对应第三方userName）
        */
        @NotBlank(message = "账号名称（对应第三方userName）不能为空")
        @Size(max = 200,message = "账号名称（对应第三方userName）最大长度不能超过200位")
        private String platformAccountName;

        /**
        * 发布时间，格式：yyyy-MM-dd HH:mm:ss（对应第三方publishTime）
        */
        @NotBlank(message = "发布时间，格式：yyyy不能为空")
        @Size(max = 50,message = "发布时间，格式：yyyy最大长度不能超过50位")
        private String publishTime;

        /**
        * 原平台链接（完整链接，对应第三方url）
        */
        private String url;

        /**
        * 原平台链接哈希值（MD5或SHA256，用于查询索引）
        */
        @NotBlank(message = "原平台链接哈希值（MD5或SHA256，用于查询索引）不能为空")
        @Size(max = 64,message = "原平台链接哈希值（MD5或SHA256，用于查询索引）最大长度不能超过64位")
        private String urlHash;

        /**
        * 帖子标题
        */
        @NotBlank(message = "帖子标题不能为空")
        @Size(max = 500,message = "帖子标题最大长度不能超过500位")
        private String title;

        /**
        * 入库时间，毫秒时间戳（对应第三方insertTimestamp）
        */
        @NotNull(message = "入库时间，毫秒时间戳（对应第三方insertTimestamp）不能为空")
        private Long insertTimestamp;

        /**
        * 粉丝量
        */
        @NotNull(message = "粉丝量不能为空")
        private Long followerCount;

        /**
        * 阅读量（对应第三方views）
        */
        @NotNull(message = "阅读量（对应第三方views）不能为空")
        private Long viewCount;

        /**
        * 播放量
        */
        @NotNull(message = "播放量不能为空")
        private Long playCount;

        /**
        * 评论数（对应第三方comments）
        */
        @NotNull(message = "评论数（对应第三方comments）不能为空")
        private Long commentCount;

        /**
        * 点赞数（对应第三方likes）
        */
        @NotNull(message = "点赞数（对应第三方likes）不能为空")
        private Long likeCount;

        /**
        * 转发量
        */
        @NotNull(message = "转发量不能为空")
        private Long repostCount;

        /**
        * 唯一键，用于去重覆盖（对应第三方unique）。云听系统可能因模型优化等原因重跑数据，同一条数据的字段可能更新。此类变更不会影响unique值，但会更新insertTimestamp入库时间，因此，请始终以最新拉取的数据为准，按unique主键进行幂等性写入，覆盖本地旧数据
        */
        @NotBlank(message = "唯一键，用于去重覆盖（对应第三方unique）。云听系统可能因模型优化等原因重跑数据，同一条数据的字段可能更新。此类变更不会影响unique值，但会更新insertTimestamp入库时间，因此，请始终以最新拉取的数据为准，按unique主键进行幂等性写入，覆盖本地旧数据不能为空")
        @Size(max = 200,message = "唯一键，用于去重覆盖（对应第三方unique）。云听系统可能因模型优化等原因重跑数据，同一条数据的字段可能更新。此类变更不会影响unique值，但会更新insertTimestamp入库时间，因此，请始终以最新拉取的数据为准，按unique主键进行幂等性写入，覆盖本地旧数据最大长度不能超过200位")
        private String uniqueKey;


    }


}