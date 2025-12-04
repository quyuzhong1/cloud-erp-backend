package com.erp.model.fms.entity;

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
 * 资产卡片明细表
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_card_detail")
public class AssetCardDetailEntity extends BaseEntity<AssetCardDetailEntity> {

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
    * 资产编码
    */
    @TableField("asset_code")
    private String assetCode;
    /**
    * 资产位置ID
    */
    @TableField("asset_location_id")
    private String assetLocationId;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 供应商ID
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 供应商名称
    */
    @TableField("supplier_name")
    private String supplierName;
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
    * 费用项目（折旧费）
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

    public static final String ASSET_CODE = "asset_code";

    public static final String ASSET_LOCATION_ID = "asset_location_id";

    public static final String QTY = "qty";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String USE_DEPT_NAME = "use_dept_name";

    public static final String USE_DEPT_ID = "use_dept_id";

    public static final String COST_TYPE = "cost_type";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}