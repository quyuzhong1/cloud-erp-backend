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
 * 
 * </p>
 *
 * @author zdy
 * @since 2025-08-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_platform_so_delivery_detail")
public class DmpPlatformSoDeliveryDetailEntity extends BaseEntity<DmpPlatformSoDeliveryDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * sellerSku
    */
    @TableField("msku")
    private String msku;
    /**
    * fulfillmentNetworkSku
    */
    @TableField("fn_sku")
    private String fnSku;
    /**
    * 卖家订单明细ID
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 发货单编码
    */
    @TableField("code")
    private String code;
    /**
     * 输入任务ID
     */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
     * 亚马逊账号代号
     */
    @TableField("platform_shop_code")
    private String platformShopCode;
    /**
     * 任务转换ID
     */
    @TableField("convert_id")
    private String convertId;
    /**
     * 店铺ID
     */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
     * 任务来源唯一加密代号
     */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
     * 任务数据加密代号
     */
    @TableField("data_encrypt")
    private String dataEncrypt;

    public static final String MAIN_ID = "main_id";

    public static final String MSKU = "msku";

    public static final String FN_SKU = "fn_sku";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String QTY = "qty";

    public static final String CODE = "code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}