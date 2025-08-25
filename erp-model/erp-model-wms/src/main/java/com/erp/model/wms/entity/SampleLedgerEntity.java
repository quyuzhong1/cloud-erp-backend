package com.erp.model.wms.entity;

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
 * 样品库存统计
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_ledger")
public class SampleLedgerEntity extends BaseEntity<SampleLedgerEntity> {

    /**
    * 归属用户ID
    */
    @TableField("user_id")
    private String userId;
    /**
    * 归属用户姓名
    */
    @TableField("user_name")
    private String userName;
    /**
    * 归属部门ID
    */
    @TableField("dept_id")
    private String deptId;
    /**
    * 归属部门名称
    */
    @TableField("dept_name")
    private String deptName;
    /**
    * SKU编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 使用方id
    */
    @TableField("use_user_id")
    private String useUserId;
    /**
    * 使用方名称
    */
    @TableField("use_user_name")
    private String useUserName;
    /**
    * 库存数量（可为正数或负数）
    */
    @TableField("qty")
    private Integer qty;
    /**
     * SKU ID
     */
    @TableField("sku_id")
    private String skuId;

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String DEPT_ID = "dept_id";

    public static final String DEPT_NAME = "dept_name";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String USE_USER_ID = "use_user_id";

    public static final String USE_USER_NAME = "use_user_name";

    public static final String QTY = "qty";

    public static final String SKU_ID = "sku_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}