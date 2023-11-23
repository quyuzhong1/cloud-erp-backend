package com.erp.model.oms.entity;

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
 * 物流规则表
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("rule_logistics")
public class RuleLogisticsEntity extends BaseEntity<RuleLogisticsEntity> {

    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 优先级
    */
    @TableField("priority")
    private Integer priority;
    /**
    * 禁用状态 false 未禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 备注描述
    */
    @TableField("remark")
    private String remark;
    /**
    * 类型多个逗号分割
    */
    @TableField("mode_type")
    private String modeType;
    /**
    * 物流供应商
    */
    @TableField("logistics_supplier_id")
    private String logisticsSupplierId;
    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 是否自动获取物流单号 
    */
    @TableField("auto_get_track_no")
    private Boolean autoGetTrackNo;


    public static final String NAME = "name";

    public static final String PRIORITY = "priority";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    public static final String TYPE = "type";

    public static final String LOGISTICS_SUPPLIER = "logistics_supplier";


    public static final String AUTO_GET_TRACK_NO = "auto_get_track_no";

    @Override
    public Serializable pkVal() {
        return null;
    }

}