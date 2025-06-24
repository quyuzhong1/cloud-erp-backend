package com.erp.model.workflow.entity;

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
 * 三方审批定义
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("third_process_definition")
public class ThirdProcessDefinitionEntity extends BaseEntity<ThirdProcessDefinitionEntity> {

    /**
    * 单据编码
    */
    @TableField("approval_code")
    private String approvalCode;
    /**
    * 状态
    */
    @TableField("status")
    private String status;
    /**
    * 单据名称
    */
    @TableField("name")
    private String name;
    /**
    * 归属平台
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 表单json
    */
    @TableField("form_json")
    private String formJson;
    /**
    * 审批组
    */
    @TableField("dict_approval_group")
    private String dictApprovalGroup;
    /**
    * 审批定义类型：发起/拉取
    */
    @TableField("type")
    private String type;
    /**
     * 启用状态
     */
    @TableField("enable_status")
    private Boolean enableStatus;

    public static final String APPROVAL_CODE = "approval_code";

    public static final String STATUS = "status";

    public static final String NAME = "name";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String FORM_JSON = "form_json";

    public static final String DICT_APPROVAL_GROUP = "dict_approval_group";

    public static final String TYPE = "type";

    public static final String ENABLE_STATUS = "enable_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}