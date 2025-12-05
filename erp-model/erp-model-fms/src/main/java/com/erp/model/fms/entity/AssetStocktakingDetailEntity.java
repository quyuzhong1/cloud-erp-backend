package com.erp.model.fms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 资产盘点明细表
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_stocktaking_detail")
public class AssetStocktakingDetailEntity extends BaseEntity<AssetStocktakingDetailEntity> {

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
    * 单位 PCS
    */
    @TableField("unit")
    private String unit;
    /**
    * 资产状态（正常使用）
    */
    @TableField("asset_status")
    private String assetStatus;
    /**
    * 资产编码
    */
    @TableField("asset_code")
    private String assetCode;
    /**
    * 账存数量
    */
    @TableField("book_qty")
    private Integer bookQty;
    /**
    * 初盘数量
    */
    @TableField("first_count_qty")
    private Integer firstCountQty;
    /**
    * 初盘差异
    */
    @TableField("first_diff_qty")
    private Integer firstDiffQty;
    /**
    * 账存资产位置
    */
    @TableField("book_location")
    private String bookLocation;
    /**
    * 初盘变动位置
    */
    @TableField("first_change_location")
    private String firstChangeLocation;
    /**
    * 初盘人ID
    */
    @TableField("first_count_user_id")
    private String firstCountUserId;
    /**
    * 初盘人姓名
    */
    @TableField("first_count_user_name")
    private String firstCountUserName;
    /**
    * 初盘日期
    */
    @TableField("first_count_date")
    private LocalDate firstCountDate;
    /**
    * 是否复盘
    */
    @TableField("is_recount")
    private Boolean isRecount;
    /**
    * 复盘数量
    */
    @TableField("recount_qty")
    private Integer recountQty;
    /**
    * 复盘差异
    */
    @TableField("recount_diff_qty")
    private Integer recountDiffQty;
    /**
    * 复盘变动位置
    */
    @TableField("recount_change_location")
    private String recountChangeLocation;
    /**
    * 复盘人ID
    */
    @TableField("recount_user_id")
    private String recountUserId;
    /**
    * 复盘人姓名
    */
    @TableField("recount_user_name")
    private String recountUserName;
    /**
    * 复盘日期
    */
    @TableField("recount_date")
    private LocalDate recountDate;
    /**
    * 最终差异
    */
    @TableField("final_diff_qty")
    private Integer finalDiffQty;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String ASSET_CATEGORY = "asset_category";

    public static final String CARD_ID = "card_id";

    public static final String CARD_DETAIL_ID = "card_detail_id";

    public static final String CARD_CODE = "card_code";

    public static final String ASSET_ID = "asset_id";

    public static final String ASSET_NAME = "asset_name";

    public static final String UNIT = "unit";

    public static final String ASSET_STATUS = "asset_status";

    public static final String ASSET_CODE = "asset_code";

    public static final String BOOK_QTY = "book_qty";

    public static final String FIRST_COUNT_QTY = "first_count_qty";

    public static final String FIRST_DIFF_QTY = "first_diff_qty";

    public static final String BOOK_LOCATION = "book_location";

    public static final String FIRST_CHANGE_LOCATION = "first_change_location";

    public static final String FIRST_COUNT_USER_ID = "first_count_user_id";

    public static final String FIRST_COUNT_USER_NAME = "first_count_user_name";

    public static final String FIRST_COUNT_DATE = "first_count_date";

    public static final String IS_RECOUNT = "is_recount";

    public static final String RECOUNT_QTY = "recount_qty";

    public static final String RECOUNT_DIFF_QTY = "recount_diff_qty";

    public static final String RECOUNT_CHANGE_LOCATION = "recount_change_location";

    public static final String RECOUNT_USER_ID = "recount_user_id";

    public static final String RECOUNT_USER_NAME = "recount_user_name";

    public static final String RECOUNT_DATE = "recount_date";

    public static final String FINAL_DIFF_QTY = "final_diff_qty";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}