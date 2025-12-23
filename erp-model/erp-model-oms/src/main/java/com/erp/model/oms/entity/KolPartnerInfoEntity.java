package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 企业达人库
 * </p>
 *
 * @author jack
 * @since 2025-12-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_partner_info")
public class KolPartnerInfoEntity extends BaseEntity<KolPartnerInfoEntity> {

    /**
    * 是否启用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 编号
    */
    @TableField("code")
    private String code;
    /**
    * 达人昵称
    */
    @TableField("nickname")
    private String nickname;
    /**
    * 达人类型
    */
    @TableField("type")
    private String type;
    /**
    * 合作类型
    */
    @TableField("cooperation_type")
    private String cooperationType;
    /**
    * 合作日期
    */
    @TableField("cooperation_date")
    private LocalDate cooperationDate;
    /**
    * 国家ID
    */
    @TableField("country_id")
    private String countryId;
    /**
    * 国家名称
    */
    @TableField("country_name")
    private String countryName;
    /**
    * 语言
    */
    @TableField("language")
    private String language;
    /**
    * 邮箱
    */
    @TableField("email")
    private String email;
    /**
    * 联系电话
    */
    @TableField("phone")
    private String phone;
    /**
    * 负责人ID
    */
    @TableField("charge_id")
    private String chargeId;
    /**
    * 负责人姓名
    */
    @TableField("charge_name")
    private String chargeName;
    /**
    * 部门ID
    */
    @TableField("dept_id")
    private String deptId;
    /**
    * 部门名称
    */
    @TableField("dept_name")
    private String deptName;


    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String CODE = "code";

    public static final String NICKNAME = "nickname";

    public static final String TYPE = "type";

    public static final String COOPERATION_TYPE = "cooperation_type";

    public static final String COOPERATION_DATE = "cooperation_date";

    public static final String COUNTRY_ID = "country_id";

    public static final String COUNTRY_NAME = "country_name";

    public static final String LANGUAGE = "language";

    public static final String EMAIL = "email";

    public static final String PHONE = "phone";

    public static final String CHARGE_ID = "charge_id";

    public static final String CHARGE_NAME = "charge_name";

    public static final String DEPT_ID = "dept_id";

    public static final String DEPT_NAME = "dept_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}