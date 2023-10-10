package com.erp.model.dmp.mabang;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import com.erp.model.dmp.mabang.item.DeliveryItemEntity;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


/**
 * 马帮发货单
 * @CreateTime: 2023-06-29  18:55
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
public class DeliveryEntity extends CleanBaseDTO {

    private String _id;

    /**
     * 物流方式名称
     */
    private String logic_name;

    /**
     * 清关时间
     */
    private String customs_clearance_time;
    

    /**
     * 总发货量
     */
    private Integer totalApplyQuantity;

    /**
     * 备注
     */
    private String remark;

    /**
     * 发货时间
     */
    private String delivery_time;

    /**
     * 完结标识  1：完结 2：未完结
     */
    private Integer is_over;

    private String head_journey_time;

    private String tail_distance_time;

    private BigDecimal logic_price;

    /**
     * 附件
     */
    private String enclosure;

    /**
     * 预计到港时间
     */
    private String estimate_time;

    /**
     * 发货单类型 1:手动发货,2:转wms发货单
     */
    private Integer delivery_type;

    /**
     * 物流方式ID
     */
    private Integer logic_id;

    /**
     * 是否付款单
     */
    private Integer has_pay_order;

    private String merge_id;

    /**
     * 发货单id
     */
    @SerializedName("id")
    @JSONField(name = "id")
    private String delivery_id;

    /**
     * weight
     */
    private String compute_type;

    /**
     * 合并标记
     */
    private String merge_flag_name;

    /**
     * 单个发货单的商品数
     */
    private Integer stockSum;

    /**
     * 总重量
     */
    private BigDecimal total_weights;

    private String channel_name;

    /**
     * 开船时间
     */
    private String sail_time;

    /**
     * 到货时间
     */
    private String arrival_time;

    /**
     * 合并标识 1合并 2循环
     */
    private Integer merge_flag;

    /**
     * 企业编号
     */
    private Integer company_id;

    /**
     * 创建时间
     */
    private String create_time;

    /**
     * 总体积
     */
    private BigDecimal total_volumes;

    /**
     * 用户名称
     */
    private String employee_name;

    /**
     * 物流中心编码
     */
    private String logistics_code;

    /**
     * 发货单号
     */
    private String delivery_no;

    private String actual_time;

    /**
     * 创建人
     */
    private Integer create_opear;

    /**
     * 总费用
     */
    private String extend_fee;

    /**
     * 仓库名称
     */
    private String warehouse_name;

    private String logic_price_currency;

    private Integer channel_id;

    /**
     * 发货单状态
     */
    private Integer delivery_status;

    /**
     * fba仓库id
     */
    private Integer warehouse_id;

    /**
     * 明细
     */
    private List<DeliveryItemEntity> stockList;


    @Override
    public String toString() {
        return "DeliveryEntity{" +
                "logic_name='" + logic_name + '\'' +
                ", customs_clearance_time='" + customs_clearance_time + '\'' +
                ", totalApplyQuantity=" + totalApplyQuantity +
                ", remark='" + remark + '\'' +
                ", delivery_time='" + delivery_time + '\'' +
                ", is_over=" + is_over +
                ", head_journey_time='" + head_journey_time + '\'' +
                ", tail_distance_time='" + tail_distance_time + '\'' +
                ", logic_price=" + logic_price +
                ", enclosure='" + enclosure + '\'' +
                ", estimate_time='" + estimate_time + '\'' +
                ", delivery_type=" + delivery_type +
                ", logic_id=" + logic_id +
                ", has_pay_order=" + has_pay_order +
                ", merge_id='" + merge_id + '\'' +
                ", delivery_id=" + delivery_id +
                ", compute_type='" + compute_type + '\'' +
                ", merge_flag_name='" + merge_flag_name + '\'' +
                ", stockSum=" + stockSum +
                ", total_weights=" + total_weights +
                ", channel_name='" + channel_name + '\'' +
                ", sail_time='" + sail_time + '\'' +
                ", arrival_time='" + arrival_time + '\'' +
                ", merge_flag=" + merge_flag +
                ", company_id=" + company_id +
                ", create_time='" + create_time + '\'' +
                ", total_volumes=" + total_volumes +
                ", employee_name='" + employee_name + '\'' +
                ", logistics_code='" + logistics_code + '\'' +
                ", delivery_no='" + delivery_no + '\'' +
                ", actual_time='" + actual_time + '\'' +
                ", create_opear=" + create_opear +
                ", extend_fee=" + extend_fee +
                ", warehouse_name='" + warehouse_name + '\'' +
                ", logic_price_currency='" + logic_price_currency + '\'' +
                ", channel_id=" + channel_id +
                ", delivery_status=" + delivery_status +
                ", warehouse_id=" + warehouse_id +
                ", stockList=" + stockList +
                '}';
    }
}