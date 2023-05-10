package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 客户销售员信息
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("customer_seller")
public class CustomerSellerEntity extends BaseEntity<CustomerSellerEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 部门id
     */
    @TableField("dept_id")
    private String deptId;

    /**
     * 销售员id
     */
    @TableField("seller_id")
    private String sellerId;

    /**
     * 销售员
     */
    @TableField("seller_name")
    private String sellerName;

    /**
     * 开始日期
     */
    @TableField("start_date")
    private Date startDate;

    /**
     * 结束日期
     */
    @TableField("end_date")
    private Date endDate;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String DEPT_ID = "dept_id";

    public static final String SELLER_ID = "seller_id";

    public static final String SELLER_NAME = "seller_name";

    public static final String START_DATE = "start_date";

    public static final String END_DATE = "end_date";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
