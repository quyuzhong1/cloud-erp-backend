package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


/**
 * <p>
 * 第三方库存流水表
 * </p>
 *
 * @author Jim
 * @since 2025-04-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_third_inventory_trans_flow")
public class DmpThirdInventoryTransFlowEntity extends BaseEntity<DmpThirdInventoryTransFlowEntity> {

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
    /**
     * 来源平台
     */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
     * 库存流水ID
     */
    @TableField("third_id")
    private String thirdId;
    /**
     * 关联单号
     */
    @TableField("reference_no")
    private String referenceNo;
    /**
     * 应用编码
     */
    @TableField("application_code")
    private String applicationCode;
    /**
     * 应用编码描述
     */
    @TableField("application_code_desc")
    private String applicationCodeDesc;
    /**
     * 商品SKU
     */
    @TableField("product_sku")
    private String productSku;
    /**
     * 仓库编码
     */
    @TableField("warehouse_code")
    private String warehouseCode;
    /**
     * 仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
     * 库存变更类型
     */
    @TableField("inventory_change_type")
    private String inventoryChangeType;
    /**
     * 库存变更类型描述
     */
    @TableField("inventory_change_type_desc")
    private String inventoryChangeTypeDesc;
    /**
     * 商品品质:0=全部,1=良品,2=不良品  枚举：DmpThirdInventoryTransFlowProductTypeEnum
     */
    @TableField("product_type")
    private String productType;
    /**
     * 库存变更数量
     */
    @TableField("change_qty")
    private Integer changeQty;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 库存变更时间
     */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;
    /**
     * 来源ID
     */
    @TableField("main_id")
    private String mainId;
    /**
     * 授权ID
     */
    @TableField("auth_id")
    private String authId;


    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String THIRD_ID = "third_id";

    public static final String REFERENCE_NO = "reference_no";

    public static final String APPLICATION_CODE = "application_code";

    public static final String APPLICATION_CODE_DESC = "application_code_desc";

    public static final String PRODUCT_SKU = "product_sku";

    public static final String WAREHOUSE_CODE = "warehouse_code";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String INVENTORY_CHANGE_TYPE = "inventory_change_type";

    public static final String INVENTORY_CHANGE_TYPE_DESC = "inventory_change_type_desc";

    public static final String PRODUCT_TYPE = "product_type";

    public static final String CHANGE_QTY = "change_qty";

    public static final String REMARK = "remark";

    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String SOURCE_ID = "source_id";

    public static final String AUTH_ID = "auth_id";

}
