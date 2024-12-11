package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 模具主表
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mould_info")
public class MouldInfoEntity extends BaseEntity<MouldInfoEntity> {

    /**
    * 项目编号
    */
    @TableField("project_no")
    private String projectNo;
    /**
    * 项目名称
    */
    @TableField("name")
    private String name;
    /**
    * 状态
    */
    @TableField("status")
    private String status;
    /**
    * 产品经理
    */
    @TableField("product_manager_id")
    private String productManagerId;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 分类id
    */
    @TableField("category_id")
    private String categoryId;
    /**
    * 模具分类编码
    */
    @TableField("mould_category_code")
    private String mouldCategoryCode;
    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
     * 审核人id
     */
    @TableField("approve_user_id")
    private String approveUserId;

    /**
     * 作废状态（false未作废，true已作废）
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;

    /**
     * 作废时间
     */
    @TableField("invalid_time")
    private LocalDateTime invalidTime;

    /**
     * 作废描述
     */
    @TableField("invalid_remark")
    private String invalidRemark;


    public static final String PROJECT_NO = "project_no";

    public static final String NAME = "name";

    public static final String STATUS = "status";

    public static final String PRODUCT_MANAGER_ID = "product_manager_id";

    public static final String REMARK = "remark";

    public static final String CATEGORY_ID = "category_id";

    public static final String MOULD_CATEGORY_CODE = "mould_category_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}