package com.erp.model.dmp.mabang;

import com.erp.model.dmp.dto.CleanBaseDTO;
import com.erp.model.dmp.mabang.item.DeliveryItemEntity;
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
     *
     */
    private Integer has_pay_order;

    private String merge_id;

    /**
     * 发货单id
     */
    private Integer id;

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
    private Integer extend_fee;

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

}