package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 中台FBA库存信息
 * </p>
 *
 * @author Jim
 * @since 2025-04-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_fba_myi_all_inventory")
public class DmpFbaMyiAllInventoryEntity extends BaseEntity<DmpFbaMyiAllInventoryEntity> {

    /**
    * 平台SPU
    */
    @TableField("asin")
    private String asin;
    /**
    * 卖家sku
    */
    @TableField("msku")
    private String msku;
    /**
    * FNSKU
    */
    @TableField("fn_sku")
    private String fnSku;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 平台最后更新时间
    */
    @TableField("last_platform_update_time")
    private LocalDateTime lastPlatformUpdateTime;
    /**
    * 亚马逊库存SKU使用状况
    */
    @TableField("condition")
    private String condition;
    /**
    * 亚马逊站点代号
    */
    @TableField("marketplace_id")
    private String marketplaceId;
    /**
    * 输入任务ID
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 亚马逊账号代号
    */
    @TableField("platform_shop_code")
    private String platformShopCode;
    /**
    * 任务转换ID
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 店铺ID
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 任务来源唯一加密代号
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 任务数据加密代号
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 计划入库数量
    */
    @TableField("inbound_working_qty")
    private Integer inboundWorkingQty;
    /**
    * 已发货数量
    */
    @TableField("inbound_shipped_qty")
    private Integer inboundShippedQty;
    /**
    * 入库中数量
    */
    @TableField("inbound_receiving_qty")
    private Integer inboundReceivingQty;
    /**
    * FBA可售
    */
    @TableField("fulfillable_qty")
    private Integer fulfillableQty;
    /**
     * FBM可售
     */
    @TableField("fbm_fulfillable_qty")
    private Integer fbmFulfillableQty;
    /**
     * 调查中数量
     */
    @TableField("researching_qty")
    private Integer researchingQty;
    /**
     * 不可售数量
     */
    @TableField("unsellable_qty")
    private Integer unsellableQty;
    /**
     * 是否是FBM
     */
    @TableField("mfn_listing_exists")
    private String mfnListingExists;
    /**
     * 是否是FBA
     */
    @TableField("afn_listing_exists")
    private String afnListingExists;


    public static final String ASIN = "asin";

    public static final String MSKU = "msku";

    public static final String FN_SKU = "fn_sku";

    public static final String PRODUCT_NAME = "product_name";

    public static final String LAST_PLATFORM_UPDATE_TIME = "last_platform_update_time";

    public static final String CONDITION = "condition";

    public static final String MARKETPLACE_ID = "marketplace_id";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String PLATFORM_SHOP_CODE = "platform_shop_code";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String INBOUND_WORKING_QTY = "inbound_working_qty";

    public static final String INBOUND_SHIPPED_QTY = "inbound_shipped_qty";

    public static final String INBOUND_RECEIVING_QTY = "inbound_receiving_qty";

    public static final String FULFILLABLE_QTY = "fulfillable_qty";

    public static final String RESERVED_QTY = "reserved_qty";

    public static final String RESEARCHING_QTY = "researching_qty";

    public static final String UNSELLABLE_QTY = "unsellable_qty";

    public static final String INVENTORY_AGE = "inventory_age";

    @Override
    public Serializable pkVal() {
        return null;
    }

}