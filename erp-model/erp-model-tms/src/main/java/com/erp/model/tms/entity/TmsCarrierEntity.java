package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 承运商
 * </p>
 *
 * @author Jim
 * @since 2024-07-04
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("tms_carrier")
public class TmsCarrierEntity extends BaseEntity<TmsCarrierEntity> {

    /**
    * 承运商代号
    */
    @TableField("code")
    private String code;
    /**
    * 轨迹查询地址
    */
    @TableField("logistics_track_url")
    private String logisticsTrackUrl;
    /**
    * 承运商名称
    */
    @TableField("name")
    private String name;
    /**
    * 销售平台
    */
    @TableField("sales_platform")
    private String salesPlatform;


    public static final String FIELD_CODE = "code";

    public static final String LOGISTICS_TRACK_URL = "logistics_track_url";

    public static final String FIELD_NAME = "name";

    public static final String SALES_PLATFORM = "sales_platform";

    public static TmsCarrierEntity init(String courierCode, String courierName, String salesPlatform, String logisticsTrackUrl) {
        return new TmsCarrierEntity()
                .setCode(courierCode)
                .setName(courierName)
                .setSalesPlatform(salesPlatform)
                .setLogisticsTrackUrl(logisticsTrackUrl);
    }
}