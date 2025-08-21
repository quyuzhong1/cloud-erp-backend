package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 样品台账
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sample_ledger_flow")
public class SampleLedgerFlowEntity extends BaseEntity<SampleLedgerFlowEntity> {

    @TableField("sample_leder_id")
    private String sampleLederId;

    /**
     * 操作时间
     */
    @TableField("operate_time")
    private Date operateTime;

    /**
     * 业务时间
     */
    @TableField("bill_date")
    private Date billDate;

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
     * 单据编号
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 单据类型
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 单据ID
     */
    @TableField("source_id")
    private String sourceId;

    @TableField("source_detail_id")
    private String sourceDetailId;

    /**
     * 操作类型
     */
    @TableField("dict_biz_type")
    private String dictBizType;

    /**
     * SKU编号
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * SKU ID
     */
    @TableField("sku_id")
    private String skuId;

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
     * 数量
     */
    @TableField("qty")
    private Integer qty;


    public static final String SAMPLE_LEDER_ID = "sample_leder_id";

    public static final String OPERATE_TIME = "operate_time";

    public static final String BILL_DATE = "bill_date";

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String DEPT_ID = "dept_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String DICT_BIZ_TYPE = "dict_biz_type";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String PRODUCT_NAME = "product_name";

    public static final String USE_USER_ID = "use_user_id";

    public static final String USE_USER_NAME = "use_user_name";

    public static final String QTY = "qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
