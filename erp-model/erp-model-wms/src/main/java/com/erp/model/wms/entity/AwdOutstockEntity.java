package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 
 * </p>
 *
 * @author wtr
 * @since 2025-12-22
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("awd_outstock")
public class AwdOutstockEntity extends BaseEntity<AwdOutstockEntity> {

    @TableField("code")
    private String code;
    @TableField("shop_id")
    private String shopId;
    @TableField("shop_name")
    private String shopName;
    @TableField("bill_date")
    private LocalDate billDate;
    @TableField("fba_shipment_id")
    private String fbaShipmentId;
    @TableField("fba_shipment_code")
    private String fbaShipmentCode;


    public static final String CODE = "code";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String BILL_DATE = "bill_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}