package com.erp.model.plm.entity;

import java.math.BigDecimal;
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
 * 模具监控
 * </p>
 *
 * @author jack
 * @since 2025-10-22
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mold_monitor")
public class MoldMonitorEntity extends BaseEntity<MoldMonitorEntity> {

    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源json
    */
    @TableField("source_rule_json")
    private String sourceRuleJson;
    /**
    * 统计状态：counting=统计中 , finish=统计完成  枚举：MoldMonitorStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * 返还状态：underachieved=未达量 , notReturned=未返 , returned=已返  枚举：MoldMonitorReturnStatusEnum
    */
    @TableField("return_status")
    private String returnStatus;
    /**
    * 寿命状态：healthy=健康 , alert=预警 , exhausted=耗尽  枚举：MoldMonitorLifeStatusEnum
    */
    @TableField("life_status")
    private String lifeStatus;
    /**
    * 采购下单数量
    */
    @TableField("purchase_order_qty")
    private Integer purchaseOrderQty;
    /**
    * 采购收货数量
    */
    @TableField("warehouse_receive_qty")
    private Integer warehouseReceiveQty;
    /**
    * 采购入库数量
    */
    @TableField("po_instock_qty")
    private Integer poInstockQty;
    /**
    * 实际返还金额
    */
    @TableField("actual_return_price")
    private BigDecimal actualReturnPrice;
    /**
    * 返还人id
    */
    @TableField("return_user_id")
    private String returnUserId;
    /**
    * 返还人
    */
    @TableField("return_user_name")
    private String returnUserName;
    /**
    * 返还日期
    */
    @TableField("return_date")
    private LocalDate returnDate;
    /**
    * 返还说明
    */
    @TableField("remark")
    private String remark;


    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_RULE_JSON = "source_rule_json";

    public static final String STATUS = "status";

    public static final String RETURN_STATUS = "return_status";

    public static final String LIFE_STATUS = "life_status";

    public static final String PURCHASE_ORDER_QTY = "purchase_order_qty";

    public static final String WAREHOUSE_RECEIVE_QTY = "warehouse_receive_qty";

    public static final String PO_INSTOCK_QTY = "po_instock_qty";

    public static final String ACTUAL_RETURN_PRICE = "actual_return_price";

    public static final String RETURN_USER_ID = "return_user_id";

    public static final String RETURN_USER_NAME = "return_user_name";

    public static final String RETURN_DATE = "return_date";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
