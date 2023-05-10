package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 客户地址信息
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("customer_address")
public class CustomerAddressEntity extends BaseEntity<CustomerAddressEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

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
     * 地址信息
     */
    @TableField("address")
    private String address;

    /**
     * 联系人
     */
    @TableField("person")
    private String person;

    /**
     * 地址类型
     */
    @TableField("type")
    private String type;


    public static final String MAIN_ID = "main_id";

    public static final String TEL_NUMBER = "tel_number";

    public static final String EMAIL = "email";

    public static final String IS_DEFAULT = "is_default";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    public static final String ADDRESS = "address";

    public static final String PERSON = "person";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
