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
 * 销售预入库明细表
 * </p>
 *
 * @author shukai
 * @since 2024-08-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_prestock_detail")
public class DmpSoPrestockDetailEntity extends BaseEntity<DmpSoPrestockDetailEntity> {

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
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku名称
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * sku名称
    */
    @TableField("sku_name")
    private String skuName;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 仓位
    */
    @TableField("warehouse_location")
    private String warehouseLocation;
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

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_NAME = "sku_name";

    public static final String QTY = "qty";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

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