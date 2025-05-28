package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 三方审批生成
 * </p>
 *
 * @author hcg
 * @since 2025-05-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_third_process")
public class CfgThirdProcessEntity extends BaseEntity<CfgThirdProcessEntity> {

    /**
    * 配置编码
    */
    @TableField("code")
    private String code;
    /**
    * 配置名称
    */
    @TableField("name")
    private String name;
    /**
    * 配置单据
    */
    @TableField("bussiness_key")
    private String bussinessKey;
    /**
    * 生成/更新配置
    */
    @TableField("operate_type")
    private String operateType;
    /**
    * 流程来源平台
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 启用状态
    */
    @TableField("enable_status")
    private Boolean enableStatus;
    /**
    * 启用时间
    */
    @TableField("enable_time")
    private LocalDateTime enableTime;
    /**
     * 第三方审批定义code
     */
    @TableField("third_process_definition_code")
    private String thirdProcessDefinitionCode;

    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String BUSSINESS_KEY = "bussiness_key";

    public static final String OPERATE_TYPE = "operate_type";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String ENABLE_STATUS = "enable_status";

    public static final String ENABLE_TIME = "enable_time";

    public static final String THIRD_PROCESS_DEFINITION_CODE = "third_process_definition_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}