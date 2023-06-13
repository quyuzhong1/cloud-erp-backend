package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 其他出库客户表
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("other_outstock_customer")
public class OtherOutstockCustomerEntity extends BaseEntity<OtherOutstockCustomerEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 客户名称
     */
    @TableField("name")
    private String name;

    /**
     * 收货地址
     */
    @TableField("receive_address")
    private String receiveAddress;

    /**
     * 收货人名称
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 联系电话
     */
    @TableField("tel_number")
    private String telNumber;


    public static final String MAIN_ID = "main_id";

    public static final String NAME = "name";

    public static final String RECEIVE_ADDRESS = "receive_address";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String TEL_NUMBER = "tel_number";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
