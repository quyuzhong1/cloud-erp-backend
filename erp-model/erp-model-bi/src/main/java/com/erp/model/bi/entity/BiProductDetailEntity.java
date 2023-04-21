package com.erp.model.bi.entity;

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
 * 产品sku表
 * </p>
 *
 * @author lambda
 * @since 2023-04-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("product_detail")
public class BiProductDetailEntity extends BaseEntity<BiProductDetailEntity> {

    /**
     * 产品表id
     */
    @TableField("product_id")
    private String productId;

    /**
     * sku
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 品名
     */
    @TableField("name")
    private String name;

    /**
     * 变体属性
     */
    @TableField("variant_property")
    private String variantProperty;

    /**
     * 计划上市时间
     */
    @TableField("plan_listing_time")
    private Date planListingTime;

    /**
     * 单位表id
     */
    @TableField("unit_id")
    private String unitId;

    /**
     * 产品开发状态 1.未开发 2.开发中 3.开发完成 4.中止开发 5.暂停开发
     */
    @TableField("product_state")
    private Integer productState;

    /**
     * sku图片
     */
    @TableField("images_url")
    private String imagesUrl;

    /**
     * 单位名称
     */
    @TableField("unit_name")
    private String unitName;

    /**
     * 产品负责人id
     */
    @TableField("charge_id")
    private String chargeId;

    /**
     * 产品负责人姓名
     */
    @TableField("charge_name")
    private String chargeName;

    /**
     * 任务状态 0待审核，1审核中，2审核通过，3审核不通过，4待提交
     */
    @TableField("status")
    private Integer status;

    /**
     * 流程id
     */
    @TableField("process_id")
    private String processId;

    /**
     * 流程表id
     */
    @TableField("business_process_id")
    private String businessProcessId;

    /**
     * 是否变更（0否，1是）
     */
    @TableField("is_change")
    private Integer isChange;

    /**
     * 首批量产入库时间
     */
    @TableField("first_mass_product_date")
    private Date firstMassProductDate;

    /**
     * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
     */
    @TableField("sync_kingdee_status")
    private String syncKingdeeStatus;

    /**
     * 同步时间
     */
    @TableField("sync_kingdee_time")
    private Date syncKingdeeTime;

    /**
     * 金蝶数据id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;


    public static final String PRODUCT_ID = "product_id";

    public static final String SKU_NO = "sku_no";

    public static final String NAME = "name";

    public static final String VARIANT_PROPERTY = "variant_property";

    public static final String PLAN_LISTING_TIME = "plan_listing_time";

    public static final String UNIT_ID = "unit_id";

    public static final String PRODUCT_STATE = "product_state";

    public static final String IMAGES_URL = "images_url";

    public static final String UNIT_NAME = "unit_name";

    public static final String CHARGE_ID = "charge_id";

    public static final String CHARGE_NAME = "charge_name";

    public static final String STATUS = "status";

    public static final String PROCESS_ID = "process_id";

    public static final String BUSINESS_PROCESS_ID = "business_process_id";

    public static final String IS_CHANGE = "is_change";

    public static final String FIRST_MASS_PRODUCT_DATE = "first_mass_product_date";

    public static final String SYNC_KINGDEE_STATUS = "sync_kingdee_status";

    public static final String SYNC_KINGDEE_TIME = "sync_kingdee_time";

    public static final String SYNC_KINGDEE_ID = "sync_kingdee_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
