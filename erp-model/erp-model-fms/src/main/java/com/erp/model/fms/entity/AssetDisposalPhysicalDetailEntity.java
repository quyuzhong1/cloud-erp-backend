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
 * 资产处置单实物明细表
 * </p>
 *
 * @author jack
 * @since 2025-10-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_disposal_physical_detail")
public class AssetDisposalPhysicalDetailEntity extends BaseEntity<AssetDisposalPhysicalDetailEntity> {

    /**
    * 资产处置单明细表id
    */
    @TableField("asset_disposal_detail_id")
    private String assetDisposalDetailId;
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
    * 资产位置名称
    */
    @TableField("asset_location_name")
    private String assetLocationName;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;


    public static final String ASSET_DISPOSAL_DETAIL_ID = "asset_disposal_detail_id";

    public static final String MAIN_ID = "main_id";

    public static final String ASSET_CODE = "asset_code";

    public static final String ASSET_LOCATION_ID = "asset_location_id";

    public static final String ASSET_LOCATION_NAME = "asset_location_name";

    public static final String QTY = "qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}