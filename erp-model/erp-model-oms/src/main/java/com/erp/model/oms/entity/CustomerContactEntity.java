package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 客户联系人信息
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("customer_contact")
public class CustomerContactEntity extends BaseEntity<CustomerContactEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 客户联系人编号
     */
    @TableField("code")
    private String code;

    /**
     * 联系人
     */
    @TableField("person")
    private String person;

    /**
     * 职位
     */
    @TableField("position")
    private String position;

    /**
     * 联系电话
     */
    @TableField("tel_number")
    private String telNumber;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 是否默认 true 是
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 是否禁用 true 是
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
     */
    @TableField("sync_kingdee_status")
    private String syncKingdeeStatus;

    /**
     * 同步金蝶时间
     */
    @TableField("sync_kingdee_time")
    private LocalDateTime syncKingdeeTime;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 同步操作
     */
    @TableField("sync_operate")
    private String syncOperate;


    public static final String MAIN_ID = "main_id";

    public static final String PERSON = "person";

    public static final String POSITION = "position";

    public static final String TEL_NUMBER = "tel_number";

    public static final String EMAIL = "email";

    public static final String IS_DEFAULT = "is_default";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
