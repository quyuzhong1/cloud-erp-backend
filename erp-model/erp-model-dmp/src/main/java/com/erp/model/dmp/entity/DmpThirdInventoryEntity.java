package com.erp.model.dmp.entity;

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
 * 第三方仓库存
 * </p>
 *
 * @author shukai
 * @since 2024-08-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_third_inventory")
public class DmpThirdInventoryEntity extends BaseEntity<DmpThirdInventoryEntity> {

    /**
    * 仓库平台类型
    */
    @TableField("warehouse_platform_type")
    private String warehousePlatformType;
    /**
    * 来源平台（编码）：goodcang、iml
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * erp授权Id
    */
    @TableField("auth_id")
    private String authId;
    /**
    * 第三方仓仓库代码
    */
    @TableField("platform_warehouse_code")
    private String platformWarehouseCode;
    /**
    * 第三方仓仓库名称
    */
    @TableField("platform_warehouse_name")
    private String platformWarehouseName;
    /**
    * 平台sku
    */
    @TableField("product_sku")
    private String productSku;
    /**
    * 尾程在途数量
    */
    @TableField("onway")
    private Integer onway;
    /**
    * 总尾程在途数量
    */
    @TableField("total_onway")
    private Integer totalOnway;
    /**
    * 发货在途数量
    */
    @TableField("transfer_onway")
    private Integer transferOnway;
    /**
    * 待上架数量
    */
    @TableField("pending")
    private Integer pending;
    /**
    * 可售数量
    */
    @TableField("sellable")
    private Integer sellable;
    /**
    * 不合格数量
    */
    @TableField("unsellable")
    private Integer unsellable;
    /**
    * 备货数量
    */
    @TableField("stocking")
    private Integer stocking;
    /**
    * 缺货数量
    */
    @TableField("pi_no_stock")
    private Integer piNoStock;
    /**
    * 待出库数量
    */
    @TableField("reserved")
    private Integer reserved;
    /**
    * 历史出库数量
    */
    @TableField("shipped")
    private Integer shipped;
    /**
    * 待确认数量
    */
    @TableField("unconfirmed")
    private Integer unconfirmed;
    /**
    * 冻结数量
    */
    @TableField("pi_freeze")
    private Integer piFreeze;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 转换id
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 唯一字段md5值
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 数据字段md5值
    */
    @TableField("data_encrypt")
    private String dataEncrypt;


    public static final String WAREHOUSE_PLATFORM_TYPE = "warehouse_platform_type";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String AUTH_ID = "auth_id";

    public static final String PLATFORM_WAREHOUSE_CODE = "platform_warehouse_code";

    public static final String PLATFORM_WAREHOUSE_NAME = "platform_warehouse_name";

    public static final String PRODUCT_SKU = "product_sku";

    public static final String ONWAY = "onway";

    public static final String TOTAL_ONWAY = "total_onway";

    public static final String TRANSFER_ONWAY = "transfer_onway";

    public static final String PENDING = "pending";

    public static final String SELLABLE = "sellable";

    public static final String UNSELLABLE = "unsellable";

    public static final String STOCKING = "stocking";

    public static final String PI_NO_STOCK = "pi_no_stock";

    public static final String RESERVED = "reserved";

    public static final String SHIPPED = "shipped";

    public static final String UNCONFIRMED = "unconfirmed";

    public static final String PI_FREEZE = "pi_freeze";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    @Override
    public Serializable pkVal() {
        return null;
    }

}