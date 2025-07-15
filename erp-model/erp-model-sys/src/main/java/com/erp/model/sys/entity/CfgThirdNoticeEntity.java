package com.erp.model.sys.entity;

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
 * 三方通知配置
 * </p>
 *
 * @author jack
 * @since 2025-05-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_third_notice")
public class CfgThirdNoticeEntity extends BaseEntity<CfgThirdNoticeEntity> {

    /**
    * 单据类型
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 通知节点
    */
    @TableField("notice_node")
    private String noticeNode;
    /**
    * 通知方式：single=单条,summary=汇总  枚举：CfgThirdNoticeMethodEnum
    */
    @TableField("method")
    private String method;
    /**
    * 通知状态
    */
    @TableField("notice_status")
    private Boolean noticeStatus;
    /**
    * 通知标题
    */
    @TableField("title")
    private String title;
    /**
    * 通知类型
    */
    @TableField("notice_type")
    private String noticeType;
    /**
    * 跳转链接
    */
    @TableField("url")
    private String url;
    /**
    * cron
    */
    @TableField("cron")
    private String cron;
    /**
     * 岗位
     */
    @TableField("post")
    private String post;
    /**
    * 通知人员
    */
    @TableField("role_type")
    private String roleType;
    /**
    * 具体人员
    */
    @TableField("specific_person")
    private String specificPerson;
    /**
    * 推送方式
    */
    @TableField("notice_method")
    private String noticeMethod;


    public static final String BUSINESS_TYPE = "business_type";

    public static final String NOTICE_NODE = "notice_node";

    public static final String METHOD = "method";

    public static final String NOTICE_STATUS = "notice_status";

    public static final String TITLE = "title";

    public static final String NOTICE_TYPE = "notice_type";

    public static final String URL = "url";

    public static final String CRON = "cron";

    public static final String ROLE_TYPE = "role_type";

    public static final String SPECIFIC_PERSON = "specific_person";

    public static final String NOTICE_METHOD = "notice_method";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
