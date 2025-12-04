package com.erp.model.fms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.fms.enums.AssetCardStatusEnum;


/**
 * <p>
 * 资产验收表明细表
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_accept_detail")
public class AssetAcceptDetailEntity extends BaseEntity<AssetAcceptDetailEntity> {

    /**
    * 来源明细ID
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 主表ID
    */
    @TableField("main_id")
    private String mainId;
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
    * 验收数量
    */
    @TableField("accept_qty")
    private Integer acceptQty;
    /**
    * 资产卡片关联状态（已生成、未生成）
    */
    @TableField("asset_card_status")
    private String assetCardStatus;

    /**
     * 获取资产卡片关联状态枚举
     * @return 枚举
     */
    public AssetCardStatusEnum getAssetCardStatusEnum() {
        return AssetCardStatusEnum.getByStatus(this.assetCardStatus);
    }
    /**
    * 待验收数量
    */
    @TableField("pending_qty")
    private Integer pendingQty;
    /**
    * 已验收数量
    */
    @TableField("accepted_qty")
    private Integer acceptedQty;
    /**
    * 可验收数量
    */
    @TableField("acceptable_qty")
    private Integer acceptableQty;
    /**
    * 资产位置ID
    */
    @TableField("asset_location_id")
    private String assetLocationId;
    /**
    * 使用部门名称
    */
    @TableField("use_dept_name")
    private String useDeptName;
    /**
    * 使用部门ID
    */
    @TableField("use_dept_id")
    private String useDeptId;
    /**
    * 费用项目
    */
    @TableField("cost_type")
    private String costType;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String MAIN_ID = "main_id";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String PRODUCT_NAME = "product_name";

    public static final String ACCEPT_QTY = "accept_qty";

    public static final String ASSET_CARD_STATUS = "asset_card_status";

    public static final String PENDING_QTY = "pending_qty";

    public static final String ACCEPTED_QTY = "accepted_qty";

    public static final String ACCEPTABLE_QTY = "acceptable_qty";

    public static final String ASSET_LOCATION_ID = "asset_location_id";

    public static final String USE_DEPT_NAME = "use_dept_name";

    public static final String USE_DEPT_ID = "use_dept_id";

    public static final String COST_TYPE = "cost_type";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}