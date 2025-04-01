package com.erp.model.mrp.entity;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;


/**
 * <p>
 * 通知配置表
 * </p>
 *
 * @author will
 * @since 2025-02-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_notice")
public class CfgNoticeEntity extends BaseEntity<CfgNoticeEntity> {

    /**
    * 通知节点
    */
    @TableField("notice_node")
    private String noticeNode;
    /**
    * 通知规则
    */
    @TableField(value = "notice_rule", jdbcType = JdbcType.OTHER)
    private JSONObject noticeRule;
    /**
    * 通知平台
    */
    @TableField("notice_platform")
    private String noticePlatform;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String NOTICE_NODE = "notice_node";

    public static final String NOTICE_RULE = "notice_rule";

    public static final String NOTICE_PLATFORM = "notice_platform";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}