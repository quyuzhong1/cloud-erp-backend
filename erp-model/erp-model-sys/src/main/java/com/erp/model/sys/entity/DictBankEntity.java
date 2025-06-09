package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 银行 字典表
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("dict_bank")
@EqualsAndHashCode
public class DictBankEntity extends BaseEntity<DictBankEntity> {

    /**
     * 银行名称
     */
    @TableField("name")
    private String name;

    /**
     * 客服电话
     */
    @TableField("services_phone")
    private String servicesPhone;

    /**
     * 总部地址
     */
    @TableField("headquarter_address")
    private String headquarterAddress;


    public static final String FIELD_NAME = "name";

    public static final String SERVICES_PHONE = "services_phone";

    public static final String HEADQUARTER_ADDRESS = "headquarter_address";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
