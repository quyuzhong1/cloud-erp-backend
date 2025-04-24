package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 物流快递/海运/空运公司列表
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_carrier")
public class LogisticsCarrierEntity extends BaseEntity<LogisticsCarrierEntity> {
    /**
     * 是否禁用
     */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 物流类型
    */
    @TableField("logistics_type")
    private String logisticsType;
    /**
    * 物流商编码
    */
    @TableField("carrier_code")
    private String carrierCode;
    /**
    * 物流商中文名称
    */
    @TableField("carrier_cn")
    private String carrierCn;
    /**
    * 物流商英文名称
    */
    @TableField("carrier_en")
    private String carrierEn;


    public static final String LOGISTICS_TYPE = "logistics_type";

    public static final String CARRIER_CODE = "carrier_code";

    public static final String CARRIER_CN = "carrier_cn";

    public static final String CARRIER_EN = "carrier_en";

    @Override
    public Serializable pkVal() {
        return null;
    }

}