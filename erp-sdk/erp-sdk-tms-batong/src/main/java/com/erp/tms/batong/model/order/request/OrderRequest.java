package com.erp.tms.batong.model.order.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname OrderRequest
 * @Description 下单参数
 * @Date 2024-01-10 16:42
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest implements Serializable {

    /**
     * 客户端的订单唯一标识
     */
    @NotBlank(message = "客户端的订单唯一标识不能为空")
    @JSONField(name = "reference_no")
    private String referenceNo;

    /**
     * 运输方式代码
     * 就是服务商代码
     */
    @NotBlank(message = "运输方式代码不能为空")
    @JSONField(name = "shipping_method")
    private String shippingMethod;

    /**
     * 服务商单号
     *
     */
    @JSONField(name = "shipping_method_no")
    private String shippingMethodNo;

    /**
     * 订单重量，单位KG，3位小数，默认为0.2
     */
    @JSONField(name = "order_weight")
    private String orderWeight;

    /**
     * 外包装件数,默认1
     */
    @JSONField(name = "order_pieces")
    private String orderPieces;

    /**
     * 货物类型
     * W：包裹
     * D：文件
     * B：袋子
     */
    @JSONField(name = "cargotype")
    private String cargotype;

    /**
     * 订单状态
     * P：已预报 (默认)
     * D：草稿 (如果创建草稿订单，则需要再调用submitforecast【提交预报】接口)
     */
    @JSONField(name = "order_status")
    private String orderStatus;

    /**
     * 买家ID
     */
    @JSONField(name = "buyer_id")
    private String buyerId;

    /**
     * 订单备注
     */
    @JSONField(name = "order_info")
    private String orderInfo;

    /**
     * 平台ID（如果您是电商平台，请联系我们添加并确认您对应的平台ID）
     */
    @JSONField(name = "platform_id")
    private String platformId;

    /**
     * 自定义单号
     */
    @JSONField(name = "custom_hawbcode")
    private String  customHawbcode;

    @JSONField(name = "shipper")
    @NotNull(message = "发件人信息不能为空")
    private Shipper shipper;


    @JSONField(name = "consignee")
    @NotNull(message = "收件人信息不能为空")
    private Consignee consignee;


    @NotNull(message = "海关申报信息不能为空")
    @Size(min = 1,message = "海关申报信息至少需要一个")
    @JSONField(name = "invoice")
    private List<Invoice> invoiceList;

    /**
     * 包裹材积信息
     */
    @JSONField(name = "cargovolume")
    private List<Cargovolume> cargovolumeList;



}
