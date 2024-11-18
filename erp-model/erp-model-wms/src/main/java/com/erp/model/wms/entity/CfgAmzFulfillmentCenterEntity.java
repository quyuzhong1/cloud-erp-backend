package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * <p>
 * 亚马逊仓储中心配置
 * </p>
 *
 * @author Jim
 * @since 2023-12-25
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_amz_fulfillment_center")
public class CfgAmzFulfillmentCenterEntity extends BaseEntity<CfgAmzFulfillmentCenterEntity> {

    /**
    * 备注 需要的时候 用到
    */
    @TableField("remark")
    private String remark;
    /**
    * 仓储中心代号
    */
    @TableField("code")
    private String code;
    /**
    * 国家代号ID
    */
    @TableField("country")
    private String country;
    /**
    * 禁用状态: f=启用, t=禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
     * 新增未知国家仓库中心代号记录
     */
    public CfgAmzFulfillmentCenterEntity(String code) {
        this.remark = "";
        this.code = code;
        this.country = "";
        this.disabled = false;
    }
}