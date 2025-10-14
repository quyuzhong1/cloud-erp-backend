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
 * 盘盈盘亏单明细表
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_profit_loss_detail")
public class AssetProfitLossDetailEntity extends BaseEntity<AssetProfitLossDetailEntity> {

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
    * 资产类别（机器设备）
    */
    @TableField("asset_category")
    private String assetCategory;
    /**
    * 卡片ID
    */
    @TableField("card_id")
    private String cardId;
    /**
    * 卡片明细ID
    */
    @TableField("card_detail_id")
    private String cardDetailId;
    /**
    * 卡片编码
    */
    @TableField("card_code")
    private String cardCode;
    /**
    * 资产ID
    */
    @TableField("asset_id")
    private String assetId;
    /**
    * 资产名称
    */
    @TableField("asset_name")
    private String assetName;
    /**
    * 资产编码
    */
    @TableField("asset_code")
    private String assetCode;
    /**
    * 计量单位 PCS
    */
    @TableField("unit")
    private String unit;
    /**
    * 账存数量
    */
    @TableField("book_qty")
    private Integer bookQty;
    /**
    * 资产实际数量
    */
    @TableField("actual_qty")
    private Integer actualQty;
    /**
    * 差异数量
    */
    @TableField("diff_qty")
    private Integer diffQty;
    /**
    * 账存资产位置
    */
    @TableField("book_location")
    private String bookLocation;
    /**
    * 实际资产位置
    */
    @TableField("actual_location")
    private String actualLocation;


    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String MAIN_ID = "main_id";

    public static final String ASSET_CATEGORY = "asset_category";

    public static final String CARD_ID = "card_id";

    public static final String CARD_DETAIL_ID = "card_detail_id";

    public static final String CARD_CODE = "card_code";

    public static final String ASSET_ID = "asset_id";

    public static final String ASSET_NAME = "asset_name";

    public static final String ASSET_CODE = "asset_code";

    public static final String UNIT = "unit";

    public static final String BOOK_QTY = "book_qty";

    public static final String ACTUAL_QTY = "actual_qty";

    public static final String DIFF_QTY = "diff_qty";

    public static final String BOOK_LOCATION = "book_location";

    public static final String ACTUAL_LOCATION = "actual_location";

    @Override
    public Serializable pkVal() {
        return null;
    }

}