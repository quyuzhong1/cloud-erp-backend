package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 第三方ERP物流渠道 DTO
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformThirdLogisticsChannelDTO  extends UniqueDto {

    /**
     * 是否禁用/停用 true 是 false 不是
     */
    private Boolean disabled;
    /**
     * 平台类型：lingxing领星
     */
    private String platformType;
    /**
     * 物流商类型
     */
    private String type;
    /**
     * 物流商id
     */
    private String logisticsSupplierId;
    /**
     * 物流商名称
     */
    private String logisticsSupplierName;
    /**
     * 平台更新时间
     */
    private LocalDateTime platformUpdateTime;
    /**
     * 平台创建时间
     */
    private LocalDateTime platformCreateTime;
    /**
     * 物流方式id
     */
    private String logisticsTypeId;
    /**
     * 物流方式名称
     */
    private String logisticsTypeName;
}