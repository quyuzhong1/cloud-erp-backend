package com.erp.model.wms.entity;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.dto.WmsDeliveryPlanDetailDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.util.List;


/**
 * <p>
 * 发货计划详情表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("wms_delivery_plan_detail")
public class WmsDeliveryPlanDetailEntity extends BaseEntity<WmsDeliveryPlanDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 产品id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 计划数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 是否组合品
    */
    @TableField("is_combination")
    private Boolean isCombination;

    /**
     * 平台sku
     */
    @TableField("platform_sku")
    private String platformSku;

    /**
     * 平台sku名称
     */
    @TableField("platform_sku_name")
    private String platformSkuName;

    /**
     * 平台spu
     */
    @TableField("platform_spu")
    private String platformSpu;

    /**
     * fnSku
     */
    @TableField("platform_fn_sku")
    private String platformFnSku;

    /**
     * 单箱数量
     */
    @TableField("box_qty")
    private Integer boxQty;

    /**
     * 来源id
     */
    @TableField(value = "source_json", jdbcType = JdbcType.OTHER)
    private JSONArray sourceJson;

    /**
     * 来源json
     */
    @TableField(exist = false)
    private List<WmsDeliveryPlanDetailDTO.SourceJsonDTO> sourceJsonList;

    /**
     * 主表编码,辅助字段
     */
    @TableField(exist = false)
    private String code;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String IS_COMBINATION = "is_combination";

    @Override
    public Serializable pkVal() {
        return null;
    }

}