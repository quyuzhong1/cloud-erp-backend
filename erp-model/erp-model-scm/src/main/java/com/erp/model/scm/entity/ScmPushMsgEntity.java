package com.erp.model.scm.entity;

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
 * 本地推送消息表
 * </p>
 *
 * @author shukai
 * @since 2024-08-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("scm_push_msg")
public class ScmPushMsgEntity extends BaseEntity<ScmPushMsgEntity> {

    /**
    * 目标系统
    */
    @TableField("target_platform")
    private String targetPlatform;
    /**
    * 来源系统
    */
    @TableField("source_platform")
    private String sourcePlatform = "scm";
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源编号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 操作类型
    */
    @TableField("sync_operate")
    private String syncOperate;
    /**
    * 推送数据
    */
    @TableField("push_data")
    private String pushData;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 父id
    */
    @TableField("parent_id")
    private String parentId;


    public static final String TARGET_PLATFORM = "target_platform";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SYNC_OPERATE = "sync_operate";

    public static final String PUSH_DATA = "push_data";

    public static final String REMARK = "remark";

    public static final String PARENT_ID = "parent_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}