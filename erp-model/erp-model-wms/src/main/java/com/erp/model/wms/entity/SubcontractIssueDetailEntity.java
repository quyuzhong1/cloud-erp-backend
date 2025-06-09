package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 委外发料明细单
 * </p>
 *
 * @author will
 * @since 2024-01-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("subcontract_issue_detail")
public class SubcontractIssueDetailEntity extends BaseEntity<SubcontractIssueDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
    * 父级skuId
    */
    @TableField("parent_sku_id")
    private String parentSkuId;
    /**
    * 父级skuNo
    */
    @TableField("parent_sku_no")
    private String parentSkuNo;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * bom版本
    */
    @TableField("bom_version")
    private String bomVersion;
    /**
    * 用量
    */
    @TableField("quantity")
    private Integer quantity;
    /**
    * 领料数量
    */
    @TableField("receive_qty")
    private Integer receiveQty;
    /**
    * 发料数量
    */
    @TableField("issue_qty")
    private Integer issueQty;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
     * 委外订单明细id
     */
    @TableField("subcontract_order_detail_id")
    private String subcontractOrderDetailId;

    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 仓位
    */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 同步金蝶id
     */
    @TableField("kingdee_detail_id")
    private String kingdeeDetailId;

    /**
     * 审核状态
     */
    @TableField(exist = false)
    private String approveStatus;

    /**
     * 发料类型
     */
    @TableField(exist = false)
    private String type;

    public static final String PARENT_SKU_ID = "parent_sku_id";

    public static final String PARENT_SKU_NO = "parent_sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String BOM_VERSION = "bom_version";



    public static final String RECEIVE_QTY = "receive_qty";

    public static final String ISSUE_QTY = "issue_qty";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    @Override
    public Serializable pkVal() {
        return null;
    }

}