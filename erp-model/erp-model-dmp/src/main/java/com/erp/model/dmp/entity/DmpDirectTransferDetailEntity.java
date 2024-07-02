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
 * 中台直接调拨单详情表
 * </p>
 *
 * @author shukai
 * @since 2024-07-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_direct_transfer_detail")
public class DmpDirectTransferDetailEntity extends BaseEntity<DmpDirectTransferDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 来源详情id
    */
    @TableField("third_detail_id")
    private String thirdDetailId;
    /**
    * 平台原始详情id
    */
    @TableField("platform_detail_id")
    private String platformDetailId;
    /**
    * 产品id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 产品名称
    */
    @TableField("sku_name")
    private String skuName;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 单位
    */
    @TableField("product_unit")
    private String productUnit;
    /**
    * 调出仓库编码
    */
    @TableField("out_warehouse_code")
    private String outWarehouseCode;
    /**
    * 调出仓库名称
    */
    @TableField("out_warehouse_name")
    private String outWarehouseName;
    /**
    * 调出仓位
    */
    @TableField("out_warehouse_location")
    private String outWarehouseLocation;
    /**
    * 调入仓库编码
    */
    @TableField("in_warehouse_code")
    private String inWarehouseCode;
    /**
    * 调入仓库名称
    */
    @TableField("in_warehouse_name")
    private String inWarehouseName;
    /**
    * 调入仓位
    */
    @TableField("in_warehouse_location")
    private String inWarehouseLocation;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
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


    public static final String MAIN_ID = "main_id";

    public static final String THIRD_DETAIL_ID = "third_detail_id";

    public static final String PLATFORM_DETAIL_ID = "platform_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_NAME = "sku_name";

    public static final String QTY = "qty";

    public static final String PRODUCT_UNIT = "product_unit";

    public static final String OUT_WAREHOUSE_CODE = "out_warehouse_code";

    public static final String OUT_WAREHOUSE_NAME = "out_warehouse_name";

    public static final String OUT_WAREHOUSE_LOCATION = "out_warehouse_location";

    public static final String IN_WAREHOUSE_CODE = "in_warehouse_code";

    public static final String IN_WAREHOUSE_NAME = "in_warehouse_name";

    public static final String IN_WAREHOUSE_LOCATION = "in_warehouse_location";

    public static final String REMARK = "remark";

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