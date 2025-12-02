package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 达人社媒数据表
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_social_media")
public class KolSocialMediaEntity extends BaseEntity<KolSocialMediaEntity> {

    /**
    * 类型：manual-手动，auto-自动
    */
    @TableField("type")
    private String type;
    /**
    * 来源平台，如小红书、微博、抖音等（对应第三方sourceName）
    */
    @TableField("media_platform")
    private String mediaPlatform;
    /**
    * 第三方平台
    */
    @TableField("third_platform")
    private String thirdPlatform;
    /**
    * 账号ID（对应第三方userId）
    */
    @TableField("platform_account_id")
    private String platformAccountId;
    /**
    * 账号名称（对应第三方userName）
    */
    @TableField("platform_account_name")
    private String platformAccountName;
    /**
    * 发布时间，格式：yyyy-MM-dd HH:mm:ss（对应第三方publishTime）
    */
    @TableField("publish_time")
    private String publishTime;
    /**
    * 原平台链接（完整链接，对应第三方url）
    */
    @TableField("url")
    private String url;
    /**
    * 原平台链接哈希值（MD5或SHA256，用于查询索引）
    */
    @TableField("url_hash")
    private String urlHash;
    /**
    * 帖子标题
    */
    @TableField("title")
    private String title;
    /**
    * 入库时间，毫秒时间戳（对应第三方insertTimestamp）
    */
    @TableField("insert_timestamp")
    private Long insertTimestamp;
    /**
    * 粉丝量
    */
    @TableField("follower_count")
    private Long followerCount;
    /**
    * 阅读量（对应第三方views）
    */
    @TableField("view_count")
    private Long viewCount;
    /**
    * 播放量
    */
    @TableField("play_count")
    private Long playCount;
    /**
    * 评论数（对应第三方comments）
    */
    @TableField("comment_count")
    private Long commentCount;
    /**
    * 点赞数（对应第三方likes）
    */
    @TableField("like_count")
    private Long likeCount;
    /**
    * 转发量
    */
    @TableField("repost_count")
    private Long repostCount;
    /**
    * 唯一键，用于去重覆盖（对应第三方unique）。云听系统可能因模型优化等原因重跑数据，同一条数据的字段可能更新。此类变更不会影响unique值，但会更新insertTimestamp入库时间，因此，请始终以最新拉取的数据为准，按unique主键进行幂等性写入，覆盖本地旧数据
    */
    @TableField("unique_key")
    private String uniqueKey;


    public static final String TYPE = "type";

    public static final String MEDIA_PLATFORM = "media_platform";

    public static final String THIRD_PLATFORM = "third_platform";

    public static final String PLATFORM_ACCOUNT_ID = "platform_account_id";

    public static final String PLATFORM_ACCOUNT_NAME = "platform_account_name";

    public static final String PUBLISH_TIME = "publish_time";

    public static final String URL = "url";

    public static final String URL_HASH = "url_hash";

    public static final String TITLE = "title";

    public static final String INSERT_TIMESTAMP = "insert_timestamp";

    public static final String FOLLOWER_COUNT = "follower_count";

    public static final String VIEW_COUNT = "view_count";

    public static final String PLAY_COUNT = "play_count";

    public static final String COMMENT_COUNT = "comment_count";

    public static final String LIKE_COUNT = "like_count";

    public static final String REPOST_COUNT = "repost_count";

    public static final String UNIQUE_KEY = "unique_key";

    @Override
    public Serializable pkVal() {
        return null;
    }

}